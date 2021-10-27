# zm-store-manager
Plugin-based Data Store for Zimbra

## Overview

A flexible StoreManager implementation intended to support pluggable backends, key schemes, and eventually encryption and compression methods.

## Getting started:

#### Install the connector

```
# install -o zimbra -g zimbra -m 644 -D \
  zimberg_store_manager-0.1.5.jar \
  /opt/zimbra/lib/ext/zimberg/zimberg_store_manager-0.1.5.jar
```

#### Create the configuration path

```
# install -o zimbra -g zimbra -m 755 -d \
> /opt/zimbra/conf/storemanager.d
```

#### Create a configuration profile:

```
# cat > /opt/zimbra/conf/storemanager.d/aws.properties << !
> name=aws
> backend_class=com.citrisoft.zimbra.store.backend.S3Backend
> location_factory_class=com.citrisoft.zimbra.store.location.DefaultLocationFactory
> aws_endpoint=s3.us-east-2.amazonaws.com
> aws_region=us-east-2
> aws_bucket=myZimbraBucket
> aws_access_key=FNORDFNORDFNORDFNORD
> aws_secret_key=FNORDFNORDFNORDFNORDFNORDFNORDFNORDFNORD
> !
```

#### Configure Zimbra to point to the store manager and desired profile:

```
# zmlocalconfig -e \
  zimbra_class_store=com.citrisoft.zimbra.store.ZimbergStoreManager \
  zimberg_store_default_profile=aws
```

## Testing

The jar file includes some basic tools for testing the validity of a given profile, allowing you to directly store, get, verify and delete blobs by their locator:

```
# alias zimberg="java -cp /opt/zimbra/lib/jars/*:/opt/zimbra/lib/ext/zimberg/* com.citrisoft.zimbra.store.ZimbergStoreUtil"
# cat > /tmp/fnord <<< fnord
# zimberg store aws /tmp/fnord fnord
# zimberg verify aws fnord
  true
# zimberg get aws fnord
  fnord
# zimberg delete aws fnord
# zimberg verify aws fnord
  false
```

## Configuration

### Localconfig Keys

#### zimberg_store_profile_path

_default: "/opt/zimbra/conf/storemanager.d"_

The path that stores configuration profiles for the connector.

#### zimberg_store_default_profile

_default: "default"_

The profile to be used when creating new blobs.

### Profile Keys

#### name

The idenfitier used to associate a particular blob with a storage profile.

#### backend_class

A class providing connectivity to a particular storage system.  Each class will typically require additional configuration keys to function.

#### location_factory_class

A class which generates a new key or path name for new blobs.

## Storage Backends

There are several storage backends currently bundled with the connector.

### com.citrisoft.zimbra.store.backend.FileBackend

An implementation that interfaces with the local filesystem.

### com.citrisoft.zimbra.store.backend.HcpBackend

An implementation that interfaces with the Hitatchi Content Platform.

#### hcp_base_uri

The endpoint URI for connecting to the HCP rest interface.

#### hcp_virtual_host

The hostname to pass in HTTP requests; defaults to the hostname in the URI.

#### hcp_username

A valid username for the HCP namespace.

#### hcp_password

A valid password for the HCP namespace.

### com.citrisoft.zimbra.store.backend.S3Backend

An implementation that interfaces with the Amazon S3 service and compatible implementations.

#### aws_endpoint

The endpoint hostname for connecting to the S3 interface.

#### aws_region

The region name to be used in the request.

#### aws_bucket

The bucket name to be used in the request.

#### aws_access_key

The access key used in generating the request signature.

#### aws_secret_key

The secret key used in generating the signing key for requests.

## Location Factories

Generation of location strings is decoupled from the backend storage in order to facilitate alternate schemes for the same protocol.  For example, Amazon recommends a key naming scheme which is inefficient on compatible S3 implementations like Hitatchi's HS3 API.

### com.citrisoft.zimbra.store.location.DefaultLocationFactory

A general purpose implementation that constructs a key from a random integer, the account ID, and the item id of the new blob.

### com.citrisoft.zimbra.store.location.PathLocationFactory

A reference implementation for use with the FileBackend that builds a path from a prefix, the account id, and the item id of the new blob.

### com.citrisoft.zimbra.store.location.RandomLocationFactory

A reference implementation for use with object stores that simply returns a hex encoded 128-bit random integer.

### com.citrisoft.zimbra.store.location.HcpLocationFactory

An implementation that constructs keys optimized for the Hitatchi Content Platform.  A base key is constructed using the account id, item id and current timestamp, and then prefixed with a two level path based on a djb2 hash of the base key.  This ensures that mailbox objects are evenly distributed among the content node databases.

## Locators

All database locators stored in the mail_item tables consist of a compound of the profile name and the location of the blob within that profile; e.g.

filesystem_test@@/opt/zimbra/store/85eb9e75-564e-4d21-aae6-2d0ab281caaa/260
hcp_test@@A0/86/6A676742-FD60-4C2D-8F79-E476A122E922/00000104-15DCDE756CF

This allows a system to be seamlessly switched to a new backend without needing to migrate the existing data, and should facilitate cross-backend HSM or deployment of new encryption keys.

## Additional Information

* [JavaDoc](https://zimbraos.github.io/zm-store-manager/)
