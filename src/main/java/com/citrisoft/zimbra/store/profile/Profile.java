package com.citrisoft.zimbra.store.profile;

import java.io.InputStream;
import java.io.IOException;
import java.lang.InstantiationException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Properties;

import com.citrisoft.util.ClassUtil;
import com.citrisoft.zimbra.store.backend.Backend;
import com.citrisoft.zimbra.store.compression.Compressor;
import com.citrisoft.zimbra.store.location.LocationFactory;

/** A representation of a complete storage configuration */
public class Profile
{
	/** The backing properties for this profile */
	public Properties props = new Properties();

	/** The identifier for this profile */
	public String name;

	/** The storage backend used by this profile */
	public Backend backend;

	/** The compression implementation to be used by this profile */
	public Compressor compressor;

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

		switch (String.valueOf(mimeType))
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
		String compressorClassName = props.getProperty("compressor_class");

		backend = ClassUtil.getInstance(backendClassName, Backend.class, props);
		compressor = ClassUtil.getInstance(compressorClassName, Compressor.class, props);

		if (compressorClassName != null)
		{
			locationFactory = ClassUtil.getInstance(locationFactoryClassName, LocationFactory.class, props);
		}
	}

	public boolean compressBlobs()
	{
		return compressor != null;
	}

}
