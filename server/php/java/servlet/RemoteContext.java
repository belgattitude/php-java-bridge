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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import php.java.bridge.http.IContext;


/**
 * A custom context, used when remote PHP scripts access the servlet. In this case the HttpServletRequest, HttpServletResponse and ServletContext
 * objects are not available. However, the session object <em>is</em> available through the RemoteHttpSession.
 * 
 * @author jostb
 *
 */
public class RemoteContext extends HttpContext {
    protected RemoteContext(ServletContext kontext, HttpServletRequest req, HttpServletResponse res) {
	super(kontext, req, res);
    }
    /**
     * Return the response object
     * @return The response
     */
    public Object getHttpServletResponse() {
	return getAttribute(IContext.SERVLET_RESPONSE);
    }
    /**
     * Return the request object
     * @return The request
     */
    public Object getHttpServletRequest() {
	return getAttribute(IContext.SERVLET_REQUEST);
    }
    /**
     * Return the servlet
     * @return the servlet
     */
    public Object getServlet() {
	return getAttribute(IContext.SERVLET);
    }
    /**
     * Return the servlet config
     * @return the servlet config
     */
     public Object getServletConfig() {
 	return getAttribute(IContext.SERVLET_CONFIG);
     }
     /**
      * Return the servlet context
      * @return the servlet context
      */
      public Object getServletContext() {
  	return getAttribute(IContext.SERVLET_CONTEXT);
      }
}
