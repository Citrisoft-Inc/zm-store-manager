package com.synacor.zimbra.store.location;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;

/** Base class for factory classes to generate locations */
public abstract class LocationFactory
{
	/**
	 * Generate a unique location identifier for a particular mailbox.
	 *
	 * @param mbox The target mailbox
	 * @return String A string representing the target location 
	 */
	public String generateLocation(Mailbox mbox)
	{
		return generateLocation(mbox.getAccountId(), mbox.getLastItemId() + 1);
	}

	/**
	 * Generate a unique location identifier for a MailboxBlob
	 *
	 * @param blob The blob
	 * @return String A string representing the target location 
	 */
	public String generateLocation(MailboxBlob blob)
	{
		return generateLocation(blob.getMailbox().getAccountId(), blob.getItemId());
	}

	/**
	 * Generate a unique location identifier for a MailboxBlob;
	 *
	 * @param accountId A string containing the id of the account object
	 * @param itemId A string containing the id of the item object
     *
	 * @return String A string representing the target location 
	 */
	public abstract String generateLocation(String accountId, int itemId);
}
