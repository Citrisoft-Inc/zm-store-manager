package com.citrisoft.zimbra.store.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.zimbra.common.net.CustomHostnameVerifier;
import com.zimbra.common.net.SocketFactories;
import com.zimbra.common.util.ZimbraLog;

/** Base class for HTTP-based object store backends using a dedicated HttpClient pool
  *
  * It is expected at miminum that implementing classes will override one or more of 
  * the following methods:
  *
  * getURI(String locator)
  * configureClient(HttpClientBuilder builder)
  * configureRequest(RequestBuilder builder)
  * executeRequest(HttpUriRequest request)
  **/
public abstract class HttpBackend
	extends Backend
{
	/** The default http client object */
	protected CloseableHttpClient httpClient;

	/** The default http connection pool */
	protected PoolingHttpClientConnectionManager connectionManager;

	/** Properties */
	public Properties props;

	/** Base endpoint URI */
	public URI baseURI;

	/** Target URI (if different than base URI) */
	public URI targetURI;

	/** Target host object */
	protected HttpHost targetHost;

	/** Max http connects */
	int maxConn = 16;

	public HttpBackend(Properties props)
	{
		this.props = props;

		String baseURIStr = props.getProperty("base_uri");
		String targetURIStr = props.getProperty("target_uri");
		String maxConnStr = props.getProperty("max_conn");

		try
		{
			this.baseURI = new URI(baseURIStr);

			if (targetURIStr != null)
			{   
				this.targetURI = new URI(targetURIStr);
			
				this.targetHost = URIUtils.extractHost(targetURI);
			}
			else
			{   
				this.targetHost = URIUtils.extractHost(baseURI);
			}

			ZimbraLog.store.debug("Zimberg Store Manager: setting target host to: %s", this.targetHost);
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException("Could not parse URI.", e);
		}

		if (maxConnStr != null)
		{
			try
			{
				maxConn = Integer.parseInt(maxConnStr, 10);
			}
			catch (NumberFormatException e)
			{
				ZimbraLog.store.error("Could not parse max_conn. Using default.");
			}
		}

		HttpClientBuilder builder = HttpClients.custom();

		ConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(
			SocketFactories.defaultSSLSocketFactory(), new CustomHostnameVerifier());
		ConnectionSocketFactory plainFactory = new PlainConnectionSocketFactory();

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
			.register("https", sslFactory)
			.register("http", plainFactory)
			.build();

		connectionManager = new PoolingHttpClientConnectionManager(registry);
		connectionManager.setMaxTotal(maxConn);
		connectionManager.setDefaultMaxPerRoute(maxConn);

		builder.setConnectionManager(connectionManager);
		builder.evictExpiredConnections();
		builder.evictIdleConnections(30, TimeUnit.SECONDS);

		this.httpClient = configureClient(builder).build();
	}


	public HttpClientBuilder configureClient(HttpClientBuilder builder)
	{
		return builder;
	}

	public void delete(String location)
		throws IOException
	{
		HttpUriRequest request = deleteBuilder(location).build();
		ZimbraLog.store.debug("Zimberg Store Manager: delete object: %s", request.getURI());

		int statusCode;
		String statusPhrase;

		CloseableHttpResponse response = executeRequest(request);
		try
		{
			EntityUtils.consume(response.getEntity()); // drain any possible response body
			StatusLine status = response.getStatusLine();
			statusCode = status.getStatusCode();
			statusPhrase = status.getReasonPhrase();
		}
		finally
		{
			response.close();
		}

		switch (statusCode)
		{
			case HttpStatus.SC_OK:
			case HttpStatus.SC_NO_CONTENT:
				break;
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_GONE:
				throw new NoSuchFileException("Could not delete object: " + Integer.toString(statusCode) + ": " + statusPhrase);
			case HttpStatus.SC_FORBIDDEN:
				throw new AccessDeniedException("Could not delete object: " + Integer.toString(statusCode) + ": " + statusPhrase);
			default:
				throw new IOException("Could not delete object: " + Integer.toString(statusCode) + ": " + statusPhrase);
		}
	}

	public InputStream get(String location)
		throws IOException
	{

		HttpUriRequest request = getBuilder(location).build();
		ZimbraLog.store.debug("Zimberg Store Manager: retrieve object: %s", request.getURI());

		CloseableHttpResponse response = executeRequest(request);
		StatusLine status = response.getStatusLine();
		int statusCode = status.getStatusCode();
		String statusPhrase = status.getReasonPhrase();

		IOException exp;

		// FIXME: This is kind of hard to follow.  Clean up.
		switch (statusCode)
		{
			case HttpStatus.SC_OK:
				HttpEntity entity = response.getEntity();
				if (entity != null)
				{
					try
					{
						return entity.getContent();
					}
					catch (IOException e)
					{
						exp = new IOException("Could not retrieve object: " + statusPhrase);
					}
					catch (UnsupportedOperationException e)
					{
						exp = new IOException("Could not retrieve object: Could not create inputStream from entity.");
					}
				}
			
				exp = new IOException("Could not retrieve object: No entity returned.");
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_GONE:
				exp = new NoSuchFileException("Could not retrieve object: " + statusPhrase);
			case HttpStatus.SC_FORBIDDEN:
				exp = new AccessDeniedException("Could not retrieve object: " + statusPhrase);
			default:
				exp = new IOException("Could not retrieve object: " + statusPhrase);
		}

		EntityUtils.consume(response.getEntity());
		response.close();
		throw exp;
	}

	public boolean verify(String location)
		throws IOException
	{
		HttpUriRequest request = verifyBuilder(location).build();
		ZimbraLog.store.debug("Zimberg Store Manager: verify object: %s", request.getURI());

		int statusCode;
		String statusPhrase;

		CloseableHttpResponse response = executeRequest(request);
		try
		{
			EntityUtils.consume(response.getEntity());
			StatusLine status = response.getStatusLine();
			statusCode = status.getStatusCode();
			statusPhrase = status.getReasonPhrase();
		}
		finally
		{
			response.close();
		}

		boolean verifyStatus = (statusCode == HttpStatus.SC_OK);

		if (verifyStatus == false)
		{
			ZimbraLog.store.warn("Zimberg Store Manager: verify object: %d: %s", statusCode, statusPhrase);
		}

		return verifyStatus;
	}

	public void store(Path path, String location, String contentType)
		throws IOException
	{
		store(Files.newInputStream(path), location, Files.size(path), contentType);
	}

	public void store(InputStream is, String location, long size, String contentType)
		throws IOException
	{
		RequestBuilder builder = storeBuilder(location);
		HttpEntity entity = new InputStreamEntity(is, size, ContentType.create(contentType));
		HttpUriRequest request = builder.setEntity(entity).build();

		ZimbraLog.store.debug("Zimberg Store Manager: store object: %s", request.getURI());

		int statusCode;
		String statusPhrase;

		CloseableHttpResponse response = executeRequest(request);
		try
		{
			EntityUtils.consume(response.getEntity());
			StatusLine status = response.getStatusLine();
			statusCode = status.getStatusCode();
			statusPhrase = status.getReasonPhrase();
		}
		finally
		{
			response.close();
		}

		switch (statusCode)
		{
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
			case HttpStatus.SC_NO_CONTENT:
				break;
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_GONE:
				throw new NoSuchFileException("Could not create object: " + Integer.toString(statusCode) + ": " + statusPhrase);
			case HttpStatus.SC_FORBIDDEN:
				throw new AccessDeniedException("Could not create object: " + Integer.toString(statusCode) + ": " + statusPhrase);
			case HttpStatus.SC_CONFLICT:
				throw new FileAlreadyExistsException("Could not create object: " + Integer.toString(statusCode) + ": " + statusPhrase);
			default:
				throw new IOException("Could not create object: " + Integer.toString(statusCode) + ": " + statusPhrase);
		}
	}

	/**
	 * Override to perform common configuration (e.g. adding an auth header)
	 *
	 * @param builder The (possibly already partially configured) builder).
	 *
	 * @return the (now possibly more configured) builder.
	 */
	protected RequestBuilder configureRequest(RequestBuilder builder)
	{
		return builder;
	}

	/**
	 * Allows customized execution of a configured request; e.g. to implement
	 * logic in choosing a target or to add a response interceptor
	 *
	 * @param request The configured request.
	 *
	 * @return the HTTP response
	 *
	 * @throws IOException on failed execution
	 */

	protected CloseableHttpResponse executeRequest(HttpUriRequest request)
		throws IOException
	{
		return httpClient.execute(targetHost, request); 
	}

	/**
	 * Generate a URI from a locator
	 *
	 * @param locator A qualified locator
	 *
	 * @return a fully qualified URI
	 */
	public abstract URI generateURI(String locator);

	/**
	 * Generate a method to remove a blob
	 *
	 * @param location The location of the target object
	 * @return RequestBuilder An http method that will delete the blob
	 *
	 * @throws IOException if there is a failure constructing the request
	 */
	public RequestBuilder deleteBuilder(String location)
		throws IOException
	{
		URI uri = generateURI(location);
		return configureRequest(RequestBuilder.delete(uri));
	}

	/**
	 * Generate a method for retrieving a blob
	 *
	 * @param location The location of the target object
	 * @return RequestBuilder An http method that will get the blob
	 * @throws IOException if there is a failure constructing the request

	 */
	public RequestBuilder getBuilder(String location)
		throws IOException
	{
		URI uri = generateURI(location);
		return configureRequest(RequestBuilder.get(uri));
	}

	/**
	 * Generate a method for storing a blob
	 *
	 * @param location The location of the target object
	 * @return RequestBuilder An http method that will store the blob
	 * @throws IOException if there is a failure constructing the request
	 */
	public RequestBuilder storeBuilder(String location)
		throws IOException
	{
		URI uri = generateURI(location);
		return configureRequest(RequestBuilder.put(uri));
	}

	/**
	 * Generate a method for verifying a blob
	 *
	 * @param location The location of the target object
	 * @return RequestBulder An http method that will verify the blob
	 * @throws IOException if there is a failure constructing the request
	 */
	public RequestBuilder verifyBuilder(String location)
		throws IOException
	{
		URI uri = generateURI(location);
		return configureRequest(RequestBuilder.head(uri));
	}

	public Object getStatus()
	{   
		HashMap<String,String> status = new HashMap<>();

		//FIXME: Probably break out connection manager to avoid using deprecated method
		PoolStats stats = connectionManager.getTotalStats();

		status.put("available", Integer.toString(stats.getAvailable()));
		status.put("leased",    Integer.toString(stats.getLeased()));
		status.put("maximum",   Integer.toString(stats.getMax()));
		status.put("pending",   Integer.toString(stats.getPending()));

		return status;
	}

}
