#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
java_require("$here/numberTest.jar");
	
$test = new java('NumberTest');
print "getInteger() = "; echo($test->getInteger()); echo "<br>\n";
print "getPrimitiveInt() = "; echo($test->getPrimitiveInt()); echo "<br>\n";
print "getFloat() = "; echo($test->getFloat()); echo "<br>\n";
print "getPrimitiveFloat() = "; echo($test->getPrimitiveFloat()); echo "<br>\n";
print "getDouble() = "; echo($test->getDouble()); echo "<br>\n";
print "getPrimitiveDouble() = "; echo($test->getPrimitiveDouble()); echo "<br>\n"; 
print "getBigDecimal() = "; echo($test->getBigDecimal()); echo "<br>\n";

?>
