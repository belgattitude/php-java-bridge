/*-*- mode: Java; tab-width:8 -*-*/

package php.java.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import php.java.bridge.ILogger;
import php.java.bridge.ThreadPool;
import php.java.bridge.Util;
import php.java.bridge.http.ContextServer;
import php.java.bridge.http.FCGIConnectException;
import php.java.bridge.http.FCGIConnection;
import php.java.bridge.http.FCGIConnectionFactory;
import php.java.bridge.http.FCGIConnectionPool;
import php.java.bridge.http.FCGIIOFactory;
import php.java.bridge.http.FCGIInputStream;
import php.java.bridge.http.FCGIOutputStream;
import php.java.bridge.http.FCGIUtil;
import php.java.bridge.http.IFCGIProcess;
import php.java.bridge.http.IFCGIProcessFactory;
import php.java.servlet.fastcgi.FCGIProcess;

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
 * Register the PHP/Java Bridge when the web context starts. Used by java_context()->onShutdown(proc). The 
 * WEB-INF/web.xml contains a listener attribute:
 * <blockquote>
 * <code>
 * &lt;listener&gt;
 * &nbsp;&nbsp;&lt;listener-class&gt;php.java.servlet.ContextLoaderListener&lt;/listener-class&gt;
 *&lt;/listener&gt;
 * </code>
 * </blockquote>
 */
public class ContextLoaderListener implements javax.servlet.ServletContextListener, IFCGIProcessFactory {
    private LinkedList closeables = new LinkedList();
    
    public static final String PEAR_DIR = "/WEB-INF/pear";
    public static final String CGI_DIR = "/WEB-INF/cgi";
    public static final String WEB_INF_DIR = "/WEB-INF";
 
    protected ServletContext context;
    
    protected FCGIConnectionFactory channelName;

    protected String php = null; 
    /** default: true. Switched off when fcgi is not configured */
    protected boolean fcgiIsConfigured;

    protected boolean canStartFCGI = false;
    protected boolean override_hosts = true;

    protected String php_fcgi_connection_pool_size = FCGIUtil.PHP_FCGI_CONNECTION_POOL_SIZE;
    protected String php_fcgi_connection_pool_timeout = FCGIUtil.PHP_FCGI_CONNECTION_POOL_TIMEOUT;
    protected boolean php_include_java;
    protected int php_fcgi_connection_pool_size_number = Integer.parseInt(FCGIUtil.PHP_FCGI_CONNECTION_POOL_SIZE);
    protected long php_fcgi_connection_pool_timeout_number = Long.parseLong(FCGIUtil.PHP_FCGI_CONNECTION_POOL_TIMEOUT);
    protected String php_fcgi_max_requests = FCGIUtil.PHP_FCGI_MAX_REQUESTS;
    protected int php_fcgi_max_requests_number = Integer.parseInt(FCGIUtil.PHP_FCGI_MAX_REQUESTS);

    protected ILogger logger;

    protected boolean promiscuous = true;
    protected ContextLoaderListener listener;

    private FCGIConnectionPool fcgiConnectionPool = null;
    
    // workaround for a bug in jboss server, which uses the log4j port 4445 for its internal purposes(!)
    private boolean isJBoss;
    private ContextServer contextServer; // shared with FastCGIServlet

    private ThreadPool fcgiThreadPool;


    
    /** The key used to store the ContextLoaderListener in the servlet context */
    public static final String CONTEXT_LOADER_LISTENER = ContextLoaderListener.class.getName()+".ROOT";

    /** Only for internal use 
     * @param ctx The servlet context 
     */
    public void destroyCloseables(ServletContext ctx) {
	List list = closeables;
	if (list == null) return;
	
	try {
	    for (Iterator ii = list.iterator(); ii.hasNext(); ) {
		Object c = ii.next();
		try {
		    Method close = c.getClass().getMethod("close", Util.ZERO_PARAM);
		    close.setAccessible(true);
		    close.invoke(c, Util.ZERO_ARG);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	} catch (Throwable t) {
	    t.printStackTrace();
	} finally {
	    closeables.clear();
	}
    }
    /**{@inheritDoc}*/  
    public void contextDestroyed(ServletContextEvent event) {
	ServletContext ctx = event.getServletContext();
	try {
	    destroyCloseables(ctx);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    	if(fcgiConnectionPool!=null) fcgiConnectionPool.destroy();
    	if (fcgiThreadPool!=null) fcgiThreadPool.destroy();
    	
    	if (channelName!=null) channelName.destroy();
    	fcgiConnectionPool = null;

    	if (contextServer != null) contextServer.destroy();
    	
    	Util.destroy();
    }
    /**{@inheritDoc}*/  
    public void contextInitialized(ServletContextEvent event) {
	ServletContext ctx = event.getServletContext();
	ctx.setAttribute(CONTEXT_LOADER_LISTENER, this);
	this.context = ctx;
	init();
	
	boolean promiscuous = true;
	try {
	    String value = ctx.getInitParameter("promiscuous");
	    if(value==null) value="";
	    value = value.trim();
	    value = value.toLowerCase();

	    if(value.equals("off") || value.equals("false")) promiscuous=false;
	} catch (Exception t) {t.printStackTrace();}

	String name = context.getServerInfo();
	if (name != null && (name.startsWith("JBoss")))    isJBoss    = true;
	logger = new Util.Logger(!isJBoss, new Logger());
    	Util.setDefaultLogger(logger);

	String servletContextName=ServletUtil.getRealPath(context, "");
	if(servletContextName==null) servletContextName="";
    	contextServer = new ContextServer(servletContextName, promiscuous);

	channelName = FCGIConnectionFactory.createChannelFactory(this, promiscuous);
	channelName.findFreePort(canStartFCGI);

	try {
	    fcgiConnectionPool = createConnectionPool(php_fcgi_connection_pool_size_number);
	} catch (FCGIConnectException e) {
	    Util.printStackTrace(e);
	}
	fcgiThreadPool = createThreadPool(php_fcgi_connection_pool_size_number);
    }
    private void init() {
	String value;
	try {
	    value = context.getInitParameter("prefer_system_php_exec");
	    if(value==null) value="";
	    value = value.trim();
	    value = value.toLowerCase();
	    if(value.equals("on") || value.equals("true")) preferSystemPhp=true;
	} catch (Throwable t) {t.printStackTrace();}	
	String val = null;
	try {
	    val = context.getInitParameter("php_fcgi_children");
	    if(val==null) val = context.getInitParameter("PHP_FCGI_CHILDREN");
	    if(val==null) val = System.getProperty("php.java.bridge.php_fcgi_children");
	    if(val==null) val = context.getInitParameter("php_fcgi_connection_pool_size");
	    if(val==null) val = System.getProperty("php.java.bridge.php_fcgi_connection_pool_size");
	    if(val!=null) php_fcgi_connection_pool_size_number = Integer.parseInt(val);
	} catch (Throwable t) {/*ignore*/}
	if(val!=null) php_fcgi_connection_pool_size = val;

	val = null;
	try {
	    val = context.getInitParameter("php_fcgi_connection_pool_timeout");
	    if(val==null) val = System.getProperty("php.java.bridge.php_fcgi_connection_pool_timeout");
	    if(val!=null) php_fcgi_connection_pool_timeout_number = Integer.parseInt(val);
	} catch (Throwable t) {/*ignore*/}
	if(val!=null) php_fcgi_connection_pool_timeout = val;
	
	val = null;
	php_include_java = false;
	try {
	    val  = context.getInitParameter("php_include_java");
	    if(val==null) val = context.getInitParameter("PHP_INCLUDE_JAVA");
	    if(val==null) val = System.getProperty("php.java.bridge.php_include_java");
	    if(val!=null && (val.equalsIgnoreCase("on") ||  val.equalsIgnoreCase("true")))
		php_include_java = true;
	} catch (Throwable t) {/*ignore*/}

	val = null;
	try {
	    val = context.getInitParameter("php_fcgi_max_requests");
	    if(val==null) val = System.getProperty("php.java.bridge.php_fcgi_max_requests");
	    if(val != null) {
		php_fcgi_max_requests_number = Integer.parseInt(val);
		php_fcgi_max_requests = val;
	    }
	} catch (Throwable t) {/*ignore*/}
	checkCgiBinary();
	createPhpFiles ();
   }
    private void checkCgiBinary() {
	String value;
	if (php==null) {
	    try {
		value = context.getInitParameter("php_exec");
		if(value==null || value.trim().length()==0) {
		    value = "php-cgi";
		    phpTryOtherLocations = true;
		}
		File f = new File(value);
		if(!f.isAbsolute()) {
		    value = ServletUtil.getRealPath(context, CGI_DIR)+File.separator+value;
		}
		php = value;
	    }  catch (Throwable t) {Util.printStackTrace(t);}      
	}      
	fcgiIsConfigured = true;
	try {
	    value = context.getInitParameter("use_fast_cgi");
	    if(value==null) try { value = System.getProperty("php.java.bridge.use_fast_cgi"); } catch (Exception e) {/*ignore*/}
	    if("false".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value)) fcgiIsConfigured=false;
	    else {
		value = context.getInitParameter("use_fast_cgi");
		if(value==null) value="auto";
		value=value.trim();
		value = value.toLowerCase();
		boolean autostart = value.startsWith("auto");
		boolean notAvailable = value.equals("false") || value.equals("off");
		if(notAvailable) fcgiIsConfigured=false;
		if(autostart) canStartFCGI = true;
	    }
	}  catch (Throwable t) {Util.printStackTrace(t);}
    }
    private  boolean useSystemPhp(File f) {
	
	// path hard coded in web.xml
	if (!phpTryOtherLocations) return true;
	
	// no local php exists
	if (!f.exists()) return true;
	
	// local exists
	if(!preferSystemPhp) return false;
	
	// check default locations for preferred system php
	for(int i=0; i<Util.DEFAULT_CGI_LOCATIONS.length; i++) {
	    File location = new File(Util.DEFAULT_CGI_LOCATIONS[i]);
	    if(location.exists()) return true;
	}
	
	return false;
    }
    static final HashMap PROCESS_ENVIRONMENT = getProcessEnvironment();
    
    private static void updateProcessEnvironment(File conf) {
	try {
	    PROCESS_ENVIRONMENT.put("PHP_INI_SCAN_DIR", conf.getCanonicalPath());
	} catch (IOException e) {
	    e.printStackTrace();
	    PROCESS_ENVIRONMENT.put("PHP_INI_SCAN_DIR", conf.getAbsolutePath());
	}
    }
    private static HashMap getProcessEnvironment() {
	HashMap map = new HashMap(Util.COMMON_ENVIRONMENT);
	return map;
    }
    private void createPhpFiles () {

	String javaDir = ServletUtil.getRealPath(context, "java");
	if (javaDir != null) {
	    File javaDirFile = new File (javaDir);
	    try {
		if (!javaDirFile.exists()) {
		    javaDirFile.mkdir();
    	    	}
	    } catch (Exception e) {/*ignore*/}
	    
	    File javaIncFile = new File (javaDir, "Java.inc");
	    try {
		if (!javaIncFile.exists()) {
		    Field f = Util.JAVA_INC.getField("bytes");
		    byte[] buf = (byte[]) f.get(Util.JAVA_INC);
		    OutputStream out = new FileOutputStream (javaIncFile);
		    out.write(buf);
		    out.close();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
/* no longer part of the PHP/Java Bridge
	    File phpDebuggerFile = new File (javaDir, "PHPDebugger.php");
	    try {
		if (!phpDebuggerFile.exists()) {
		    Field f = Util.PHPDEBUGGER_PHP.getField("bytes");
		    byte[] buf = (byte[]) f.get(Util.PHPDEBUGGER_PHP);
		    OutputStream out = new FileOutputStream (phpDebuggerFile);
		    out.write(buf);
		    out.close();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
*/	    
	    File javaProxyFile = new File (javaDir, "JavaProxy.php");
	    try {
		if (!javaProxyFile.exists()) {
		    Field f = Util.JAVA_PROXY.getField("bytes");
		    byte[] buf = (byte[]) f.get(Util.JAVA_PROXY);
		    OutputStream out = new FileOutputStream (javaProxyFile);
		    out.write(buf);
		    out.close();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	String pearDir = ServletUtil.getRealPath(context, PEAR_DIR);
	if (pearDir != null) {
	    File pearDirFile = new File (pearDir);
	    try {
		if (!pearDirFile.exists()) {
		    pearDirFile.mkdir();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	String cgiDir = ServletUtil.getRealPath(context, CGI_DIR);
	File cgiOsDir = new File(cgiDir, Util.osArch+"-"+Util.osName);
	File conf = new File(cgiOsDir, "conf.d");
	File ext = new File(cgiOsDir, "ext");
	File cgiDirFile = new File (cgiDir);
	try {
	    if (!cgiDirFile.exists()) {
		cgiDirFile.mkdirs();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    if (!conf.exists()) {
		conf.mkdirs ();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    if (!ext.exists()) {
		ext.mkdir();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	File javaIncFile = new File (cgiOsDir, "launcher.sh");
	if (Util.USE_SH_WRAPPER) {
	    try {
		if (!javaIncFile.exists()) {
		    Field f = Util.LAUNCHER_UNIX.getField("bytes");
		    byte[] buf = (byte[]) f.get(Util.LAUNCHER_UNIX);
		    OutputStream out = new FileOutputStream (javaIncFile);
		    out.write(buf);
		    out.close();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	File javaProxyFile = new File (cgiOsDir, "launcher.exe");
	if (!Util.USE_SH_WRAPPER) {
	    try {
		if (!javaProxyFile.exists()) {
		    Field f =  Util.LAUNCHER_WINDOWS.getField("bytes");
		    Field f2 = Util.LAUNCHER_WINDOWS2.getField("bytes");
		    Field f3 = Util.LAUNCHER_WINDOWS3.getField("bytes");
		    Field f4 = Util.LAUNCHER_WINDOWS4.getField("bytes");
		    byte[] buf =  (byte[]) f.get(Util.LAUNCHER_WINDOWS);
		    byte[] buf2 = (byte[]) f2.get(Util.LAUNCHER_WINDOWS2);
		    byte[] buf3 = (byte[]) f3.get(Util.LAUNCHER_WINDOWS3);
		    byte[] buf4 = (byte[]) f4.get(Util.LAUNCHER_WINDOWS4);
		    OutputStream out = new FileOutputStream (javaProxyFile);
		    out.write(buf);
		    out.write(buf2);
		    out.write(buf3);
		    out.write(buf4);
		    out.close();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	boolean exeExists = true;
	if (Util.USE_SH_WRAPPER) {
	    try {
		File phpCgi = new File (cgiOsDir, "php-cgi");
		if (!useSystemPhp(phpCgi)) {
		    updateProcessEnvironment(conf);
		    File wrapper = new File(cgiOsDir, "php-cgi.sh");
		    if (!wrapper.exists()) {
			byte[] data = ("#!/bin/sh\nchmod +x ./"+Util.osArch+"-"+Util.osName+"/php-cgi\n"+
			"exec ./"+Util.osArch+"-"+Util.osName+"/php-cgi -c ./"+Util.osArch+"-"+Util.osName+"/php-cgi.ini \"$@\"").getBytes();
			OutputStream out = new FileOutputStream (wrapper);
			out.write(data);
			out.close();
		    }
		    File ini = new File(cgiOsDir, "php-cgi.ini");
		    if (!ini.exists()) {
			byte[] data = (";; -*- mode: Scheme; tab-width:4 -*-\n;; A simple php.ini\n"+
				";; DO NOT EDIT THIS FILE!\n" +
				";; Add your configuration files to the "+conf+" instead.\n"+
				";; PHP extensions go to "+ext+". Please see phpinfo() for ABI version details.\n"+
				"extension_dir=\""+ext+"\"\n"+
				"include_path=\""+pearDir+":/usr/share/pear:.\"\n").getBytes();
			OutputStream out = new FileOutputStream (ini);
			out.write(data);
			out.close();
		    }
		} else {
		    exeExists = false;
		    File readme = new File(cgiOsDir, "php-cgi.MISSING.README.txt");
		    if (!readme.exists()) {
			byte[] data = ("You can add \"php-cgi\" to this directory and re-deploy your web application.\n").getBytes();
			OutputStream out = new FileOutputStream (readme);
			out.write(data);
			out.close();
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    try {
		File phpCgi = new File (cgiOsDir, "php-cgi.exe");
		if (!useSystemPhp(phpCgi)) {
		    updateProcessEnvironment(conf);
		    File ini = new File(cgiOsDir, "php.ini");
		    if (!ini.exists()) {
			byte[] data = (";; -*- mode: Scheme; tab-width:4 -*-\r\n;; A simple php.ini\r\n"+
				";; DO NOT EDIT THIS FILE!\r\n" +
				";; Add your configuration files to the "+conf+" instead.\r\n"+
				";; PHP extensions go to "+ext+". Please see phpinfo() for ABI version details.\r\n"+
				"extension_dir=\""+ext+"\"\r\n"+
				"include_path=\""+pearDir+";.\"\r\n").getBytes();
			OutputStream out = new FileOutputStream (ini);
			out.write(data);
			out.close();
		    }
		} else {
		    exeExists = false;
		    File readme = new File(cgiOsDir, "php-cgi.exe.MISSING.README.txt");
		    if (!readme.exists()) {
			byte[] data = ("You can add \"php-cgi.exe\" to this directory and re-deploy your web application.\r\n").getBytes();
			OutputStream out = new FileOutputStream (readme);
			out.write(data);
			out.close();
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	File tmpl = new File(conf, "mysql.ini");
	if (exeExists && !tmpl.exists()) {
	    String str;
	    if (Util.USE_SH_WRAPPER) {
		str = ";; -*- mode: Scheme; tab-width:4 -*-\n"+
		";; Example extension.ini file: mysql.ini.\n"+
		";; Copy the correct version (see phpinfo()) of the PHP extension \"mysql.so\" to the ./../ext directory and uncomment the following line\n"+
		"; extension = mysql.so\n";
	    } else {
		str = ";; -*- mode: Scheme; tab-width:4 -*-\r\n"+
		";; Example extension.ini file: mysql.ini.\r\n"+
		";; Copy the correct version (see phpinfo()) of the PHP extension \"php_mysql.dll\" to the .\\..\\ext directory and uncomment the following line\r\n"+
		"; extension = php_mysql.dll\r\n";
	    }
	    byte[] data = str.getBytes();
	    try {
		OutputStream out = new FileOutputStream (tmpl);
		out.write(data);
		out.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
    private final FCGIIOFactory defaultPoolFactory = new FCGIIOFactory() {
	public InputStream createInputStream() { return new FCGIInputStream(ContextLoaderListener.this); }
	public OutputStream createOutputStream() { return new FCGIOutputStream(); }
	public FCGIConnection connect(FCGIConnectionFactory name) throws FCGIConnectException {
	    return name.connect();
	}
    };
    private FCGIConnectionPool createConnectionPool(int children) throws FCGIConnectException {
	channelName.initialize();

	// Start the launcher.exe or launcher.sh
	channelName.startServer(logger);
	return new FCGIConnectionPool(channelName, children, php_fcgi_max_requests_number, defaultPoolFactory, php_fcgi_connection_pool_timeout_number);
    }
    private ThreadPool createThreadPool(int children) {
	return new ThreadPool("JavaBridgeServletScriptEngineProxy", children);
    }
    public ThreadPool getThreadPool() {
	return fcgiThreadPool;
    }
    public ContextServer getContextServer() {
	return contextServer;
    }
    
    public ILogger getLogger() {
	return logger;
    }
    public FCGIConnectionFactory getChannelName() {
	return channelName;
    }
    public List getCloseables() {
	return closeables;
    }
    public FCGIConnectionPool getConnectionPool() {
	return fcgiConnectionPool;
    }
    public static ContextLoaderListener getContextLoaderListener(ServletContext ctx) {
	return (ContextLoaderListener)ctx.getAttribute(ContextLoaderListener.CONTEXT_LOADER_LISTENER);
    }
    /** required by IFCGIProcessFactory */
    protected boolean preferSystemPhp = false; // prefer /usr/bin/php-cgi over WEB-INF/cgi/php-cgi?
    protected boolean phpTryOtherLocations = false;
    /** {@inheritDoc} */
    public IFCGIProcess createFCGIProcess(String[] args, boolean includeJava, File home, Map env)
            throws IOException {
	return new FCGIProcess(args, includeJava, getCgiDir(), getPearDir(), getWebInfDir(), home, env, getCgiDir(), phpTryOtherLocations, preferSystemPhp);
    }
    /** {@inheritDoc} */
    public String getPhpConnectionPoolSize() {
	return php_fcgi_connection_pool_size;
    }
    /** {@inheritDoc} */
    public String getPhpMaxRequests() {
	return php_fcgi_max_requests; 
    }
    /** {@inheritDoc} */
    public String getPhp() {
	return php;
    }
    /** {@inheritDoc} */
    public boolean getPhpIncludeJava() {
	return php_include_java;
    }
    /** {@inheritDoc} */
    public HashMap getEnvironment () {
	return getProcessEnvironment();
    }
    /** {@inheritDoc} */
    public boolean canStartFCGI() {
	return canStartFCGI;
    }
    private String cgiDir;
    /** {@inheritDoc} */
    public String getCgiDir() {
	if (cgiDir != null) return cgiDir;
	return cgiDir = ServletUtil.getRealPath(context, CGI_DIR);
    }
    private String pearDir;
    /** {@inheritDoc} */
    public String getPearDir() {
	if (pearDir != null) return pearDir;
	return pearDir = ServletUtil.getRealPath(context, PEAR_DIR);
    }
    private String webInfDir;
    /** {@inheritDoc} */
   public String getWebInfDir() {
	if (webInfDir != null) return webInfDir;
	return webInfDir = ServletUtil.getRealPath(context, WEB_INF_DIR);
   }
   /** {@inheritDoc} */
   public void log(String msg) {
       logger.log(Logger.INFO, msg);
   }
}
