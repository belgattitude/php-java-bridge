<?php

$s = "";
array_shift($argv);
$outfile = array_shift($argv);
foreach($argv as $file) $s.= preg_replace('/\r/', '', file_get_contents($file));

$s = preg_replace('/^.*JAVA_DEBUG.*$/m', '', $s);
$s = preg_replace('/^.*!java_defined.*$/m', '', $s);
$s = preg_replace('/^<\?.*$/m', '', $s);
$s = preg_replace('/\?>.*$/m', '', $s);
$s = preg_replace('!/\*([^/]|([^*]/))*\*/!s', '', $s);
$s = preg_replace('|[	 ]+|', ' ', $s);
$s = preg_replace('/^ *require_once.*$/m', '', $s);
$s = preg_replace('|^ |m', '', $s);
$s = preg_replace('| $|m', '', $s);
$s = preg_replace('|([;{:]) //.*$|m', '$1', $s);
$s = preg_replace('|; *//.*$|m', ';', $s);
$s = preg_replace('|^//.*$|m', '', $s);
$s = preg_replace('|^ *$\n|m', '', $s);
$s = preg_replace('/ *= */', '=', $s);
$s = preg_replace('/, /', ',', $s);
//$s = preg_replace('/\n}/', '}', $s);

$version = trim(file_get_contents("../VERSION"));
$s = preg_replace('/define[ ]*\("JAVA_PEAR_VERSION","[0-9.]+"\);/', 'define("JAVA_PEAR_VERSION","'.$version.'");', $s);

$s = preg_replace("/^.*do not delete this line.*$/m", '', $s);

$str = <<<EOF
<?php
# Java.inc -- The PHP/Java Bridge PHP library. Compiled from JavaBridge.inc.
# Copyright (C) 2003-2009 Jost Boekemeier.
# Distributed under the MIT license, see Options.inc for details.
# Customization examples:
# define ("JAVA_HOSTS", 9267); define ("JAVA_SERVLET", false);
# define ("JAVA_HOSTS", "127.0.0.1:8787");
# define ("JAVA_HOSTS", "ssl://my-secure-host.com:8443");
# define ("JAVA_SERVLET", "/MyWebApp/servlet.phpjavabridge");
# define ("JAVA_PREFER_VALUES", 1);


EOF;

$str .= "${s}}\n?>\n";
$str = preg_replace('|\r|', '', $str);

file_put_contents($outfile, $str);

?>
