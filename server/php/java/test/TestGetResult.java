package php.java.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

public class TestGetResult extends TestCase {

    public TestGetResult(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    public void testDiscovery() {
	try {
	    ScriptEngine e = new ScriptEngineManager().getEngineByName("php");
	    String result = String.valueOf(e.eval("<?php exit(2+3);"));
	    if (!result.equals("5")) throw new ScriptException("test failed");

	    e = new ScriptEngineManager().getEngineByName("php-invocable");
	    OutputStream out = new ByteArrayOutputStream();
	    Writer w = new OutputStreamWriter(out); 
	    e.getContext().setWriter(w);
	    e.getContext().setErrorWriter(w);
	    Object o = e.eval("<?php exit(7+9); ?>");
	    result = String.valueOf(o); // note that this releases the engine, the next invoke will implicitly call eval() with an empty script
	    ((Invocable)e).invokeFunction("phpinfo", new Object[]{});
	    if (!result.equals("16")) throw new ScriptException("test failed");
	    if (out.toString().length() == 0) throw new ScriptException("test failed");
	} catch (Exception e1) {
	    fail(String.valueOf(e1));
        }
    }
}
