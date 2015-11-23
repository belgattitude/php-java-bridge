#!/usr/bin/php

<?php
include_once ("java/Java.inc");
function x1($s1, $o1) {
  echo "c1: $s1, $o1\n";
  return 1;
}
function c2($b) {
  return !java_values($b);
}
$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
java_require("$here/../tests.php5/callback.jar");

$closure=java_closure(null, array("c1" => "x1"), java("Callback"));
$callbackTest=new java('Callback$Test', $closure);

if($callbackTest->test()) {
  echo "test okay\n";
  exit(0);
} else {
  echo "test failed\n";
  exit(1);
}
?>
