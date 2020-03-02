VERSION=0.4.2

INSTALL=/usr/bin/install
TAR=/bin/gtar

PREFIX=/opt/zimbra
PKGDIR=target

class:
	buildr

clean:
	buildr clean

jar:
	buildr package

doc:
	buildr doc

tarball:
	$(INSTALL) -m 755 -d $(PKGDIR)
	$(TAR) czf $(PKGDIR)/zm-store-manager-$(VERSION).tar.gz \
	--transform "s/^./zm-store-manager-${VERSION}/" \
	--exclude-from=.gitignore \
	--exclude-vcs \
	--exclude=target \
	--exclude=lib .

rpm: jar
	rpmbuild -bb zm-store-manager.spec --target noarch

all: jar doc

install:
	$(INSTALL) -m 755 -d $(DESTDIR)/$(PREFIX)/conf/storemanager.d
	$(INSTALL) -m 755 -d $(DESTDIR)/$(PREFIX)/lib/ext/zimberg
	$(INSTALL) -m 644 -D target/zimberg_store_manager-${VERSION}.jar \
	$(DESTDIR)/$(PREFIX)/lib/ext/zimberg/zimberg_store_manager-${VERSION}.jar
	$(INSTALL) -m 644 -D lib/scality-commons-4.0.0-1.jar \
	$(DESTDIR)/$(PREFIX)/lib/ext/zimberg/scality-commons-4.0.0-1.jar
	$(INSTALL) -m 755 -D bin/zimberg $(DESTDIR)/$(PREFIX)/bin/zimberg
