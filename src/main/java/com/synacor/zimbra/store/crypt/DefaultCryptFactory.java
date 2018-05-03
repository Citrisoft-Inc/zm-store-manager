package com.synacor.zimbra.store.crypt;

import java.io.InputStream;
import java.io.FileInputStream;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.google.common.hash.Hashing;

// add main function utility for alias / password management

public class DefaultCryptFactory
	extends CryptFactory
{
	private static final String DEFAULT_KEYSTORE = "/tmp/zimberg.jck";
	private static final String DEFAULT_ALIAS    = "mberg";
	private static final String DEFAULT_PASSWORD = "i0wnj00";

	Key key;

	public DefaultCryptFactory()
	{
		try
		{
			KeyStore store = KeyStore.getInstance("JCEKS");
			//String keyFile = new FileInputStream(DEFAULT_KEYSTORE);
			char[] password = DEFAULT_PASSWORD.toCharArray();
		}
		catch (KeyStoreException e)
		{
			// DON'T JUST STAND THERE, DO SOMETHING!
		}

		//key = keyStore.getKey(alias, password);
	}

	public DefaultCryptFactory(KeyStore keyStore, String alias, char[] password)
	{
		//key = keyStore.getKey(alias, password);
	}

	public CryptFactoryInputStream getInputStream(InputStream is, long actualSize)
	{
		// placeholder
		return null;
	}

	public CryptFactoryInputStream getInputStream(InputStream is, Cipher cipher, String locator, long actualSize)
	{
		// placeholder
		return null;
	}

}
