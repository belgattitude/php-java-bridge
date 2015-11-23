#!/usr/bin/php

<?php
   //define ("JAVA_DEBUG", true);
require_once ("java/Java.inc");

$here = getcwd();
$java_output = "workbook_java.xls";
$php_output = "workbook_php.xls";

ini_set("max_execution_time", 0);
$sys = java("java.lang.System");

// fetch classes and compile them to native code.
// use local poi.jar, if installed
try {
  java_require("$here/exceltest.jar;$here/../../unsupported/poi.jar");
} catch (JavaException $e) {
  java_require("$here/exceltest.jar;http://php-java-bridge.sf.net/poi.jar");
}
$excel = new java("ExcelTest");
$excel->createWorkbook("/dev/null", 1, 1);

// test starts
$sys->gc();
$start = java_values($sys->currentTimeMillis());
$excel = new java("ExcelTest");
$excel->createWorkbook("$here/$java_output", 200, 200);
$sys->gc();
$t_java = java_values($sys->currentTimeMillis()) - $start;

include("$here/excel_antitest.php");
$sys->gc();
$start = java_values($sys->currentTimeMillis());
createWorkbook("$here/$php_output", 200, 200);
$sys->gc();

$t_php = java_values($sys->currentTimeMillis()) - $start;

echo "$java_output\t: $t_java ms.\n";
echo "$php_output\t: $t_php ms.\t(" . $t_php/$t_java .")\n";

/*
Sample results on a 1.4GHZ i686, kernel 2.6.8

--------------------------------------------------
PHP/Java Bridge Version 1.0.8:
       pure java              mix PHP/Java           pure java     mix PHP/Java
       interpreted (-Xint)    interpreted (-Xint)    compiled        compiled
jdk
1.4:   13367 ms               42919 ms               2325 ms         22276 ms
1.5:   18342 ms               42048 ms               2227 ms         21008 ms

--------------------------------------------------
PHP/Java Bridge Version 2.0.7:
jdk
1.4:   13929 ms               39723 ms               2349 ms         9232 ms

--------------------------------------------------
PHP/Java Bridge Version 4.3.3:
jdk
1.6:   -     ms               -     ms               2120 ms         7655 ms

*/
?>
