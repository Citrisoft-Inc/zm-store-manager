package com.synacor.zimbra.store.backend;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/** Backend adaptor for the Hitatchi Content Platform */
public class HcpBackend
	extends HttpBackend
{
	/** Base URI for the HCP namespace */
	public String baseURI;

	/** Virtual hostname for the HCP namespace */
	public String virtualHost;

	/**
	 * Construct a backend object based on a Properties object
	 *
	 * @param props Properties object containing initialization parameters
	 * @throws URISyntaxException if passed an invalid namespace URI
	 */
	public HcpBackend(Properties props)
		throws URISyntaxException
	{
		this(
			props.getProperty("hcp_base_uri"),
			props.getProperty("hcp_virtual_host"),
			props.getProperty("hcp_username"),
			props.getProperty("hcp_password")
		);
	}

	/**
	 * Construct a backend object
	 *
	 * @param baseURI Base URI for the HCP namespace
	 * @param virtualHost Username for the HCP platform
	 * @param username Username for the HCP platform
	 * @param password Password for the HCP platform
	 * @throws URISyntaxException if passed an invalid namespace URI
	 */
	public HcpBackend(String baseURI, String virtualHost, String username, String password)
		throws URISyntaxException
	{
		this.baseURI = baseURI;
		this.virtualHost = virtualHost;

		try
		{
			URI uri = new URI(baseURI);
			String encUsername = ByteUtil.encodeFSSafeBase64(username.getBytes());
			String encPassword = ByteUtil.getMD5Digest(password.getBytes(), false);
			String cookieHost = (virtualHost) == null ? uri.getHost() : virtualHost;
			Cookie authCookie = new Cookie(cookieHost, "hcp-ns-auth", encUsername + ":" + encPassword, "/", null, false);

			ZimbraLog.store.debug("Zimberg Store Manager: HCP hcp-ns-auth: " + authCookie.toString());
 
			defaultState = new HttpState();
			defaultState.addCookie(authCookie);

			defaultClient = ZimbraHttpConnectionManager.getInternalHttpConnMgr().getDefaultHttpClient();
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException("Could not parse URI.", e);
		}
	}

	/**
	 * Copy an object from one location to another
	 */
	public void copy(String srcLocation, String dstLocation)
		throws IOException
	{
		PutMethod putMethod = getStore(dstLocation);
		putMethod.addRequestHeader("X-HCP-CopySource", srcLocation);

		int statusCode;

		try
		{
			statusCode = HttpClientUtil.executeMethod(defaultClient, putMethod, defaultState);
		}
		catch (HttpException e)
		{
			throw new IOException("Could not copy " + srcLocation + " to " + dstLocation + ": " + e.getMessage());
		}
		finally
		{
			putMethod.releaseConnection();
		}

		if (statusCode != HttpStatus.SC_OK)
		{
			throw new IOException("Could not copy " + srcLocation + " to " + dstLocation + ": " + putMethod.getStatusText());
		}
	}

	public DeleteMethod getDelete(String location)
	{
		DeleteMethod method = new DeleteMethod(getURI(location));
		setParams(method);
		return method;
	}

	public GetMethod getGet(String location)
	{
		GetMethod method = new GetMethod(getURI(location));
		setParams(method);
		return method;
	}

	public HeadMethod getVerify(String location)
	{
		HeadMethod method = new HeadMethod(getURI(location));
		setParams(method);
		return method;
	}

	public PutMethod getStore(String location)
	{
		PutMethod method = new PutMethod(getURI(location));
		setParams(method);
		return method;
	}

	private String getURI(String location)
	{
		return baseURI + "/rest/" + location;
	}

	/**
	 * Set default parameters required for all methods
	 *
	 * @param method The method to operate on
	 */
	private void setParams(HttpMethod method)
	{
		HttpMethodParams params = method.getParams();

		params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

		if (virtualHost != null)
		{
			params.setVirtualHost(virtualHost);
		}
	}

}
