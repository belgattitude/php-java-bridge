#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$h=array("k"=>"v", "k2"=>"v2");
$m=new java("java.util.HashMap",$h);
echo $m->size(); 
echo "\n";
if(java_values($m['k'])!="v"||java_values($m['k2'])!="v2") {
  echo $h[0];
  echo "test failed\n";
  exit(1);
 }
echo "test okay\n";
exit(0);
?>
