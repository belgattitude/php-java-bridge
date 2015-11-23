#!/usr/bin/php
<?php
include_once ("java/Java.inc");

$sys = new java("java.lang.System");
$sys->setProperty("utf8", "Cześć! -- שלום -- Grüß Gott -- Dobrý deň -- Dobrý den -- こんにちは, ｺﾝﾆﾁﾊ");
$arr=$sys->getProperties();
foreach ($arr as $key => $value) {
  print $key . " -> " .  java_values($value) . "<br>\n";
}
?>

