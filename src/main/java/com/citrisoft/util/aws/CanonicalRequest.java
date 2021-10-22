package com.citrisoft.util.aws;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import com.citrisoft.util.ByteUtil;


// A class represneting an canonical request against the AWS interface
public class CanonicalRequest
{
	String method = "";
	String path = "";
	String queryString = "";
	String payloadSignature = "UNSIGNED-PAYLOAD";
	LinkedHashMap<String,String> headers = new LinkedHashMap<>();

	/**
	 * Setter for the HTTP method of the request
	 *
	 * @param method The request method
	 */
	public void setMethod(String method)
	{
		if (method != null)
			this.method = method;
	}

	/**
	 * Setter for the path of the request
	 *
	 * @param path The request path
	 */
	public void setPath(String path)
	{
		if (path != null)
			this.path = path;
	}

	/**
	 * Setter for the query string of the request
	 *
	 * @param queryString The query string for the request
	 */
	public void setQueryString(String queryString)
	{
		if (queryString != null)
			this.queryString = queryString;
	}

	/**
	 * Setter for the payload signature of the request
	 *
	 * @param payloadSignature The payload signature of the request
	 */
	public void setPayloadSignature(String payloadSignature)
	{
		if (payloadSignature != null)
			this.payloadSignature = payloadSignature;
	}

	/**
	 * Add a header to be signed to this request
	 *
	 * @param name The header name
	 * @param value The header value
	 */
	public void addHeader(String name, String value)
	{
		headers.put(name.trim().toLowerCase(), value);
	}

	/**
	 * Returns signed headers
	 *
	 * @return String containing a semi-colon delimited list of signed headers
	 */
	public String getSignedHeaders()
	{
		return String.join(";", headers.keySet());
	}

	/**
	 * Returns the timestamp of this request as derived from the headers
	 *
	 * @return the timestamp in whatever format it is stored in
	 */
	public String getTimestamp()
	{
		return headers.containsKey("x-amz-date") ? headers.get("x-amz-date") : headers.get("date");
	}

	/**
	 * Generates a signing string for the request
	 *
	 * @param instant The time of the request
	 * @param key The signing key to use on the request
	 * @return String that needs to be signed
	 */
	public String getStringToSign(Instant instant, SigningKey key)
	{
		return getStringToSign(instant, key.region, key.service);
	}

	/**
	 * Generates a signing string for the request
	 *
	 * @param instant The time of the request
	 * @param region The region the request is bound for
	 * @param service The service the requires
	 * @return String that needs to be signed
	 */
	public String getStringToSign(Instant instant, String region, String service)
	{
		StringBuilder sb = new StringBuilder();

		String timestamp = AwsUtil.getTimestamp(instant);
		String credentialScope = AwsUtil.getCredentialScope(instant, region, service);
		String hashedRequest = this.getHexString();

		sb.append("AWS4-HMAC-SHA256\n");
		sb.append(timestamp);
		sb.append("\n");
		sb.append(credentialScope);
		sb.append("\n");
		sb.append(hashedRequest);

		return sb.toString();
	}

	/**
	 * Renders the request as a string; necessary to build string to sign
	 *
	 * @return the request as a string
	 */
	public String toString()
	{

		StringBuilder sb = new StringBuilder();

		sb.append(method);
		sb.append("\n");
		sb.append(path);
		sb.append("\n");
		sb.append(queryString);
		sb.append("\n");

		for (Map.Entry<String,String> header: headers.entrySet())
		{   
			sb.append(header.getKey());
			sb.append(":");
			sb.append(header.getValue());
			sb.append("\n");
		}

		sb.append("\n");
		sb.append(getSignedHeaders());

		sb.append("\n");
		sb.append(payloadSignature);

		return sb.toString();
	}

	/**
	 * Generates a signature for the represented request
	 *
	 * @param instant The time of the request
	 * @param key The signing key to use for the request
	 * @return Signature for the request
	 * @throws InvalidKeyException if an invalid key is provided
	 */
	public Signature sign(SigningKey key, Instant instant)
		throws InvalidKeyException
	{
		return new Signature(this, key, instant);
	}

	/**
	 * Returns a byte representation of the request string
	 *
	 * @return byte[] representation of the request
	 */
	public byte[] getBytes()
	{
		return toString().getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Retruns a string representatiion of the request
	 *
	 * @return String representing the request
	 */
	public String getHexString()
	{
		return ByteUtil.encodeHex(ByteUtil.getDigest(getBytes()));
	}

}
