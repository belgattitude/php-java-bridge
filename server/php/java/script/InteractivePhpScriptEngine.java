/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script;

/*
 * Copyright (C) 2003-2007 Jost Boekemeier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER(S) OR AUTHOR(S) BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.IOException;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import php.java.bridge.Util;
import php.java.bridge.http.IContext;

/**
 * A convenience variant of the PHP script engine which can be used interactively.<p>
 * Example:<p>
 * <code>
 * ScriptEngine e = (new ScriptEngineManager()).getEngineByName("php-interactive);<br>
 * e.eval("$v = 1+2"); <br>
 * System.out.println(e.eval("echo $v")); <br>
 * e.eval((String)null);<br>
 * </code>
 * @author jostb
 *
 */
public class InteractivePhpScriptEngine extends InvocablePhpScriptEngine {

    private static final String restoreState = "" +
    		"$javabridge_values=unserialize(java_values(java_context()->getAttribute('php.java.bridge.JAVABRIDGE_TMP_VALUES', 100)));" +
    		"if($javabridge_values)" +
    		"foreach ($javabridge_values as $javabridge_key=>$javabridge_val) " +
    		"{eval(\"\\$$javabridge_key=\\$javabridge_values[\\$javabridge_key];\");}\n";
    private static final String saveState = "" +
    		"foreach (get_defined_vars() as $javabridge_key=>$javabridge_val) " +
    		"{if(in_array($javabridge_key, $javabridge_ignored_keys)) continue;" +
    		"eval(\"\\$javabridge_values[\\$javabridge_key]=\\$$javabridge_key;\");};" +
    		"java_context()->setAttribute('php.java.bridge.JAVABRIDGE_TMP_VALUES', serialize($javabridge_values), 100);\n";


    public InteractivePhpScriptEngine() {
	super(new InteractivePhpScriptEngineFactory());
    }
    /**
     * Create the interactive php script engine.
     * @param factory The engine factory
     */
    public InteractivePhpScriptEngine(InteractivePhpScriptEngineFactory factory) {
        super(factory);
    }
    /**
     * Create a new ScriptEngine with bindings.
     * @param n the bindings
     */
    public InteractivePhpScriptEngine(Bindings n) {
	this();
	setBindings(n, ScriptContext.ENGINE_SCOPE);
    }

    boolean hasScript = false;
    /* (non-Javadoc)
     * @see javax.script.ScriptEngine#eval(java.lang.String, javax.script.ScriptContext)
     */
    /**
     * Evaluate a PHP line.
     * @param script The script line
     * @param context The context
     * @return The result
     * @throws ScriptException 
     */
    public Object eval(String script, ScriptContext context)
	throws ScriptException {
	if(script==null) {
	    release();
	    return null;
	}
	
	if(!hasScript) {
	    super.eval("<?php " +
		       "ini_set('max_execution_time', 0);\n" +
		       "ini_set('display_errors','On');\n" +
		       "$javabridge_values = array();\n"+
		       "$javabridge_ignored_keys = array(\"javabridge_key\", \"javabridge_val\", \"javabridge_values\", \"javabridge_ignored_keys\", \"javabridge_param\");\n"+
		       "function javabridge_eval($javabridge_param) {\n" +
		       "global $javabridge_values;\n" +
		       "global $javabridge_ignored_keys;\n" +
		       "ob_start();\n" +
		       restoreState +
		       "eval(java_cast($javabridge_param,\"s\"));\n" +
		       saveState +
		       "$javabridge_retval = ob_get_contents();\n" +
		       "ob_end_clean();\n" +
		       "return $javabridge_retval;\n" +
		       "};\n" +
		       "?>", context);
	    hasScript = true;
	}
	script=script.trim() + ";";
	Object o = null;
	try {
	    o=((Invocable)this).invokeFunction("javabridge_eval", new Object[]{script});
	} catch(NoSuchMethodException ex){
	    /*ignore*/
	} finally {
	    try { context.getWriter().flush(); }      catch (IOException e) { Util.printStackTrace(e); }
	    try { context.getErrorWriter().flush(); } catch (IOException e) { Util.printStackTrace(e); }
	}
	
	return o;
    }
    /**{@inheritDoc}*/
    public void release() {
	    super.release();
	    hasScript = false;
    }
    
    /**
     * Create a new context ID and a environment map which we send to the client.
     *
     */
    protected void addNewContextFactory() {
	ctx = InteractivePhpScriptContextFactory.addNew((IContext)getContext());
    }
}
