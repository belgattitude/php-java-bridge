<?php

/** adjust these values **/
$J2EE_LIBRARY_DIR="/opt/SUNWappserver/lib";
$HOST="127.0.0.1";
$PORT=8080;


/**
  * Import the web service definition and compile it to java classes, 
  * so that the communication can be debugged easily.
  */

$SERVICE="jaxws-fromwsdl/addnumbers?wsdl";

// require J2EE web service libraries
$here=getcwd(); 
$ws_libs="-Djava.ext.dirs=$J2EE_LIBRARY_DIR";
java_require("$here;$J2EE_LIBRARY_DIR");

// compile wsdl, if necessary
$stub=new java("java.io.File", "$here/org");
if(java_is_false($stub->exists())) {
  $java=ini_get("java.java"); if(!$java) $java="java";
  echo "Compiling http://$HOST:$PORT/$SERVICE ...";
  system("$java $ws_libs com.sun.tools.ws.WsImport -keep http://$HOST:$PORT/$SERVICE");
  echo "done.\nStubs created in $here/org directory.\n";
}

?>
