#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$ar=array(1, 2, 3, "X"=>5, "X"=>99, 7, 11, -13, -17.01, 19);
$ar2=array(1, 2, 3, 5, 7, 1000=>23, 6=>11, 7=>-13, 8=>-17.01, 9=>19);
echo "Set       : " . java_cast(new java("java.util.Vector", $ar),"S") ."\n";
echo "Ordered   : " . java_cast(new java("java.util.Vector", $ar2),"S") ."\n";
echo "Dictionary: " . java_cast(new java("java.util.HashMap", $ar),"S") ."\n";
echo "Dictionary: " . java_cast(new java("java.util.HashMap", $ar2),"S") ."\n";

?>
