%define _builddir /%(echo $PWD)/

Name:    zm-store-manager
Summary: Plugin-based Data Store for Zimbra
BuildArch: noarch
Version: 0.5.1
Release: 1
License: GPLv3

%description
Summary: Plugin-based Data Store for Zimbra

%prep
make install DESTDIR=${RPM_BUILD_ROOT}

%files
%dir /opt/zimbra/conf/storemanager.d
/opt/zimbra/lib/ext/zimberg/zm-store-manager-0.5.1.jar
/opt/zimbra/bin/zimberg

%changelog
* Fri Oct 29 2021  Matthew Berg <mberg@citrisoft.com> -  0.5.1-1
- Initial compression support
* Tue Oct 26 2021  Matthew Berg <mberg@citrisoft.com> -  0.5.0-1
- New namespace
- Removing deprecated backends
- Splitting out Scality support to separate package
* Mon Mar 02 2020  Matthew Berg <mberg@synacor.com> - 0.4.2-2
- Remove debug statement that are adding log noise
* Mon Feb 24 2020  Matthew Berg <mberg@synacor.com> - 0.4.2-1
- Fix bug that causes NPE if mimetype of profile cannot be detected
* Thu Sep 12 2019  Matthew Berg <mberg@synacor.com> - 0.4.1-1
- Make max http connection pool side configurable in HttpClientBackend
* Tue Aug 20 2019  Matthew Berg <mberg@synacor.com> - 0.4.0-1
- Add Scality support.
* Thu Oct 25 2018  Matthew Berg <mberg@synacor.com> - 0.3.0-2
- Add command line util.
* Wed Oct 24 2018  Matthew Berg <mberg@synacor.com> - 0.3.0-1
- Initial build.
