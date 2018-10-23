package com.synacor.zimbra.store.location;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;

/** A general purpose key location generator  */
public class DefaultLocationFactory
	extends LocationFactory
{

	Random rnd; 

	/**
	 * Generate a new location factory
	 *
	 * @param props Initialization parameters
	 */
	public DefaultLocationFactory(Properties props)
	{
		rnd = new Random();
	}

	/**
	 * Generates a key location based on a compound of a random integer,
	 * the account UUID and the target item item id as a hex string.
	 * 
	 * @param accountId A string containing the id of the account object
	 * @param itemId A string containing the id of the item object
     *
	 * @return String A string representing the target location
	 */
	public String generateLocation(String accountId, int itemId)
	{
		UUID uuid = UUID.fromString(accountId);

		return String.format("%08X%016X%016X%08X",
			rnd.nextInt(),
			uuid.getMostSignificantBits(),
			uuid.getLeastSignificantBits(),
			itemId);
	}
}
