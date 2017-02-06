<?php require_once("java/Java.inc");
$session = java_session();
?>

<HTML>
<TITLE>PHP and JSP session sharing</title>
<BODY>
<?php

if(java_is_null($session->get("counter"))) {
  $session->put("counter", 1);
}

$counter = java_values($session->get("counter"));
print "HttpSession variable \"counter\": $counter<br>\n";
$session->put("counter", $counter+1);
?>
<a href="sessionSharing.jsp">JSP page</a>
</BODY>
</HTML>
