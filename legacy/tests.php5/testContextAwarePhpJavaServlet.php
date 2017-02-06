<?php
try {
  include_once ("java/Java.inc");
  $ctx = java_context();
  $request = $ctx->getServletContext();
  echo $request;
} catch (Exception $e) {
  die("$e");
}
echo "test okay\n";

?>
