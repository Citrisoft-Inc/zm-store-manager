package com.synacor.zimbra.store.profile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;

import com.zimbra.common.util.ZimbraLog;

/** Class containing methods for loading and caching profiles */
public class Profiles
{

	/** Map of loaded and initialized profiles */
	private static HashMap<String,Profile> profiles = new HashMap<String,Profile>();

	/**
	 * Retrieves a loaded profile by name
	 *
	 * @param name The name of the requested profile
	 * @return Profile The requested profile
	 */
	public static Profile get(String name)
	{
		//FIX: Add NoSuchProfile exception for missing profile?
		return profiles.get(name);
	}

	/**
	 * Adds a single profile by filename
	 *
	 * @param fileName The name of the file containing the profile
	 * @return Profile The newly loaded profile
	 * @throws InstantiationException if the profile could not be loaded
	 * @throws IOException if the file could not be read
	 */
	public static Profile add(String fileName)
		throws InstantiationException, IOException
	{
		return add(Paths.get(fileName));
	}

	/**
	 * Adds a single profile by filesystem path
	 *
	 * @param path The path containing the profile
	 * @return Profile The newly loaded profile
	 * @throws InstantiationException if the profile could not be loaded
	 * @throws IOException if the file could not be read
	 */
	public static Profile add(Path path)
		throws InstantiationException, IOException
	{
		Profile profile = new Profile(path);
		ZimbraLog.store.debug("Zimberg Store Manager: loaded profile: " + path.toString());
		ZimbraLog.store.debug("Zimberg Store Manager: profile data: " + profile.toString());
		return profiles.put(profile.name, profile);
	}

	/**
	 * Loads a directory of profiles by name
	 *
	 * @param directoryName The name of the directory containing the profiles
	 * @throws IOException if directory cannot be read
	 */
	public static void load(String directoryName)
		throws IOException
	{
		load(Paths.get(directoryName));
	}

	/**
	 * Loads a directory of profiles by path
	 *
	 * @param path The path of the directory containing the profiles
	 * @throws IOException if the direcotry cannot be read
	 */
	public static void load(Path path)
		throws IOException
	{
		DirectoryStream<Path> stream = Files.newDirectoryStream(path);

		for(Path profilePath: stream)
		{
			try
			{
				add(profilePath);
			}
			catch (InstantiationException e)
			{
				ZimbraLog.store.error("Zimberg Store Manager: error loading profile: " + profilePath + ": " + e.getMessage());
			}
		}
	}
}
