package php.java.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;
import php.java.script.IPhpScriptContext;
import php.java.script.URLReader;

public class TestURLReader extends TestCase {

    public TestURLReader(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void test() throws Exception {
	ScriptEngine e = new ScriptEngineManager().getEngineByName("php-invocable");
	
	// Note: don't use the following line in your own code
	// URLReader should be used to connect to a real HTTP server, usually running on port 80
	e.eval(new URLReader(new URL("http://localhost:"+((IPhpScriptContext)e.getContext()).getSocketName()+"/JavaBridge/java/JavaProxy.php")));
	
	OutputStream out = new ByteArrayOutputStream();
	Writer w = new OutputStreamWriter(out); 
	e.getContext().setWriter(w);
	e.getContext().setErrorWriter(w);
	String result = String.valueOf(((Invocable)e).invokeFunction("addslashes", new Object[]{"Is your name O'reilly?"}));
	assertTrue(out.toString().length()==0);
	assertTrue("Is your name O\\'reilly?".equals(result));
    }
}
