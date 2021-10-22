package com.citrisoft.util.aws;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.Instant;

import com.citrisoft.util.ByteUtil;

// Class representing an AWS signing key
public class SigningKey
{

	public String key;
	public Instant instant;
	public String region;
	public String service;

	public byte[] bytes;

	/**
	 * Construct a signing key
	 *
	 * @param key The secret key
	 * @param region The region to use in the credential scope
	 * @param service The target service for this signing key
	 * @throws InvalidKeyException if an invalid key is provided
	*/
	public SigningKey(String key, String region, String service)
		throws InvalidKeyException
	{
		this(key,Instant.now(),region,service);
	}

	/**
	 * Construct a signing key with a particular timestamp
	 *
	 * @param key The secret key
	 * @param instant The time of key creation
	 * @param region The region to use in the credential scope
	 * @param service The target service for this signing key
	 * @throws InvalidKeyException if an invalid key is provided
	 */
	public SigningKey(String key, Instant instant, String region, String service)
		throws InvalidKeyException
	{
		this.key = key;
		this.instant = instant;
		this.region = region;
		this.service = service;

		String datestamp = AwsUtil.getDatestamp(instant);

		byte[] seed = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
		byte[] date_mac = ByteUtil.getMAC(datestamp, seed);
		byte[] region_mac = ByteUtil.getMAC(region, date_mac);
		byte[] service_mac = ByteUtil.getMAC(service, region_mac);

		bytes = ByteUtil.getMAC("aws4_request", service_mac);
	}

	/**
	 * Converts signing key to a hex-encoded string
	 *
	 * @return String representing the signing key
	 */
	public String toHexString()
	{
		return ByteUtil.encodeHex(bytes);
	}

}
