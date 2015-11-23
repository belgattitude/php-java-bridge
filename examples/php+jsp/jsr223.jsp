<%@page import="javax.script.*" %>
<%@page import="php.java.script.servlet.PhpHttpScriptContext" %>

<%!
private static final CompiledScript script;
static {
	try {
		script =((Compilable)(new ScriptEngineManager().getEngineByName("php"))).compile(
        "<?php echo 'Hello '.java_context()->get('hello').'!<br>\n'; ?>");
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
	  instance.getEngine().put("hello", "eval2: " + Thread.currentThread());
	  instance.eval();
	  instance.getEngine().put("hello", "eval3: " + Thread.currentThread());
	  instance.eval();
	  instance.getEngine().put("hello", "eval4: " + Thread.currentThread());
	  instance.eval();
	  out.println("thread ended: " + Thread.currentThread());
  } catch (Exception ex) {
	out.println("Could not evaluate script: "+ex);
  }
%>
