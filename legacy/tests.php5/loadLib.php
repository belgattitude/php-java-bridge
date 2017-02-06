#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
$ext=trim(`php-config --extension-dir`);
if(!file_exists("$ext/lib")) {
  mkdir("$ext/lib");
}
if(file_exists("$ext/lib/array/array.jar")) {
  unlink("$ext/lib/array/array.jar");
}
if(file_exists("$ext/lib/array")) {
  rmdir("$ext/lib/array");
}
mkdir("$ext/lib/array");
copy("$here/array.jar", "$ext/lib/array/array.jar");
try {
  $testvar = new Java('Array');
  echo "Test okay\n";
  exit(0);
} catch (Exception $e) {
  echo "Exception: " . $e . "\n";
  exit(1);
}
?>
