#!/usr/bin/php

<?php
include_once ("java/Java.inc");

// the following tests if a class "NoClassDefFound" which contains a
// constructor and a method which internally reference an external
// class "DoesNotExist" can be referenced, and, if an exception is
// thrown if the two classes are loaded by different class loaders.

// make sure to start the backend at log_level 3, to see the "loading..."
// messages.

java_reset();
$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
java_require("$here/doesNotExist.jar");
java_require("$here/noClassDefFound.jar");
java_require("$here/doesNotExist.jar;$here/noClassDefFound.jar");
$v=new JavaClass("NoClassDefFound");

// must fail: the second loader is selected, which does not include
// DoesNotExist.
echo "must fail\n";
$v->call(null);
exit (java_last_exception_get()?0:1);
?>
