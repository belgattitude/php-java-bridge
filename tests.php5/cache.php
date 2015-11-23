#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
$Cache = new JavaClass("Cache");
$instance= $Cache->getInstance();
echo $instance->hashCode();
?>

