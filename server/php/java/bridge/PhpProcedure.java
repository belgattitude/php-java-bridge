/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge;

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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

/**
 * This class takes the supplied PHP environment and creates a dynamic
 * proxy for calling PHP code.
 */
public final class PhpProcedure implements InvocationHandler {
	
    private IJavaBridgeFactory bridge;
    private long object;
    private Map names;
    protected String name;
    protected PhpProcedure(IJavaBridgeFactory bridge, long object, String name, Map names) {
	this.bridge = bridge;
	this.object = object;
	this.names = names;
	this.name = name;
    }

    /**
     * Called from java_closure().
     * @param bridge - The request handling bridge
     * @param name - The name, e.g. java_closure($this, "alwaysCallMe")
     * @param names - A map of names, e.g. java_closure($this, array("javaName1" => "php_name1", ...);
     * @param interfaces - The list of interfaces that this proxy must implement, may be empty. E.g. java_closure($this, null, null, array(new Java("java.awt.event.ActionListener"));
     * @param object - An opaque object ID (protocol-level).
     * @return A new proxy instance.
     */
    protected static Object createProxy(IJavaBridgeFactory bridge, String name, Map names, Class interfaces[], long object) {
	PhpProcedure handler = new PhpProcedure(bridge, object, name, names);
	ClassLoader loader = Util.getContextClassLoader();

	Object proxy = Proxy.newProxyInstance(loader, interfaces, handler);
	return proxy;
    }
    /**
     * Called from getInterface().
     * @param interfaces - The list of interfaces that this proxy must implement, may be empty. 
     * @param proc - A procedure obtained from java_closure().
     * @return A new proxy instance.
     */
    public static Object createProxy(Class interfaces[], PhpProcedure proc) {
	return createProxy (proc.bridge, proc.name, proc.names, interfaces, proc.object);
    }
	
    private Object invoke(Object proxy, String method, Class returnType, Object[] args) throws Throwable {
	JavaBridge bridge = this.bridge.getBridge();
	if(bridge.logLevel>3) bridge.logDebug("invoking callback: " + method);
	String cname;
	if(name!=null) {
	    cname=name;
	} else {
	    cname = (String)names.get(method);
	    if(cname==null) cname=method;
	}
	bridge.request.response.setResultProcedure(object, cname, method, args);
	Object[] result = bridge.request.handleSubRequests();
	if(bridge.logLevel>3) bridge.logDebug("result from cb: " + Arrays.asList(result));
	return bridge.coerce(returnType, result[0], bridge.request.response);
    }

    private void checkPhpContinuation() throws IllegalStateException {
	if (bridge.isNew())
	    throw new IllegalStateException ("Cannot call closure anymore: the closed-over PHP script continuation has been terminated."); 
    }
    /**
     * Invoke a PHP function or a PHP method.
     * @param proxy The php environment or the PHP object
     * @param method The php method name
     * @param args the arguments
     * @return the result or null.
     * @throws Throwable script exception.
     */
    public Object invoke(Object proxy, String method, Object[] args) throws Throwable {
	checkPhpContinuation();
	
	return invoke(proxy, method, Object.class, args);
    }

    /**{@inheritDoc}*/
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	checkPhpContinuation ();
	
	return invoke(proxy, method.getName(), method.getReturnType(), args);
    }
    
    static long unwrap (Object ob) {
	InvocationHandler handler = Proxy.getInvocationHandler(ob);
	PhpProcedure proc = (PhpProcedure)handler;
	return proc.object;
    }
}
