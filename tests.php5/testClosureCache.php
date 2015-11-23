<?php

include_once ("java/Java.inc");

$here=getcwd();
java_require("$here/testClosureCache.jar");
function toString() {return "ccl";}
$tc = new Java("TestClosureCache");
"ichild::ccl"==$tc->proc1(java_closure(null, null, java('TestClosureCache$IChild'))) ||die(1);
"iface::ccl"==$tc->proc1(java_closure(null, null, java('TestClosureCache$IFace'))) || die(2);
echo "test okay\n";
?>
