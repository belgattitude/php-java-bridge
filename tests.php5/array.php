#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
$array = new java("Array");

$map = $array->getConversion();
$set = $map->entrySet();
$iterator = $set->iterator();
while (java_values($iterator->hasNext())) {
  $next = $iterator->next();
  $key = $next->getKey();
  $value = $next->getValue();
  echo java_values($key)." => ".java_values($value)."\n";
}

echo "\n";

$idx = $array->getIndex("Seelandschaft mit Pocahontas, Arno Schmidt, 1914--1979");
$entry = $array->getEntry($idx);
echo java_values($idx)." => ".java_values($entry)."\n";
?>
