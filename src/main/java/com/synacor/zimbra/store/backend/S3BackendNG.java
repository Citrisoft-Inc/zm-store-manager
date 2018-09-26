package com.synacor.zimbra.store.backend;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synacor.util.ByteUtil;
import com.synacor.util.aws.AwsUtil;
import com.synacor.util.aws.CanonicalRequest;
import com.synacor.util.aws.SigningKey;
import com.synacor.util.aws.Signature;

import com.zimbra.common.util.ZimbraLog;

public class S3BackendNG
	extends HttpClientBackend
{
	public String region;
	public String bucket;
	public String accessKey;
	public String secretKey;

	public S3BackendNG(Properties props)
	{
		super(props);

		this.region = props.getProperty("aws_region");
		this.bucket = props.getProperty("aws_bucket");
		this.accessKey = props.getProperty("aws_access_key");
		this.secretKey = props.getProperty("aws_secret_key");

	}	

	public URI generateURI(String location)
	{

		return baseURI.resolve("/"+bucket+"/"+location);
	}

	/**
	 * Constuct a canonical request from an HttpMethod
	 *
	 * @param method An HttpMethod we need to sign
	 * @return String An AWS canonical request
	 */
	private CanonicalRequest getCanonicalRequest(HttpUriRequest request)
	{   
		return getCanonicalRequest(request, "UNSIGNED-PAYLOAD");
	}

	/**
	 * Conduct a canonical request from an HttpMethod
	 *
	 * @param method An HttpMethod we need to sign
	 * @param payloadSignature A hex SHA-256 digest of the payload
	 * @return String An AWS canonical request
	 */
	private CanonicalRequest getCanonicalRequest(HttpUriRequest request, String payloadSignature)
	{   

		CanonicalRequest cr = new CanonicalRequest();
		URI uri = request.getURI();

		cr.setMethod(request.getMethod());
		cr.setPath(uri.getPath());
		cr.setQueryString(uri.getQuery());

		for (Header header: request.getAllHeaders())
		{   
			cr.addHeader(header.getName(), header.getValue());
		}

		cr.setPayloadSignature(payloadSignature);

		return cr;
	}

	@Override
	protected CloseableHttpResponse executeRequest(HttpUriRequest request)
		throws IOException
	{   
		Instant instant = Instant.now();

		request.addHeader("Host", request.getURI().getHost());
		request.addHeader("X-Amz-Content-SHA256", "UNSIGNED-PAYLOAD");
		request.addHeader("X-Amz-Date", AwsUtil.getTimestamp(instant));

		CanonicalRequest canonicalRequest = getCanonicalRequest(request);

		try
		{  
			SigningKey signingKey = new SigningKey(secretKey, instant, region, "s3");
			Signature signature = canonicalRequest.sign(signingKey, instant);
			String authorization = signature.getAuthorizationString(accessKey);

			ZimbraLog.store.trace("Zimberg Store Manager: S3 signing key: " + signingKey.toHexString());
			ZimbraLog.store.trace("Zimberg Store Manager: S3 signature: " + signature.toHexString());
			ZimbraLog.store.trace("Zimberg Store Manager: S3 auth string: " + authorization);

			request.addHeader("Authorization", authorization);
		}
		catch (InvalidKeyException e)
		{   
			throw new IOException("Could not sign HttpMethod: " + e.getMessage());
		}
		
		return httpClient.execute(targetHost, request);
	}

}
