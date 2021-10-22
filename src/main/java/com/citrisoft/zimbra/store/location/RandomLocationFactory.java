package com.citrisoft.zimbra.store.location;

import java.lang.String;
import java.math.BigInteger;
import java.util.Properties;
import java.security.SecureRandom;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;

/** A strictly random key location generator */
public class RandomLocationFactory
	extends LocationFactory

{
	SecureRandom rnd;

	/**
	 * Generate a new location factory
	 *
	 * @param props Initialization parameters
	 */
	public RandomLocationFactory(Properties props)
	{
		rnd = new SecureRandom();
	}

	/**
	 * Generates a key location consisting of a hex encoded 128-bit integer
	 *
	 * @param accountId A string containing the id of the account object
	 * @param itemId A string containing the id of the item object
	 * @return String A string representing the target location
	 */
	public String generateLocation(String accountId, int itemId)
	{
		return String.format("%032X", new BigInteger(128,rnd));
	}
}
