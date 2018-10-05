package com.synacor.zimbra.store.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Base class for storage backends */
public abstract class Backend
{

	/**
	 * Store a file to a target location
	 *
	 * @param fileName Name of the file to store
	 * @param location Target location for the blob
	 * @throws IOException if there is a problem storing the blob
	 */
	public void store(String fileName, String location)
		throws IOException
	{
		store(fileName, location, "application/octet-stream");
	}

	/**
	 * Store a file to a target location with a specific content type
	 *
	 * @param fileName Name of the file to store
	 * @param location Target location for the blob
	 * @param contentType IANA Content type of blob
	 * @throws IOException if there is a problem storing the blob
	 */
	public void store(String fileName, String location, String contentType)
		throws IOException
	{
		store(Paths.get(fileName), location, contentType);
	}

	/**
	 * Store a file to a target location with a specific content type
	 *
	 * @param file File to store
	 * @param location Target location for the blob
	 * @param contentType IANA Content type of blob
	 * @throws IOException if there is a problem storing the blob
	 */
	public void store(File file, String location, String contentType)
		throws IOException
	{
		store(file.toPath(), location, contentType);
	}

	/**
	 * Store an input stream to a target location
	 *
	 * @param inputStream Stream to store
	 * @param location Target location for the blob
	 * @param size Size of the object represented by the input stream
	 * @throws IOException if there is a problem storing the blob
	 */
	public void store(InputStream inputStream, String location, long size)
		throws IOException
	{
		store(inputStream, location, size, "application/octet-stream");
	}

	/**
	 * Copy a stored blob to another location
     *
     * @param srcLocation The original location
	 * @param dstLocation The new location
	 * @throws IOException on any error
	 *
	 * TODO: Add default GET / PUT implementation
	 */
	public void copy(String srcLocation, String dstLocation)
		throws IOException
	{
		throw new IOException("Unimplemented operation.");
	}

	/**
	 * Retrieve an input stream for the blob at the specied location
	 *
	 * @param location Location of the blob
	 * @return InputStream Stream of blob data
	 * @throws IOException if there is a problem retrieving the blob
	 */
	public abstract InputStream get(String location)
		throws IOException;

	/**
	 * Delete the mailbox blob at the specified location
	 *
	 * @param location Location of the blob
	 * @throws IOException if there is a problem deleting the blob
	 */
	public abstract void delete(String location)
		throws IOException;

	/**
	 * Store an input stream to a target location
	 *
	 * @param path Filesystem path to store
	 * @param location Target location for the blob
	 * @param contentType IANA Content type of blob
	 * @throws IOException if there is a problem storing the blob
	 */
	public abstract void store(Path path, String location, String contentType)
		throws IOException;

	/**
	 * Store an input stream to a target location
	 *
	 * @param inputStream Stream to store
	 * @param location Target location for the blob
	 * @param contentType IANA Content type of blob
	 * @param size Size of the object represented by the input stream
	 * @throws IOException if there is a problem storing the blob
	 */
	public abstract void store(InputStream inputStream, String location, long size, String contentType)
		throws IOException;

	/**
	 * Verify that an object exists within the store
	 *
	 * @param location Target location for the blob
	 * @throws IOException if there is a problem storing the blob
	 * @return true if object is addressible and accessible
	 */
	public abstract boolean verify(String location)
		throws IOException;


	/**
	 * Return a POJO representing the status of the backend
	 *
	 * @return Object representing the status of the backend
	 */
	public Object getStatus()
	{
		return new Object();
	}
}
