<?php include_once("java/Java.inc");
// check if the first statement sends the context switch immediately
$s = new java("java.lang.String", 1);
sleep(600);
$s = new java("java.lang.Long", 1);
echo "test okay";
exit(0);
?>