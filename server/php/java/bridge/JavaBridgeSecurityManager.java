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

import java.security.Permission;

/**
 * A custom security manager for the PHP/Java Bridge.
 * 
 * Example:<br>
 * <code> PHP_HOME=/usr/lib/php/modules java -Dphp.java.bridge.base=${PHP_HOME} -Djava.security.policy=${PHP_HOME}/javabridge.policy -jar ${PHP_HOME}/JavaBridge.jar </code>
 * <br>
 * Example options for eclipse:<br>
 * <code>
 * -Dphp.java.bridge.base=${workspace_loc}${project_path}/<br>
 * -Djava.security.policy=${workspace_loc}${project_path}/javabridge.policy
 * </code><br>
 * @author jostb
 *
 */
public class JavaBridgeSecurityManager extends SecurityManager {
    protected static final Permission MODIFY_THREADGROUP_PERMISSION = new RuntimePermission("modifyThreadGroup");
    protected static final Permission MODIFY_THREAD_PERMISSION = new RuntimePermission("modifyThread");

    /**
     * @inheritDoc 
     * Internal groups may pass, user groups are checked against the <code>javabridge.policy</code> file.
     */
    public void checkAccess(ThreadGroup g) {
	if (g == null) {
	    throw new NullPointerException("thread group can't be null");
	}
	// one of our request-handling thread groups, check only if called from an application thread
	if((g instanceof AppThreadPool.Group) && ((AppThreadPool.Group)g).isLocked) 
	    checkPermission(MODIFY_THREADGROUP_PERMISSION);
	// an application thread group, check this one
	else if(g instanceof AppThreadPool.AppGroup) 
	    checkPermission(MODIFY_THREADGROUP_PERMISSION);
	// a system thread group
	// disabled: Sun jdk1.5 calls checkAccess from a system thread
	// running with our(!) privileges:
        // at java.lang.ThreadGroup.checkAccess(ThreadGroup.java:288)
        // at java.lang.Thread.init(Thread.java:310)
        // at java.lang.Thread.<init>(Thread.java:429)
        // at sun.misc.Signal.dispatch(Signal.java:199)
	// This is probably a bug. 
	// However, this means that we must not check system thread groups. 
	// If we do, dispatch will fail and the VM cannot
	// react to signals like SIGTERM or Control-C. 
	//else super.checkAccess(g);
    }
    /**
     * All user threads belong to the "JavaBridgeThreadPoolAppGroup" and all internal threads
     * to "JavaBridgeThreadPoolGroup".
     * @return The current thread group
     */
    public ThreadGroup getThreadGroup() {
        try {
            AppThreadPool.Delegate delegate = (AppThreadPool.Delegate)Thread.currentThread();
            return delegate.getAppGroup();
        } catch (ClassCastException e) {
            // must be a system thread
            return super.getThreadGroup();
        }
    }
    /**
     * @inheritDoc
     * <code>System.exit(...)</code> can by switched off by removing 
     * <code>permission java.lang.RuntimePermission "exitVM";</code> 
     * from the policy file.
     */
    public void checkExit(int status) {      
	//super.checkExit(status);

	// from Sun's Launcher.java
	// protected PermissionCollection getPermissions(CodeSource codesource)
	// {
        // PermissionCollection perms = super.getPermissions(codesource);
        // perms.add(new RuntimePermission("exitVM"));
        //return perms;
	// so switch off exitVM once and for all:
	
	throw new SecurityException("exitVM disabled by JavaBridgeSecurityManager.java");
    }
}
