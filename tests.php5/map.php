#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$v=new java("java.util.HashMap");
for($i=0; $i<100; $i++) {
  $v->put($i, $i);
}

foreach($v as $key=>$val) {
  if($key!=java_values($val)) { echo "ERROR\n"; exit(1); }
}
echo "test okay\n";
exit(0);

?>
