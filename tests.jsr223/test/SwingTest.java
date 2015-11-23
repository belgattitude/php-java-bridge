/*-*- mode: Java; tab-width:8 -*-*/

package test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;

import javax.script.Invocable;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;

import php.java.script.InvocablePhpScriptEngine;

/**
 * @author jostb
 *
 */
public class SwingTest {

    private interface SwingApplication extends Runnable {
        public void init();
    }
    public static void main(String s[]) throws FileNotFoundException, ScriptException, InterruptedException, InvocationTargetException {
	System.setProperty("php.java.bridge.default_log_level", "5");
	System.setProperty("php.java.bridge.default_log_file", "");
	System.setProperty("php.java.bridge.php_exec", "/usr/bin/php-cgi");

	    InvocablePhpScriptEngine engine = new InvocablePhpScriptEngine();
	    engine.eval(new FileReader("test/SwingTest.php"));
	    SwingApplication phpApp = (SwingApplication) ((Invocable)engine).getInterface(SwingApplication.class);
	    phpApp.init();
	    SwingUtilities.invokeAndWait(phpApp);
	    Thread.sleep(5000);
	    engine.release();
	    System.exit(0);
    }
}
