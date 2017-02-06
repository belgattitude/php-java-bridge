#!/bin/env php
<?php

/**
  * This example uses the PHP/Java Bridge to connect to a SOAP service.
  */

require_once("java/Java.inc");

include("wsimport.php");

try {
  $addNumbersService = new java("org.duke.AddNumbersService");
  $port = $addNumbersService->getAddNumbersPort();
  $number1 = 10;
  $number2 = 20;
  echo ("Invoking one-way operation. Nothing is returned from service.\n");
  $port->oneWayInt($number1);
  echo ("Invoking addNumbers($number1, $number2)\n");
  $result = $port->addNumbers($number1, $number2);
  echo ("The result of adding $number1 and $number2 is $result\n\n");
  $number1 = -10;
  echo ("Invoking addNumbers($number1, $number2)\n");
  $result = $port->addNumbers($number1, $number2);
  echo ("The result of adding $number1 and $number2 is $result\n\n");
} catch (JavaException $ex) {
  $ex = $ex->getCause();
  if (java_instanceof($ex, new JavaClass("org.duke.AddNumbersFault_Exception"))) {
    $info = $ex->getFaultInfo()->getFaultInfo ();
    echo ("Caught AddNumbersFault_Exception: $ex, INFO: $info.\n");
  } else {
    echo ("Exception occured: $ex\n");
  }
}
