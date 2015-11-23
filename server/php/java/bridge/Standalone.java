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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;

import javax.swing.JOptionPane;

/**
 * This is the standalone container of the PHP/Java Bridge. It starts
 * the standalone back-end, listenes for protocol requests and handles
 * CreateInstance, GetSetProp and Invoke requests. Supported protocol
 * modes are INET (listens on all interfaces), INET_LOCAL (loopback
 * only), HTTP, HTTP_LOCAL, HTTPS and HTTPS_LOCAL
 * (starts the built-in servlet engine listening on all interfaces or loopback).  
 * <p> Example:<br> <code> java
 * -Djava.awt.headless=true -jar JavaBridge.jar INET_LOCAL:9676 5
 * bridge.log &<br> telnet localhost 9676<br> &lt;CreateInstance
 * value="java.lang.Long" predicate="Instance" id="0"&gt;<br> &lt;Long
 * value="6"/&gt; <br> &lt;/CreateInstance&gt;<br> &lt;Invoke
 * value="1" method="toString" predicate="Invoke" id="0"/&gt;<br>
 * </code>
 *
 */

public class Standalone {

    /** The default HTTP port for management clients */
    public static final int HTTP_PORT_BASE = 8080;
    /** The default HTTP port for management clients */
    public static final int HTTPS_PORT_BASE = 8443;

    /**
     * Create a new server socket and return it. 
     * @param logLevel the current logLevel
     * @param sockname the socket name
     * @return the server socket
     * @throws IOException 
     */
    static ISocketFactory bind(int logLevel, String sockname) throws IOException {
	ISocketFactory socket = null;
	socket = TCPServerSocket.create(sockname, Util.BACKLOG);

	if(null==socket)
	    throw new IOException("Could not create socket: " + sockname);

	return socket;
    }
    protected static void disclaimer() {
	System.err.println("Copyright (C) 2003, 2006 Jost Boekemeier and others.");
	System.err.println("This is free software; see the source for copying conditions.  There is NO");
	System.err.println("warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
    }
    protected void javaUsage() {
	System.err.println("PHP/Java Bridge version "+Util.VERSION);
	disclaimer();
	System.err.println("Usage: java -jar JavaBridge.jar [SOCKETNAME LOGLEVEL LOGFILE]");
	System.err.println("SOCKETNAME is one of INET_LOCAL, INET, HTTP_LOCAL, HTTP, HTTPS_LOCAL, HTTPS");
	System.err.println("");
	System.err.println("Example 1: java -jar JavaBridge.jar");
	System.err.println("Example 2: java -jar JavaBridge.jar HTTP_LOCAL:8080 3 JavaBridge.log");
	System.err.println("Example 3: java -Djavax.net.ssl.keyStore=mySrvKeystore -Djavax.net.ssl.keyStorePassword=YOURPASSWD -jar JavaBridge.jar HTTPS:8443 3 JavaBridge.log");
	System.err.println("The certificate for example 3 can be created with e.g.: jdk1.6.0/bin/keytool -keystore mySrvKeystore -genkey -keyalg RSA");
	System.err.println("");
	System.err.println("Influential system properties: threads, daemon, php_exec, default_log_file, default_log_level, base.");
	System.err.println("Example: java -Djava.awt.headless=\"true\" -Dphp.java.bridge.threads=50 -Dphp.java.bridge.base=/usr/lib/php/modules -Dphp.java.bridge.php_exec=/usr/local/bin/php-cgi -Dphp.java.bridge.default_log_file= -Dphp.java.bridge.default_log_level=5 -jar JavaBridge.jar");
	System.err.println("Example: java -Dphp.java.bridge.daemon=\"true\" -jar JavaBridge.jar");
    }
    protected void usage() {
	javaUsage();
	
	System.exit(1);
    }

    protected void checkOption(String s[]) {
	if ("--version".equals(s[0])) {
	    System.out.println(Util.VERSION);
	    System.exit(0);
	}
	usage();
    }
    private static boolean testPort(int port) {
	try {
	    ServerSocket sock = new ServerSocket(port);
	    sock.close();
	    return true;
	} catch (IOException e) {/*ignore*/}
	return false;
    }
    private static int findFreePort(int start) {
	for (int port = start; port < start+100; port++) {
	    if(testPort(port)) return port;
	}
	return start;
   }

   
    /**
     * Global init. Redirects System.out and System.err to the server
     * log file(s) or to System.err and creates and opens the
     * communcation channel. Note: Do not write anything to
     * System.out, this stream is connected with a pipe which waits
     * for the channel name.
     * @param s an array of [socketname, level, logFile]
     */
    protected void init(String s[]) {
	String sockname=null;
	int logLevel = -1;
        String tcpSocketName = "9267";
	
	if(s.length>3) checkOption(s);
	try {
	    if(s.length>0) {
		sockname=s[0];
		if(sockname.startsWith("-")) checkOption(s);
	    }
	    try {
		if(s.length>1) {
		    logLevel=Integer.parseInt(s[1]);
		}
	    } catch (NumberFormatException e) {
		usage();
	    } catch (Throwable t) {
		t.printStackTrace();
	    }
	    if(s.length==0) {
		try {
		    int tcpSocket = Integer.parseInt(tcpSocketName);
		    int freeJavaPort = findFreePort(tcpSocket);
		    int freeHttpPort = findFreePort(Standalone.HTTP_PORT_BASE);
		    int freeHttpsPort = findFreePort(Standalone.HTTPS_PORT_BASE);
		    Object result = JOptionPane. showInputDialog(null,
			    "Start a socket listener on port", "Starting the PHP/Java Bridge ...", JOptionPane.QUESTION_MESSAGE, null,
		            new String[] {
			    "INET_LOCAL:"+freeJavaPort,"INET:"+freeJavaPort,
			    "HTTP_LOCAL:"+freeHttpPort,"HTTP:"+freeHttpPort,
			    "HTTPS_LOCAL:"+freeHttpsPort,"HTTPS:"+freeHttpsPort}, "HTTP_LOCAL:"+freeHttpPort
			    );
		       if(result==null) System.exit(0);
		      sockname  = result.toString();
		} catch (Throwable t) {/*ignore*/}
	    }

	    if(s.length==0) {
		// do not access Util unless invoked as standalone component
		TCPServerSocket.TCP_PORT_BASE=Integer.parseInt(tcpSocketName);
	    }
	    if (checkServlet(logLevel, sockname, s)) return;
	    
	    ISocketFactory socket = bind(logLevel, sockname);

	    if("true".equals(System.getProperty("php.java.bridge.test.startup"))) System.exit(0);
	    JavaBridge.initLog(String.valueOf(socket), logLevel, s);
	    JavaBridge.init(socket, logLevel, s);
	} catch (Exception e) { throw new RuntimeException(e);} 
    }

    /**
     * Returns the canonical windows file. For example c:\program files instead of c:\programme
     * @param path The path, may be an empty string.
     * @return the canonical file.
     */
    public static File getCanonicalWindowsFile (String path) {
            try {
                return new File(path).getCanonicalFile();
        } catch (IOException e) {
                return new File(path);
        }
    }
    private static boolean checkServlet(int logLevel, String sockname, String[] s) throws InterruptedException, IOException {
	if(sockname==null) return false;
	if(sockname.startsWith("SERVLET_LOCAL:")||sockname.startsWith("HTTP_LOCAL:")||sockname.startsWith("HTTPS_LOCAL:")) {
	    Util.JAVABRIDGE_PROMISCUOUS = false;
	    System.setProperty("php.java.bridge.promiscuous", "false");
	} else if(sockname.startsWith("SERVLET:")||sockname.startsWith("HTTP:")||sockname.startsWith("HTTPS:")) {
	    Util.JAVABRIDGE_PROMISCUOUS = true;
	    System.setProperty("php.java.bridge.promiscuous", "true");
	} else 
	    return false;
	
	boolean isSecure = sockname.startsWith("HTTPS");
	JavaBridge.initLog(sockname, logLevel, s);
	sockname=sockname.substring(sockname.indexOf(':')+1);
	String serverPort = (Util.JAVABRIDGE_PROMISCUOUS ? "INET:" :"INET_LOCAL:") +sockname;
	Util.logMessage("JavaBridgeRunner started on port " + serverPort);

	Class runner = JavaBridgeRunner.class;
	JavaBridgeRunner r;
	try {
	    runner = Util.classForName("php.java.script.JavaBridgeScriptRunner");
	    Method m = runner.getMethod("getRequiredInstance", new Class[]{String.class, Boolean.TYPE});
	    r = (JavaBridgeRunner)m.invoke(runner, new Object[]{serverPort, new Boolean(isSecure)});
        } catch (Throwable e) {
            r = JavaBridgeRunner.getRequiredInstance(serverPort, isSecure);
        }
	r.waitFor();
	r.destroy();

	return true;
    }
    /* Don't use Util or DynamicJavaBridgeClassLoader at this stage! */
    private static final boolean checkGNUVM() {
	try {
	    return "libgcj".equals(System.getProperty("gnu.classpath.vm.shortname"));
	} catch (Throwable t) {
	    return false;
	}
    }
    /**
     * Start the PHP/Java Bridge. <br>
     * Example:<br>
     * <code>java -Djava.awt.headless=true -jar JavaBridge.jar INET:9656 5 /var/log/php-java-bridge.log</code><br>
     * Note: Do not write anything to System.out, this
     * stream is connected with a pipe which waits for the channel name.
     * @param s an array of [socketname, level, logFile]
     */
    public static void main(String s[]) {
	// check for -Dphp.java.bridge.daemon=true
	if (!(System.getProperty("php.java.bridge.daemon", "false").equals("false"))) {
	    final String[] args = new String[s.length + 8];
	    args[0] = System.getProperty("php.java.bridge.daemon");
	    if ("true".equals(args[0])) args[0]="java";
	    args[1]="-Djava.library.path="+System.getProperty("java.library.path", ".");
	    args[2] = "-Djava.ext.dirs="+System.getProperty("java.ext.dirs", ".");
	    args[3] = "-Djava.awt.headless="+System.getProperty("java.awt.headless", "true");
	    args[4] = "-Dphp.java.bridge.asDaemon=true";
	    args[5]="-classpath"; 
	    args[6]=System.getProperty("java.class.path", ".");
	    args[7] = "php.java.bridge.Standalone";

	    for (int j=0; j<s.length; j++) {
		args[j+8]=s[j];
	    }
	    try {
	        System.in.close();
	        System.out.close();
		System.err.close();
	    } catch (java.io.IOException e) {
		System.exit(12);
	    }

	    new Util.Thread(new Runnable () {
		public void run() {
		    try {
			Runtime.getRuntime().exec(args);
		    } catch (IOException e) {
			System.exit(13);
		    }
		}
	    }).start();
	    try {Thread.sleep(20000);} catch (Throwable t) {}
	    System.exit (0);
	}
	
	try {
	    System.loadLibrary("natcJavaBridge");
	} catch (Throwable t) {/*ignore*/}
	try { // this hack tries to workaround two problems
	      // 1. On Unix an older JDK may be in the path, even though sun jdk >= 1.6 is installed
	      // 2. The standard Unix desktop ("Gnome") executes JavaBridge.jar from the $HOME dir, not the current dir
	    String cp = System.getProperty("java.class.path", ".");
	    File jbFile = null;
	    boolean isExecutableJavaBridgeJar = (cp.indexOf(File.pathSeparatorChar)==-1) && 
	    					cp.endsWith("JavaBridge.jar") && 
	    					((jbFile=new File(cp)).isAbsolute());
	    File wd = Standalone.getCanonicalWindowsFile(isExecutableJavaBridgeJar ? jbFile.getParent() : "");
	    boolean sunJavaInstalled = (new File("/usr/java/default/bin/java")).exists();
	    String javaExec = sunJavaInstalled ? "/usr/java/default/bin/java" : "java";

	    if(s.length==0 && 
		    (System.getProperty("php.java.bridge.exec_sun_vm", "true").equals("true")) &&
		    ((sunJavaInstalled && checkGNUVM()) || isExecutableJavaBridgeJar)) {
		Process p = Runtime.getRuntime().exec(new String[] {javaExec, "-Dphp.java.bridge.exec_sun_vm=false", "-classpath", cp, "php.java.bridge.Standalone"}, null, wd);
		if(p != null) System.exit(p.waitFor());
	    }
	} catch (Throwable t) {/*ignore*/}
	try {
	    (new Standalone()).init(s);
	} catch (Throwable t) {
	    t.printStackTrace();
	    System.exit(9);
	}
    }
}
