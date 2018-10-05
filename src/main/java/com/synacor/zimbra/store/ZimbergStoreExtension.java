package com.synacor.zimbra.store;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;

public class ZimbergStoreExtension
	implements ZimbraExtension
{

	/** Extension to load ZimbergStoreManager implementation */
	public ZimbergStoreExtension()
	{
	}

	/** Returns extension identifier */
	public String getName()
	{
		return "ZimbergConnector";
	}

	/** Empty method to satisfy interface requirements */
	public void init()
		throws ExtensionException, ServiceException
	{
		ZimbraLog.extensions.info("Registering ZimbergStoreService.");
		ExtensionDispatcherServlet.register(this, new ZimbergStoreService());
	}

	/** Empty method to satisfy interface requirements */
	public void destroy()
	{
		ExtensionDispatcherServlet.unregister(this);
	}

}
