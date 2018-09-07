package com.synacor.zimbra.store.backend;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;

/** Backend adaptor for the Hitatchi Content Platform */
public class HcpBackendNG
	extends HttpClientBackend
{
	/** Base URI for the HCP namespace */
	public URI baseURI;

	/** Target URI (if different than base URI) */
	public URI targetURI;

	/** Target host object */
	private HttpHost targetHost;

	/**
	 * Construct a backend object based on a Properties object
	 *
	 * @param props Properties object containing initialization parameters
	 * @throws URISyntaxException if passed an invalid namespace URI
	 */
	public HcpBackendNG(Properties props)
		throws URISyntaxException
	{
		this(
			props.getProperty("hcp_base_uri"),
			props.getProperty("hcp_target_uri"),
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
	public HcpBackendNG(String baseURIStr, String targetURIStr, String username, String password)
		throws IllegalArgumentException
	{
		try
		{
			this.baseURI = new URI(baseURIStr);

			if (targetURIStr != null)
			{
				targetURI = new URI(targetURIStr);
				targetHost = URIUtils.extractHost(targetURI);
			}
			else
			{
				targetHost = URIUtils.extractHost(baseURI);
			}
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException("Could not parse URI.", e);
		}

		String encUsername = ByteUtil.encodeFSSafeBase64(username.getBytes());
		String encPassword = ByteUtil.getMD5Digest(password.getBytes(), false);

		HttpClientBuilder builder = getClientBuilder();

		String cookieHost = baseURI.getHost();

		CookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie authCookie = new BasicClientCookie("hcp-ns-auth", encUsername + ":" + encPassword);
		authCookie.setDomain(cookieHost);
		authCookie.setPath("/");
		authCookie.setSecure(false);
		cookieStore.addCookie(authCookie);

		builder.setDefaultCookieStore(cookieStore);

		ZimbraLog.store.debug("Zimberg Store Manager: HCP: " + authCookie.toString());

		this.httpClient = builder.build();
	}

	public URI generateURI(String location)
	{
		return baseURI.resolve("/rest/"+location);
	}

}
