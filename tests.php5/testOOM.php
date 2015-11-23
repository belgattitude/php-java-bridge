<?php 
/**
  * Test OutOfMemory behaviour
  * PHP must not hang if an out of memory error occurs in either Java or PHP
  */
require_once("../server/META-INF/java/JavaBridge.inc");
$s = str_repeat("1", 3*8192*1024);
$str = new java("java.lang.String", $s);
echo $str->length()
?>
