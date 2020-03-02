package com.synacor.zimbra.store.location;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import com.scality.commons.UKS;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;

public class ScalityLocationFactory
	extends LocationFactory
{
	private Random rnd;
	private int objectCos = 2;
	private int serviceId = 11;
	private boolean useArc = false;

    /**
     * Generate a new location factory
     *
     * @param props Initialization parameters
     */
    public ScalityLocationFactory(Properties props)
    {
		String useArcStr = props.getProperty("scality_use_arc");
		String objectCosStr = props.getProperty("scality_object_cos");
		String serviceIdStr = props.getProperty("scality_service_id");

		if (useArcStr != null)
		{
			useArc = Boolean.parseBoolean(useArcStr);
			serviceId = 12;
		}

		if (objectCosStr != null)
		{
			try
			{
				objectCos = Integer.parseInt(objectCosStr);
			}
			catch (NumberFormatException e)
			{
				ZimbraLog.store.error("Could not parse scality_object_cos. Using default.");
			}
		}

		if (serviceIdStr != null)
		{
			if (useArc)
			{
				ZimbraLog.store.warn("Ignoring scality_service_id in arc configuration.");
			}
			else
			{
				try
				{
					serviceId = Integer.parseInt(serviceIdStr, 10);
					if (serviceId > 255)
					{
						serviceId = 11;
						ZimbraLog.store.warn("Ignoring scality_service_id over 255.");
					}
				}
				catch (NumberFormatException e)
				{
					ZimbraLog.store.error("Could not parse scality_service_id. Using default.");
				}
			}
		}
	
		rnd = new Random(System.currentTimeMillis());
    }

	//UKS key = new UKS(userId, (int)System.currentTimeMillis(), serviceId, this.rand.nextInt(), this.chordObjectCos);
	//public UKS(long oId, int volId, int serviceId, int appSpecific, int classId)
	public String generateLocation(String accountId, int itemId)
	{
		
		String locator = null;

		try
		{
			UUID uuid = UUID.fromString(accountId);
			long oId = uuid.getLeastSignificantBits();
			int volId = (int)System.currentTimeMillis(); // Maybe make this configurable?
			int appSpecific = rnd.nextInt();

			//System.out.printf("arc: %b\n", useArc);
			//System.out.printf("svc: %d\n", serviceId);
			//System.out.printf("cos: %d\n", objectCos);

			UKS key = new UKS(oId, volId, serviceId, appSpecific, objectCos);

			locator = key.getString();
		}
		catch (IllegalArgumentException e)
		{
			ZimbraLog.store.error("Error generating scality key: " + e.getMessage());
		}

		return locator;
	}

}
