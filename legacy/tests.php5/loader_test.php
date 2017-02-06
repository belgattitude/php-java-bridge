#!/usr/bin/php

<?php
//
// this test must be called twice with a standalone or J2EE back end
//
include_once ("java/Java.inc");

$rc=false;
for($i=0; $i<100; $i++) {
  @java_reset();
  $here=getcwd();
  java_require("$here/arrayToString.jar");
  $Thread = new JavaClass("java.lang.Thread");
  $loader = $Thread->currentThread()->getContextClassLoader();
  $Class = new JavaClass("java.lang.Class");
  $class = $Class->forName("ArrayToString", false, $loader);
  $class2 = $loader->loadClass("ArrayToString");
  $System = new JavaClass("java.lang.System");
  $hc1 = java_values($System->identityHashCode($class));
  $hc2 = java_values($System->identityHashCode($class2));
  $rc = $hc1==$hc2;
  if(!$rc) { 
    $Util = new JavaClass("php.java.bridge.Util");
    $vm_name = $Util->VM_NAME;
    echo "ERROR: $hc1, $hc2\n";
    echo "Dynamic loading not available in this VM.\n"; 
    echo "Responsible VM: $vm_name\n";
    break;
  }
 }
if($rc) {
  echo "test okay\n";
  return 0;
 }
echo "test failed\n";
return 1;
?>
    
