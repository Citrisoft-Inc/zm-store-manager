package com.citrisoft.zimbra.store.compression;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class Compressor
{
	public abstract InputStream getInputStream(InputStream is, long actualSize)
		throws IOException;
	public abstract OutputStream getOutputStream(OutputStream os, long actualSize)
		throws IOException;
}
