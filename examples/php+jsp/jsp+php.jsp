<%@page import="javax.script.*" %>
<%@page import="php.java.script.servlet.PhpHttpScriptContext" %>

<%!
private static final CompiledScript script;
static {
	try {
		script =((Compilable)(new ScriptEngineManager().getEngineByName("php-invocable"))).compile(
        "<?php echo 'Hello '.java_context()->get('hello').'!<br>\n'; function f($v){return (string)$v+1;};?>");
	} catch (ScriptException e) {
		throw new RuntimeException(e);
	}
}
%>

<%
  // create a new copy of the compiled script
  CompiledScript instance = (CompiledScript)((java.security.cert.CertStoreParameters)script).clone();
  try {
	  // create a custom ScriptContext to connect the engine to the ContextLoaderListener's FastCGI runner 
	  instance.getEngine().setContext(new PhpHttpScriptContext(instance.getEngine().getContext(),this,application,request,response));
	
	  // display hello world
	  instance.getEngine().put("hello", "eval1: " + Thread.currentThread());
	  instance.eval();
	  out.println(((Invocable)instance.getEngine()).invokeFunction("f", new Object[]{1})+"<br>\n");
	  instance.getEngine().put("hello", "eval2: " + Thread.currentThread());
	  instance.eval();
	  out.println(((Invocable)instance.getEngine()).invokeFunction("f", new Object[]{2})+"<br>\n");
	  instance.getEngine().put("hello", "eval3: " + Thread.currentThread());
	  instance.eval();
	  out.println(((Invocable)instance.getEngine()).invokeFunction("f", new Object[]{3})+"<br>\n");
	  instance.getEngine().put("hello", "eval4: " + Thread.currentThread());
	  instance.eval();
	  out.println(((Invocable)instance.getEngine()).invokeFunction("f", new Object[]{4})+"<br>\n");
	  out.println("thread ended: " + Thread.currentThread());
  } catch (Exception ex) {
	  out.println("Could not evaluate script: "+ex);
  } finally {
	  ((java.io.Closeable)instance.getEngine()).close();
  }
%>
