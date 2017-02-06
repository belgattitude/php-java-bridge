#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$s = new JavaClass("java.lang.System");
if(!$s) die("test failed1\n");
// check null proxy
java_begin_document();
$k = $s->getProperty("php.java.bridge.VOID");
java_end_document();
echo $k; echo "\n";
if(is_null($k)) die("test failed3\n");

echo "test okay\n";
exit(0);
