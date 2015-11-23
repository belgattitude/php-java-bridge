#!/usr/bin/php

<?php

include_once ("java/Java.inc");

try {
  $here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();
  $e = new java("Exception");

  // trigger ID=42
  $i = $e->inner;
  $i->o = new java("java.lang.Integer", 42);

  // should return 33
  $e->inner->meth(33);

  try {
    // should throw exception "Exception$Ex"
    $e->inner->meth(42);
    return 2;
  } catch (java_exception $exception) {
    echo "An exception occured: ".java_cast($exception, "S")."\n";

    $cause = $exception->getCause();
    echo "exception ".java_cast($cause,"S")." --> " . $cause->getID() . "\n";
    return (java_values($cause->getID()) == 42) ? 0 : 3; 
  }
} catch (exception $err) {
  print "unexpected: ".java_cast($err, "S")." \n";
  return 4;
}
