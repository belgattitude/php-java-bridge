<?php
include_once ("java/Java.inc");

$s = new java("java.lang.String", 12);
$c1=java_context();
$c2=java_session();
$c3=java_session();
$c4=java_context();
echo "$c1\n$c2\n$c3\n$c4\n";

$instance = java_instanceof($s,new java("java.lang.String"));
if(!$c1||!$c2||!$c3||!$c4||!$instance) {
  echo "ERROR\n";
  exit(1);
 }
echo "test okay\n";
exit(0);
?>
