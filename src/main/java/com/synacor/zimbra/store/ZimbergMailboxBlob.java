package com.synacor.zimbra.store;

import java.io.IOException;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.external.ExternalMailboxBlob;
import com.zimbra.cs.store.external.ExternalStoreManager;

/** ExternalMailboxBlob subclass that uses explicit validate method */
public class ZimbergMailboxBlob extends ExternalMailboxBlob
{

	/**
	 * Passthrough constructor
	 *
	 * @param mbox The containing mailbox
	 * @param itemId The database id of the mailbox blob
	 * @param revision The mailbox blob revision
	 * @param locator The locator string for the mailbox blob
	 */
	protected ZimbergMailboxBlob(Mailbox mbox, int itemId, int revision, String locator)
	{
		super(mbox, itemId, revision, locator);
	}

	/**
	 * Validates the blob exists and is addressible by the StoreManager
	 *
	 * @return true if the blob is valid
	 */
	@Override
	public boolean validateBlob()
	{
		ZimbergStoreManager sm = (ZimbergStoreManager) StoreManager.getInstance();
		boolean status = false;

		try
		{
			status = sm.validateFromStore(getLocator(), getMailbox());
		}
		catch (IOException e)
		{
			ZimbraLog.store.warn("Unable to validate blob [%s] due to IOException", this, e);
		}

		return status;
	}

}
