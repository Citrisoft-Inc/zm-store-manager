package com.synacor.zimbra.store;

import java.io.InputStream;
import java.io.IOException;

import com.zimbra.common.localconfig.KnownKey;
import com.zimbra.common.util.CliUtil;

import com.synacor.util.cli.Command;
import com.synacor.util.cli.CommandSet;
import com.synacor.zimbra.store.backend.Backend;
import com.synacor.zimbra.store.profile.Profile;
import com.synacor.zimbra.store.profile.Profiles;

public class ZimbergStoreUtil
{
	private static KnownKey profilePath = new KnownKey("zimberg_store_profile_path", "${zimbra_home}/conf/storemanager.d");
	
	static
	{
		CliUtil.toolSetup("DEBUG", "/tmp/zimberg.out", true);

		try
		{
			Profiles.load(profilePath.value());
		}
		catch (IOException e)
		{
			System.err.println("Cannot load profiles: " + e.getMessage());
			System.exit(1);
		}
	}

	private static class GetCommand
		extends Command
	{
		public GetCommand()
		{
			this.name = "get";
		}

		public void execute(String args[])
		{
			String profileName = args[0];
			String location = args[1];

			Profile profile = getProfile(profileName);
			try
			{
				InputStream is = profile.backend.get(location);

				int size = 0;
				byte[] buffer = new byte[1024];
				while ((size = is.read(buffer)) != -1) System.out.write(buffer, 0, size);
			}
			catch (IOException e)
			{
				System.err.println("Unable to get object: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	private static class StoreCommand
		extends Command
	{
		public StoreCommand()
		{
			this.name = "store";
		}

		public void execute(String args[])
		{
			String profileName = args[0];
			String src = args[1];
			String dst = args[2];

			Profile profile = getProfile(profileName);

			try
			{
				profile.backend.store(src, dst);
			}
			catch (IOException e)
			{
				System.err.println("Unable to store object: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	private static class CopyCommand
		extends Command
	{
		public CopyCommand()
		{
			this.name = "copy";
		}

		public void execute(String args[])
		{
			String profileName = args[0];
			String src = args[1];
			String dst = args[2];

			Profile profile = getProfile(profileName);

			try
			{
				profile.backend.copy(src, dst);
			}
			catch (IOException e)
			{
				System.err.println("Unable to store object: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	private static class VerifyCommand
		extends Command
	{
		public VerifyCommand()
		{
			this.name = "verify";
		}

		public void execute(String args[])
		{
			String profileName = args[0];
			String objectName= args[1];

			Profile profile = getProfile(profileName);

			try
			{
				System.out.println(Boolean.toString(profile.backend.verify(objectName)));
			}
			catch (IOException e)
			{
				System.err.println("Unable to verify object: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	private static class DeleteCommand
		extends Command
	{
		public DeleteCommand()
		{
			this.name = "delete";
		}

		public void execute(String args[])
		{
			String profileName = args[0];
			String objectName= args[1];

			Profile profile = getProfile(profileName);

			try
			{
				profile.backend.delete(objectName);
			}
			catch (IOException e)
			{
				System.err.println("Unable to delete object: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	private static class GenerateCommand
		extends Command
	{
		public void execute(String args[])
		{
			String profileName = args[0];
			String accountId = args[1];
			String itemIdStr = args[2];

			Profile profile = getProfile(profileName);

			try
			{
				int itemId = Integer.parseInt(itemIdStr, 10);
				profile.locationFactory.generateLocation(accountId, itemId);
			}
			catch (NumberFormatException e)
			{
				System.err.printf("Invalid itemId: %s: %s\n", itemIdStr, e.getMessage());
			}
		}
	}

	private static Profile getProfile(String profileName)
	{
		Profile profile = Profiles.get(profileName);

		if (profile == null)
		{
			System.err.println("Unable to load profile: " + profileName);
			System.exit(1);
		}

		return profile;
	}

	public static void main(String args[])
	{
		CommandSet main = new CommandSet("main");

		main.add(new GetCommand());
		main.add(new StoreCommand());
		main.add(new CopyCommand());
		main.add(new VerifyCommand());
		main.add(new DeleteCommand());
		main.add(new GenerateCommand());
		main.execute(args);
	}

}
