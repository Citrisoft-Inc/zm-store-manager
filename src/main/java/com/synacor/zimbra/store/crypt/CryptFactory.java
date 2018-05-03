package com.synacor.zimbra.store.crypt;

import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public abstract class CryptFactory
{   
	public abstract CryptFactoryInputStream getInputStream(InputStream is, long actualSize);

	protected abstract class CryptFactoryInputStream
		extends CipherInputStream
	{
		public String locator;
		public long actualSize;

		public abstract long getCryptSize();

		private CryptFactoryInputStream(InputStream is, Cipher cipher, String locator, long actualSize)
		{
			super(is, cipher);
			this.locator = locator;
			this.actualSize = actualSize;
		}
	}

/*
	public static CryptFactory getInstance()
		throws IllegalAccessException, InstantiationException
	{   
		return getInstance(DefaultCryptFactory.class);
	}
*/

	public static CryptFactory getInstance(String className)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{   
		if (className == null || className.equals(""))
			throw new InstantiationException("No class specified.");

		return getInstance(Class.forName(className));
	}

	public static CryptFactory getInstance(Class source)
		throws IllegalAccessException, InstantiationException
	{   
		return (CryptFactory) source.newInstance();
	}

}
