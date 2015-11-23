#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$Thread = new JavaClass("java.lang.Thread");
$name=java_values($Thread->getName());
if("$name" != "java.lang.Thread") {
  echo "ERROR\n";
  exit(1);
 }
echo "test okay\n";
exit(0);

?>
