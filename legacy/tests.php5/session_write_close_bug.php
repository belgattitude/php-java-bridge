#!/usr/bin/php
<?php
include_once ("java/Java.inc");

session_id("test");
session_start();
$a=$_SESSION['a'];
if(!$a) {
  echo "new";
  $a=new java("java.lang.StringBuffer");
  $_SESSION['a']=$a;
 }
$a=$_SESSION['a'];
echo $a;
//session_write_close();
?>
