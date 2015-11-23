#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
$Array = new java_class("ArrayArray");
$arrayArray=$Array->create(10);

$String=new java_class("java.lang.String");
for($i=0; $i<10; $i++) {
	$ar = $arrayArray[$i]->array;
	echo java_cast($ar,"S") . " " .java_cast($ar[0],"S") . "\n"; 
}

echo "\n";

foreach($arrayArray as $value) {
	$ar = $value->array;
	echo java_cast($ar,"S") . " " .java_cast($ar[0],"S") ."\n";
}


?>
