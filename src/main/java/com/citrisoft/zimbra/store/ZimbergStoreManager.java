package com.citrisoft.zimbra.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.zimbra.common.localconfig.KnownKey;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.FileCache;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.external.ExternalBlob;
import com.zimbra.cs.store.external.ExternalStoreManager;
import com.zimbra.cs.stats.ActivityTracker;
import com.zimbra.common.stats.StatsDumper;

import com.citrisoft.zimbra.store.profile.Profile;
import com.citrisoft.zimbra.store.profile.Profiles;

/** Flexible StoreManager implementation.  */
public class ZimbergStoreManager
	extends ExternalStoreManager
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

	/** Holds the name of the current profile */
	ThreadLocal<String> localProfileName = new ThreadLocal<>();

	public static final ActivityTracker activityTracker = new ActivityTracker("store.csv");

	/** Constructs an uninitialized StoreManager instance */
	public ZimbergStoreManager()
	{
		StatsDumper.schedule(activityTracker, Constants.MILLIS_PER_MINUTE);
	}

	/**
	 * Extracts profile specific location from locator string
	 *
	 * @param locator The stored locator of a blob
	 * @return String blob location
	 */
	public static String getLocation(String locator)
	{
		String[] parts = locator.split("@@", 2);

		return (parts.length == 2) ? parts[1] : parts[0];
	}

	/**
	 * Extracts profile name from locator string
	 *
	 * @param locator The stored locator of a blob
	 * @return String profile name
	 */
	public static String getProfileName(String locator)
	{
		String[] parts = locator.split("@@", 2);

		return (parts.length == 2) ? parts[0] : null;
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

		Profile profile = (parts.length == 2) ? Profiles.get(parts[0]) : fallbackProfile;

		localProfileName.set(profile.name);

		return profile;
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
		return writeStreamToStore(is, actualSize, mbox, defaultProfile);
	}		

	/**
	 * Presists the object to a specific store backand
	 *
	 * @param is The data stream for a mailbox blob
	 * @param actualSize The full size of a mailbox blob
	 * @param mbox The mailbox to recieve a mailbox blob
	 * @param profile The destination profile
	 *
	 * @return String The locator key for the written message
	 *
	 * @throws IOException if there is an error writing to the backing store
	 * @throws ServiceException if there is any other error in processing the request
	 *
	 */
	public static String writeStreamToStore(InputStream is, long actualSize, Mailbox mbox, Profile profile)
		throws IOException, ServiceException
	{
		String location = profile.locationFactory.generateLocation(mbox);
		String locator = profile.name + "@@" + location;

		long startTime = System.currentTimeMillis();
		profile.backend.store(is, location, actualSize);
		activityTracker.addStat(profile.name+".put", startTime);

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
		long startTime = System.currentTimeMillis();
		getProfile(locator).backend.delete(getLocation(locator));
		activityTracker.addStat(localProfileName.get()+".delete", startTime);

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
		long startTime = System.currentTimeMillis();
		boolean isValid = getProfile(locator).backend.verify(getLocation(locator));
		activityTracker.addStat(localProfileName.get()+".verify", startTime);

		return isValid;
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

	/**
	 * Convienence method to get mailbox blob from MailboxBlobInfo
	 *
	 * @param blobInfo The MailboxBlobInfo object
	 *
	 * @return the actual MailboxBlob object
	 *
	 * @throws ServiceException is mailbox does not exist
	 **/
	public MailboxBlob getMailboxBlob(MailboxBlobInfo blobInfo)
		throws ServiceException
	{
		Mailbox mbox = MailboxManager.getInstance().getMailboxById(blobInfo.mailboxId);
		return getMailboxBlob(mbox, blobInfo);
	}

	/**
	 * Convienence method to get mailbox blob from MailboxBlobInfo if mbox already resolved
	 *
	 * @param mbox The containing mailbox
	 * @param blobInfo The MailboxBlobInfo object
	 *
	 * @return the actual MailboxBlob object
	 *
	 * @throws ServiceException is mailbox does not exist
	 **/
	public MailboxBlob getMailboxBlob(Mailbox mbox, MailboxBlobInfo blobInfo)
		throws ServiceException
	{
		return getMailboxBlob(mbox, blobInfo.itemId, blobInfo.revision, blobInfo.locator, false);
	}

	/**
	 * Returns a local instance of a blob, pulling from the retore store if necessary
	 *
	 * Overriding to add in tracking for remote fetches since readStreamFromStore only
	 * measures the time to get the InputStream
	 *
	 * @param mbox The mailbox containing the blob
	 * @param locator The stored locator for a mailbox blob object
	 * @param fromCache Whether to retrieve cached object if available
	 *
	 * @throws IOException if there is an error reading the store from the backing store
	 **/
	@Override
	protected Blob getLocalBlob(Mailbox mbox, String locator, boolean fromCache)
		throws IOException
	{
		FileCache.Item cached = null;
		if (fromCache)
		{
			cached = localCache.get(locator);
			if (cached != null)
			{
				ExternalBlob blob = new ExternalBlob(cached.file, cached.file.length(), cached.digest);
				blob.setLocator(locator);
				blob.setMbox(mbox);
				return blob;
			}
		}

		long startTime = System.currentTimeMillis();
		InputStream is = readStreamFromStore(locator, mbox);
		if (is == null)
		{
			throw new IOException("Store " + this.getClass().getName() +" returned null for locator " + locator);
		}
		else
		{
			cached = localCache.put(locator, is);
			activityTracker.addStat(localProfileName.get()+".get", startTime);
			ExternalBlob blob = new ExternalBlob(cached.file, cached.file.length(), cached.digest);
			blob.setLocator(locator);
			blob.setMbox(mbox);
			return blob;
		}
	}
}
