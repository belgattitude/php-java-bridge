#!/bin/env php
<?php

require_once("java/Java.inc");

class SERVER {const JBOSS=1, WEBSPHERE=2, SUN=3, ORACLE=4;}

// -------------- adjust these variables, if necessary
$server=array_key_exists(1, $argv)? $argv[1] : SERVER::JBOSS;
$WAS_HOME=array_key_exists(2, $argv)? $argv[2]: "/opt/IBM/WebSphere/AppServer";
$JBOSS_HOME=array_key_exists(2, $argv)? $argv[2] : "/opt/jboss-4.0.2/";
$app_server=array_key_exists(2, $argv)? $argv[2] : getenv("HOME")."/SUNWappserver";
$HOST=array_key_exists(3, $argv)? $argv[3] : "127.0.0.1";
// ---------------

if($argc <3 || $argc >4) {
  echo "Usage: php documentClient.php SERVER SERVER_DIR REMOTE_HOST\n";
  echo "Example: php documentClient.php 1 /opt/jboss-4.0.3SP1 localhost\n";
  echo "Example: php documentClient.php 2 /opt/IBM/WebSphere/AppServer localhost\n";
  echo "Example: php documentClient.php 3 /opt/SUNWappserver localhost\n";
  exit(1);
 }


$System = new JavaClass("java.lang.System");
$props = $System->getProperties();
echo "Using java VM from: ${props['java.vm.vendor']}\n";
echo "connecting to server: ";

$clientJar = getcwd() . "/documentBeanClient.jar";
switch($server) {
 case SERVER::JBOSS: 
   echo "jboss. Loading $JBOSS_HOME/lib \n";

   if(!java_values($props['java.vm.vendor']->toLowerCase()->startsWith("sun")))
      echo "WARNING: You need to run this example with the SUN VM\n";
   if(!is_dir($JBOSS_HOME)) die("ERROR: Incorrect $JBOSS_HOME.");

   $name = "DocumentEJB";
   java_require("$JBOSS_HOME/client/;$clientJar");
   $server=array("java.naming.factory.initial"=>
		 "org.jnp.interfaces.NamingContextFactory",
		 "java.naming.provider.url"=>
		 "jnp://$HOST:1099");
   break;
 case SERVER::WEBSPHERE: 
   echo "websphere. Loading $WAS_HOME/lib/\n";

   if(!java_values($props['java.vm.vendor']->toLowerCase()->startsWith("ibm")))
      echo "WARNING: You need to run this example with the IBM VM\n";
   if(!is_dir($WAS_HOME)) die("ERROR: Incorrect $WAS_HOME.");

   $name = "RMIdocument";
   java_require("$WAS_HOME/lib/;$clientJar");
   $server=array("java.naming.factory.initial"=>
		 "com.ibm.websphere.naming.WsnInitialContextFactory",
		 "java.naming.provider.url"=>
		 "iiop://$HOST:2809");
   break;
 case SERVER::SUN:
   echo "sun. Loading: $app_server/lib\n";

   if(!java_values($props['java.vm.vendor']->toLowerCase()->startsWith("sun")))
      echo "WARNING: You need to run this example with the SUN VM\n";
   if(!is_dir($app_server)) die("ERROR: Incorrect $app_server.");

   $name = "RMIdocument";
   java_require("$app_server/lib/;$clientJar");
   $server=array("java.naming.factory.initial"=>
		 "com.sun.jndi.cosnaming.CNCtxFactory",
		 "java.naming.provider.url"=>
		 "iiop://$HOST:3700");
   break;
 }

try {
  $doc=createDocument($name, $server);
} catch (JavaException $e) {
  echo "Could not create remote document. Have you deployed documentBean.jar?\n";
  echo $e->getCause() ."\n";
  exit (1);
}

/* add pages to the remote document */
$doc->addPage(new java ("Page", 0, "this is page 1"));
$doc->addPage(new java ("Page", 0, "this is page 2"));
/* and print a summary */
print $doc->analyze() . "\n";

destroyDocument($doc);


/* Utility procedures */

/*
 * convenience function which connects to the AS server using the URL
 * $url, looks up the service $jndiname and returns a new remote
 * document.
 * @param jndiname The name of the remote document, see sun-ejb-jar.xml
 * @param serverArgs An array describing the connection parameters.
 */
function createDocument($jndiname, $serverArgs) {
  // find initial context
  $initial = new java("javax.naming.InitialContext", $serverArgs);
  
  // find the service
  $objref  = $initial->lookup("$jndiname");
  
  // access the home interface
  $DocumentHome = new JavaClass("DocumentHome");
  $PortableRemoteObject = new JavaClass("javax.rmi.PortableRemoteObject");
  $home=$PortableRemoteObject->narrow($objref, $DocumentHome);

  // create a new remote document and return it
  $doc = $home->create();
  return $doc;
}

/*
 * convenience function which destroys the reference to the remote
 * document
 * @param The remote document.
 */
function destroyDocument($doc) {
	$doc->remove();
}

?>
