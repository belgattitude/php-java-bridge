#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();

$testvar = new Java('Array6');
$testobj = $testvar->test();
echo $testobj[0][0][0][0][0][1];
echo $testobj[0][0][0][0][1][0];
echo $testobj[0][0][0][1][0][0];
echo $testobj[0][0][1][0][0][0];
echo $testobj[0][1][0][0][0][0];
echo $testobj[1][0][0][0][0][0];
echo "\n";
?>
