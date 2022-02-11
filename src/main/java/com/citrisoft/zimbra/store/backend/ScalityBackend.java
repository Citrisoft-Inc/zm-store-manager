package com.citrisoft.zimbra.store.backend;

import java.net.URI;
import java.util.Properties;

public class ScalityBackend
	extends HttpBackend
{

	private String prefix;

	public ScalityBackend(Properties props)
	{
		super(props);
		String driverStr = props.getProperty("scality_driver");
		prefix = "/proxy/" + driverStr + "/";
	}

	public URI generateURI(String location)
	{
		return baseURI.resolve(prefix).resolve(location);
	}
}
