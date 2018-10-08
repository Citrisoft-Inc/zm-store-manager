package com.synacor.zimbra.store;

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
import com.zimbra.cs.util.SpoolingCache;
import com.zimbra.soap.ZimbraSoapContext;

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
		int processed= 0;

		MailboxManager mm = MailboxManager.getInstance();

		Mailbox mbox = mm.getMailboxByAccount(account);

		if (account != null)
		{
			DbConnection conn = null;

			try
			{
				conn = DbPool.getConnection(mbox);
				SpoolingCache<MailboxBlobInfo> blobs = DbMailItem.getAllBlobs(conn,mbox);

				for (MailboxBlobInfo blob: blobs)
				{
					String profileName = ZimbergStoreManager.getProfileName(blob.locator);

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

		ZimbraLog.misc.info(String.format("BOOF: moved %d / %d", moved, processed));

		return moved;
	}

}
