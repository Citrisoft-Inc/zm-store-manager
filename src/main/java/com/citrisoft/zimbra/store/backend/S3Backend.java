package com.citrisoft.zimbra.store.backend;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.zimbra.common.util.ZimbraHttpConnectionManager;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.citrisoft.util.aws.AwsUtil;
import com.citrisoft.util.aws.CanonicalRequest;
import com.citrisoft.util.aws.SigningKey;
import com.citrisoft.util.aws.Signature;

import com.zimbra.common.util.ZimbraLog;

public class S3Backend
	extends HttpBackend
{
	public String endpoint;
	public String region;
	public String bucket;
	public String accessKey;
	public String secretKey;

	public S3Backend(Properties props)
	{
		this(
			props.getProperty("aws_endpoint"),
			props.getProperty("aws_region"),
			props.getProperty("aws_bucket"),
			props.getProperty("aws_access_key"),
			props.getProperty("aws_secret_key")
		);
	}

	public S3Backend(String endpoint, String region, String bucket, String accessKey, String secretKey)
	{
		this.endpoint = endpoint;
		this.region = region;
		this.bucket = bucket;
		this.accessKey = accessKey;
		this.secretKey = secretKey;

		defaultState = new HttpState();
		defaultClient = ZimbraHttpConnectionManager.getInternalHttpConnMgr().getDefaultHttpClient();
	}

	public DeleteMethod getDelete(String location)
		throws IOException
	{
		DeleteMethod method = new DeleteMethod(getUri(location));
		signMethod(method);
		return method;
	}

	public GetMethod getGet(String location)
		throws IOException
	{
		GetMethod method = new GetMethod(getUri(location));
		signMethod(method);
		return method;
	}

	public GetMethod getGet(String location, Instant instant)
		throws IOException
	{
		GetMethod method = new GetMethod(getUri(location));
		signMethod(method, instant);
		return method;
	}

	public HeadMethod getVerify(String location)
		throws IOException
	{
		HeadMethod method = new HeadMethod(getUri(location));
		signMethod(method);
		return method;
	}

	public PutMethod getStore(String location)
		throws IOException
	{
		PutMethod method = new PutMethod(getUri(location));
		signMethod(method);
		return method;
	}

	private String getUri(String location)
	{
		return String.format("https://%s/%s/%s", endpoint, bucket, location);
	}

	/**
	 * Constuct a canonical request from an HttpMethod
	 *
	 * @param method An HttpMethod we need to sign
	 * @return String An AWS canonical request
	 */
	private CanonicalRequest getCanonicalRequest(HttpMethod method)
	{
		return getCanonicalRequest(method, "UNSIGNED-PAYLOAD");
	}

	/**
	 * Conduct a canonical request from an HttpMethod
	 *
	 * @param method An HttpMethod we need to sign
	 * @param payloadSignature A hex SHA-256 digest of the payload
	 * @return String An AWS canonical request
	 */
	private CanonicalRequest getCanonicalRequest(HttpMethod method, String payloadSignature)
	{

		CanonicalRequest cr = new CanonicalRequest();

		cr.setMethod(method.getName());
		cr.setPath(method.getPath());
		cr.setQueryString(method.getQueryString());

		for (Header header: method.getRequestHeaders())
		{
			cr.addHeader(header.getName(), header.getValue());
		}

		cr.setPayloadSignature(payloadSignature);

		return cr;
	}

	private void signMethod(HttpMethod method)
		throws IOException
	{
		signMethod(method, Instant.now());
	}

	private void signMethod(HttpMethod method, Instant instant)
		throws IOException
	{
		method.addRequestHeader("Host", endpoint);
		method.addRequestHeader("X-Amz-Content-SHA256", "UNSIGNED-PAYLOAD");
		method.addRequestHeader("X-Amz-Date", AwsUtil.getTimestamp(instant));

		CanonicalRequest canonicalRequest = getCanonicalRequest(method);

		try
		{
			SigningKey signingKey = new SigningKey(secretKey, instant, region, "s3");
			Signature signature = canonicalRequest.sign(signingKey, instant);
			String authorization = signature.getAuthorizationString(accessKey);

			ZimbraLog.store.trace("Zimberg Store Manager: S3 signing key: " + signingKey.toHexString());
			ZimbraLog.store.trace("Zimberg Store Manager: S3 signature: " + signature.toHexString());
			ZimbraLog.store.trace("Zimberg Store Manager: S3 auth string: " + authorization);

			method.addRequestHeader("Authorization", authorization);
		}
		catch (InvalidKeyException e)
		{
			throw new IOException("Could not sign HttpMethod: " + e.getMessage());
		}

	}

}
