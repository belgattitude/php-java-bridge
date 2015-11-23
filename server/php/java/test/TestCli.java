package php.java.test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

public class TestCli extends TestCase {

    public TestCli(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    public void testSimple() {
	try {
	    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
	    Writer err = new OutputStreamWriter(errOut);
	    ScriptEngine eng = (new ScriptEngineManager()).getEngineByName("php-interactive");
	    eng.getContext().setErrorWriter(err);
	    eng.eval("$a=new java('java.util.Vector');");
	    eng.eval("$a->add(1);");
	    eng.eval("$a->add(2);");
	    eng.eval("$a->add(3);");
	    eng.eval("class C{function toString() {return 'foo';}}");
	    eng.eval("$a->add(java_closure(new C()));");
	    eng.eval("$b=new java('java.util.Vector');");
	    eng.eval("$b->add(1);");
	    eng.eval("$b->add(2);");
	    eng.eval("$b->add(3);");
	    assertTrue("[1, 2, 3]".equals(eng.eval("echo $b")));
	    assertTrue("[1, 2, 3, foo]".equals(eng.eval("echo $a")));
	    ((Closeable)eng).close();
	} catch (Exception e) {
	    fail (String.valueOf(e));
	}
    }
    public void testClosure() {
	try {
	    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
	    Writer err = new OutputStreamWriter(errOut);
	    ScriptEngine eng = (new ScriptEngineManager()).getEngineByName("php-interactive");
	    eng.getContext().setErrorWriter(err);
	    eng.eval("$a=new java('java.util.Vector');");
	    eng.eval("$a->add(1);");
	    eng.eval("$a->add(2);");
	    try { eng.eval("die();"); } catch (Exception e) {
		assertTrue(e.getMessage().equals("php.java.bridge.Request$AbortException"));
	    }
	    assertTrue(eng.eval("echo $a").equals("[1, 2]"));
	    ((Closeable)eng).close();
	} catch (Exception e) {
	    fail (String.valueOf(e));
	}
    }
}
