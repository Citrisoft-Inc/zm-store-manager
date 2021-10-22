package com.citrisoft.zimbra.store;

import org.dom4j.Namespace;
import org.dom4j.QName;

import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;

public class ZimbergStoreService
	implements DocumentService
{

    static final Namespace NAMESPACE = Namespace.get("urn:zimbraAdmin");

    static final QName MIGRATE_REQUEST  = new QName("ZimbergMigrateBlobsRequest",  NAMESPACE);
    static final QName MIGRATE_RESPONSE = new QName("ZimbergMigrateBlobsResponse", NAMESPACE);

	static final String E_ACCOUNT = "account";
	static final String A_NAME    = "name";
	static final String A_SOURCE  = "src";
	static final String A_TARGET  = "dest";
	static final String A_DELETE  = "delete";

	public void registerHandlers(DocumentDispatcher dispatcher)
	{
		dispatcher.registerHandler(MIGRATE_REQUEST, new ZimbergMigrateBlobs());
	}
}
