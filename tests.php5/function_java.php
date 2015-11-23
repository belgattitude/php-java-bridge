<?php
  // check if the java() function caches the allocated classes
include_once("java/Java.inc");
$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();

java_require("$here/array.jar");
$c = java_values (java("Array")->hashCode());

@java_reset();

//java_require("$here/array.jar");
$c1 = java_values (java("Array")->hashCode());

if($c1!=$c) die("test failed");

echo "test okay\n";
exit(0);
