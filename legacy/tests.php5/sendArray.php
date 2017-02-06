#!/usr/bin/php

<?php
include_once ("java/Java.inc");


$ar=array(1, 2, 3, 5, 7, 11, -13, -17.01, 19);
unset($ar[1]);
$v=new java("java.util.Vector", $ar);
$Arrays=new java_class("java.util.Arrays");
$l=$Arrays->asList($ar); 
$v->add(1, null);
$l2 = $v->sublist(0,$v->size());

echo java_cast($l, "S")."\n".java_cast($l2,"S")."\n";
$res1 = java_values($l);
$res2 = java_values($l2);
$res3 = array();
$res4 = array();
$i=0;

foreach($v as $key=>$val) {
  $res3[$i++]=java_values($val);
}
for($i=0; $i<java_values($l2->size()); $i++) {
  $res4[$i]=java_values($l2[$i]);
}

if(!$l->equals($l2)) {
  echo "ERROR\n";
  exit(1);
}
if(java_values($l[1]) != null || (string)$res3 != (string)$res1 || (string)$res4 != (string)$res1) {
  echo "ERROR\n";
  exit(2);
}

echo "test okay\n";
exit(0);
?>
