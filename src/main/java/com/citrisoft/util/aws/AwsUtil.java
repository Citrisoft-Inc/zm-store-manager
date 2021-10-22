package com.citrisoft.util.aws;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

// Collection of utility methods for interacting with the AWS interface
public class AwsUtil
{

	/**
	 * Generate a credential scope string from a time and signing key
	 *
	 * @param instant The time contained in the date or x-amz-date header
	 * @param key A valid signing key for the specified region and service
	 * @return A string representing the scope
	 */
	protected static String getCredentialScope(Instant instant, SigningKey key)
	{
		return getCredentialScope(instant, key.region, key.service);
	}

	/**
	 * Generate a credential scope string from a time, region and service
	 *
	 * @param instant The time contained in the date or x-amz-date header
	 * @param region The AWS region the request targets
	 * @param service The AWS service the request targets
	 * @return String representing the scope
	 */
	protected static String getCredentialScope(Instant instant, String region, String service)
	{
		return String.format("%s/%s/%s/aws4_request", getDatestamp(instant), region, service);
	}

	/**
	 * Generate a timestamp string for the current instant
	 * 
	 * @return String with the formatted time
	 */
	public static String getTimestamp()
	{   
		return getTimestamp(Instant.now());
	}

	/**
	 * Generate a timestamp string for a given instant
	 * 
	 * @param instant The time being formatted
	 * @return String with the formatted time
	 */
	public static String getTimestamp(Instant instant)
	{   
		return DateTimeFormatter
				.ofPattern("yyyyMMdd'T'HHmmssX")
				.withZone(ZoneOffset.UTC)
				.format(instant);
	}

	/**
	 * Generate a datestamp string for a given instant
	 * 
	 * @param instant The time being formatted
	 * @return String with the formatted time
	 */
	public static String getDatestamp(Instant instant)
	{   
		return DateTimeFormatter
				.ofPattern("yyyyMMdd")
				.withZone(ZoneOffset.UTC)
				.format(instant);
	}

}
