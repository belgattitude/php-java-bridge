<?php include_once("java/Java.inc");

$ex = new JavaException("java.lang.Exception", "bleh");
echo $ex->getMessage();
if ($ex->getMessage()=="") die ("test failed");

echo "\n---\n";

try {
  java("java.lang.System")->getProperties();
} catch (JavaException $e) {
  echo $e->getMessage();
  if ($e->getMessage()=="") die ("test failed");
}

echo "\n---\n";

?>
