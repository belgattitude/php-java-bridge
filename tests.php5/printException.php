#!/usr/bin/php

<?php
include_once ("java/Java.inc");

function fault() {
  new java("java.lang.String", null);
}

function me() {
  $environment = java_closure();
  $Proxy = new JavaClass("java.lang.reflect.Proxy");
  $proc = $Proxy->getInvocationHandler($environment);

  $proc->invoke($environment, "fault", array());
}  

function call() {
  me();
}
call();

?>
