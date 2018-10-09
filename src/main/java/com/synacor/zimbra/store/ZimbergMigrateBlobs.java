package com.synacor.zimbra.store;

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

import com.synacor.zimbra.store.profile.Profile;
import com.synacor.zimbra.store.profile.Profiles;

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

		ZimbraLog.misc.info("Got ZimbergMigrateBlobs request.");

		int moved = migrateBlobs(account, source, target);

		return response;
	}

	public int migrateBlobs(Account account, String source, String target)
		throws ServiceException
	{
		int moved = 0;
		int failed = 0;
		int processed= 0;

		StoreManager sm = StoreManager.getInstance();
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

			try
			{
				conn = DbPool.getConnection(mbox);
				SpoolingCache<MailboxBlobInfo> blobs = DbMailItem.getAllBlobs(conn, mbox);

				for (MailboxBlobInfo blobInfo: blobs)
				{
					String profileName = ZimbergStoreManager.getProfileName(blobInfo.locator);

					if (profileName.equals(source))
					{
						ZimbraLog.misc.info("BOOF: " + profileName);

						moved++;
					}

					processed++;
				}

			}
			finally
			{
				DbPool.quietClose(conn);
			}
		}

		ZimbraLog.misc.info(String.format("BOOF: moved: %d failed: %d / %d ", moved, failed, processed));

		return moved;
	}

}
