package com.citrisoft.zimbra.store.compression;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPCompressor
	extends Compressor
{

	public InputStream getInputStream(InputStream is, long actualSize)
		throws IOException
	{
		return new GZIPInputStream(is);
	}

	public OutputStream getOutputStream(OutputStream os, long actualSize)
		throws IOException
	{
		return new GZIPOutputStream(os);
	}

}
