package com.synacor.zimbra.store;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.extension.ZimbraExtension;

import org.apache.http.pool.PoolStats;

import com.synacor.zimbra.store.profile.Profile;
import com.synacor.zimbra.store.profile.Profiles;

public class ZimbergHttpHandler
	extends ExtensionHttpHandler
{

	public void init(ZimbraExtension ext)
		throws ServiceException
	{
		super.init(ext);
		ZimbraLog.extensions.info("Initializing ZimbergHttpHandler.");
	}

	public String getPath()
	{
		return "/store";
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException
	{

		ZimbraLog.extensions.info("Handling a GET request in ZimbergStoreService.");

		String[] path = request.getPathInfo().split("/");

		if (path.length < 4)
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String profileName = path[2];
		String command = path[3];

		Profile profile = Profiles.get(profileName);		

		if (profile == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (command.equals("status"))
		{
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.println(profile.backend.getStatus().toString());
		}
		else
		{
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	public void destroy()
	{
	}

}
