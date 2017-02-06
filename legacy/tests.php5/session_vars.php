#!/usr/bin/php

<?php

include_once ("java/Java.inc");

$ser="vector.ser";

// load the session from the disc
if(file_exists($ser)) {
  $file=fopen($ser,"r");
  $id=fgets($file);
  fclose($file);
  try {
    $v=unserialize($id);
  } catch (JavaException $e) {
    echo "Could not deserialize: ". $e->getCause() . "\n";
    $v=null;
  }
}

// either a new session or previous session destroyed
if(!$v) {
  echo "creating new session\n";
  $vector=new java("java.util.Vector", array(1, true, -1.345e99, "hello", new java("java.lang.Object")));
  
  $v=array (
	"test",
	$vector,
	new java("java.lang.String", "HelloWorld"),
	3.14,
	new java("java.lang.Object"));
  $id=serialize($v);
  $file=fopen($ser,"w");
  fwrite($file, $id);
  fclose($file);
} else {
  echo "cont. session\n";
}
echo ($v[1]->__toString())."\n". $v[2]->__toString()."\n";
?>

