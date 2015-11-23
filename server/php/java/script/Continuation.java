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
import java.io.OutputStream;
import java.util.Map;

import php.java.bridge.Util;
import php.java.bridge.http.HeaderParser;

/**
 * This class represents the logic to run PHP scripts through CGI, FastCGI or on a remote HTTP server
 * (accessed through URLReader).
 *  
 * @author jostb
 *
 * @see php.java.bridge.http.HttpServer
 * @see php.java.script.URLReader
 * @see php.java.script.HttpProxy
 */

public abstract class Continuation implements IContinuation, Runnable {
	
    protected Map env;
    protected OutputStream out, err;
 //   protected Reader reader;
    protected HeaderParser headerParser;
    protected ResultProxy resultProxy;
    
    private ScriptLock scriptLock = new ScriptLock();
    private Lock phpScript = new Lock();
    // used to wait for the script to terminate
    private static class ScriptLock {
	    private boolean running = true;
	    public synchronized void waitForRunner () throws InterruptedException {
		    if (running)
                        wait();
	    }
	    public synchronized void finish () {
		    running = false;
		    notify();
	    }
    }
    
    // used to wait for the cont.call(cont) call from the script
    protected class Lock {
	private Object val = null;
	private boolean finish = false;
		
	public synchronized Object getVal() {
	    if(!finish && val==null)
		try {
		    wait();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    return val;
	}
	public synchronized void setVal(Object val) {
	    this.val = val;
	    notify();
	}
	public synchronized void finish() {
	    finish = true;
	    notify();
	}
    }
    protected Continuation(Map env, OutputStream out, OutputStream err, HeaderParser headerParser, ResultProxy resultProxy) {
	this.env = env;
	this.out = out;
	this.err = err;
	this.headerParser = headerParser;
	this.resultProxy = resultProxy;
    }
    public void run() {
	try {
	    doRun();
	} catch (IOException e) {
	    phpScript.val = e;
	} catch (Util.Process.PhpException e1) {
	    phpScript.val = e1;	    
	} catch (Exception ex) {
	    phpScript.val = ex;
	    Util.printStackTrace(ex);
        } finally {
	    phpScript.finish();
	    scriptLock.finish();
	}
    }
    protected abstract void doRun() throws IOException, Util.Process.PhpException;
    
    private Object lockObject = new Object();
    /**
     * The PHP script must call this function with the current
     * continuation as an argument.<p>
     * 
     * Example:<p>
     * <code>
     * java_context()-&gt;call(java_closure());<br>
     * </code>
     * @param script - The php continuation
     * @throws InterruptedException
     */
    public void call(Object script) throws InterruptedException {
	synchronized (lockObject) {
	    phpScript.setVal(script);
	    lockObject.wait();
	}
    }
	
    /**
     * One must call this function if one is interested in the php continuation.
     * @return The php continuation.
     * @throws Exception 
     */
    public Object getPhpScript() throws Exception {
        Object val = phpScript.getVal(); 
        if (val instanceof Exception) throw (Exception)val; 
        return val;
    }

    /**
     * This function must be called to release the allocated php continuation.
     * Note that simply calling this method does not guarantee that
     * the script is finished, as the ContextRunner may still produce output.
     * Use contextFactory.waitFor() to wait for the script to terminate.
     * @throws InterruptedException 
     *
     */
    public void release() throws InterruptedException {
	    /* Release the cont.call(cont) from PHP. After that the PHP script may terminate */
	    synchronized (lockObject) {
		lockObject.notifyAll();
	    }
	    scriptLock.waitForRunner();
    }
}
