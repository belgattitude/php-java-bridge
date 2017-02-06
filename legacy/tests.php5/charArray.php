#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$s=new java("java.lang.String", 12);
$c=$s->toCharArray();
if(java_cast($c[0],'integer')==1 && java_cast($c[1],"integer")==2) {
  echo "test okay\n";
  exit(0);
}
else {
  echo "ERROR\n";
  exit(1);
}
?>
