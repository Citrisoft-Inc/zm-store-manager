package com.citrisoft.zimbra.store;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.soap.SoapServlet;

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
		return "ZimbergStoreManager";
	}

	/** Empty method to satisfy interface requirements */
	public void init()
		throws ExtensionException, ServiceException
	{
		ZimbraLog.extensions.info("Registering ZimbergHttpHandler.");
		ExtensionDispatcherServlet.register(this, new ZimbergHttpHandler());

		ZimbraLog.extensions.info("Adding ZimbergStoreService.");
		SoapServlet.addService("AdminServlet", new ZimbergStoreService());
	}

	/** Empty method to satisfy interface requirements */
	public void destroy()
	{
		ExtensionDispatcherServlet.unregister(this);
	}

}
