#!/usr/bin/php

<?php 
include_once ("java/Java.inc");

$Array = new java_class("java.lang.reflect.Array");
$String = new java_class("java.lang.String");
$entries = $Array->newInstance($String, 8);
$entries[0] ="Jakob der Lügner, Jurek Becker 1937--1997";
$entries[1] ="Mutmassungen über Jakob, Uwe Johnson, 1934--1984";
$entries[2] ="Die Blechtrommel, Günter Grass, 1927--";
$entries[3] ="Die Verfolgung und Ermordung Jean Paul Marats dargestellt durch die Schauspielgruppe des Hospizes zu Charenton unter Anleitung des Herrn de Sade, Peter Weiss, 1916--1982";
$entries[4] ="Der Mann mit den Messern, Heinrich Böll, 1917--1985";
$entries[5] ="Biedermann und die Brandstifter, Max Frisch, 1911--1991";
$entries[6] ="Seelandschaft mit Pocahontas, Arno Schmidt, 1914--1979";
for ($i = 0; $i < java_values($Array->getLength($entries)); $i++) { 
  echo "$i: $entries[$i]\n";
 }

?>
