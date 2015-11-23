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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import php.java.bridge.ISession;
import php.java.bridge.http.IContextFactory;

/**
 * Wraps the J2EE session interface
 */
final class HttpSessionFacade implements ISession {

    private HttpSession session;
    private int timeout;
    private HttpSession sessionCache=null;
    private boolean isNew;
    private IContextFactory ctxFactory;
    
    HttpSession getCachedSession() {
	if(sessionCache!=null) return sessionCache;
	sessionCache = session;
	sessionCache.setMaxInactiveInterval(timeout);
	return sessionCache;
    }
    private HttpSessionFacade (IContextFactory ctxFactory, ServletContext ctx, HttpServletRequest req, HttpServletResponse res, short clientIsNew, int timeout) {
	this.ctxFactory = ctxFactory;
	switch (clientIsNew) {
		case ISession.SESSION_CREATE_NEW: 	this.session = req.getSession(true); 	break;
		case ISession.SESSION_GET_OR_CREATE: 	this.session = req.getSession(); 	break;
		case ISession.SESSION_GET: 		this.session = req.getSession(false); 	break;
		default: 				throw new IllegalStateException("clientIsNew:"+clientIsNew);
	}
	this.timeout = timeout;
	this.isNew = session.isNew();
    }
    
    /**
     * Returns the HttpServletRequest
     * @return The HttpServletRequest.
     * Use {@link HttpContext#getHttpServletRequest()}
     */
    public HttpServletRequest getHttpServletRequest() {
    	return (HttpServletRequest) ((HttpContext)ctxFactory.getContext()).getHttpServletRequest();
    }
    
    /**
     * Returns the ServletContext
     * @return The ServletContext.
     *  Use {@link HttpContext#getServletContext()}
     */
    public ServletContext getServletContext() {
        return (ServletContext) ((HttpContext)ctxFactory.getContext()).getServletContext();
    }
    
    /**
     * Returns the ServletResponse
     * @return The ServletResponse.
     *  Use {@link HttpContext#getHttpServletResponse()}
     */
    public HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) ((HttpContext)ctxFactory.getContext()).getHttpServletResponse();
    }
    /**@inheritDoc*/
    public Object get(Object ob) {
	return getCachedSession().getAttribute(String.valueOf(ob));
    }

    /**@inheritDoc*/
    public void put(Object ob1, Object ob2) {
	getCachedSession().setAttribute(String.valueOf(ob1), ob2);
    }

    /**@inheritDoc*/
   public Object remove(Object ob) {
	String key = String.valueOf(ob);
	Object o = getCachedSession().getAttribute(key);
	if(o!=null)
	    getCachedSession().removeAttribute(key);
	return o;
    }

   /**@inheritDoc*/
    public void setTimeout(int timeout) {
	getCachedSession().setMaxInactiveInterval(timeout);
    }

    /**@inheritDoc*/
    public int getTimeout() {
	return getCachedSession().getMaxInactiveInterval();
    }

    /**@inheritDoc*/
    public int getSessionCount() {
	return -1;
    }

    /**@inheritDoc*/
    public boolean isNew() {
	return isNew;
    }

    /**@inheritDoc*/
    public void destroy() {
	getCachedSession().invalidate();
    }

    /**@inheritDoc*/
    public void putAll(Map vars) {
	for(Iterator ii = vars.keySet().iterator(); ii.hasNext();) {
	    Object key = ii.next();
	    Object val = vars.get(key);
	    put(key, val);
	}
    }

    /**@inheritDoc*/
    public Map getAll() {
	HttpSession session = getCachedSession();
	HashMap map = new HashMap();
	for(Enumeration ee = session.getAttributeNames(); ee.hasMoreElements();) {
	    Object key = ee.nextElement();
	    Object val = get(key);
	    map.put(key, val);
	}
	return map;
    }
    /**@inheritDoc*/
    public long getCreationTime() {
      return getCachedSession().getCreationTime();
    }
    /**@inheritDoc*/
    public long getLastAccessedTime() {
      return getCachedSession().getLastAccessedTime();
    }
    public static ISession getFacade(
	    IContextFactory factory,
            ServletContext kontext, HttpServletRequest req,
            HttpServletResponse res, short clientIsNew, int timeout) {
	
	if ((clientIsNew != ISession.SESSION_GET) || ((clientIsNew == ISession.SESSION_GET) && req.getSession(false) != null))
	    return new HttpSessionFacade(factory, kontext, req, res, clientIsNew, timeout);
	else
	    return null;
    }
}
