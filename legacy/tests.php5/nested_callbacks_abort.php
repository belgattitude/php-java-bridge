#!/usr/bin/php

<?php
include_once ("java/Java.inc");
/*
 The server log should contain:
 --> <F p="A" />
 <-- <F p="A"/>
An exception occured: php.java.bridge.Request$AbortException
[...]
 --> <F p="E" />
 <-- <F p="E"/>
*/

$count=0;
function toString() {
  global $count;
  $s = new java("java.lang.String", "hello");
  $v = new java("java.lang.String", "hello");
  $t = new java("java.lang.String", "hello");
  $s=$v=$t=null;
  if($count<2) {
    $count++;
    return java_cast(java_closure(), "string");
  }
  echo 'check the server log, it should contain a php.java.bridge.Request$AbortException';
  exit(0);
  return "leaf";
}

echo java_closure();
echo "test failed\n";
exit(1);
?>
