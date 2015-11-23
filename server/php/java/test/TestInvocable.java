package php.java.test;

import java.io.Reader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

public class TestInvocable extends TestCase {

    public TestInvocable(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    public void test () throws Exception {

	ScriptEngineManager manager = new ScriptEngineManager();
	ScriptEngine e = manager.getEngineByName("php-invocable");

	e.eval("<?php class f {function a($p) {return java_values($p)+1;}}\n" +
			"java_context()->setAttribute('f', java_closure(new f()), 100); ?>");

	Invocable i = (Invocable)e;
	Object f = e.getContext().getAttribute("f", 100);
	assertTrue(2==(Integer)i.invokeMethod(f, "a", new Object[] {new Integer(1)}));

	e.eval((Reader)null);
    }
}
