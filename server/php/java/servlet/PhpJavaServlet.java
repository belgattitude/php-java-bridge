/*-*- mode: Java; tab-width:8 -*-*/

package php.java.servlet;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import php.java.bridge.JavaBridge;
import php.java.bridge.Request;
import php.java.bridge.Util;
import php.java.bridge.http.AbstractChannelName;
import php.java.bridge.http.ContextFactory;
import php.java.bridge.http.ContextServer;

/**
 * Handles requests from PHP clients.  <p> When Apache, IIS or php
 * (cli) or php-cgi is used as a front-end, this servlet handles PUT
 * requests and then re-directs to a private (socket- or pipe-)
 * communication channel.  This is the fastest mechanism to connect
 * php and java. It is even 1.5 times faster than local ("unix
 * domain") sockets used by the php.java.bridge.JavaBridge standalone
 * listener.  </p>
 * <p>
 * To enable fcg/servlet debug code start the servlet engine with -Dphp.java.bridge.default_log_level=6.
 * For example: <code>java -Dphp.java.bridge.default_log_level=6 -jar /opt/jakarta-tomcat-5.5.9/bin/bootstrap.jar</code>
 * </p>
 * <p>There cannot be more than one PhpJavaServlet instance per web application. If you extend from this class, make sure to change
 * the .phpjavabridge =&gt; PhpJavaServlet mapping in the WEB-INF/web.xml. </p>
 */
public /*singleton*/ class PhpJavaServlet extends HttpServlet {

    private static final long serialVersionUID = 3257854259629144372L;

    private ContextServer contextServer;
    protected int logLevel = -1;
    private Util.Logger logger;
    protected boolean promiscuous = false;

    // workaround for a bug in jboss server, which uses the log4j port 4445 for its internal purposes(!)
    private boolean isJBoss = false;

    protected int maxKeepAliveRequests;
    protected int keepAliveTimeout;
    private String keepAliveParam;

    /**@inheritDoc*/
    public void init(ServletConfig config) throws ServletException {
	maxKeepAliveRequests = ServletUtil.getMBeanProperty("*:type=Connector,port=8080", "maxKeepAliveRequests");
	keepAliveTimeout = ServletUtil.getMBeanProperty("*:type=Connector,port=8080", "keepAliveTimeout");
	StringBuffer buf = new StringBuffer("timeout=");
	buf.append(keepAliveTimeout);
	buf.append(", max=");
	buf.append(maxKeepAliveRequests);
	// use redirect if maxKeepAliveRequests and keepAliveTimeout are not set to infinity
	promiscuous = true;
	keepAliveParam = buf.toString();
	
 	String servletContextName=ServletUtil.getRealPath(config.getServletContext(), "");
	if(servletContextName==null) servletContextName="";
	ServletContext ctx = config.getServletContext();

	String value = ctx.getInitParameter("promiscuous");
	if(value==null) value="";
	value = value.trim();
	value = value.toLowerCase();
	
	if(value.equals("off") || value.equals("false")) promiscuous=false;
	if(value.equals("on") || value.equals("true")) promiscuous=true;
	contextServer = ContextLoaderListener.getContextLoaderListener(ctx).getContextServer();
    	 
    	super.init(config);
       
	String name = ctx.getServerInfo();
	if (name != null && (name.startsWith("JBoss")))    isJBoss    = true;

	logger = new Util.Logger(!isJBoss, new Logger());
    	Util.setDefaultLogger(logger);
    	
	if(Util.VERSION!=null)
    	    log("PHP/Java Bridge servlet "+servletContextName+" version "+Util.VERSION+" ready.");
	else
	    log("PHP/Java Bridge servlet "+servletContextName+" ready.");
	
    }

    /**{@inheritDoc}*/
    public void destroy() {
    	super.destroy();
    }
    /**
     * This hook can be used to create a custom context factory. The default implementation checks if there's a ContextFactory 
     * by calling ContextFactory.get(req.getHeader("X_JAVABRIDGE_CONTEXT"), credentials); 
     * If it doesn't exist, a new RemoteServletContextFactory is created.
     * This procedure should set the response header X_JAVABRIDGE_CONTEXT as a side effect.
     * @param req The HttpServletRequest
     * @param res The HttpServletResponse
     * @param credentials The provided credentials.
     * @return The (new) ServletContextFactory.
     */
    protected SimpleServletContextFactory getContextFactory(HttpServletRequest req, HttpServletResponse res) {
    	JavaBridge bridge;
	SimpleServletContextFactory ctx = null;
    	String id = req.getHeader(Util.X_JAVABRIDGE_CONTEXT);
    	if(id!=null) ctx = (SimpleServletContextFactory) ContextFactory.get(id);
    	if(ctx==null) {
    	  ctx = (SimpleServletContextFactory) RemoteServletContextFactory.addNew(this, getServletContext(), null, req, res); // no session sharing
    	  bridge = ctx.getBridge();
    	  bridge.logDebug("HTTP request");
    	} else {
    	    bridge = ctx.getBridge();
    	    bridge.logDebug("redirect");
    	}
    	updateRequestLogLevel(bridge);
    	res.setHeader(Util.X_JAVABRIDGE_CONTEXT, ctx.getId());
    	return ctx;
    }
    /**
     * Set the log level from the servlet into the bridge
     * @param bridge The JavaBridge from the ContextFactory.
     */
    protected void updateRequestLogLevel(JavaBridge bridge) {
	if(logLevel>-1) bridge.logLevel = logLevel;
    }

    /**
     * <p>
     * This hook can be used to suspend the termination of the servlet until the (Remote-)ServletContextFactory is finished.
     * It may be useful if one wants to access the Servlet, ServletContext or ServletRequest from a remote PHP script.
     * The notification comes from the php script when it is running as a sub component of the J2EE server or servlet engine.
     * </p>
     * <p>The default is to not wait for a local ServletContextFactory (the ContextFactory is passed from the PhpCGIServlet) 
     * and to wait RemoteContextFactory for 30 seconds.</p>
     * @param ctx The (Remote-) ContextFactory.
     */
    protected void waitForContext(SimpleServletContextFactory ctx) {
	try {
	    ctx.waitFor(Util.MAX_WAIT);
        } catch (InterruptedException e) {
	    Util.printStackTrace(e);
        }
    }

    /**
     * Handle a redirected connection. The local channel is more than 50 
     * times faster than the HTTP tunnel. Used by Apache and cgi.
     * 
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    protected void handleLocalConnection (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
	InputStream sin=null; ByteArrayOutputStream sout; OutputStream resOut = null;
	SimpleServletContextFactory ctx = getContextFactory(req, res);
	JavaBridge bridge = ctx.getBridge();
	ctx.setSessionFactory(req);
	
	bridge.in = sin=req.getInputStream();
	bridge.out = sout = new ByteArrayOutputStream();
	Request r = bridge.request = new Request(bridge);
	
	if(r.init(sin, sout)) {
	    AbstractChannelName channelName = contextServer.getChannelName(ctx);
	    res.setHeader("X_JAVABRIDGE_REDIRECT", channelName.getName());

	    // start the context runner before generating the first response
	    contextServer.start(channelName, logger);
	    	
	    // generate response
	    if(!r.handleOneRequest()) throw new IOException("parse error");

	    // redirect and re-open
	    res.setContentLength(sout.size());
	    resOut = res.getOutputStream();
	    sout.writeTo(resOut);
	    if(bridge.logLevel>3) bridge.logDebug("redirecting to port# "+ channelName);
	    sin.close();
	    try {res.flushBuffer(); } catch (Throwable t) {Util.printStackTrace(t);} // resin ignores resOut.close()
	    try {resOut.close(); } catch (Throwable t) {Util.printStackTrace(t);} // Sun Java System AS 9 ignores flushBuffer()
	    this.waitForContext(ctx);
	}
	else {
	    Util.warn("handleLocalConnection init failed");
	    ctx.destroy();
	}
    }
   
    /** Only for internal use */
    public static String getHeader(String key, HttpServletRequest req) {
  	String val = req.getHeader(key);
  	if(val==null) return null;
  	if(val.length()==0) val=null;
  	return val;
    }
    protected void handleHttpConnection (HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	RemoteHttpServletContextFactory ctx = new RemoteHttpServletContextFactory(this, getServletContext(), req, req, res);
	res.setHeader(Util.X_JAVABRIDGE_CONTEXT, ctx.getId());
	res.setHeader("Pragma", "no-cache");
	res.setHeader("Cache-Control", "no-cache");
	res.setHeader("Keep-Alive", keepAliveParam);
	
	try {
	    ctx.getBridge().handleRequests(req.getInputStream(), res.getOutputStream());
	} finally {
	    ctx.destroy();
	}
    }
    private static final String LOCAL_ADDR = "127.0.0.1"; 
    protected void handlePut (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

    	if(Util.logLevel>3) Util.logDebug("doPut:"+req.getRequestURL()); 
    	boolean isLocal = LOCAL_ADDR.equals(req.getRemoteAddr());
    	boolean isHttps = req.isSecure();
    	if(contextServer!=null && contextServer.isAvailable(null) && (isLocal || (!isLocal && promiscuous)) && !isHttps) 
    	    handleLocalConnection(req, res); /* re-direct */
    	else
    	    handleHttpConnection(req, res);
    }
    
    /**
     * Dispatcher for the "http tunnel", "local channel" or "override redirect".
     */
    protected void doPut (HttpServletRequest req, HttpServletResponse res) 
    	throws ServletException, IOException {
	try {
	    handlePut(req, res);
	} catch (RuntimeException e) {
	    Util.printStackTrace(e);
	    throw new ServletException(e);
	} catch (IOException e) {
	    Util.printStackTrace(e);
	    throw e;
	} catch (ServletException e) {
	    Util.printStackTrace(e);
	    throw e;
	}
    }

    /** For backward compatibility */
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
      		String uri = req.getRequestURI();
     		req.getRequestDispatcher(uri.substring(0, uri.length()-10)).forward(req, res);
    }
}
