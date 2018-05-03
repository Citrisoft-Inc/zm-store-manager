package com.synacor.zimbra.store.location;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import com.zimbra.cs.mailbox.Mailbox;

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
	 * @param mbox The target mailbox
	 * @return String A string representing the target location
	 */
	public String generateLocation(Mailbox mbox)
	{
		UUID uuid = UUID.fromString(mbox.getAccountId());

		return String.format("%08X%016X%016X%08X",
			rnd.nextInt(),
			uuid.getMostSignificantBits(),
			uuid.getLeastSignificantBits(),
			mbox.getLastItemId() + 1);
	}
}
