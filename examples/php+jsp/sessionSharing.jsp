<HTML>
<TITLE>PHP and JSP session sharing</title>
<BODY>
<%

javax.servlet.http.HttpSession $session = request.getSession();
if($session.getAttribute("counter")==null) {
  $session.setAttribute("counter", new java.lang.Integer(1));
}

int $counter = ((java.lang.Integer)$session.getAttribute("counter")).intValue();
out.println ("HttpSession variable \"counter\": " + $counter + "<br>");
java.lang.Integer $next = new java.lang.Integer($counter+1);
session.setAttribute("counter", $next);
%>
<a href="sessionSharing.php">PHP page</a>
</BODY>
</HTML>
