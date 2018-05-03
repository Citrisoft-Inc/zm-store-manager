package com.synacor.zimbra.store.location;

import java.nio.file.Paths;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import com.zimbra.cs.mailbox.Mailbox;

/** A simple location generator for filesystem paths */
public class PathLocationFactory
	extends LocationFactory
{

	String root;

	/**
	 * Generate a new location factory
	 *
	 * @param props Initialization parameters
	 */
	public PathLocationFactory(Properties props)
	{
		this(props.getProperty("path_location_base_path"));
	}

	/**
	 * Initialize a new location factor
	 *
	 * @param root The root prefix for storing blobs
	 */
	public PathLocationFactory(String root)
	{
		this.root = root;
	}

	/**
	 * Generates an absolute location path comprised of a base prefix,
	 * the account id, and the target item id of the mailbox blob.
	 *
	 * @param mbox The target mailbox
	 * @return String A string representing the target location
	 */
	public String generateLocation(Mailbox mbox)
	{
		return Paths.get(root, mbox.getAccountId(), String.valueOf(mbox.getLastItemId() + 1)).toString();
	}

}
