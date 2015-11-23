#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
$ArrayToString = new java_class("ArrayToString");


// create long array ...
$length=10;
for($i=0; $i<$length; $i++) {
  $arr[$i]=$i;
}

// ... and post it to java.  Should print integers
print "integer array: ". java_values($ArrayToString->arrayToString($arr)) . "<br>\n";


// double
for($i=0; $i<$length; $i++) {
  $arr[$i]=$i +1.23;
}

// should print doubles
print "double array: ". java_values($ArrayToString->arrayToString($arr)) . "<br>\n";


// boolean
for($i=0; $i<$length; $i++) {
  $arr[$i]=$i%2?true:false;
}

// should print booleans
print "boolean array: ". java_values($ArrayToString->arrayToString($arr)) ."<br>\n";

?>
