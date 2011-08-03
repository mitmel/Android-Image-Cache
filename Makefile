srcdir := ImageCache
version := $(shell sed '/versionName/ { s/.*versionName="\([^"]*\)".*/\1/; s/ /_/g; p }; d' AndroidManifest.xml)
out_package := ../$(srcdir)_$(version).tar.gz

package: $(out_package)

$(out_package):
	 tar -zcv --exclude .git --exclude-vcs --exclude \*\~ --exclude-backups -X .gitignore -f $@ -C ../ $(srcdir)
