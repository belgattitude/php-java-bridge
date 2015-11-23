package php.java.test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

public class TestExceptionInvocable2 extends TestCase {

    public TestExceptionInvocable2(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    public void test() throws Exception {
	ScriptEngineManager manager = new ScriptEngineManager();
	ScriptEngine e = manager.getEngineByName("php-invocable");
	OutputStream out = new ByteArrayOutputStream();
	Writer w = new OutputStreamWriter(out); 
	e.getContext().setWriter(w);
	e.getContext().setErrorWriter(w);
	e.eval("<?php function f() { return 'f ok';}; \n" +
			"class c {function p() {return 'c::p ok';}}; \n" +
			"java_context()->setAttribute('c', java_closure(new c()), 100); " +
			"?>");

	Invocable i = (Invocable) e;
	try {
	    assertTrue("f ok".equals(i.invokeFunction("f", new Object[] {})));
	} catch (Throwable ex) {
	    fail("2");
	}
	try {
	    i.invokeFunction("g", new Object[] {});
	    System.err.println("test failed");
	    fail("3");
	} catch (NoSuchMethodException ex) {
//	    ex.printStackTrace(System.out);
	}
	try {
	    assertTrue("c::p ok".equals(i.invokeMethod(e.get("c"), "p", new Object[] {})));
	} catch (NoSuchMethodException ex) {
	    fail("4");
	}
	try {
	    i.invokeMethod(e.get("c"), "g", new Object[] {});
	    fail("5");
	} catch (NoSuchMethodException ex) {
	    //ex.printStackTrace(System.out);
	}
	((Closeable)e).close();
	if (out.toString().length() != 0) throw new Exception("test failed");
    }    
}
