# -*- mode: Makefile; -*-
tmp_prefix:=$(prefix)
prefix = ${DESTDIR}$(tmp_prefix)
TMP_EXTENSION_DIR:=$(EXTENSION_DIR)
EXTENSION_DIR=${DESTDIR}$(TMP_EXTENSION_DIR)



$(srcdir)/java_inc.c: $(srcdir)/Java.inc
	echo -n 'char java_inc[]="' >$(srcdir)/java_inc.c
	cat $(srcdir)/Java.inc | sed 's/\\/\\\\/g;s/"/\\"/g;s/.*/&\\n\\/'  >>$(srcdir)/java_inc.c
	echo '";' >>$(srcdir)/java_inc.c
	echo '#include <stddef.h>' >>$(srcdir)/java_inc.c
	echo 'size_t java_inc_length() {return sizeof(java_inc);}' >>$(srcdir)/java_inc.c


$(srcdir)/JavaRaw.inc:
	cat $(srcdir)/server/META-INF/java/JavaBridge.inc $(srcdir)/server/META-INF/java/Options.inc $(srcdir)/server/META-INF/java/Client.inc $(srcdir)/server/META-INF/java/GlobalRef.inc $(srcdir)/server/META-INF/java/NativeParser.inc $(srcdir)/server/META-INF/java/Parser.inc $(srcdir)/server/META-INF/java/Protocol.inc $(srcdir)/server/META-INF/java/SimpleParser.inc $(srcdir)/server/META-INF/java/JavaProxy.inc | sed -f $(srcdir)/server/extract.sed | sed '/^\/\*/,/\*\/$$/d' >$(srcdir)/JavaRaw.inc

$(srcdir)/Java.inc: $(srcdir)/JavaRaw.inc
	cat $(srcdir)/JavaRaw.inc | sed '/do not delete this line/d' >$(srcdir)/Java.inc

$(srcdir)/mono_inc.c: $(srcdir)/Mono.inc
	echo -n 'char mono_inc[]="' >$(srcdir)/mono_inc.c
	cat $(srcdir)/Mono.inc | sed 's/\\/\\\\/g;s/"/\\"/g;s/.*/&\\n\\/'  >>$(srcdir)/mono_inc.c
	echo '";' >>$(srcdir)/mono_inc.c
	echo '#include <stddef.h>' >>$(srcdir)/mono_inc.c
	echo 'size_t mono_inc_length() {return sizeof(mono_inc);}' >>$(srcdir)/mono_inc.c


$(srcdir)/Mono.inc: $(srcdir)/JavaRaw.inc
	cat $(srcdir)/JavaRaw.inc | sed -f $(srcdir)/server/append.sed | sed 's/JAVA/MONO/g;s/java/mono/g;s/Java/Mono/g;s/updateJarLibraryPath/updateLibraryPath/;s/^.*do not delete this line.*$$/$$name="cli.".$$name;/;' >$(srcdir)/Mono.inc


$(phplibdir)/stamp:
	cd $(srcdir)/server; $(MAKE) CFLAGS="$(CFLAGS_CLEAN)" GCJFLAGS="$(GCJFLAGS) `echo $(CFLAGS_CLEAN)|sed 's/-D[^ ]*//g'`" install
