/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

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
import java.io.Writer;
import java.util.Map;

import javax.script.ScriptContext;


/**
 * Interface which all contexts must implement. Used when the JSR223 interface is not available.
 * @author jostb
 *
 */
public interface IContext {

    /**
     * The engine scope
     */
    static final int ENGINE_SCOPE = ScriptContext.ENGINE_SCOPE;

    /**
     * The global scope
     */
    static final int GLOBAL_SCOPE = ScriptContext.GLOBAL_SCOPE;

    /**
     * This key can be used to get the current continuation.
     * Example: <code>java_context-&gt;getAttribute("php.java.bridge.PhpProcedure");</code>
     */
    public static final String PHP_PROCEDURE = "php.java.bridge.PhpProcedure";
    /**
     * This key can be used to get the current JavaBridge instance.
     * Example: <code>java_context-&gt;getAttribute("php.java.bridge.JavaBridge");</code>
     */
    public static final String JAVA_BRIDGE = "php.java.bridge.JavaBridge";
    /**
     * This key can be used to get the current ServletContext instance.
     * Example: <code>java_context-&gt;getAttribute("php.java.servlet.ServletContext");</code>
     */
    public static final String SERVLET_CONTEXT = "php.java.servlet.ServletContext";
    /**
     * This key can be used to get the current ServletConfig instance.
     * Example: <code>java_context-&gt;getAttribute("php.java.servlet.ServletConfig");</code>
     */
    public static final String SERVLET_CONFIG = "php.java.servlet.ServletConfig";
    /**
     * This key can be used to get the current Servlet instance.
     * Example: <code>java_context-&gt;getAttribute("php.java.servlet.Servlet");</code>
     */
    public static final String SERVLET = "php.java.servlet.Servlet";
    /**
     * This key can be used to get the current HttpServletRequest instance.
     * Example: <code>java_context-&gt;getAttribute("php.java.servlet.HttpServletRequest");</code>
     */
    public static final String SERVLET_REQUEST = "php.java.servlet.HttpServletRequest";
    /**
     * This key can be used to get the current HttpServletResponse instance.
     * Example: <code>java_context-&gt;getAttribute("php.java.servlet.HttpServletResponse");</code>
     */
    public static final String SERVLET_RESPONSE = "php.java.servlet.HttpServletResponse";
    
    /**
     * Retrieves the value for getAttribute(String, int) for the 
     * lowest scope in which it returns a non-null value.
     * 
     * @param name the name of the attribute 
     * @return the value of the attribute
     * @throws IllegalArgumentException 
     */
    public abstract Object getAttribute(String name)
	    throws IllegalArgumentException;

    /**
     * Retrieves the value associated with specified name in the 
     * specified level of scope. Returns null if no value is 
     * associated with specified key in specified level of scope.
     *  
     * @param name the name of the attribute
     * @param scope the level of scope
     * @return the value value associated with the specified name in
     *         specified level of scope
     * @throws IllegalArgumentException 
     */
    public abstract Object getAttribute(String name, int scope)
	    throws IllegalArgumentException;

    /**
     * Retrieves the lowest value of scopes for which the attribute 
     * is defined. If there is no associate scope with the given 
     * attribute (-1) is returned.
     * 
     * @param  name the name of attribute
     * @return the value of level of scope  
     */
    public abstract int getAttributesScope(String name);

    /**
     * Retrieves an instance of java.io.Writer which can be used by 
     * scripts to display their output.
     * 
     * @return an instance of java.io.Writer
     * @throws IOException 
     */
    public abstract Writer getWriter() throws IOException;

    /**
     * Removes the specified attribute form the specified level of 
     * scope.
     * 
     * @param name the name of the attribute
     * @param scope the level of scope 
     * @return value which is removed
     * @throws IllegalArgumentException
     */
    public abstract Object removeAttribute(String name, int scope)
	    throws IllegalArgumentException;

    /**
     * Sets an attribute specified by the name in specified level of 
     * scope.
     *  
     * @param name   the name of the attribute
     * @param value the value of the attribute
     * @param scope the level of the scope
     * @throws IllegalArgumentException if the name is null scope is
     *         invlaid
     */
    public abstract void setAttribute(String name, Object value, int scope)
	    throws IllegalArgumentException;
    
    /**
     * Return the http servlet response
     * @return The http servlet reponse
     */
     public Object getHttpServletResponse();
     
     /**
      * Return the http servlet request
      * @return The http servlet request
      */
     public Object getHttpServletRequest();
     
     /**
      * Return the http servlet
      * @return The http servlet
      */
     public Object getServlet();
     
     /**
      * Return the servlet config
      * @return The servlet config
      */
      public Object getServletConfig();
      
      /**
       * Return the servlet context
       * @return The servlet context
       */
       public Object getServletContext();
       
       /**
        * Get the full file system path for the given resource.
        * @param path the relative path to an existing resource
        * @return the file system path
        */
       public String getRealPath(String path);
       
       /**
        * Add a new binding to the engine scope
        * @param key the key
        * @param val the value
        */
       public void put(String key, Object val);

       /**
        * Get a binding from the engine scope
        * @param key the key
        * @return the value
        */
       public Object get(String key);
       
       /**
        * Remove a bindings from the engine scope
        * @param key the key
        */
       public void remove(String key);
       
       /**
        * Put all bindings to the engine scope
        * @param map the map
        */
       public void putAll(Map map);
       
       /**
        * Get all bindings from the engine scope
        * @return the map
        */
       public Map getAll();
       /**
        * Return the redirect string, for example http://localhost:8080/webPath
        * Used by the non-compiling script engines to hard-code the
        * back-end URL in the Java.inc
        * @param webPath Usually request.getContextPath()+request.getServletPath()
        * @return the redirect string
        */
       public String getRedirectURL(String webPath);
       /**
	* @deprecated Use {@link #getRedirectURL(String)}
       */
       public String getRedirectString(String webPath);
       /**
	* @deprecated Use {@link #getRedirectURL(String)}
        */
       public String getRedirectString();

       /**
        * Return the socket name, for example 8080
        * @return the socket name
        */
       public String getSocketName();
}