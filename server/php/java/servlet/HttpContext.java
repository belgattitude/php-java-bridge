/*-*- mode: Java; tab-width:8 -*-*/

package php.java.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import php.java.bridge.NotImplementedException;
import php.java.bridge.Util;
import php.java.bridge.http.IContext;

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
 * A custom context which keeps the HttpServletResponse. Used when JSR223 is not available.
 * 
 * @author jostb
 *
 */
public class HttpContext extends php.java.bridge.http.Context {
    protected HttpServletResponse response;
    protected ServletContext context;
    protected HttpServletRequest request;

  /**{@inheritDoc}*/  
  public Object getAttribute(String name) throws IllegalArgumentException{
	Object result;
	if (name == null) {
	    throw new IllegalArgumentException("name cannot be null");
	}
	          
	if ((getEngineScope()!=null) && (result=getEngineScope().get(name)) != null) {
	    return result;
	} else if ((getGlobalScope()!=null) && (result=getGlobalScope().get(name)) != null) {
	    return result;
	} else if ((result=request.getAttribute(name)) != null)  {
	    return result;
	} else if ((result=request.getSession().getAttribute(name)) != null)  {
	    return result;
	} else if ((result=context.getAttribute(name)) != null) {
	    return result;
	}
	return null;
  }
	
    /**
     * Create a new context.
     * @param kontext The servlet context
     * @param req The servlet request
     * @param res The HttpServletResponse
     */
    public HttpContext(ServletContext kontext, HttpServletRequest req, HttpServletResponse res) {
      this.context = kontext;
      this.response = res;
      this.request = req;
    }
	
    /**{@inheritDoc}*/  
    public Writer getWriter() throws IOException {
	return response.getWriter();
    }
    /**
     * Return the http servlet response
     * @return The http servlet reponse
     */
     public Object getHttpServletResponse() {
 	return getAttribute(IContext.SERVLET_RESPONSE);
     }
     /**
      * Return the http servlet request
      * @return The http servlet request
      */
     public Object getHttpServletRequest() {
 	return getAttribute(IContext.SERVLET_REQUEST);
     }
     /**
      * Return the http servlet
      * @return The http servlet
      */
     public Object getServlet() {
 	return getAttribute(IContext.SERVLET);
     }
     /**
      * Return the servlet config
      * @return The servlet config
      */
      public Object getServletConfig() {
  	return getAttribute(IContext.SERVLET_CONFIG);
      }
      /**
       * Return the servlet context
       * @return The servlet context
       */
       public Object getServletContext() {
   	return getAttribute(IContext.SERVLET_CONTEXT);
       }

       /**
        * Only for internal use. <br><br>
        * Used when scripts are running within of a servlet environment:
        * Either php.java.servlet.Context or the JSR223 Context (see PhpSimpleHttpScriptContext).<br>
        * Outside of a servlet environment use the ContextLoaderListener instead:
        * Either the Standalone or the JSR223 Standalone (see PhpScriptContext).
        * @param closeable The manageable beforeShutdown(), will be called by the {@link ContextLoaderListener#contextDestroyed(javax.servlet.ServletContextEvent)}
        * @param ctx The ServletContext
        */
       public static void handleManaged(Object closeable, ServletContext ctx) {
	 List list = (List) ContextLoaderListener.getContextLoaderListener(ctx).getCloseables();
	 list.add(closeable);
       }
       /**{@inheritDoc}*/
       public Object init(Object callable) throws Exception {
	 if(Util.logLevel>3) Util.logDebug("calling servlet context init");
	 return php.java.bridge.http.Context.getManageable(callable);
       }
       /**{@inheritDoc}*/
       public void onShutdown(Object closeable) {
	 if(Util.logLevel>3) Util.logDebug("calling servlet context register shutdown ");
	 handleManaged(closeable, context);
       }

       /** Only for internal use
        * @param path the path
        * @param ctx the servlet context 
        * @return the real path
        * @deprecated Use {@link php.java.servlet.ServletUtil#getRealPath(ServletContext, String)}
        */
       public static String getRealPathInternal(String path, ServletContext ctx) {
	   return ServletUtil.getRealPath(ctx, path);
       }
       /**{@inheritDoc}*/
       public String getRealPath(String path) {
   	return ServletUtil.getRealPath(context, path);
       }
       /**@deprecated*/
       public String getRedirectString() {
	   throw new NotImplementedException();
       }
       /**@deprecated*/
       public String getRedirectString(String webPath) {
	   throw new NotImplementedException();
       }

       /**{@inheritDoc}*/
       public String getSocketName() {
   	return String.valueOf(ServletUtil.getLocalPort(request));
       }
}
