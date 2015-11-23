<?php

include_once("java/Java.inc");

$session=java_get_session("testSession");
if($session->isNew()) {
  echo "new session\n";
  $session->put("a", 1);
  $session->put("b", 5);
  $session->put("c", null);
}
else {
  echo "cont session\n";
}

$session->put("a", java_values($session->get("a"))+1);
$session->put("b", java_values($session->get("b"))-1);

$val=java_values($session->get("a"));
$c=java_values($session->get("c"));
if($c!=null) {echo "test failed"; exit(1);}
echo "session var: ".java_values($val)."\n";

if(java_values($session->get("b"))==0) $session->destroy();

?>
