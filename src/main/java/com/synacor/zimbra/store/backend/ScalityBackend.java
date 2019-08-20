package com.synacor.zimbra.store.backend;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.util.Properties;

public class ScalityBackend
	extends HttpClientBackend
{

	private String prefix;

	public ScalityBackend(Properties props)
	{
		super(props);
		String useArcStr = props.getProperty("scality_use_arc");
		prefix = Boolean.parseBoolean(useArcStr) ? "/proxy/arc/" : "/proxy/chord/";
	}

	public URI generateURI(String location)
	{
		return baseURI.resolve(prefix).resolve(location);
	}
}
