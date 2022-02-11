package com.citrisoft.zimbra.store.backend;

import java.io.IOException;
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

import com.citrisoft.util.aws.AwsUtil;
import com.citrisoft.util.aws.CanonicalRequest;
import com.citrisoft.util.aws.SigningKey;
import com.citrisoft.util.aws.Signature;

import com.zimbra.common.util.ZimbraLog;

public class S3Backend
	extends HttpBackend
{
	public String region;
	public String bucket;
	public String accessKey;
	public String secretKey;
	public Boolean prependBucket;

	public S3Backend(Properties props)
	{
		super(props);

		this.region = props.getProperty("s3_region");
		this.bucket = props.getProperty("s3_bucket");
		this.accessKey = props.getProperty("s3_access_key");
		this.secretKey = props.getProperty("s3_secret_key");
		this.prependBucket = Boolean.parseBoolean(props.getProperty("s3_prepend_bucket", "true"));

	}	

	public URI generateURI(String location)
	{
		return baseURI.resolve(prependBucket ? "/"+bucket+"/"+location : "/"+location);
		//return baseURI.resolve("/"+bucket+"/"+location);
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
