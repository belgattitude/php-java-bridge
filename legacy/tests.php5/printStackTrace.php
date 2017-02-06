#!/usr/bin/php

<?php include_once("java/Java.inc");


try {
  try {
    new java("java.lang.String", null);
  } catch(JavaException $ex) {
    if(!($ex instanceof java_exception)) {
      echo "TEST FAILED: The exception is not a java exception!\n";
      return 2;
    }
  }

  try {
    new java("java.lang.String", null);
  } catch(java_exception $ex) {
    // print the stack trace to $trace
    // note that a simple "echo (string)$ex" also prints the stack trace
    $trace = new java("java.io.ByteArrayOutputStream");
    $ex->printStackTrace(new java("java.io.PrintStream", $trace));

    echo "Exception occured:" . $trace->__toString() . "\n";
    return 0;
  }
} catch (exception $err) {
  print "An error occured: $err\n";
  return 1;
}
