<?php
include_once("java/Java.inc");

$passwd="hello";
try {
  java_require("mail.jar"); // mail.jar is not part of the standard jdk

  $password=new java("java.lang.String", "$passwd");
  $algorithm=java("java.security.MessageDigest")->getInstance("md5");
  $algorithm->reset();
  $algorithm->update($password->getBytes());
  $encrypted = $algorithm->digest();
  $out = new java("java.io.ByteArrayOutputStream");

  java_inspect(java("javax.mail.internet.MimeUtility"));

  $encoder = java("javax.mail.internet.MimeUtility")->encode($out, "base64");
  $encoder->write($encrypted);
  $encoder->flush();
  echo new java("java.lang.String",$out->toByteArray()); echo "\n";
  exit(0);
} catch (Exception $e) {
  echo "Echo invocation failed: $e\n";
  //print_r ($e->getTrace());
  exit(1);
}
?>

