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

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import php.java.bridge.ISession;
import php.java.bridge.http.IContext;

/**
 * Create session contexts for servlets.<p> In addition to the
 * standard ContextFactory this factory keeps a reference to the
 * HttpServletRequest.
 *
 * @see php.java.bridge.http.ContextFactory
 * @see php.java.bridge.http.ContextServer
 */
public class SimpleServletContextFactory extends php.java.bridge.http.SimpleContextFactory {
    protected HttpServletRequest proxy, req;
    protected HttpServletResponse res;
    protected ServletContext kontext;
    protected Servlet servlet;

    protected SimpleServletContextFactory(Servlet servlet, ServletContext ctx, HttpServletRequest proxy, HttpServletRequest req, HttpServletResponse res, boolean isManaged) {
    	super(ServletUtil.getRealPath(ctx, ""), isManaged);
    	this.kontext = ctx;
    	this.proxy = proxy;
    	this.req = req;
    	this.res = res;
    	this.servlet = servlet;
    }
    
    /**
     * Set the HttpServletRequest for session sharing. This implementation does nothing, the proxy must have been set in the constructor.
     * @see php.java.servlet.RemoteServletContextFactory#setSessionFactory(HttpServletRequest) 
     * @param req The HttpServletRequest
     */
    protected void setSessionFactory(HttpServletRequest req) {
    }
    /**{@inheritDoc}*/
    public ISession getSimpleSession(String name, boolean clientIsNew,
            int timeout) {
	throw new IllegalStateException("Named sessions not supported by servlet.");
    }
    /**{@inheritDoc}*/
    public ISession getSession(String name, short clientIsNew, int timeout) {
	 // if name != null return a "named" php session which is not shared with jsp
	if(name!=null) return getSimpleSession(name, clientIsNew, timeout);
	
	if(session != null) return session;

	if(proxy==null) throw new NullPointerException("This context "+getId()+" doesn't have a session proxy.");
	return session = HttpSessionFacade.getFacade(this, kontext, proxy, res, clientIsNew, timeout);
    }
    
    /**{@inheritDoc}*/
    public synchronized void destroy() {
    	super.destroy();
    	proxy=null;
    }
    /**
     * Return an emulated JSR223 context.
     * @return The context.
     * @see php.java.servlet.HttpContext
     */
    public IContext createContext() {
	IContext ctx = new HttpContext(kontext, req, res);
	ctx.setAttribute(IContext.SERVLET_CONTEXT, kontext, IContext.ENGINE_SCOPE);
	ctx.setAttribute(IContext.SERVLET_CONFIG, servlet.getServletConfig(), IContext.ENGINE_SCOPE);
	ctx.setAttribute(IContext.SERVLET, servlet, IContext.ENGINE_SCOPE);

	ctx.setAttribute(IContext.SERVLET_REQUEST, req, IContext.ENGINE_SCOPE);
	ctx.setAttribute(IContext.SERVLET_RESPONSE, new SimpleHttpServletResponse(res), IContext.ENGINE_SCOPE);
	return ctx;
    }
    /** Only for internal use */
    public static void throwJavaSessionException() {
	throw new IllegalStateException ("Cannot call java_session() anymore. Response headers already sent! java_session() must be called at the beginning of the php script. Please add \"java_session();\" to the beginning of your PHP script.");
    }

    /**
     * Return the http session handle;
     * @throws IllegalStateException if java_session has not been called at the beginning of the PHP script
     * @return The session handle
     */
    public HttpSession getSession() {
	if(session!=null) return ((HttpSessionFacade)session).getCachedSession();
	throwJavaSessionException();
	return null;
    }
}
