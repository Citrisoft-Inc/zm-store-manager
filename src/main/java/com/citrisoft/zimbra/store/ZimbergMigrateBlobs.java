package com.citrisoft.zimbra.store;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.cs.service.admin.AdminServiceException;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.util.SpoolingCache;
import com.zimbra.soap.ZimbraSoapContext;

import com.citrisoft.zimbra.store.profile.Profile;
import com.citrisoft.zimbra.store.profile.Profiles;

public class ZimbergMigrateBlobs
	extends AdminDocumentHandler
{
	@Override
	public Element handle(Element request,  Map<String, Object> context)
		throws ServiceException
	{
		ZimbraSoapContext zc = getZimbraSoapContext(context);

		Element response = zc.createElement(ZimbergStoreService.MIGRATE_RESPONSE);
		Element accountElem = request.getElement(ZimbergStoreService.E_ACCOUNT);

		String email  = accountElem.getAttribute(ZimbergStoreService.A_NAME);
		String source = accountElem.getAttribute(ZimbergStoreService.A_SOURCE);
		String target = accountElem.getAttribute(ZimbergStoreService.A_TARGET);
		boolean delete = accountElem.getAttributeBool(ZimbergStoreService.A_DELETE);

		Provisioning prov = Provisioning.getInstance();
		Account account = prov.get(AccountBy.name, email);

		if (account == null)
		{
			throw ServiceException.FAILURE("Account " + email + " not found", null);
		}

		if (source.equals(target))
		{
			throw ServiceException.FAILURE("Identical source and target: " + source, null);
		}

		int moved = migrateBlobs(account, source, target, delete);

		response.addAttribute("moved", moved);

		return response;
	}

	public int migrateBlobs(Account account, String source, String target, boolean delete)
		throws ServiceException
	{
		int moved = 0;
		int failed = 0;
		int skipped = 0;

		ZimbergStoreManager sm;

		try
		{
			sm = (ZimbergStoreManager) StoreManager.getInstance();
		}
		catch (ClassCastException e)
		{
			throw ServiceException.FAILURE("Incompatible storage manager.", null);
		}

		MailboxManager mm = MailboxManager.getInstance();

		Mailbox mbox = mm.getMailboxByAccount(account);

		Profile targetProfile = Profiles.get(target);

		if (targetProfile == null)
		{
			throw ServiceException.FAILURE("Invalid target profile: " + target, null);
		}

		if (account != null)
		{
			DbConnection conn = null;

			ZimbraLog.addAccountNameToContext(account.getName());

			try
			{
				conn = DbPool.getConnection(mbox);
				SpoolingCache<MailboxBlobInfo> blobs = DbMailItem.getAllBlobs(conn, mbox);
				ZimbraLog.store.info(String.format("examining %d blobs for migration from %s to %s", blobs.size(), source, target));

				for (MailboxBlobInfo blobInfo: blobs)
				{
					String profileName = sm.getProfileName(blobInfo.locator);

					if (!profileName.equals(source))
					{
						skipped++;
						continue;
					}

					try
					{
						MailboxBlob blob = sm.getMailboxBlob(mbox, blobInfo.itemId, blobInfo.revision, blobInfo.locator, false);
						String newLocation = targetProfile.locationFactory.generateLocation(blob);
						String newLocator = targetProfile.name + "@@" + newLocation;

						// Update the locator*
						int rows = DbMailItem.updateLocatorAndDigest(conn, mbox, DbMailItem.getMailItemTableName(mbox), "id",
							blobInfo.itemId, blobInfo.revision, newLocator, blobInfo.digest);
						ZimbraLog.store.debug("Updated " + rows + " rows");

						try
						{
							// Copy the blob
							InputStream is = blob.getLocalBlob().getInputStream();
							targetProfile.backend.store(is, newLocation, blob.getSize());
							// Commit the transaction updating the locator
							conn.commit();

						}
						catch (IOException | ServiceException e)
						{
							ZimbraLog.store.error("Failed to copy blob: " + e.toString());
							conn.rollback();
							failed++;
							continue;
						}

						// Delete the old blob

						try
						{
							if (delete)
								sm.deleteFromStore(blobInfo.locator, mbox);
						}
						catch (IOException e)
						{
							ZimbraLog.store.error("Failed to delete old blob: " + blobInfo.locator + ": " + e.toString());
						}
						
						ZimbraLog.store.debug(String.format("copied %s to %s", blobInfo.locator, newLocator));
						moved++;
					}
					catch (ServiceException e)
					{
						ZimbraLog.store.error("Failed to migrate blob: " + e.toString());
						failed++;
					}
				}
			}
			finally
			{
				DbPool.quietClose(conn);
			}
		}

		ZimbraLog.store.info(String.format("skipped: %d moved: %d failed: %d", skipped, moved, failed));

		return moved;
	}

}
