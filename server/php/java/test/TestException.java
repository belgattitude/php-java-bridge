package php.java.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

public class TestException extends TestCase {

    public TestException(String name) {
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
	ScriptEngine e = manager.getEngineByName("php");
	OutputStream out = new ByteArrayOutputStream();
	Writer w = new OutputStreamWriter(out); 
	e.getContext().setWriter(w);
	e.getContext().setErrorWriter(w);
	try {
	    e.eval("<?php bleh();?>");
	} catch (ScriptException ex) {
	    if (out.toString().length() == 0) throw new Exception("test failed");
	    return;
	}
	fail("test failed");
    }    
}
