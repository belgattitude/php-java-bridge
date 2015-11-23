#!/usr/bin/php

<?php
include_once ("java/Java.inc");
function c1($s1, $o1) {
  echo "c1: $s1, $o1\n";
  // must display a php warning
  throw new JavaException("java.lang.Exception", "bleh!");

  // not reached 
  echo "ERROR.\n"; 
  exit(3); 
}
function c2($b) {
  echo "c2: $b\n";
  return $b;
}
function c3 ($e) {
  echo "c3: $e\n";
  return 2;
}
$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
java_require("$here/callback.jar");

$closure=java_closure(null, null, java("Callback"));
$callbackTest=new java('Callback$Test', $closure);

if($callbackTest->test()) {
  echo "test okay\n";
  exit(0);
} else {
  echo "test failed\n";
  exit(1);
}
?>
