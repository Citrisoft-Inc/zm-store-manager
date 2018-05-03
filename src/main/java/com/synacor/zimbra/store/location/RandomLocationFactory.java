package com.synacor.zimbra.store.location;

import java.lang.String;
import java.math.BigInteger;
import java.util.Properties;
import java.security.SecureRandom;

import com.zimbra.cs.mailbox.Mailbox;

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
	 * @param mbox The target mailbox
	 * @return String A string representing the target location
	 */
	public String generateLocation(Mailbox mbox)
	{
		return String.format("%032X", new BigInteger(128,rnd));
	}
}
