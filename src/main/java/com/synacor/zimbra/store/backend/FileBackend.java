package com.synacor.zimbra.store.backend;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Properties;

/** Backend for persisting objects to a filesystem path */
public class FileBackend
	extends Backend
{

	/**
	 * Construct a filesystem backend
	 *
	 * @param props Initialization parameters
	 */
	public FileBackend(Properties props)
	{   
	}

	public InputStream get(String location)
		throws IOException
	{
		return get(Paths.get(location));
	}

	/**
	 * Get a mailbox blob from the specified path
	 *
	 * @param path Filesystem path to retrieve
	 * @throws IOException if there is a problem deleting the blob
	 * @return InputStream A blob inputstream 
	 */
	public InputStream get(Path path)
		throws IOException
	{
		return Files.newInputStream(path);
	}

	public void delete(String location)
		throws IOException
	{
		delete(Paths.get(location));
	}

	/**
	 * Delete the mailbox blob at the specifed path
	 *
	 * @param path Filesystem path to delete
	 * @throws IOException if there is a problem deleting the blob
	 */
	public void delete(Path path)
		throws IOException
	{
		Files.delete(path);
	}

    public void store(Path path, String location, String contentType)
        throws IOException
	{
		store(path, Paths.get(location), contentType);
	}

    public void store(Path path, Path location, String contentType)
        throws IOException
	{
		try
		{
			Files.copy(path, location);
		}
		catch (NoSuchFileException e)
		{
			Path parent = location.getParent();

			if (parent != null)
			{
				System.out.println("Creating path: " + parent.toString());
				Files.createDirectories(parent);
			}

			Files.copy(path, location);
		}
	}

    public void store(InputStream inputStream, String location, long size, String contentType)
        throws IOException
	{
		store(inputStream, Paths.get(location), size, contentType);
	}

    public void store(InputStream inputStream, Path location, long size, String contentType)
        throws IOException
	{
		try
		{
			Files.copy(inputStream, location);
		}
		catch (NoSuchFileException e)
		{
			Path parent = location.getParent();

			if (parent != null)
			{
				Files.createDirectories(parent);
			}

			Files.copy(inputStream, location);
		}
	}

    public boolean verify(String location)
        throws IOException
	{
		return Files.exists(Paths.get(location));
	}

}
