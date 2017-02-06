#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$s=new java("java.lang.String", 12);
$c=$s->getBytes("ASCII");
if(java_values($c[0])==ord('1') && java_values($c[1])==ord('2')) {
  echo "test okay\n";
  exit(0);
}
else {
  echo "ERROR\n";
  exit(1);
}
?>
