#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$here=realpath(dirname($_SERVER["SCRIPT_FILENAME"]));
if(!$here) $here=getcwd();

$ar = new java("TestArrayMapCollection");
try {				// must fail
  echo $ar->array(array(1=>$ar, 2=>$ar, 3=>new JavaClass("TestArrayMapCollection")));
  echo "<br>\n";
  echo "test failed1\n"; exit(1);
} catch (JavaException $e) {
  echo "test okay <br>\n";
}
try {				// must fail
  echo $ar->array(array(1=>$ar, 2=>$ar, "a"=>$ar));
  echo "<br>\n";
  echo "test failed2\n"; exit(2);
} catch (JavaException $e) {
  echo "test okay <br>\n";
}
try {				// must succeed
  echo $ar->array(array(1=>$ar, 2=>$ar, 3=>$ar));
  echo "<br>\n";
} catch (JavaException $e) {
  echo "test failed3: $e\n"; exit(3);
}
try {				// must succeed
  echo $ar->map(array(1, 2, 3));
  echo "<br>\n";
} catch (JavaException $e) {
  echo "test failed4: $e\n"; exit(4);
}
try {				// must succeed
  echo $ar->list(array(1, 2,3));
  echo "<br>\n";
} catch (JavaException $e) {
  echo "test failed5: $e\n"; exit(5);
}
try {				// must succeed
  echo $ar->collection(array(1, 2, 3));
  echo "<br>\n";
} catch (JavaException $e) {
  echo "test failed6: $e\n"; exit(6);
}
try {				// must succeed
  $res = new Java("java.util.Vector", $ar->collection(array("3"=>3, "2"=>2, "1"=>1)));
  if(java_values($res[0])!='1' || java_values($res[1])!='2' || java_values($res[2])!='3') throw new Exception("test failed");
  echo "<br>\n";
} catch (Exception $e) {
  echo "test failed7: $e\n"; exit(7);
}

?>

