#!/usr/bin/php

<?php

require_once ("java/Java.inc");
ini_set("max_execution_time", 0);

$n=200000;
$Sys = new JavaClass("java.lang.System");
$a1 = array();
for($i=0; $i<$n; $i++) {
  $a1[$i]=$i;
}
$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
$v = new java("java.util.LinkedList", $a1);
$t2 = java_values($Sys->currentTimeMillis());
$T1 = $t2-$t1;

$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
$v = new java("java.util.HashMap", $a1);
$t2 = java_values($Sys->currentTimeMillis());
$T2 = $t2-$t1;

$Arrays=new JavaClass("java.util.Arrays");
$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
$v = $Arrays->asList($a1);
$t2 = java_values($Sys->currentTimeMillis());
$T3 = $t2-$t1;

$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
java_begin_document();
$v = new java("java.util.LinkedList");
for($i=0; $i<$n; $i++) {
  $v->add($i, $i);
}
java_end_document();
$t2 = java_values($Sys->currentTimeMillis());
$T4 = $t2-$t1;

$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
java_begin_document();
$v = new java("java.util.HashMap");
for($i=0; $i<$n; $i++) {
  $v->put($i, $i);
}
java_end_document();
$t2 = java_values($Sys->currentTimeMillis());
$T5 = $t2-$t1;

$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
java_begin_document();
$Array = new JavaClass("java.lang.reflect.Array");
$ar=$Array->newInstance(new JavaClass("java.lang.Integer"), $n);
for($i=0; $i<$n; $i++) {
  $Array->set($ar, $i, $i);
}
java_end_document();
$t2 = java_values($Sys->currentTimeMillis());
$T6 = $t2-$t1;

$s="";
for($i=0; $i<$n; $i++) {
  $s.=$i;
}
$Sys->gc();
$t1 = java_values($Sys->currentTimeMillis());
$str = new java("java.lang.String", $s);
$t2 = java_values($Sys->currentTimeMillis());
$T7 = $t2-$t1;



echo "Time needed to send $n values to the server.\n";
echo "constructor : LinkedList: $T1 ms, HashMap: $T2 ms, Array: $T3 ms\n";
echo "invocations : LinkedList: $T4 ms, HashMap: $T5 ms, Array: $T6 ms\n";
echo "Sending a {$str->length()} length string: $T7 ms\n";
?>

