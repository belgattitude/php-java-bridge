package php.java.test;

import java.io.File;
import java.io.FileWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;
import php.java.bridge.Request.AbortException;

public class TestInteractiveRequestAbort extends TestCase {

    public TestInteractiveRequestAbort(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    public  void test() throws Exception {
	String devNull = new File("/dev/null").exists()? "/dev/null" : "devNull";
	ScriptEngineManager m = new ScriptEngineManager();
	ScriptEngine e = m.getEngineByName("php-interactive");
	e.getContext().setErrorWriter(new FileWriter(new File(devNull)));
	e.getContext().setWriter(new FileWriter(new File(devNull)));
	
	try {
	    e.eval("function toString() {return 'hello'; }; echo java_closure(); echo new JavaException('java.lang.Exception', 'hello'); echo JavaException('foo')");
	} catch (ScriptException ex) {
	    Throwable orig = ex.getCause();
	    if (orig instanceof AbortException) {
		return;
	    }
	}

	fail("test failed");
    }

}
