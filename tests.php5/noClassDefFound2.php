#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();

// must succeed
echo "must succeed\n";
java_require("$here/noClassDefFound.jar;$here/doesNotExist.jar");
$v=new java("NoClassDefFound");
$v->call(null);

?>
