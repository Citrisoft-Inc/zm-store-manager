package com.citrisoft.util.aws;

import java.security.InvalidKeyException;
import java.time.Instant;

import com.citrisoft.util.ByteUtil;

// Class representing an AWS request signature
public class Signature
{
	public Instant instant;
	public CanonicalRequest request;
	public SigningKey key;
	public byte[] bytes;

	/**
	 * Construct a signature object for a request
	 *
	 * @param request The request being signed
	 * @param key The generated signing key
	 * @param instant The timestamp of the request
	 * @throws InvalidKeyException if an invalid key is provided
	 */
	public Signature(CanonicalRequest request, SigningKey key, Instant instant)
		throws InvalidKeyException
	{
		this.request = request;
		this.key = key;
		this.instant = instant;

		String stringToSign = request.getStringToSign(instant, key);

		this.bytes = ByteUtil.getMAC(stringToSign, key.bytes);
	}

	/**
	 * Construct an authorization string suitable to embeded as a header
	 *
	 * @param accessKey The access key associated with the secret key provided
	 * @return String with the constructed authorization string
	 */
	public String getAuthorizationString(String accessKey)
	{
		StringBuilder sb = new StringBuilder();

		String datestamp = AwsUtil.getDatestamp(instant);
		String credentialScope = AwsUtil.getCredentialScope(instant, key);
		String region = key.region;
		String signedHeaders = request.getSignedHeaders();

		sb.append("AWS4-HMAC-SHA256 Credential=");
        sb.append(accessKey);
        sb.append("/");
		sb.append(credentialScope);
        sb.append(", SignedHeaders=");
        sb.append(signedHeaders);
        sb.append(", Signature=");
        sb.append(toHexString());

		return sb.toString();
	}

	/**
	 * Convert signature to a hex-encoded string
	 *
	 * @return String representing the signature
	 */
	public String toHexString()
	{
		return ByteUtil.encodeHex(bytes);
	}

}
