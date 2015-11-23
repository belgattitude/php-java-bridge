#!/bin/sh
set -x
LANG=C

rm -rf [^C][^V][^S]* .??* *~
cvs -Q update -APd 
find . -print0 | xargs -0 touch
dirs=`ls -l | grep '^d' | fgrep -v CVS | awk '{print $9}'`
find $dirs -name "CVS" -print | xargs rm -rf

version=`cat VERSION`
ln -s `pwd` php-java-bridge-${version}

# create archive
tar czhf php-java-bridge_${version}.tar.gz --exclude "php-java-bridge-${version}/php-java-bridge[_-]*" --exclude CVS --exclude ".??*" php-java-bridge-${version}

rpmbuild -tb php-java-bridge_${version}.tar.gz
mv ~/rpmbuild/RPMS/i386/php-java-bridge-${version}-1.i386.rpm "./php-java-bridge-${version}-1.fc`cat /etc/issue | sed 1q | awk '{print $3}'`.i386.rpm"
mv ~/rpmbuild/RPMS/i386/php-java-bridge-devel-${version}-1.i386.rpm "./php-java-bridge-devel-${version}-1.fc`cat /etc/issue | sed 1q | awk '{print $3}'`.i386.rpm"

ant clean &&
ant PhpDoc 2>/dev/null >/dev/null && 
ant JavaDoc &&
ant && 
ant SrcZip

cp dist/*.war dist/src.zip .
cp -r php_java_lib tests.php5 tests.jsr223 server

cp JavaBridge.war JavaBridgeTemplate.war
for i in 'META-INF/*' 'WEB-INF/lib/[^pJ]*.jar' 'WEB-INF/lib/poi.jar' 'WEB-INF/cgi/*' 'WEB-INF/web.xml' 'WEB-INF/platform/*' 'locale/*' '*.class' '*.jsp' '*.rpt*' '*.php'; do
  zip -d JavaBridgeTemplate.war "$i"; 
done
cat examples/php+jsp/settings.php >./index.php
echo '<?php phpinfo();?>' >test.php
rm -rf WEB-INF; mkdir WEB-INF
cp server/example-web.xml WEB-INF/web.xml
zip JavaBridgeTemplate.war index.php test.php
zip JavaBridgeTemplate.war WEB-INF/web.xml
zip -d JavaBridgeTemplate.war birtreportlib/
rm -f test.php

cp  src.zip README FAQ.html PROTOCOL.TXT INSTALL.STANDALONE INSTALL.J2EE INSTALL.J2SE NEWS documentation
mv examples documentation
mv server documentation
list="documentation/API documentation/examples documentation/README documentation/FAQ.html documentation/PROTOCOL.TXT documentation/INSTALL.J2EE documentation/INSTALL.J2SE documentation/INSTALL.STANDALONE documentation/src.zip documentation/NEWS JavaBridge.war documentation/server/documentation documentation/server/php_java_lib documentation/server/tests.jsr223 documentation/server/tests.php5"
find $list -type d -name "CVS" -print | xargs rm -rf


chmod +x JavaBridge.war

# create j2ee download
zip -q -r php-java-bridge_${version}_documentation.zip $list
mv JavaBridgeTemplate.war "JavaBridgeTemplate`echo ${version}|sed 's/\.//g'`.war"
rm -rf $dirs
cvs -Q update -APd 

scp "php-java-bridge_`cat VERSION`_documentation.zip" "JavaBridgeTemplate`echo ${version}|sed 's/\.//g'`.war" jost_boekemeier,php-java-bridge@web.sf.net:"/home/pfs/project/p/ph/php-java-bridge/Binary\ package/php-java-bridge_`cat VERSION`/"
scp dist/Java.inc dist/php-script.jar dist/JavaBridge.jar dist/php-servlet.jar dist/script-api.jar jost_boekemeier,php-java-bridge@web.sf.net:"/home/pfs/project/p/ph/php-java-bridge/Binary\ package/php-java-bridge_`cat VERSION`/exploded/"

scp "php-java-bridge_`cat VERSION`.tar.gz" jost_boekemeier,php-java-bridge@web.sf.net:"/home/pfs/project/p/ph/php-java-bridge/RHEL_FC\ SecurityEnhancedLinux/php-java-bridge_`cat VERSION`/"
