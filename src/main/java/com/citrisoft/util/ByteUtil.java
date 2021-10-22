package com.citrisoft.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


// Collection of utility methods
public class ByteUtil
{
	// Default HMAC algorithm
	public static final String DEFAULT_HMAC = "HmacSHA256";
	// Default digest algorithm
	public static final String DEFAULT_DIGEST = "SHA-256";
	// Array of hex digits used in encoding method
	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	/**
	 * Perform a MAC operation using the default algorithm
	 *
	 * @param data The data to be encrypted.
	 * @param key A byte array representing the secret key.
	 * @return byte[] of the applied MAC operation
	 * @throws InvalidKeyException if an invalid key is provided
	 */
	public static byte[] getMAC(String data, byte[] key)
		throws InvalidKeyException
	{
		byte[] mac = null;

		// Die on exception since we rely only on required algorithms here
		try
		{
			mac = getMAC(DEFAULT_HMAC, data, key);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("Your JVM is broken: " + e.getMessage());
			System.exit(0);
		}

		return mac;
	}

	/**
	 * Perform a MAC operation using the specified algorithm
	 *
	 * @param algorithm The name of the requested HMAC algorithm
	 * @param data The data to be encrypted.
	 * @param key A byte array representing the secret key.
	 * @return byte[] of the applied MAC operation
	 * @throws NoSuchAlgorithmException if an invalid algorith name is provided
	 * @throws InvalidKeyException if an invalid key is provided
	 */
	public static byte[] getMAC(String algorithm, String data, byte[] key)
		throws NoSuchAlgorithmException, InvalidKeyException
	{
		Mac mac = Mac.getInstance(algorithm);
		SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);

		mac.init(keySpec);
		return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Perform a digest operation using the default algorithm
	 *
	 * @param data The data to digest (nom nom)
	 * @return byte[] The digested data
	 */
	public static byte[] getDigest(byte[] data)
	{
		byte[] digest = null;

		// Die on exception since we rely only on required algorithms here
		try
		{
			digest = getDigest(DEFAULT_DIGEST, data);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("Your JVM is broken: " + e.getMessage());
			System.exit(0);
		}

		return digest;
	}

	/**
	 * Perform a digest operation using the specified algorithm
	 *
	 * @param algorithm The name of the requested digest algorithm
	 * @param data The data to digest (nom nom)
	 * @return byte[] The digested data
	 * @throws NoSuchAlgorithmException if an invalid algorith name is provided
	 */
	public static byte[] getDigest(String algorithm, byte[] data)
		throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance(algorithm);

		return md.digest(data);
	}

	/**
	 * Encode a byte array as a hex string
	 *
	 * @param data The raw data
	 * @return String with the hex-encoded data
	 *
	 */
	public static String encodeHex(byte[] data)
	{
		char[] chars = new char[data.length << 1];

		int i = 0;
		for (byte b: data)
		{
			chars[i++] = HEX_DIGITS[(0xF0 & b) >>> 4];
			chars[i++] = HEX_DIGITS[(0x0F & b)];
		}

		return new String(chars);
	}

}
