<?php

include_once ("java/Java.inc");
ini_set("max_execution_time", 0);

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();

$binaryData = new java("BinaryData");
$data = java_values($binaryData->getData(700*1024));
for($i=0; $i < 10; $i++) {
  $data=java_values($binaryData->compare($data));
  $str1=substr($data, 255, 256);
}
$str='&;a&amp;&quote"&quot;&&&;;"';
$binaryData->b='&;a&amp;&quote"&quot;&&&;;"';
$binaryData->compare('&;a&amp;&quote"&quot;&&&;;"');
if($str!=java_values($binaryData->toString())) { echo "ERROR\n"; exit(1); }

$data = java_values($binaryData->getData(1024));
if(strlen($data)!=1024) { echo "ERROR\n"; exit(5); }
$s1=substr(java_values($binaryData->toString()), 0, 256);
$binaryData->b=$str1;
$s2=substr(java_values($binaryData->toString()), 0, 256);

if($s1!=$s2) { echo "ERROR\n"; exit(2); }
echo "test ok\n";
exit(0);

?>
