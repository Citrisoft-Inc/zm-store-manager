# Generated by Buildr 1.4.0, change to your liking
# Standard maven2 repository
repositories.remote << 'http://repo1.maven.org/maven2'
# Version number for this release
VERSION_NUMBER = '0.4.1'

desc 'Zimberg Storage Manager'
define 'ZimbergStorageManager' do
        project.group = 'com.synacor.zimbra.store'
        project.version = VERSION_NUMBER
        compile.with Dir[_("/opt/zimbra/lib/jars/*.jar")]
        compile.with Dir[_("lib/*.jar")]
		compile.options.lint = 'all'
		package(:jar, :id => 'zimberg_store_manager').with(
			:manifest => {
			'Zimbra-Extension-Class' => 'com.synacor.zimbra.store.ZimbergStoreExtension'
		})

end
