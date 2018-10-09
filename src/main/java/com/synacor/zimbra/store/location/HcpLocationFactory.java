package com.synacor.zimbra.store.location;

import java.util.UUID;
import java.util.Properties;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;

/** Generates locator strings optimized for Hitatchi Content Platform */
public class HcpLocationFactory
	extends LocationFactory
{

	/**
	 * Generate a new location factory
	 * 
	 * @param props Initialization parameters
	 */
	public HcpLocationFactory(Properties props)
	{
	}

	/**
	 * Generates an HCP optimized locator prefixed with a djb2 hash
	 * directory path to increase object dispersion.
	 *
	 * @param String accountId
	 * @param String itemId
	 * @return String A string representing the target location
	 */
	public String generateLocation(String accountId, int itemId)
	{
		String path = String.format("%S-%08X-%11X",
			accountId,
			itemId,
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
