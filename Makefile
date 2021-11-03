VERSION=0.5.1

INSTALL=/usr/bin/install
TAR=/bin/gtar

PREFIX=/opt/zimbra
PKGDIR=target

class:
	./gradlew

clean:
	./gradlew clean

jar:
	./gradlew jar

doc:
	./gradlew javadoc

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
	$(INSTALL) -m 644 -D build/libs/zm-store-manager.jar \
	$(DESTDIR)/$(PREFIX)/lib/ext/zimberg/zm-store-manager-${VERSION}.jar
	$(INSTALL) -m 755 -D bin/zimberg $(DESTDIR)/$(PREFIX)/bin/zimberg
