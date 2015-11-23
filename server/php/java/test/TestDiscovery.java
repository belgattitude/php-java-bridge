package php.java.test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

public class TestDiscovery extends TestCase {

    public TestDiscovery(String name) {
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
		StringBuffer s = new StringBuffer();
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine e = manager.getEngineByName("php");
		e.put("hello", new StringBuffer("hello world"));
		e.put("s", s);
		e.eval("<?php " +
				"$s = java_context()->getAttribute('s');" +
				"$s->append(java_values(java_context()->getAttribute('hello')));" +
				"/*echo java_values($s);*/" +
				"java_context()->setAttribute('hello', '!', 100);" +
				"?>");
		s.append(e.get("hello"));
		if(!(s.toString().equals("hello world!"))) {
		  fail("ERROR");
		}
	} catch (Exception e) {
	    fail (String.valueOf(e));
	}
    }
}
