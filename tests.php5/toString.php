#!/usr/bin/php
<?php

include_once ("java/Java.inc");

$Object = new java_class ("java.lang.Object");
$ObjectC = new JavaClass ("java.lang.Object");
$object = $Object->newInstance();

// test __toString()
// should display "class java.lang.Object"
echo $Object; echo "\n";

// test cast to string
// should display "class java.lang.Object"
echo "" . $Object->__toString() . "\n";
echo "" . $ObjectC->__toString() . "\n";

echo $object->__toString()."\n";
?>
