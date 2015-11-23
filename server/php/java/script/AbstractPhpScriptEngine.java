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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import php.java.bridge.Util;
import php.java.bridge.http.AbstractChannelName;
import php.java.bridge.http.ContextServer;
import php.java.bridge.http.HeaderParser;
import php.java.bridge.http.IContext;
import php.java.bridge.http.IContextFactory;
import php.java.bridge.http.WriterOutputStream;

/**
 * This class implements the ScriptEngine.<p>
 *@see php.java.script.InvocablePhpScriptEngine
 *@see php.java.script.PhpScriptEngine
 */
abstract class AbstractPhpScriptEngine extends AbstractScriptEngine implements IPhpScriptEngine, Compilable, java.io.FileFilter, CloneableScript {

    /**
     * The allocated script
     */
    protected Object script = null;
    protected Object scriptClosure = null;
    
    /**
     * The continuation of the script
     */
    protected Continuation continuation = null;
    protected Map env = null;
    protected IContextFactory ctx = null;
    
    private ScriptEngineFactory factory = null;
    protected ResultProxy resultProxy;
    protected File compilerOutputFile;

    private boolean isCompiled;

    static HashMap getProcessEnvironment() {
	return Util.COMMON_ENVIRONMENT;
    }

    /**
     * Create a new ScriptEngine from a factory.
     * @param factory The factory
     * @see #getFactory()
     */
    public AbstractPhpScriptEngine(PhpScriptEngineFactory factory) {
	super();
	this.factory = factory;
        getContext(); // update context in parent as a side effect
    }
    /**
     * Set the context id (X_JAVABRIDGE_CONTEXT) and the override flag (X_JAVABRIDGE_OVERRIDE_HOSTS) into env
     * @param env the environment which will be passed to PHP
     */
    protected void setStandardEnvironmentValues (Map env) {
	/* send the session context now, otherwise the client has to 
	 * call handleRedirectConnection */
	env.put(Util.X_JAVABRIDGE_CONTEXT, ctx.getId());
    }
    protected void addNewContextFactory() {
	ctx = PhpScriptContextFactory.addNew((IContext)getContext());
    }
    protected ContextServer getContextServer() {
	return ((IPhpScriptContext)getContext()).getContextServer();
    }
    /**
     * Create a new context ID and a environment map which we send to the client.
     *
     */
    protected void setNewContextFactory() {
	env = (Map) getProcessEnvironment().clone();

	addNewContextFactory();
	
    	// short path S1: no PUT request
	    ContextServer contextServer = getContextServer();
    		AbstractChannelName channelName = contextServer.getChannelName(ctx);
    		if (channelName != null) {
    		    env.put("X_JAVABRIDGE_REDIRECT", channelName.getName());
    		    ctx.getBridge();
    		    contextServer.start(channelName, Util.getLogger());
    		}
	
    	setStandardEnvironmentValues(env);
    }

    /* (non-Javadoc)
     * @see javax.script.ScriptEngine#eval(java.io.Reader, javax.script.ScriptContext)
     */
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return evalPhp(reader, context);
    }
    
    protected Object evalPhp(Reader reader, ScriptContext context) throws ScriptException {
	if (isCompiled) 
	    throw new IllegalStateException("already compiled");
	
	ScriptContext current = getContext();
	if (current != context)
	        try {
		    setContext(context);
	            return doEvalPhp(reader, context);
	        } finally {
	            setContext(current);
	        }
	else 
	    return doEvalPhp(reader, current);
    }
    protected Object evalCompiledPhp(Reader reader, ScriptContext context) throws ScriptException {
	
	ScriptContext current = getContext();
	if (current != context)
	        try {
		    setContext(context);
	            return doEvalCompiledPhp(reader, context);
	        } finally {
	            setContext(current);
	        }
	else 
	    return doEvalCompiledPhp(reader, current);
    }
    
    protected void compilePhp(Reader reader) throws IOException {
	this.isCompiled = true;
  	
  	if (compilerOutputFile == null) {
  	    compilerOutputFile = File.createTempFile("compiled-", ".php", null);
  	}
	FileWriter writer = new FileWriter(compilerOutputFile);
	char[] buf = new char[Util.BUF_SIZE];
	Reader localReader = getLocalReader(reader, true);
	try {
		int c;
		while((c = localReader.read(buf))>0) 
		    writer.write(buf, 0, c);
		writer.close();
	} finally {
	    localReader.close();
	}
    }
    
    private void updateGlobalEnvironment(ScriptContext context) throws IOException {
	if (isCompiled) {
	    if (compilerOutputFile == null) 
		throw new NullPointerException("SCRIPT_FILENAME");
	    env.put("SCRIPT_FILENAME", compilerOutputFile.getCanonicalPath());
	}
    }
    private final class SimpleHeaderParser extends HeaderParser {
	private WriterOutputStream writer;
	public SimpleHeaderParser(WriterOutputStream writer) {
	    this.writer = writer;
	}
	public void parseHeader(String header) {
	    if(header==null) return;
	    int idx = header.indexOf(':');
	    if(idx==-1) return;
	    String key = header.substring(0, idx).trim().toLowerCase();
	    String val = header.substring(idx+1).trim();
	    addHeader(key, val);
	}
	public void addHeader(String key, String val) {
	    if(val!=null && key.equals("content-type")) {
		int idx = val.indexOf(';');
		if(idx==-1) return;
		String enc = val.substring(idx+1).trim();
		idx = enc.indexOf('=');
		if(idx==-1) return;
		enc=enc.substring(idx+1);
		writer.setEncoding(enc);
	    }
	}
    }
    protected Continuation getContinuation(Reader reader, ScriptContext context) throws IOException {
	HeaderParser headerParser = HeaderParser.DEFAULT_HEADER_PARSER; // ignore encoding, we pass everything directly
	IPhpScriptContext phpScriptContext = (IPhpScriptContext)context;
    	updateGlobalEnvironment(context);
    	OutputStream out =((PhpScriptWriter)(context.getWriter())).getOutputStream();
    	OutputStream err =  ((PhpScriptWriter)(context.getErrorWriter())).getOutputStream();

	/*
	 * encode according to content-type charset
	 */
    	if(out instanceof WriterOutputStream)
    	    headerParser = new SimpleHeaderParser((WriterOutputStream)out);

    	Continuation kont = phpScriptContext.createContinuation(reader, env, out,  err, headerParser, resultProxy = new ResultProxy(this), Util.getLogger(), isCompiled);

    	phpScriptContext.setContinuation(kont);
    	phpScriptContext.startContinuation();
	return kont;
    }
    /** Method called to evaluate a PHP file w/o compilation */
    protected abstract Object doEvalPhp(Reader reader, ScriptContext context) throws ScriptException;
    protected abstract Object doEvalCompiledPhp(Reader reader, ScriptContext context) throws ScriptException;
    protected abstract Reader getLocalReader(Reader reader, boolean embedJavaInc) throws IOException;
    
    /*
     * Obtain a PHP instance for url.
     */
    final protected Object doEval(Reader reader, ScriptContext context) throws Exception {
    	continuation = getContinuation(reader, context);
    	return continuation.getPhpScript();
    }

    /* (non-Javadoc)
     * @see javax.script.ScriptEngine#eval(java.lang.String, javax.script.ScriptContext)
     */
    /**@inheritDoc*/
    public Object eval(String script, ScriptContext context)
	throws ScriptException {
      	if(script==null) return evalPhp((Reader)null, context);
      	
	script = script.trim();
	Reader localReader = new StringReader(script);
	try {
	    return eval(localReader, context);
	} finally {
	    try { localReader.close(); } catch (IOException e) {/*ignore*/}
	}
    }

    /**@inheritDoc*/
    public ScriptEngineFactory getFactory() {
	return this.factory;
    }

    /**
     * Release the continuation
     */
    public void release() {
	if(continuation != null) {
	    try {
		continuation.release();
		ctx.releaseManaged();
	    } catch (InterruptedException e) {
		    return;
	    }
	    ctx = null;
	    
	    continuation = null;
	    script = null;
	    scriptClosure = null;
	    
	    try {getContext().getWriter().flush();} catch (Exception e) {Util.printStackTrace(e);}
	    try {getContext().getErrorWriter().flush();} catch (Exception e) {Util.printStackTrace(e);}
	}
    }

    /* (non-Javadoc)
     * @see javax.script.ScriptEngine#createBindings()
     */
    /** {@inheritDoc} */
    public Bindings createBindings() {
        return new SimpleBindings();
    } 
    /**
     * Release the script engine.
     * @throws IOException 
     */
    public void close() throws IOException {
	release();
    }

    /** {@inheritDoc} */
    public CompiledScript compile(String script) throws ScriptException {
	Reader reader = new StringReader(script);
	try {
	    return compile(reader);
	} finally {
	    try {
	        reader.close();
            } catch (IOException e) {
	        Util.printStackTrace(e);
            }
	}
    }
    static final void throwNoOutputFile() {
        throw new IllegalStateException("No compilation output file has been set!");
    }
    static final Reader DUMMY_READER = new Reader() {
	/** {@inheritDoc} */
        public void close() throws IOException {
            throwNoOutputFile();
        }
	/** {@inheritDoc} */
        public int read(char[] cbuf, int off, int len)
                throws IOException {
            throwNoOutputFile();
            return 0;
       }};
    private static final String STANDARD_HEADER = new String("<?php require_once(\"/java/Java.inc\");" +
    		"$java_bindings = java_context()->getBindings(100);" +
    		"$java_scriptname = @java_values($java_bindings['javax.script.filename']);"+
    		"if(!isset($argv)) $argv = @java_values($java_bindings['javax.script.argv']);"+
    		"if(!isset($argv)) $argv=array();\n"+
    		"$_SERVER['SCRIPT_FILENAME'] =  isset($java_scriptname) ? $java_scriptname : '';"+
    		"array_unshift($argv, $_SERVER['SCRIPT_FILENAME']);"+
    		"if (!isset($argc)) $argc = count($argv);"+
    		"$_SERVER['argv'] = $argv;"+
    		"?>");
    private static final String STANDARD_HEADER_EMBEDDED = new String("<?php " +
    		"$java_bindings = java_context()->getBindings(100);" +
    		"$java_scriptname = @java_values($java_bindings['javax.script.filename']);"+
    		"if(!isset($argv)) $argv = @java_values($java_bindings['javax.script.argv']);"+
    		"if(!isset($argv)) $argv=array();\n"+
    		"$_SERVER['SCRIPT_FILENAME'] =  isset($java_scriptname) ? $java_scriptname : '';"+
    		"array_unshift($argv, $_SERVER['SCRIPT_FILENAME']);"+
    		"if (!isset($argc)) $argc = count($argv);"+
    		"$_SERVER['argv'] = $argv;"+
    		"?>");
    
    /** {@inheritDoc} */
    public CompiledScript compile(final Reader reader) throws ScriptException {
	try {
	    compilePhp(reader);
	    return new CompiledPhpScript(this);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    private String cachedSimpleStandardHeader;
    private ScriptContext ctxCache;
    private String cachedEmbeddedStandardHeader;

    /** {@inheritDoc} */
    protected ScriptContext getScriptContext(Bindings bindings) {
	return new PhpScriptContext(super.getScriptContext(bindings));
    }
    
    /** {@inheritDoc} */
    public ScriptContext getContext() {
	if (ctxCache == null) {
	    ctxCache = super.getContext();
	    if (!(ctxCache instanceof IPhpScriptContext)) {
		if (ctxCache == null) 
		    ctxCache = new SimpleScriptContext();
		ctxCache = new PhpScriptContext(ctxCache);
		super.setContext(ctxCache);
	    }
	}
	return ctxCache;
    }
    /** {@inheritDoc} */
    public void setContext(ScriptContext context) {
	super.setContext(context);
	this.ctxCache = null;
	getContext();
    }
    private String getSimpleStandardHeader(String filePath) {
        if (cachedSimpleStandardHeader != null) return cachedSimpleStandardHeader;
        StringBuffer buf = new StringBuffer(STANDARD_HEADER);
        buf.insert(20, filePath);
        return cachedSimpleStandardHeader = buf.toString();
    }

    /** {@inheritDoc} */
    public boolean accept(File outputFile) {
	this.compilerOutputFile = outputFile;
	return true;
    }

    private String getEmbeddedStandardHeader(String filePath) throws IOException {
        if (cachedEmbeddedStandardHeader!=null) return cachedEmbeddedStandardHeader;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Field f = Util.JAVA_INC.getField("bytes");
            byte[] buf = (byte[]) f.get(Util.JAVA_INC);
            out.write(buf);
    
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(STANDARD_HEADER_EMBEDDED);
            writer.close();
            return cachedEmbeddedStandardHeader = out.toString(Util.ASCII);
        } catch (Exception e) {
	    IOException ex = new IOException("Cannot create standard header");
	    ex.initCause(e);
	    throw ex;
        }
    }

    protected String getStandardHeader(String filePath) throws IOException {
        return filePath == null ? getEmbeddedStandardHeader(filePath):getSimpleStandardHeader(filePath);
    }
    
    /** {@inheritDoc} */
    public Object clone() {
	AbstractPhpScriptEngine other = (AbstractPhpScriptEngine) getFactory().getScriptEngine();
	other.isCompiled = isCompiled;
	other.compilerOutputFile = compilerOutputFile;
	return other;
    }
}
