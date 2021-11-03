package com.citrisoft.zimbra.store.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.zimbra.common.util.ZimbraLog;

public class GZIPCompressor
	extends Compressor
{

	public GZIPCompressor(Properties props)
	{
	}

	public InputStream getInputStream(InputStream is, long actualSize)
		throws IOException
	{
		return new GZIPInputStream(is);
	}

	public OutputStream getOutputStream(OutputStream is, long actualSize)
		throws IOException
	{
		return new GZIPOutputStream(is);
	}

}
