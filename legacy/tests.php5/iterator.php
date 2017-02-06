<?php

include_once ("java/Java.inc");

$here=getcwd(); java_require("$here/hashSetFactory.jar");

$hash = java("HashSetFactory")->getSet();
$hash->add(1);
$hash->add(3);

foreach($hash as $key=>$val) {
  echo "$key=>$val\n";
}

?>
