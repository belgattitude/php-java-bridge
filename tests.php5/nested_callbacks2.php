#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$count=0;
function toString() {
  global $count;
  $s = new java("java.lang.String", "hello");
  $v = new java("java.lang.String", "hello");
  $t = new java("java.lang.String", "hello");
  $s=$v=$t=null;
  if($count++<3) {
    return java_closure();
  }
  return "leaf";
}

$res=java_values(java_closure());
echo java_cast($res,"S")."\n";
if(java_cast($res, "S") != "leaf") {
  echo "test failed\n";
  exit(1);
 }
echo "test okay\n";
exit(0);

?>
