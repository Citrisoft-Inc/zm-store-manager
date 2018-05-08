package com.synacor.zimbra.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.zimbra.common.localconfig.KnownKey;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.external.ExternalStoreManager;

import com.synacor.zimbra.store.backend.Backend;
import com.synacor.zimbra.store.backend.FileBackend;
import com.synacor.zimbra.store.location.LocationFactory;
import com.synacor.zimbra.store.location.PathLocationFactory;
import com.synacor.zimbra.store.profile.Profile;
import com.synacor.zimbra.store.profile.Profiles;

/** Flexible StoreManager implementation.  */
public class ZimbergStoreManager extends ExternalStoreManager
{
	/** Holds the path to the profile directory */
	private static KnownKey profilePath = new KnownKey("zimberg_store_profile_path", "${zimbra_home}/conf/storemanager.d");
	/** Holds the name of the default profile */
	private static KnownKey profileName = new KnownKey("zimberg_store_default_profile", "default");
	/** Holds the name of the fallback profile */
	private static KnownKey fallbackName = new KnownKey("zimberg_store_fallback_profile", "default");

	/** Holds the default store profile for persisting new blobs */
	Profile defaultProfile;

	/** Holds the fallback store profile for persisting new blobs */
	Profile fallbackProfile;

	/** Constructs an uninitialized StoreManager instance */
	public ZimbergStoreManager()
	{
	}

	/**
	 * Extracts profile specific location from locator string
	 *
	 * @param locator The stored locator of a blob
	 * @return String blob location
	 */
	public String getLocation(String locator)
	{
		String[] parts = locator.split("@@", 2);

		return (parts.length == 2) ? parts[1] : parts[0];
	}

	/**
	 * Extracts profile from locator string or returns fallback implementation
	 *
	 * @param locator The stored locator of a blob
	 * @return Profile profile to use for accessing the blob
	 */
	public Profile getProfile(String locator)
	{
		String[] parts = locator.split("@@", 2);

		return (parts.length == 2) ? Profiles.get(parts[0]) : fallbackProfile;
	}

	/**
	 * Initialize the StoreManager
	 */
	public void startup()
		throws IOException, ServiceException
	{
		Profiles.load(profilePath.value());
		// FIXME: Need to handle non-existent profiles properly.
		defaultProfile = Profiles.get(profileName.value());
		fallbackProfile = Profiles.get(fallbackName.value());

		ZimbraLog.store.info("Zimberg Store Manager: profile path: " + profilePath.value());
		ZimbraLog.store.info("Zimberg Store Manager: default profile: " + profileName.value());
		ZimbraLog.store.info("Zimberg Store Manager: fallback profile: " + fallbackName.value());

		super.startup();
	}

	/**
	* Persists the object to the default store backend
	* 
	* @param is The data stream for a mailbox blob
	* @param actualSize The full size of a mailbox blob
	* @param mbox The mailbox to recieve a mailbox blob
	*
	* @return String The locator key for the written message
	*
	*/
	public String writeStreamToStore(InputStream is, long actualSize, Mailbox mbox)
		throws IOException, ServiceException
	{
		String location = defaultProfile.locationFactory.generateLocation(mbox);
		String locator = defaultProfile.name + "@@" + location;

		defaultProfile.backend.store(is, location, actualSize);

		return locator;
	}		

	/**
	 * Reads a message back from store backend
	 *
	 * @param locator The stored locator for a mailbox blob object
	 * @param mbox The mailbox to retrieve a mailbox blob from
	 *
	 * @return InputStream A stream of the retrieved message
	 *
	 */
	public InputStream readStreamFromStore(String locator, Mailbox mbox)
		throws IOException
	{
		return getProfile(locator).backend.get(getLocation(locator));
	}

	/**
	 * Deletes a message from the store backend
	 *
	 * @param locator The stored locator for a mailbox blob object
	 * @param mbox The mailbox to retrieve a mailbox blob from
	 */
	public boolean deleteFromStore(String locator, Mailbox mbox)
		throws IOException
	{
		getProfile(locator).backend.delete(getLocation(locator));

		return true;
	}

	/**
	 * Validates a mailbox blob exists in the store backend
	 *
	 * @param locator The stored locator for a mailbox blob object
	 * @param mbox The mailbox to retrieve a mailbox blob from
	 * @throws IOException if status of mailbox blob cannot be determined
	 * @return boolean True is blob successfully located
	 */
	public boolean validateFromStore(String locator, Mailbox mbox)
		throws IOException
	{
		return getProfile(locator).backend.verify(getLocation(locator));
	}

	/**
	 * Returns a mailbox blob object if it can be validated.
	 *
	 * @param mbox The mailbox containing the blob
	 * @param itemId The database itemId of the blob
	 * @param revision The database revision of the blob
	 * @param locator The locator string for the blob
	 * @return MailboxBlob A blob object or null if the blob doesn't exist
	 **/
	@Override
	public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator)
		throws ServiceException
	{
		ZimbergMailboxBlob mblob = new ZimbergMailboxBlob(mbox, itemId, revision, locator);

		return mblob.validateBlob() ? mblob : null;
	}

}
