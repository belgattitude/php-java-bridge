#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$h=array("k"=>"v", "k2"=>"v2");
$m=new java("java.util.Properties",$h);
echo $m->size() . " " . java_cast($m->getProperty("k", "ERROR"),"S")." \n";
if(java_values($m->getProperty("k2", "ERROR")) != "v2") {
  echo "ERROR\n";
  exit(1);
}
echo "test okay\n";
exit(0);
?>
