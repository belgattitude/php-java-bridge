#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$class = new java_class("java.lang.Class");
$arr = java_get_values($class->getConstructors());
if(0==sizeof($arr)) {
     echo "test okay\n";
     exit(0);
}
echo "error\n";
exit(1);

?>
