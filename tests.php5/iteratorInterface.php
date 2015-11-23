#!/usr/bin/php

<?php

include_once ("java/Java.inc");

$conversion = new  java("java.util.Properties");
$conversion->put("long", "java.lang.Byte java.lang.Short java.lang.Integer");
$conversion->put("boolean", "java.lang.Boolean");
$conversion->put("double", "java.lang.Double");
$conversion->put("null", "null");
$conversion->put("object", "depends");
$conversion->put("array of longs", "int[]");
$conversion->put("array of doubles", "double[]");
$conversion->put("array of boolean", "boolean[]");
$conversion->put("mixed array", "");
foreach ($conversion as $key=>$value) {               
  echo "$key => ".java_cast($value, "S")."\n";
}
?>
