package com.synacor.zimbra.store.profile;

import java.io.InputStream;
import java.io.IOException;
import java.lang.InstantiationException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Properties;

import com.synacor.util.ClassUtil;
import com.synacor.zimbra.store.backend.Backend;
import com.synacor.zimbra.store.location.LocationFactory;

/** A representation of a complete storage configuration */
public class Profile
{
	/** The backing properties for this profile */
	public Properties props = new Properties();

	/** The identifier for this profile */
	public String name;

	/** The storage backend used by this profile */
	public Backend backend;
	/** The location factory used by this profile */
	public LocationFactory locationFactory;

	/**
	 * Constructs a profile based on a filename
	 *
	 * @param fileName The name of a file containing the profile properties
	 * @throws InstantiationException if a required class cannot be instantiated
	 * @throws IOException if the specified file does not exist
	 */
	public Profile(String fileName)
		throws InstantiationException, IOException
	{
		this(Paths.get(fileName));
	}

	/**
	 * Constructs a profile based on a filesystem path
	 *
	 * @param path The filesystem path containing the profile properties
	 * @throws InstantiationException if a required class cannot be instantiated
	 * @throws IOException if the specified file does not exist
	 */
	public Profile(Path path)
		throws InstantiationException, IOException
	{
		String mimeType = Files.probeContentType(path);
		InputStream inputStream = Files.newInputStream(path);

		switch (mimeType)
		{
			case "application/xml":
				props.loadFromXML(inputStream);
				break;
			default:
				props.load(inputStream);
		}

		name = props.getProperty("name");
		String backendClassName = props.getProperty("backend_class");
		String locationFactoryClassName = props.getProperty("location_factory_class");

		backend = ClassUtil.getInstance(backendClassName, Backend.class, props);
		locationFactory = ClassUtil.getInstance(locationFactoryClassName, LocationFactory.class, props);
	}

}
