package com.synacor.zimbra.store.location;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

import com.zimbra.cs.mailbox.Mailbox;

/** Base class for factory classes to generate locations */
public abstract class LocationFactory
{
	/**
	 * Generate a unique location identifier for a particular mailbox.
	 *
	 * @param mbox The target mailbox
	 * @return String A string representing the target location 
	 */
	public abstract String generateLocation(Mailbox mbox);
}
