#!/usr/bin/php

<?php
include_once ("java/Java.inc");

class c {
  function toString() {
    return "class c";
  }
}
function toString() {
  return "top-level";
}
function getClosure() {
  $cl = java_closure(new c());
  $res = java_cast($cl, "S");
  if($res != "class c") throw new JavaException ("java.lang.Exception", "test failed");
  return $cl;
}

try {
  $cl = java_closure();
  $Proxy = new JavaClass("java.lang.reflect.Proxy");
  $proc = $Proxy->getInvocationHandler($cl);
  $ncl = $proc->invoke($cl, "getClosure", array());
  $res = java_cast($ncl, "S");
  if($res != "class c") throw new Exception ("test failed");
  echo "test okay<br>\n";
} catch (Exception $e) {
  echo "test failed: $e<br>\n";
  exit(1);
}

