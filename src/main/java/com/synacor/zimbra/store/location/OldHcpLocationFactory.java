package com.synacor.zimbra.store.location;

import java.util.UUID;
import java.util.Properties;

import com.zimbra.cs.mailbox.Mailbox;

/** Generates locator strings optimized for Hitatchi Content Platform */
public class OldHcpLocationFactory
	extends LocationFactory
{

	/**
	 * Generate a new location factory
	 * 
	 * @param props Initialization parameters
	 */
	public OldHcpLocationFactory(Properties props)
	{
	}

	/**
	 * Generates an HCP optimized locator prefixed with a djb2 hash
	 * directory path to increase object dispersion.
	 *
	 * @param mbox The target mailbox
	 * @return String A string representing the target location
	 */
	public String generateLocation(Mailbox mbox)
	{
		String path = String.format("%S/%08X-%11X",
			mbox.getAccountId(),
			mbox.getLastItemId() + 1,
			System.currentTimeMillis()
			);

		long h = 5481;

		for (byte b: path.getBytes())
		{
			 h = (h * 33) ^ b;
		}

		return String.format("%02X/%02X/%S",
			(h ^ (h >> 8)) & 0xff,
			((h >> 16) ^ (h >> 24)) & 0xff,
			path
			);
	}
}
