package com.synacor.zimbra.store.backend;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.util.Properties;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;

/** Backend adaptor for the Hitatchi Content Platform */
public class HcpBackendNG
	extends HttpClientBackend
{
	public HcpBackendNG(Properties props)
	{
		super(props);
	}

	public HttpClientBuilder configureClient(HttpClientBuilder builder)
		throws IllegalArgumentException
	{
		String username = props.getProperty("hcp_username");
		String password = props.getProperty("hcp_password");
		String encUsername = ByteUtil.encodeFSSafeBase64(username.getBytes());
		String encPassword = ByteUtil.getMD5Digest(password.getBytes(), false);

		CookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie authCookie = new BasicClientCookie("hcp-ns-auth", encUsername + ":" + encPassword);
		authCookie.setDomain(baseURI.getHost());
		authCookie.setPath("/");
		authCookie.setSecure(false);
		cookieStore.addCookie(authCookie);

		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		builder.setDefaultRequestConfig(requestConfig);

		builder.setDefaultCookieStore(cookieStore);

		ZimbraLog.store.debug("Zimberg Store Manager: HCP: " + authCookie.toString());

		return builder;
	}

	public URI generateURI(String location)
	{
		return baseURI.resolve("/rest/"+location);
	}

}
