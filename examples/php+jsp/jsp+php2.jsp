 
<%@page import="javax.script.*" %>
<%@page import="java.net.*" %>
<%@page import="php.java.script.URLReader"%>
 
<%!
private static final ScriptEngineManager scriptManager = new ScriptEngineManager();
%>
 
<%
  ScriptEngine instance = scriptManager.getEngineByName("php-invocable");
  try {
	  URI remotePhpApp = new URI(request.getScheme(), null, "127.0.0.1", request.getLocalPort(), "/JavaBridge/java/JavaProxy.php", null, null);
	  instance.eval(new URLReader(remotePhpApp.toURL()));
	  Object result = ((Invocable)instance).invokeFunction("phpversion", new Object[]{});
	  out.println ("PHP application called \"JavaBridge/java/JavaProxy.php\" responds to phpversion(): " + result);
  } catch (Exception ex) {
	  out.println("Could not evaluate script: "+ex);
  } finally {
	  // release the resources immediately
  	((java.io.Closeable)instance).close();
  }
%>
 