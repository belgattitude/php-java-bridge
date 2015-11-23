#!/usr/bin/php

<?php
include_once ("java/Java.inc");

function toString() {
  return "php top level environment";
}
function hello($a, $b, $c) {
  $a=$a?"true":"false";
  return "Hello java from php: $a, $b, $c";
}

// close over the current environment and create a generic
// proxy (i.e. a proxy which defines only toString(), equals() and
// hashCode()).
$environment = java_closure();

// access $environment's invocation handler and invoke hello()
$Proxy = new JavaClass("java.lang.reflect.Proxy");
$proc = $Proxy->getInvocationHandler($environment);

// implicit toString() should display "php top level environment"
echo $environment->__toString();

// invoke java function "hello" which is backed by our php hello()
// function
echo "\ncalling ".$proc->__toString()."->invoke(...);\n";
$val = $proc->invoke($environment, "hello", array(true, 7, 3.14));

// should display "Hello java from php"
echo "=> ".($val->__toString())."\n";
?>
