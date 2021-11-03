package com.citrisoft.zimbra.store.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public abstract class Compressor
{
	public InputStream getInputStream(InputStream is)
		throws IOException
	{
		return getInputStream(is, -1);
	}

	public OutputStream getOutputStream(OutputStream os)
		throws IOException
	{
		return getOutputStream(os, -1);
	}

	public abstract InputStream getInputStream(InputStream is, long actualSize)
		throws IOException;
	public abstract OutputStream getOutputStream(OutputStream is, long actualSize)
		throws IOException;
}
