%define _builddir /%(echo $PWD)/

Name:    zm-store-manager
Summary: Plugin-based Data Store for Zimbra
BuildArch: noarch
Version: 0.3.0
Release: 2
License: GPLv3

%description
Summary: Plugin-based Data Store for Zimbra

%prep
make install DESTDIR=${RPM_BUILD_ROOT}

%files
%dir /opt/zimbra/conf/storemanager.d
/opt/zimbra/lib/ext/zimberg/zimberg_store_manager-0.3.0.jar
/opt/zimbra/bin/zimberg

%changelog
* Thu Oct 25 2018  Matthew Berg <mberg@synacor.com> - 0.3.0-2
- Add command line util.
* Wed Oct 24 2018  Matthew Berg <mberg@synacor.com> - 0.3.0-1
- Initial build.
