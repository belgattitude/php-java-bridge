package php.java.test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

public class TestSimpleInvocation extends TestCase {

    public TestSimpleInvocation(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    public void test() throws Exception {
	ScriptEngineManager m = new ScriptEngineManager();
	ScriptEngine e = m.getEngineByName("php-invocable");
	OutputStream out = new ByteArrayOutputStream();
	Writer w = new OutputStreamWriter(out); 
	e.getContext().setWriter(w);
	e.getContext().setErrorWriter(w);
	Invocable i = (Invocable)e;
	i.invokeFunction("phpinfo", new Object[0]);
	if (out.toString().length() == 0) throw new ScriptException("test failed");
	((Closeable)e).close();
    }
}
