#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$val=1234523456.789;
$v=new java("java.lang.Double", $val);
echo $val . "\n";
echo $v->doubleValue();
echo "\n";

java_values($v->doubleValue()) == $val or die ("test failed");
exit (0);
?>
