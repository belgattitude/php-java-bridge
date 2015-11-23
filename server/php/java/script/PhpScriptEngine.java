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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import php.java.bridge.Util;
import php.java.bridge.http.IContext;

/**
 * This class implements the ScriptEngine.<p>
 * Example:<p>
 * <code>
 * ScriptEngine e = (new ScriptEngineManager()).getEngineByName("php");<br>
 * try { e.eval(&lt;?php foo() ?&gt;"); } catch (ScriptException e) { ... }<br>
 * </code>
 * @author jostb
 *
 */
public class PhpScriptEngine extends AbstractPhpScriptEngine {
     
    /**
     * Create a new ScriptEngine with a default context.
     */
    public PhpScriptEngine() {
	super(new PhpScriptEngineFactory());
    }

    /**
     * Create a new ScriptEngine from a factory.
     * @param factory The factory
     * @see #getFactory()
     */
    public PhpScriptEngine(PhpScriptEngineFactory factory) {
        super(factory);
    }
    /**
     * Create a new ScriptEngine with bindings.
     * @param n the bindings
     */
    public PhpScriptEngine(Bindings n) {
	this();
	setBindings(n, ScriptContext.ENGINE_SCOPE);
    }
	
    protected Reader getLocalReader(Reader reader,boolean embedJavaInc) throws IOException {
        /* header: <? require_once("http://localhost:<ourPort>/JavaBridge/java/Java.inc"); ?> */
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	Writer w = new OutputStreamWriter(out);
	try {
	    Reader localReader = null;
	    char[] buf = new char[Util.BUF_SIZE];
	    int c;
	    String stdHeader = embedJavaInc ? null : ((IContext)getContext()).getRedirectURL("/JavaBridge");
	    localReader = new StringReader(getStandardHeader(stdHeader));

	    while((c=localReader.read(buf))>0) w.write(buf, 0, c);
	    localReader.close(); localReader=null;

	    /* the script: */
	    while((c=reader.read(buf))>0) w.write(buf, 0, c);
	    w.close(); w = null;
        
	    /* now evaluate our script */
	    localReader = new InputStreamReader(new ByteArrayInputStream(out.toByteArray()));
	    return localReader;
	} finally {
	    if (w!=null) try {w.close();} catch (IOException e) {/*ignore*/}
	}
    }

    protected Object doEvalPhp(Reader reader, ScriptContext context) throws ScriptException {
        if((continuation != null) || (reader == null) ) release();
  	if(reader==null) return null;
  	
  	setNewContextFactory();
        Reader localReader = null;
        
        try {
            localReader = getLocalReader(reader, false);
            this.script = doEval(localReader, context);
        } catch (Exception e) {
            Util.printStackTrace(e);
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            if (e instanceof ScriptException) throw (ScriptException)e;
            throw new ScriptException(e);
         } finally {
            if(localReader!=null) try { localReader.close(); } catch (IOException e) {/*ignore*/}

            // release the engine, so that any error reported by the script can trigger a Java exception
            release();
       }
        
       return resultProxy;
    }
    protected Object doEvalCompiledPhp(Reader reader, ScriptContext context) throws ScriptException {
        if((continuation != null) || (reader == null) ) release();
  	if(reader==null) return null;
  	
  	setNewContextFactory();
        try {
            this.script = doEval(reader, context);
        } catch (Exception e) {
            Util.printStackTrace(e);
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            if (e instanceof ScriptException) throw (ScriptException)e;
            throw new ScriptException(e);
         } finally {

            // release the engine, so that any error reported by the script can trigger a Java exception
            release();
       }
        
       return resultProxy;
    }
}
