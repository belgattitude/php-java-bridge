/*-*- mode: Java; tab-width:8 -*-*/
package php.java.bridge.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Map;

import php.java.bridge.ILogger;
import php.java.bridge.Util;
import php.java.bridge.Util.Process;

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

/**
 * A factory which creates FastCGI channels.
 * @author jostb
 */
public abstract class FCGIConnectionFactory {
    protected boolean promiscuous;
    protected IFCGIProcessFactory processFactory;
    
    /* The fast CGI Server process on this computer. Switched off per default. */
    protected IFCGIProcess proc = null;
    private boolean fcgiStarted = false;
    private final Object fcgiStartLock = new Object();
    protected Exception lastException;
    
    /**
     * Create a new FCGIConnectionFactory using a FCGIProcessFactory
     * @param processFactory the FCGIProcessFactory
     */
    public FCGIConnectionFactory(IFCGIProcessFactory processFactory) {
	this.processFactory = processFactory;
    }
    /**
     * Start the FastCGI server
     * @return false if the FastCGI server failed to start.
     */
    public final boolean startServer(ILogger logger) {
	/*
	 * Try to start the FastCGI server,
	 */
	synchronized(fcgiStartLock) {
	    if(!fcgiStarted) {
		    if(canStartFCGI()) 
			try {
			    bind(logger);
			} catch (Exception e) {/*ignore*/}
		
		fcgiStarted = true; // mark as started, even if start failed
	    } 
	}
	return fcgiStarted;
    }
    /**
     * Test the FastCGI server.
     * @throws FCGIConnectException thrown if a IOException occured.
     */
    public abstract void test() throws FCGIConnectException;
    
    protected abstract void waitForDaemon() throws UnknownHostException, InterruptedException;
    protected final void runFcgi(Map env, String php, boolean includeJava) {
	int c;
	byte buf[] = new byte[Util.BUF_SIZE];
	try {
	    Process proc = doBind(env, php, includeJava);
	    if(proc==null || proc.getInputStream() == null) return;
	    /// make sure that the wrapper script launcher.sh does not output to stdout
	    proc.getInputStream().close();
	    // proc.OutputStream should be closed in shutdown, see PhpCGIServlet.destroy()
	    InputStream in = proc.getErrorStream();
	    while((c=in.read(buf))!=-1) System.err.write(buf, 0, c);
	    try { in.close(); } catch (IOException e) {/*ignore*/}
	} catch (Exception e) {
	    lastException = e;
	    System.err.println("Could not start FCGI server: " + e);
	};
    }

    protected abstract Process doBind(Map env, String php, boolean includeJava) throws IOException;
    protected void bind(final ILogger logger) throws InterruptedException, IOException {
	Thread t = (new Util.Thread("JavaBridgeFastCGIRunner") {
		public void run() {
		    Map env = (Map) processFactory.getEnvironment().clone();
		    env.put("PHP_FCGI_CHILDREN", processFactory.getPhpConnectionPoolSize());
		    env.put("PHP_FCGI_MAX_REQUESTS", processFactory.getPhpMaxRequests());
		    runFcgi(env, processFactory.getPhp(), processFactory.getPhpIncludeJava());
		}
	    });
	t.start();
	waitForDaemon();
    }

    private boolean canStartFCGI() {
	return processFactory.canStartFCGI();
    }
	
    public void destroy() {
	synchronized(fcgiStartLock) {
	    fcgiStarted = false;
	    if(proc==null) return;  	
	    try {
		OutputStream out = proc.getOutputStream();
		if (out != null) out.close();
	    } catch (IOException e) {
		Util.printStackTrace(e);
	    }
	    try {
		proc.waitFor();
	    } catch (InterruptedException e) {
		// ignore
	    }
	    proc.destroy();
	    proc=null;
	}
    }

    /**
     * Connect to the FastCGI server and return the connection handle.
     * @return The FastCGI Channel
     * @throws FCGIConnectException thrown if a IOException occured.
     */
    public abstract FCGIConnection connect() throws FCGIConnectException;

    /**
     * For backward compatibility the "JavaBridge" context uses the port 9667 (Linux/Unix) or <code>\\.\pipe\JavaBridge@9667</code> (Windogs).
     */
    public void initialize() {
	setDynamicPort();
    }
    protected abstract void setDynamicPort();
    protected abstract void setDefaultPort();

    /**
     * Return a command which may be useful for starting the FastCGI server as a separate command.
     * @param base The context directory
     * @param php_fcgi_max_requests The number of requests, see appropriate servlet option.
     * @return A command string
     */
    public abstract String getFcgiStartCommand(String base, String php_fcgi_max_requests);
	
    /**
     * Find a free port or pipe name. 
     * @param select If select is true, the default name should be used.
     */
    public abstract void findFreePort(boolean select);

    /**
     * Create a new ChannelFactory.
     * @return The concrete ChannelFactory (NP or Socket channel factory).
     */
    public static FCGIConnectionFactory createChannelFactory(IFCGIProcessFactory processFactory, boolean promiscuous) {
	if(Util.USE_SH_WRAPPER)
	    return new SocketChannelFactory(processFactory, promiscuous);
	else 
	    return new NPChannelFactory(processFactory);
    }
    public abstract String toString();
}