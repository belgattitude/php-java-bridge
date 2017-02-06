#!/usr/bin/php

<?php

include_once ("java/Java.inc");

include("../php_java_lib/JPersistence.php");
$ser="persist.ser";

// load the session from the disc
if(file_exists($ser)) {
  $file=fopen($ser,"r");
  $id=fgets($file);
  fclose($file);
  try {
    $v=unserialize($id);
  } catch (JavaException $e) {
    echo "Warning: Could not deserialize: ". $e->getCause() . "\n";
    $v=0;
  }
}

// a new session
if(!$v) {
  echo "creating new session\n";
  $vector=new JPersistenceAdapter(new java("java.util.Vector"));
  $vector->add("hello");
  $vector->add(new java("java.lang.Double", "3.14"));
  $vector->add(new java("java.lang.StringBuffer","stringbuffer"));
  $v=array (
	"test",
	$vector,
	3.14);
  $id=serialize($v);
  $file=fopen($ser,"w");
  fwrite($file, $id);
  fclose($file);
} else {
  echo "cont. session\n";
}
echo $v[0];
echo $v[1];
echo $v[2];
echo "\n";

exit(0);
?>

