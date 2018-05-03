package com.synacor.zimbra.store.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.UserServlet;

/** Base class for HTTP-based object store backends using the built-in HttpClient 3.x pool */
public abstract class HttpBackend
	extends Backend
{

	/** The default http client object */
	protected HttpClient defaultClient;
	/** The default http client state */
	protected HttpState  defaultState;

	/**
	 * Return the default HttpClient
	 *
	 * @return HttpClient The default HttpClient
	 */
	public HttpClient getDefaultClient()
	{
		return defaultClient;
	}

	/**
	 * Return the default HttpState
	 *
	 * @return HttpState The default HttpState
	 */
	public HttpState getDefaultState()
	{
		return defaultState;
	}

	public void delete(String location)
		throws HttpException, IOException
	{
		HttpMethod deleteMethod = getDelete(location);
		ZimbraLog.store.debug("Zimberg Store Manager: delete object: %s", deleteMethod.getURI());

		int statusCode;

		try
		{
			statusCode = HttpClientUtil.executeMethod(defaultClient, deleteMethod, defaultState);
		}
		finally
		{
			deleteMethod.releaseConnection();
		}

		switch (statusCode)
		{
			case HttpStatus.SC_OK:
			case HttpStatus.SC_NO_CONTENT:
				break;
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_GONE:
				throw new NoSuchFileException("Could not delete object: " + Integer.toString(statusCode) + ": " + deleteMethod.getStatusText());
			case HttpStatus.SC_FORBIDDEN:
				throw new AccessDeniedException("Could not delete object: " + Integer.toString(statusCode) + ": " + deleteMethod.getStatusText());
			default:
				throw new IOException("Could not delete object: " + Integer.toString(statusCode) + ": " + deleteMethod.getStatusText());
		}
	}

	public InputStream get(String location)
		throws HttpException, IOException
	{

		HttpMethod getMethod = getGet(location);
		ZimbraLog.store.debug("Zimberg Store Manager: retrieve object: %s", getMethod.getURI());

		int statusCode = HttpClientUtil.executeMethod(defaultClient, getMethod, defaultState);

		IOException exp;

		switch (statusCode)
		{
			case HttpStatus.SC_OK:
				return new UserServlet.HttpInputStream(getMethod);
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_GONE:
				exp = new NoSuchFileException("Could not retrieve object: " + getMethod.getStatusText());
			case HttpStatus.SC_FORBIDDEN:
				exp = new AccessDeniedException("Could not retrieve object: " + getMethod.getStatusText());
			default:
				exp = new IOException("Could not retrieve object: " + getMethod.getStatusText());
		}

		getMethod.releaseConnection();
		throw exp;
	}

	public boolean verify(String location)
		throws HttpException, IOException
	{
		HttpMethod verifyMethod = getVerify(location);
		ZimbraLog.store.debug("Zimberg Store Manager: verify object: %s", verifyMethod.getURI());

		int statusCode;

		try
		{
			statusCode = HttpClientUtil.executeMethod(defaultClient, verifyMethod, defaultState);
		}
		finally
		{
			verifyMethod.releaseConnection();
		}

		boolean status = (statusCode == HttpStatus.SC_OK);

		if (status == false)
		{
			ZimbraLog.store.warn("Zimberg Store Manager: verify object: %d: %s", statusCode, verifyMethod.getStatusText());

		}

		return status;
	}

	public void store(Path path, String location, String contentType)
		throws IOException
	{
		store(Files.newInputStream(path), location, Files.size(path), contentType);
	}

	public void store(InputStream is, String location, long size, String contentType)
		throws HttpException, IOException
	{
		EntityEnclosingMethod storeMethod = getStore(location);
		HttpClientUtil.addInputStreamToHttpMethod(storeMethod, is, size, contentType);
		ZimbraLog.store.debug("Zimberg Store Manager: store object: %s", storeMethod.getURI());

		int statusCode;

		try
		{
			statusCode = HttpClientUtil.executeMethod(defaultClient, storeMethod, defaultState);
		}
		finally
		{
			storeMethod.releaseConnection();
		}

		switch (statusCode)
		{
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
			case HttpStatus.SC_NO_CONTENT:
				break;
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_GONE:
				throw new NoSuchFileException("Could not create object: " + Integer.toString(statusCode) + ": " + storeMethod.getStatusText());
			case HttpStatus.SC_FORBIDDEN:
				throw new AccessDeniedException("Could not create object: " + Integer.toString(statusCode) + ": " + storeMethod.getStatusText());
			case HttpStatus.SC_CONFLICT:
				throw new FileAlreadyExistsException("Could not create object: " + Integer.toString(statusCode) + ": " + storeMethod.getStatusText());
			default:
				throw new IOException("Could not create object: " + Integer.toString(statusCode) + ": " + storeMethod.getStatusText());
		}
	}

	/**
	 * Generate a method to remove a blob
	 *
	 * @param location The location of the target object
	 * @return HttpMethod An http method that will delete the blob
	 * @throws IOException if there is a failure constructing the request
	 */
	public abstract HttpMethod getDelete(String location)
		throws IOException;

	/**
	 * Generate a method for retrieving a blob
	 *
	 * @param location The location of the target object
	 * @return HttpMethod An http method that will get the blob
	 * @throws IOException if there is a failure constructing the request
	 */
	public abstract HttpMethod getGet(String location)
		throws IOException;

	/**
	 * Generate a method for storing a blob
	 *
	 * @param location The location of the target object
	 * @return HttpMethod An http method that will store the blob
	 * @throws IOException if there is a failure constructing the request
	 */
	public abstract EntityEnclosingMethod getStore(String location)
		throws IOException;

	/**
	 * Generate a method for verifying a blob
	 *
	 * @param location The location of the target object
	 * @return HttpMethod An http method that will verify the blob
	 * @throws IOException if there is a failure constructing the request
	 */
	public abstract HttpMethod getVerify(String location)
		throws IOException;

}
