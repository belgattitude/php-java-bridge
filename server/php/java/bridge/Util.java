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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.Map.Entry;

import php.java.bridge.http.FCGIConnectionPool;



/**
 * Miscellaneous functions.
 * @author jostb
 *
 */
public final class Util {

    static {
        initGlobals();
    }

    /** 
     * Script engines are started from this pool.
     * Use pool.destroy() to destroy the thread pool upon JVM or servlet shutdown
     */
    public static final ThreadPool PHP_SCRIPT_ENGINE_THREAD_POOL = new ThreadPool("JavaBridgeStandaloneScriptEngineProxy", Integer.parseInt(Util.THREAD_POOL_MAX_SIZE)) {
	    protected Delegate createDelegate(String name) {
		Delegate d = super.createDelegate(name);
		d.setDaemon(true);
		return d;
	    }
	};

    /** 
     * Only for internal use. The library standalone ScriptEngine FastCGI connection pool, if any 
     */
    public static FCGIConnectionPool fcgiConnectionPool;


    /** Used by the watchdog. After MAX_WAIT (default 1500ms) the ContextRunner times out. Raise this value if you want to debug the bridge.
     * See also system property <code>php.java.bridge.max_wait</code>
     */
    public static int MAX_WAIT;
    
    /** The java/Java.inc code */
    public static Class JAVA_INC;
    /** The java/Java.inc code */
    public static Class PHPDEBUGGER_PHP;
    /** The java/JavaProxy.php code */
    public static Class JAVA_PROXY;
    /** The launcher.sh code */
    public static Class LAUNCHER_UNIX;
    /** The launcher.exe code */
    public static Class LAUNCHER_WINDOWS, LAUNCHER_WINDOWS2, LAUNCHER_WINDOWS3, LAUNCHER_WINDOWS4;
    /** Only for internal use */
    public static final byte HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /** True if /bin/sh exists, false otherwise */
    public static final boolean USE_SH_WRAPPER = new File("/bin/sh").exists();
    
    /** The PHP argument allow_url_include=On, passed to all JSR223 script engines */
    public static final String[] ALLOW_URL_INCLUDE = {"-d", "allow_url_include=On"};

    /** Used to re-direct back to the current VM */
    public static final String X_JAVABRIDGE_OVERRIDE_HOSTS = "X_JAVABRIDGE_OVERRIDE_HOSTS";

    /** The standard Context ID used by the ContextFactory */
    public static final String X_JAVABRIDGE_CONTEXT = "X_JAVABRIDGE_CONTEXT";

    public static final String X_JAVABRIDGE_OVERRIDE_HOSTS_REDIRECT = "X_JAVABRIDGE_OVERRIDE_HOSTS_REDIRECT";

    public static final String X_JAVABRIDGE_REDIRECT = "X_JAVABRIDGE_REDIRECT";

    public static final String X_JAVABRIDGE_INCLUDE = "X_JAVABRIDGE_INCLUDE";

    public static final String X_JAVABRIDGE_INCLUDE_ONLY = "X_JAVABRIDGE_INCLUDE_ONLY";
    
    private Util() {}
    
    /**
     * Only for internal use. Use Util.getLogger() instread.
     * 
     * A bridge which uses log4j or the default logger.
     *
     */
    public static class Logger implements ILogger {
        protected ChainsawLogger clogger = null;
        protected ILogger logger;
        /**
         * Use chainsaw, if available or a default logger.
         *
         */
        public Logger() {
            logger = new FileLogger(); // log to logStream        
        }
        /**
         * Use chainsaw, if available.
         * @param logger The specified logger.
         */
        public Logger(ILogger logger) {
            this(!DEFAULT_LOG_FILE_SET, logger);
        }
        public Logger(boolean useChainsaw, ILogger logger) {
            if (useChainsaw) 
              try {this.clogger = ChainsawLogger.createChainsawLogger();} catch (Throwable t) {
        	if(Util.logLevel>5) t.printStackTrace();
                this.logger = logger;
            } else {
                this.logger = logger;
            }
        }
        private ILogger getLogger() {
            if(logger==null) return logger=new FileLogger();
            return logger;
        }
        /**{@inheritDoc}*/
	public void printStackTrace(Throwable t) {
	    if (clogger==null) logger.printStackTrace(t);
	    else
		try {
		    clogger.printStackTrace(t);
		} catch (Exception e) {
		    clogger=null;
		    getLogger().printStackTrace(t);
		}
	}

        /**{@inheritDoc}*/
	public void log(int level, String msg) {
	    if(clogger==null) logger.log(level, msg);
	    else
		try {
		    clogger.log(level, msg);
		} catch (Exception e) {
		    clogger=null;
		    getLogger().log(level, msg);
		}
	}

        /**{@inheritDoc}*/
	public void warn(String msg) {
	    if(clogger==null) logger.warn(msg);
	    else
		try {
		    clogger.warn(msg);
		} catch (Exception e) {
		    clogger=null;
		    getLogger().warn(msg);
		}
	}
    }

    /** 
     * The default PHP arguments. Can be passed via -Dphp.java.bridge.php_exec_args=list of urlencoded strings separated by space
     * Default: "-d display_errors=Off -d log_errors=On -d java.persistent_servlet_connections=On"
     */
    private static String[] PHP_ARGS;
    private static String DEFAULT_PHP_ARGS;
    
    /**
     * The default CGI locations: <code>"/usr/bin/php-cgi"</code>, <code>"c:/Program Files/PHP/php-cgi.exe</code>
     */
    public static String DEFAULT_CGI_LOCATIONS[];

    /**
     * ASCII encoding
     */
    public static final String ASCII = "ASCII";

    /**
     * UTF8 encoding
     */
    public static final String UTF8 = "UTF-8";

    /**
     * DEFAULT currently UTF-8, will be changed when most OS support and use UTF-16.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The default buffer size
     */
    public static final int BUF_SIZE = 8192;

    /** Environment entries which should NOT be passed to PHP. For example PHPRC, which is set by some broken PHP installers */
    
    public static List ENVIRONMENT_BLACKLIST;
    /**
     * A map containing environment values not in ENVIRONMENT_BLACKLIST. At least:
     * "PATH", "LD_LIBRARY_PATH", "LD_ASSUME_KERNEL", "USER", "TMP", "TEMP", "HOME", "HOMEPATH", "LANG", "TZ", "OS"
     * They can be set with e.g.: <code>java -DPATH="$PATH" -DHOME="$HOME" -jar JavaBridge.jar</code> or
     * <code>java -DPATH="%PATH%" -jar JavaBridge.jar</code>. 
     */
    public static HashMap COMMON_ENVIRONMENT;

    /**
     * The default extension directories. If one of the directories
     * "/usr/share/java/ext", "/usr/java/packages/lib/ext" contains
     * java libraries, the bridge loads these libraries automatically.
     * Useful if you have non-pure java libraries (=libraries which
     * use the Java Native Interface to load native dll's or shared
     * libraries).
     */
    public static final String DEFAULT_EXT_DIRS[] = { "/usr/share/java/ext", "/usr/java/packages/lib/ext" };
    
    
    /** Set to true if the VM is gcj, false otherwise */
    public static final boolean IS_GNU_JAVA = checkVM();

    /**
     * The name of the extension, usually "JavaBridge" or "MonoBridge"
     */
    public static String EXTENSION_NAME;

    /**
     * The max. number of threads in the thread pool. Default is 20.
     * @see System property <code>php.java.bridge.threads</code>
     */
    public static String THREAD_POOL_MAX_SIZE;
    
    /**
     * The default log level, java.log_level from php.ini
     * overrides. Default is 3, if started via java -jar
     * JavaBridge.jar or 2, if started as a sub-process of Apache/IIS.
     * @see System property <code>php.java.bridge.default_log_level</code>
     */
    public static int DEFAULT_LOG_LEVEL;

    /**
     * Backlog for TCP and unix domain connections.
      */
    public static final int BACKLOG = 20;

    /** Only for internal use */
    public static final Object[] ZERO_ARG = new Object[0];

    /** Only for internal use */
    public static final Class[] ZERO_PARAM = new Class[0];
    
    /** Only for internal use */
    public static final byte[] RN = Util.toBytes("\r\n");

    public static File TMPDIR;
    
    /** The name of the VM, for example "1.4.2@http://java.sun.com/" or "1.4.2@http://gcc.gnu.org/java/".*/
    public static String VM_NAME;
    /**
     * Set to true, if the Java VM has been started with -Dphp.java.bridge.promiscuous=true;
     */
    public static boolean JAVABRIDGE_PROMISCUOUS;

    /**
     * The default log file. Default is stderr, if started as a
     * sub-process of Apache/IIS or <code>EXTENSION_NAME</code>.log,
     * if started via java -jar JavaBridge.jar.
     * @see System property <code>php.java.bridge.default_log_file</code>
     */
    public static String DEFAULT_LOG_FILE;

    private static boolean DEFAULT_LOG_FILE_SET;
    
    /** The base directory of the PHP/Java Bridge. Usually /usr/php/modules/ or $HOME  */
    public static String JAVABRIDGE_BASE;
    
    private static String getProperty(Properties p, String key, String defaultValue) {
	String s = null;
	if(p!=null) s = p.getProperty(key);
	if(s==null) s = System.getProperty("php.java.bridge." + String.valueOf(key).toLowerCase());
	if(s==null) s = defaultValue;
	return s;
    }
    /** Only for internal use */
    public static String VERSION;
    /** Only for internal use */
    public static String osArch;
    /** Only for internal use */
    public static String osName;
    /** Only for internal use */
    public static String PHP_EXEC;
    /** Only for internal use */
    public static File HOME_DIR;

    private static String sessionSavePath;

    private static void initGlobals() {

	try {
	    JAVA_INC = Class.forName("php.java.bridge.JavaInc");
	} catch (Exception e) {/*ignore*/}
	try {
	    PHPDEBUGGER_PHP = Class.forName("php.java.bridge.PhpDebuggerPHP");
	} catch (Exception e) {/*ignore*/}
	try {
	    JAVA_PROXY = Class.forName("php.java.bridge.JavaProxy");
	} catch (Exception e) {/*ignore*/}
	try {
	    LAUNCHER_UNIX = Class.forName("php.java.bridge.LauncherUnix");
	} catch (Exception e) {/*ignore*/}
	try {
	    LAUNCHER_WINDOWS = Class.forName("php.java.bridge.LauncherWindows");
	    LAUNCHER_WINDOWS2 = Class.forName("php.java.bridge.LauncherWindows2");
	    LAUNCHER_WINDOWS3 = Class.forName("php.java.bridge.LauncherWindows3");
	    LAUNCHER_WINDOWS4 = Class.forName("php.java.bridge.LauncherWindows4");
	} catch (Exception e) {/*ignore*/}
	    
    	Properties p = new Properties();
	try {
	    InputStream in = Util.class.getResourceAsStream("global.properties");
	    p.load(in);
	    VERSION = p.getProperty("BACKEND_VERSION");
	} catch (Throwable t) {
	    VERSION = "unknown";
	    //t.printStackTrace();
	};
	ENVIRONMENT_BLACKLIST = getEnvironmentBlacklist(p);
	COMMON_ENVIRONMENT = getCommonEnvironment(ENVIRONMENT_BLACKLIST);
	DEFAULT_CGI_LOCATIONS = new String[] {"/usr/bin/php-cgi", "c:/Program Files/PHP/php-cgi.exe"};
	try {
	    if (!new File(DEFAULT_CGI_LOCATIONS[0]).exists() && !new File(DEFAULT_CGI_LOCATIONS[0]).exists())
		try {
		    File filePath = null;
		    boolean found = false;
		    String path = (String)COMMON_ENVIRONMENT.get("PATH");
		    StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
		    while(tok.hasMoreTokens()) {
			String s = tok.nextToken();
			if ((filePath = new File(s, "php-cgi.exe")).exists()) { found = true; break; }
			if ((filePath = new File(s, "php-cgi")).exists())     { found = true; break; }
		    }
		    if (!found) found = ((filePath = new File("/usr/php/bin/php-cgi")).exists());
		    if (!found) {
			String programFiles = (String)COMMON_ENVIRONMENT.get("ProgramFiles");
			if (programFiles!=null)
			 found = ((filePath = new File(programFiles+"\\PHP\\php-cgi.exe")).exists());
		    }
		    if (found) 
			DEFAULT_CGI_LOCATIONS = new String[] {filePath.getCanonicalPath(), DEFAULT_CGI_LOCATIONS[0], DEFAULT_CGI_LOCATIONS[1]};
		    
		} catch (Exception e) { /*ignore*/ }
	} catch (Throwable xe) {/*ignore*/}
	try {
	    MAX_WAIT = Integer.parseInt(getProperty(p, "php.java.bridge.max_wait", "15000"));
	} catch (Exception e) {
	    MAX_WAIT = 15000;
	}
	try {
	    HOME_DIR = new File(System.getProperty("user.home"));
	} catch (Exception e) {
	    HOME_DIR = null;
	}
	try {
	    JAVABRIDGE_BASE = getProperty(p, "php.java.bridge.base",  System.getProperty("user.home"));
	} catch (Exception e) {
	    JAVABRIDGE_BASE=".";
	}
	try {
    	    VM_NAME = "unknown";
	    VM_NAME = System.getProperty("java.version")+"@" + System.getProperty("java.vendor.url");	    
	} catch (Exception e) {/*ignore*/}
    	try {
    	    JAVABRIDGE_PROMISCUOUS = false;
	    JAVABRIDGE_PROMISCUOUS = getProperty(p, "php.java.bridge.promiscuous", "false").toLowerCase().equals("true");	    
	} catch (Exception e) {/*ignore*/}

	try {
	    THREAD_POOL_MAX_SIZE = "20";
	    THREAD_POOL_MAX_SIZE = getProperty(p, "THREADS", "20");
	} catch (Throwable t) {
	    //t.printStackTrace();
	};
	
	// resolve java.io.tmpdir for windows; PHP doesn't like dos short file names like foo~1\bar~2\...
	TMPDIR = new File(System.getProperty("java.io.tmpdir", "/tmp"));
	if (!TMPDIR.exists() || !TMPDIR.isDirectory()) TMPDIR = null;
	sessionSavePath = null;
	if (TMPDIR != null) try {TMPDIR = TMPDIR.getCanonicalFile(); } catch (IOException ex) {/*ignore*/}
	
	if (TMPDIR != null) {
	    sessionSavePath = TMPDIR.getPath();
	}
	DEFAULT_PHP_ARGS = "-d java.session=On -d display_errors=Off -d log_errors=On -d java.persistent_servlet_connections=On";
	    
	try {
	    String str = getProperty(p, "PHP_EXEC_ARGS", DEFAULT_PHP_ARGS);
	    String[] args = str.split(" ");
	    for (int i=0; i<args.length; i++) {
		try {
		    args[i] = java.net.URLDecoder.decode(args[i], UTF8);
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
	    }
	    PHP_ARGS = args;
	} catch (Throwable t) {
	    //t.printStackTrace();
	};
	try {
	    EXTENSION_NAME = "JavaBridge";
	    EXTENSION_NAME = getProperty(p, "EXTENSION_DISPLAY_NAME", "JavaBridge");
	} catch (Throwable t) {
	    //t.printStackTrace();
	};
	try {
	    PHP_EXEC = getProperty(p, "PHP_EXEC", null);
	} catch (Throwable t) {
	    //t.printStackTrace();
	}
	try {
	    String s = getProperty(p, "DEFAULT_LOG_LEVEL", "3");
	    DEFAULT_LOG_LEVEL = Integer.parseInt(s);
	    Util.logLevel=Util.DEFAULT_LOG_LEVEL; /* java.log_level in php.ini overrides */
	} catch (Throwable t) {/*ignore*/}
	try {
	    DEFAULT_LOG_FILE_SET = false;
	    DEFAULT_LOG_FILE = getProperty(p, "DEFAULT_LOG_FILE", Util.EXTENSION_NAME+".log");
	    DEFAULT_LOG_FILE_SET = System.getProperty("php.java.bridge.default_log_file") != null;
	} catch (Throwable t) {/*ignore*/}

	String separator = "/-+.,;: ";
	try {
	    String val = System.getProperty("os.arch").toLowerCase();
	    StringTokenizer t = new StringTokenizer(val, separator);
	    osArch = t.nextToken();
	} catch (Throwable t) {/*ignore*/}
	if(osArch==null) osArch="unknown";
	try {
	    String val = System.getProperty("os.name").toLowerCase();
	    StringTokenizer t = new StringTokenizer(val, separator);
	    osName = t.nextToken();
	} catch (Throwable t) {/*ignore*/}
	if(osName==null) osName="unknown";
    }
    /**
     * The logStream, defaults to System.err
     */
    static PrintStream logStream;
    
    private static ILogger defaultLogger =  new Logger(new FileLogger());
    
    /**
     * The loglevel:<br>
     * 0: log off <br>
     * 1: log fatal <br>
     * 2: log messages/exceptions <br>
     * 3: log verbose <br>
     * 4: log debug <br>
     * 5: log method invocations
     */
    public static int logLevel;


    /**
     * print a message on a given log level
     * @param level The log level
     * @param msg The message
     */
    public static void println(int level, String msg) {
	getLogger().log(level, msg);
    }
    
    /**
     * Display a warning if logLevel &gt;= 1
     * @param msg The warn message
     */
    public static void warn(String msg) {
	if(logLevel<=0) return;
	getLogger().warn(msg);
    }
    
    /**
     * Display a stack trace if logLevel >= 1
     * @param t The Throwable
     */
    public static void printStackTrace(Throwable t) {
        getLogger().printStackTrace(t);
    }
    /**
     * Display a debug message
     * @param msg The message
     */
    public static void logDebug(String msg) {
        if(logLevel>3) println(4, msg);
    }
    
    /**
     * Display a fatal error
     * @param msg The error
     */
    public static void logFatal(String msg) {
	if(logLevel>0) println(1, msg);
    }
    
    /**
     * Display an error or an exception
     * @param msg The error or the exception
     */
    public static void logError(String msg) {
	if(logLevel>1) println(2, msg);
    }
    
    /**
     * Display a message
     * @param msg The message
     */
    public static void logMessage(String msg) {
	if(logLevel>2) println(3, msg);
    }
    
    /**
     * Return the class name
     * @param obj The object
     * @return The class name
     */
    public static String getClassName(Object obj) {
        if(obj==null) return "null";
        Class c = getClass(obj);
        String name = c.getName();
        if(name.startsWith("[")) name = "array_of_"+name.substring(1);
        return name;
    }
    
    /**
     * Return the short class name
     * @param obj The object
     * @return The class name
     */
    public static String getShortClassName(Object obj) {
	String name = getClassName(obj);
	int idx = name.lastIndexOf('.');
	if(idx!=-1) 
	    name = name.substring(idx+1);
	return name;
    }
    /**
     * Return the short class name
     * @param clazz The class
     * @return The class name
     */
    public static String getShortName(Class clazz) {
	String name = clazz.getName();
        if(name.startsWith("[")) name = "array_of_"+name.substring(1);
	int idx = name.lastIndexOf('.');
	if(idx!=-1) 
	    name = name.substring(idx+1);
	return name;
    }
    
    /**
     * Return the class or the object, if obj is already a class.
     * @param obj The object
     * @return Either obj or the class of obj.
     */
    public static Class getClass(Object obj) {
	if(obj==null) return null;
	return obj instanceof Class?(Class)obj:obj.getClass();
    }
    
    /**
     * Append an object to a StringBuffer
     * @param obj The object
     * @param buf The StringBuffer
     */
    public static void appendObject(Object obj, StringBuffer buf) {
	if(obj==null) { buf.append("null"); return; }

    	if(obj instanceof Class) {
	    if(((Class)obj).isInterface()) 
		buf.append("[i:");
	    else
		buf.append("[c:");
    	} else {
	    buf.append("[o:");
	}
        buf.append(getShortClassName(obj));
	buf.append("]:");
	buf.append("\"");
	buf.append(Util.stringValueOf(obj));
	buf.append("\"");
    }
    /**
     * Append a stack trace to buf.
     * @param throwable The throwable object
     * @param trace The trace from PHP
     * @param buf The current buffer.
     */
    public static void appendTrace(Throwable throwable, String trace, StringBuffer buf) {
	    buf.append(" at:\n");
	    StackTraceElement stack[] = throwable.getStackTrace();
	    int top=stack.length;
	    for(int i=0; i<top; i++) {
		buf.append("#-");
		buf.append(top-i);
		buf.append(" ");
		buf.append(stack[i].toString());
		buf.append("\n");
	    }
	    buf.append(trace);
    }
    /**
     * Append a parameter object to a StringBuffer
     * @param obj The object
     * @param buf The StringBuffer
     */
    public static void appendShortObject(Object obj, StringBuffer buf) {
	if(obj==null) { buf.append("null"); return; }

    	if(obj instanceof Class) {
	    if(((Class)obj).isInterface()) 
		buf.append("[i:");
	    else
		buf.append("[c:");
    	} else {
	    buf.append("[o:");
	}
        buf.append(getShortClassName(obj));
	buf.append("]");
    }
    
    /**
     * Append a function parameter to a StringBuffer
     * @param c The parameter 
     * @param buf The StringBuffer
     */
    public static void appendParam(Class c, StringBuffer buf) {
	if(c.isInterface()) 
	    buf.append("(i:");
	else if (c==java.lang.Class.class)
	    buf.append("(c:");
	else
	    buf.append("(o:");
	buf.append(getShortClassName(c));
	buf.append(")");
    }
    
    /**
     * Append a function parameter to a StringBuffer
     * @param obj The parameter object
     * @param buf The StringBuffer
     */
    public static void appendParam(Object obj, StringBuffer buf) {
	if(obj instanceof Class) {
	    Class c = (Class)obj;
	    if(c.isInterface()) 
		buf.append("(i:");
	    else
		buf.append("(c:");
	}
	else
	    buf.append("(o:");
	buf.append(getShortClassName(obj));
	buf.append(")");
    }
    
    /**
     * Return function arguments and their types as a String
     * @param args The args
     * @param params The associated types
     * @return A new string
     */
    public static String argsToString(Object args[], Class[] params) {
	StringBuffer buffer = new StringBuffer("");
	appendArgs(args, params, buffer);
	return buffer.toString();
    }
    
    /**
     * Append function arguments and their types to a StringBuffer
     * @param args The args
     * @param params The associated types
     * @param buf The StringBuffer
     */
    public static void appendArgs(Object args[], Class[] params, StringBuffer buf) {
	if(args!=null) {
	    for(int i=0; i<args.length; i++) {
		if(params!=null) {
		    appendParam(params[i], buf); 
		}
	    	appendShortObject(args[i], buf);
		
		if(i+1<args.length) buf.append(", ");
	    }
	}
    }
    
    /**
     * Locale-independent getBytes(), uses ASCII encoding
     * @param s The String
     * @return The ASCII encoded bytes
     */
    public static byte[] toBytes(String s) {
	try {
	    return s.getBytes(ASCII);
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return s.getBytes();
	}
    }
    
    /**
     * Create a string array from a hashtable.
     * @param h The hashtable
     * @return The String
     * @throws NullPointerException
     */
    public static String[] hashToStringArray(Map h) {
	Vector v = new Vector();
	Iterator e = h.keySet().iterator();
	while (e.hasNext()) {
	    String k = e.next().toString();
	    v.add(k + "=" + h.get(k));
	}
	String[] strArr = new String[v.size()];
	v.copyInto(strArr);
	return strArr;
    }

    /**
     * Sets the fall back logger, used when no thread-local logger exists. The default logger is initialized with: <code>new Logger(new FileLogger())</code>. 
     * @param logger the logger
     * @see #logDebug
     */
    public static synchronized void setDefaultLogger(ILogger logger) {
	Util.defaultLogger = logger;
    }
    /**
     * @return Returns the logger.
     */
    public static ILogger getLogger() {
	return defaultLogger;
    }

    /**
     * Returns the string "127.0.0.1". If the system property "php.java.bridge.promiscuous" is "true", 
     * the real host address is returned.
     * @return The host address as a string.
     */
    public static String getHostAddress(boolean promiscuous) {
	String addr = "127.0.0.1";
	try {
	    if(JAVABRIDGE_PROMISCUOUS || promiscuous) 
		addr = InetAddress.getLocalHost().getHostAddress();
	} catch (UnknownHostException e) {/*ignore*/}
	return addr;
    }
    /**
     * Checks if the cgi binary buf-&lt;os.arch&gt;-&lt;os.name&gt;.sh or buf-&lt;os.arch&gt;-&lt;os.name&gt;.exe or buf-&lt;os.arch&gt;-&lt;os.name&gt; exists.
     * @param php the php binary or null
     * @return The full name or null.
     */
    public static String[] checkCgiBinary(String php) {
    	File location;
 
    	File phpFile = new File(php);
    	String path = phpFile.getParent();
    	String file = phpFile.getName();
    	
    	StringBuffer buf = new StringBuffer();
    	if (path != null) {
    	    buf.append(path);
    	    buf.append(File.separatorChar);
    	}
	buf.append(osArch);
	buf.append("-");
	buf.append(osName);
	buf.append(File.separatorChar);
	buf.append(file);
	
	if (USE_SH_WRAPPER) {
	    location = new File(buf.toString() + ".sh");
	    if(Util.logLevel>3) Util.logDebug("trying: " + location);
	    if(location.exists()) return new String[] {"/bin/sh", location.getAbsolutePath()};
	} else {
	    location = new File(buf.toString() + ".exe");
	    if(Util.logLevel>3) Util.logDebug("trying: " + location);
	    if(location.exists()) return new String[] {location.getAbsolutePath()};
	}
	
	location = new File(buf.toString());
	if(Util.logLevel>3) Util.logDebug("trying: " + location);
	if(location.exists()) return new String[] {location.getAbsolutePath()};
	
	return null;
    }

    /**
     * Returns s if s contains "PHP Fatal error:";
     * @param s The error string
     * @return The fatal error or null
     */
    public static String checkError(String s) {
        // Is there a better way to check for a fatal error?
        return (s.startsWith("PHP") && (s.indexOf("error:")>-1)) ? s : null;
    }

    /** 
     * Convenience daemon thread class
      */
    public static class Thread extends java.lang.Thread {
	/**Create a new thread */
	public Thread() {
	    super();
	    initThread();
	}
	/**Create a new thread 
	 * @param name */
	public Thread(String name) {
	    super(name);
	    initThread();
	}
	/**Create a new thread 
	 * @param target */
	public Thread(Runnable target) {
	    super(target);
	    initThread();
	}
	/**Create a new thread 
	 * @param group 
	 * @param target */
	public Thread(ThreadGroup group, Runnable target) {
	    super(group, target);
	    initThread();
	}
	/**Create a new thread 
	 * @param group 
	 * @param name */
	public Thread(ThreadGroup group, String name) {
	    super(group, name);
	    initThread();
	}
	/**Create a new thread 
	 * @param target 
	 * @param name */
	public Thread(Runnable target, String name) {
	    super(target, name);
	    initThread();
	}
	/**Create a new thread 
	 * @param group 
	 * @param target 
	 * @param name */
	public Thread(ThreadGroup group, Runnable target, String name) {
	    super(group, target, name);
	    initThread();
	}
	/**Create a new thread 
	 * @param group 
	 * @param target 
	 * @param name 
	 * @param stackSize */
	public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
	    super(group, target, name, stackSize);
	    initThread();
	}
	private void initThread() {
	    setDaemon(true);
	}
    }
    /**
     * Starts a CGI process and returns the process handle.
     */
    public static class Process extends java.lang.Process {

        protected java.lang.Process proc;
	private String[] args;
	private File homeDir;
	private Map env;
	private boolean tryOtherLocations;
	private boolean preferSystemPhp;
	private boolean isOldPhpVersion = false; // php < 5.3
	private boolean includeJava;
	private String cgiDir;
	private String pearDir;
	private String webInfDir;

	private String getQuoted(String key, String val) {
	    if (isOldPhpVersion) return key+val;
	    StringBuffer buf = new StringBuffer(key);
	    buf.append("'");
	    buf.append(val);
	    buf.append("'");
	    return buf.toString();
	}
	     /**
	     * Return args + PHP_ARGS
	     * @param args The prefix
	     * @param includeJava The option php_include_java
	     * @param cgiDir The WEB-INF/cgi directory
	     * @param pearDir The WEB-INF/pear directory
	     * @param webInfDir The WEB-INF directory
	     * @return args with PHP_ARGS appended
	     */
	    private String[] getPhpArgs(String[] args, boolean includeJava, String cgiDir, String pearDir, String webInfDir) {
		String[] allArgs = new String[args.length+PHP_ARGS.length+((sessionSavePath!=null)?2:0)+(includeJava?1:0)+(cgiDir!=null?2:0)+(pearDir!=null?2:0)+(webInfDir!=null?2:0)];
		int i=0;
		for(i=0; i<args.length; i++) {
		    allArgs[i]=args[i];
		}
		if (sessionSavePath!=null) {
		    allArgs[i++] = "-d";
		    allArgs[i++] = getQuoted("session.save_path=", sessionSavePath);
		}
		if (cgiDir!=null) {
		    File extDir = new File(cgiDir, Util.osArch+"-"+Util.osName);
		    try {
			cgiDir = extDir.getCanonicalPath();
		    } catch (IOException e) {
			Util.printStackTrace(e);
			cgiDir = extDir.getAbsolutePath();
		    }
		    allArgs[i++] = "-d";	    
		    allArgs[i++] = getQuoted("java.os_arch_dir=",cgiDir);	    
		}
		if (pearDir!=null) {
		    allArgs[i++] = "-d";	    
		    allArgs[i++] = getQuoted("java.pear_dir=",pearDir);	    
		}
		if (webInfDir!=null) {
		    allArgs[i++] = "-d";	    
		    allArgs[i++] = getQuoted("java.web_inf_dir=",webInfDir);	    
		}
		if (includeJava) allArgs[i++] = "-C"; // don't chdir, we'll do it
		for(int j=0; j<PHP_ARGS.length; j++) {
		    allArgs[i++]=PHP_ARGS[j];
		}
		
		return allArgs;
	    }
	
	protected String[] quoteArgs(String[] s) {
	    // quote all args for windows
	    if (!USE_SH_WRAPPER)
		for(int j=0; j<s.length; j++) 
		    if(s[j]!=null) s[j] = "\""+s[j]+"\"";
	    return s;
	}
	protected boolean testPhp(String[] php, String[] args) {
	    Runtime rt = Runtime.getRuntime();
	    String[] s = quoteArgs(getTestArgumentArray(php, args));
	    byte[] buf = new byte[BUF_SIZE];
	    int c, result, errCode;
	    InputStream in = null;
	    OutputStream out = null;
	    InputStream err = null;
	    
	    try {
	        proc = rt.exec(s, hashToStringArray(env), homeDir);
	        in = proc.getInputStream();
	        err = proc.getErrorStream();
	        out = proc.getOutputStream();
	            
	        out.close();
	        out = null;
	        
	        while((c=err.read(buf))>0) 
	            Util.logError(new String(buf, 0, c, ASCII));
	        err.close();
	        err = null;
	        
	        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
	        while((c=in.read(buf))>0)
	            outBuf.write(buf, 0, c);
	        in.close();
	        in = null;
	        
	        errCode = proc.waitFor();
	        result = proc.exitValue();
	        
	        if (errCode != 0 || result != 0) 
	            throw new IOException("php could not be run, returned error code: " + errCode + ", result: " + result);
	        
	        try {
	            checkOldPhpVersion(outBuf);
	        } catch (Throwable t) {
	            Util.printStackTrace(t);
	        } finally {
    	        outBuf.close();
	        }
	        
            } catch (IOException e) {
        	Util.logFatal("Fatal Error: Failed to start PHP "+java.util.Arrays.asList(s)+", reason: " + e);
        	return false;
            } catch (InterruptedException e) {
        	return false;
            } finally {
        	try {if (in!=null) in.close(); } catch (Exception e) {/*ignore*/}
        	try {if (out!=null) out.close(); } catch (Exception e) {/*ignore*/}
        	try {if (err!=null) err.close(); } catch (Exception e) {/*ignore*/}
            }
	    return true;
	}
	private void checkOldPhpVersion(ByteArrayOutputStream outBuf) {
	    String ver = outBuf.toString();
	    
	    StringTokenizer tok = new StringTokenizer(ver);
	    int n = tok.countTokens();
	    if (n < 2) return;
	    
	    String[] str = new String[n];
	    for (int i=0; tok.hasMoreTokens(); i++) {
		str[i] = tok.nextToken();
	    }
	    
	    tok = new StringTokenizer(str[1], ".");
	    n = tok.countTokens();
	    if (n < 1) return;
	    
	    str = new String[n];
	    for (int i=0; tok.hasMoreTokens(); i++) {
		str[i] = tok.nextToken();
	    }

	    int major = Integer.parseInt(str[0]);
	    if ((major > 5)) return;
	    if (major == 5) {
		 if(n < 2) return;
		 int minor = Integer.parseInt(str[1]);
		 if (minor > 2) return;
	    }
	    isOldPhpVersion = true;
	}
	protected void runPhp(String[] php, String[] args) throws IOException {
	    Runtime rt = Runtime.getRuntime();
	    String[] s = quoteArgs(getArgumentArray(php, args));

	    proc = rt.exec(s, hashToStringArray(env), homeDir);
	    if(Util.logLevel>3) Util.logDebug("Started "+ java.util.Arrays.asList(s));
	}
	protected String[] getTestArgumentArray(String[] php, String[] args) {
	    LinkedList buf = new LinkedList();
	    buf.addAll(java.util.Arrays.asList(php));
	    buf.add("-v");
	    
	    return  (String[]) buf.toArray(new String[buf.size()]);
	}
	protected String[] getArgumentArray(String[] php, String[] args) {
	    LinkedList buf = new LinkedList();
	    buf.addAll(java.util.Arrays.asList(php));
	    buf.addAll(java.util.Arrays.asList(ALLOW_URL_INCLUDE));
	    for(int i=1; i<args.length; i++) {
		buf.add(args[i]);
	    }
	    
	    return  (String[]) buf.toArray(new String[buf.size()]);
	}
	protected void start() throws NullPointerException, IOException {
	    File location;
	    /*
	     * Extract the php executable from args[0] ...
	     */
	    String[] php = new String[] {null};
	    if(args==null) args=new String[]{null};
	    String phpExec = args[0];
	    String[] cgiBinary = null;
	    if(PHP_EXEC==null) {
	      if(!preferSystemPhp) {
		if(phpExec != null && 
				((cgiBinary=checkCgiBinary(phpExec)) != null)) php = cgiBinary;
		/*
		 * ... resolve it ..
		 */            
		if(tryOtherLocations && php[0]==null) {
		    for(int i=0; i<DEFAULT_CGI_LOCATIONS.length; i++) {
			location = new File(DEFAULT_CGI_LOCATIONS[i]);
			if(location.exists()) {php[0] = location.getAbsolutePath(); break;}
		    }
		}
	      } else {
		/*
		 * ... resolve it ..
		 */            
		if(tryOtherLocations && php[0]==null) {
		    for(int i=0; i<DEFAULT_CGI_LOCATIONS.length; i++) {
			location = new File(DEFAULT_CGI_LOCATIONS[i]);
			if(location.exists()) {
			    php[0] = location.getAbsolutePath(); 
			    break;
			}
		    }
		}
		if(phpExec != null && 
				(php[0]==null &&  (cgiBinary=checkCgiBinary(phpExec)) != null)) php = cgiBinary;
	      }
	    }
            if(php[0]==null && tryOtherLocations) php[0]=PHP_EXEC;
            if(php[0]==null && phpExec!=null && (new File(phpExec).exists())) php[0]=phpExec;
            if(php[0]==null) php[0]="php-cgi";
            if(Util.logLevel>3) Util.logDebug("Using php binary: " + java.util.Arrays.asList(php));

            /*
             * ... and construct a new argument array for this specific process.
             */
            if(homeDir!=null && cgiBinary ==null)
        	homeDir = HOME_DIR; // system PHP executables are always executed in the user's HOME dir
            
            if(homeDir!=null &&!homeDir.exists()) homeDir = null;
            
            if (testPhp(php, args)) 
        	runPhp(php, getPhpArgs(args, includeJava, cgiDir, pearDir, webInfDir));
            else 
        	throw new IOException("PHP not found. Please install php-cgi. PHP test command was: " + java.util.Arrays.asList(getTestArgumentArray(php, args)) + " ");
        }
	protected Process(String[] args, boolean includeJava, String cgiDir, String pearDir, String webInfDir, File homeDir, Map env, boolean tryOtherLocations, boolean preferSystemPhp) {
	    this.args = args;
	    this.homeDir = homeDir;
	    this.env = env;
	    this.tryOtherLocations = tryOtherLocations;
	    this.preferSystemPhp = preferSystemPhp;
	    this.includeJava = includeJava;
	    this.cgiDir = cgiDir;
	    this.pearDir = pearDir;
	    this.webInfDir = webInfDir;
	}
        /**
	 * Starts a CGI process and returns the process handle.
	 * @param args The args array, e.g.: new String[]{null, "-b", ...};. If args is null or if args[0] is null, the function looks for the system property "php.java.bridge.php_exec".
	 * @param homeDir The home directory. If null, the current working directory is used.
	 * @param env The CGI environment. If null, Util.DEFAULT_CGI_ENVIRONMENT is used.
         * @param tryOtherLocations true if we should check the DEFAULT_CGI_LOCATIONS first
         * @param preferSystemPhp 
         * @param err 
	 * @return The process handle.
         * @throws IOException 
         * @throws NullPointerException 
	 * @throws IOException
	 * @see Util#checkCgiBinary(String)
	 */	  
        public static Process start(String[] args, boolean includeJava, String cgiDir, String pearDir, String webInfDir, File homeDir, Map env, boolean tryOtherLocations, boolean preferSystemPhp, OutputStream err) throws IOException {
            Process proc = new Process(args, includeJava, cgiDir, pearDir, webInfDir, homeDir, env, tryOtherLocations, preferSystemPhp);
            proc.start();
            return proc;
        }

        /** A generic PHP exception */
        public static class PhpException extends Exception {
	    private static final long serialVersionUID = 767047598257671018L;
	    private String errorString;
	    /** 
	     * Create a PHP exception 
	     * @param errorString the PHP error string 
	     */
	    public PhpException(String errorString) {
		super(errorString);
		this.errorString = errorString;
	    }
	    /** 
	     * Return the error string
	     * @return the PHP error string
	     */
	    public String getError() {
		return errorString;
	    }
	};

	/**
	 * Check for a PHP fatal error and throw a PHP exception if necessary.
	 * @throws PhpException
	 */
	public void checkError() throws PhpException {}

	/**{@inheritDoc}*/
        public OutputStream getOutputStream() {
            return proc.getOutputStream();
        }

	/**{@inheritDoc}*/
        public InputStream getInputStream() {
            return proc.getInputStream();
        }

	/**{@inheritDoc}*/
        public InputStream getErrorStream() {
            return proc.getErrorStream();
        }

	/**{@inheritDoc}*/
        public int waitFor() throws InterruptedException {
            return proc.waitFor();
        }

	/**{@inheritDoc}*/
        public int exitValue() {
            return proc.exitValue();
        }

	/**{@inheritDoc}*/
        public void destroy() {
            proc.destroy();
        }
        
    }

    /**
     * Starts a CGI process with an error handler attached and returns the process handle.
     */
    public static class ProcessWithErrorHandler extends Process {
	StringBuffer error = null;
	InputStream in = null;
	OutputStream err = null;

	protected ProcessWithErrorHandler(String[] args, boolean includeJava, String cgiDir, String pearDir, String webInfDir, File homeDir, Map env, boolean tryOtherLocations, boolean preferSystemPhp, OutputStream err) throws IOException {
	    super(args, includeJava, cgiDir, pearDir, webInfDir,  homeDir, env, tryOtherLocations, preferSystemPhp);
	    this.err = err;
	}
	protected void start() throws IOException {
	    super.start();
	    (new Util.Thread("CGIErrorReader") {public void run() {readErrorStream();}}).start();
	}
	/**{@inheritDoc}*/
	public void checkError() throws PhpException {
	    String errorString = error==null?null:Util.checkError(error.toString());
	    if(errorString!=null) throw new PhpException(errorString);
	}
	private synchronized void readErrorStream() {
	    byte[] buf = new byte[BUF_SIZE];
	    int c;
	    try { 
		in =  proc.getErrorStream();
		while((c=in.read(buf))!=-1) {
			err.write(buf, 0, c);
			String s = new String(buf, 0, c, ASCII); 
			if(Util.logLevel>4) Util.logError(s);
			if(error==null) error = new StringBuffer(s);
			else error.append(s);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    finally {
		if(in!=null) 
		    try { in.close();} catch (IOException e1) {
			e1.printStackTrace();
		    }
		notify();
	    }
	}
	/**{@inheritDoc}*/
	public synchronized int waitFor() throws InterruptedException {
	    if(in==null) wait();
	    return super.waitFor();
	}

	/**
         * Starts a CGI process and returns the process handle.
         * @param args The args array, e.g.: new String[]{null, "-b", ...};. If args is null or if args[0] is null, the function looks for the system property "php.java.bridge.php_exec".
         * @param homeDir The home directory. If null, the current working directory is used.
         * @param env The CGI environment. If null, Util.DEFAULT_CGI_ENVIRONMENT is used.
	 * @param tryOtherLocations true if the should check DEFAULT_CGI_LOCATIONS 
	 * @param preferSystemPhp true if the should check DEFAULT_CGI_LOCATIONS first
	 * @param err The error stream
         * @return The process handle.
         * @throws IOException
         * @see Util#checkCgiBinary(String)
         */
        public static Process start(String[] args, boolean includeJava, String cgiDir, String pearDir, String webInfDir, File homeDir, Map env, boolean tryOtherLocations, boolean preferSystemPhp, OutputStream err) throws IOException {
            Process proc = new ProcessWithErrorHandler(args, includeJava, cgiDir, pearDir, webInfDir, homeDir, env, tryOtherLocations, preferSystemPhp, err);
            proc.start();
            return proc;
        }
    }

    /** Redirect System.out and System.err to the configured logFile or System.err.
     * System.out is always redirected, either to the logFile or to System.err.
     * This is because System.out is reserved to report the status back to the 
     * container (IIS, Apache, ...) running the JavaBridge back-end.
     * @param redirectOutput this flag is set, if natcJavaBridge has already redirected stdin, stdout, stderr
     * @param logFile the log file
     */
    static void redirectOutput(String logFile) {
	redirectJavaOutput(logFile);
    }
    static void redirectJavaOutput(String logFile) {
        Util.logStream = System.err;
        if(logFile != null && logFile.length()>0) 
            try {
        	Util.logStream=new java.io.PrintStream(new java.io.FileOutputStream(logFile));
            } catch (Exception e) {e.printStackTrace();}
            try { System.setErr(logStream); } catch (Exception e) {e.printStackTrace(); }
	try { System.setOut(logStream); } catch (Exception e) {e.printStackTrace(); System.exit(9); }
    }

    private static List getEnvironmentBlacklist(Properties p) {
	List l = new LinkedList();
	try {
	    String s = getProperty(p, "PHP_ENV_BLACKLIST", "PHPRC");
	    StringTokenizer t = new StringTokenizer(s, " ");
	    while(t.hasMoreTokens()) l.add(t.nextToken());
	} catch (Exception e) {
	    e.printStackTrace();
	    l = new LinkedList ();
	    l.add("PHPRC");
	}
	return l;
    }
    
    private static HashMap getCommonEnvironment(List blacklist) {
	String entries[] = {
		"PATH", "PATH", "LD_LIBRARY_PATH", "LD_ASSUME_KERNEL", "USER", "TMP", "TEMP", "HOME", "HOMEPATH", "LANG", "TZ", "OS"
	};
	HashMap defaultEnv = new HashMap();
	String key, val;
        Method m = null;
        try {m = System.class.getMethod("getenv", new Class[]{String.class});} catch (Exception e) {/*ignore*/}
	for(int i=0; i<entries.length; i++) {
	    val = null;
	    if (m!=null) { 
	      try {
	        val = (String) m.invoke(System.class, (Object[])new String[]{entries[i]});
	      } catch (Exception e) {
		 m = null;
	      }
	    }
	    if(val==null) {
	        try { val = System.getProperty(entries[i]); } catch (Exception e) {/*ignore*/}
	    }
	    if((val!=null) && (!blacklist.contains(entries[i])))
		defaultEnv.put(entries[i], val);
	}
	
	// check for windows SystemRoot, needed for socket operations
	key = val = null;
	if((new File("c:/winnt")).isDirectory()) val="c:\\winnt";
	else if((new File("c:/windows")).isDirectory()) val = "c:\\windows";
	try {
	    String s = System.getenv(key = "SystemRoot"); 
	    if(s!=null) val=s;
        } catch (Throwable t) {/*ignore*/}
        try {
	    String s = System.getProperty(key = "Windows.SystemRoot");
	    if(s!=null) val=s;
        } catch (Throwable t) {/*ignore*/}
	if(val!=null && (!blacklist.contains(key))) defaultEnv.put("SystemRoot", val);

	// add all non-blacklisted environment entries
	try {
	    m = System.class.getMethod("getenv", ZERO_PARAM);
	    Map map = (Map) m.invoke(System.class, ZERO_ARG);
	    for (Iterator ii = map.entrySet().iterator(); ii.hasNext(); ) {
		Entry entry = (Entry) ii.next();
		key = (String) entry.getKey();
		val = (String) entry.getValue();
		
		if (!blacklist.contains(key)) 
		    defaultEnv.put(key, val);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return defaultEnv;
    }
    /** 
     * This procedure should be used whenever <code>object</code> may be a dynamic proxy: 
     * <code>String.valueOf(object) returns null, if object is a proxy and returns null.</code>
     * 
     * @param object The object or dynamic proxy
     * @return The string representation of object
     */
    public static String stringValueOf(Object object) {
        String s = String.valueOf(object);
        if(s==null) s = String.valueOf(s);
        return s;
    }

    /**
     * Create a new AppThreadPool.
     * @param name The pool name
     * @return A new AppThreadPool for up to {@link #THREAD_POOL_MAX_SIZE} runnables
     */
    public static AppThreadPool createThreadPool(String name) {
        AppThreadPool pool = null;
        int maxSize = 20;
        try {
        	maxSize = Integer.parseInt(Util.THREAD_POOL_MAX_SIZE);
        } catch (Throwable t) {
        	Util.printStackTrace(t);
        }
        if(maxSize>0) {
            pool = new AppThreadPool(name, maxSize);
	}
        return pool;
    }
    
    
    /**
     * parse java.log_file=@HOST:PORT
     * @param logFile The log file from the PHP .ini file
     * @return true, if we can use the log4j logger, false otherwise.
     */
    static boolean setConfiguredLogger(String logFile) {
        try {
	  return tryConfiguredChainsawLogger(logFile);
	} catch (Exception e) {
	  printStackTrace(e);
	  Util.setDefaultLogger(new FileLogger());
	}
	return true;
    }
    private static final class ConfiguredChainsawLogger extends ChainsawLogger {
        private String host;
	private int port;
	private ConfiguredChainsawLogger(String host, int port) {
	    super();
	    this.host=host;
	    this.port=port;
        }
        public static ConfiguredChainsawLogger createLogger(String host, int port) throws Exception {
            ConfiguredChainsawLogger logger = new ConfiguredChainsawLogger(host, port);
            logger.init();
	    return logger;
        }
        public void configure(String host, int port) throws Exception {
            host = this.host!=null ? this.host : host;
            port = this.port > 0 ? this.port : port;
            super.configure(host, port);
        }
    }
    /**
     * parse java.log_file=@HOST:PORT
     * @param logFile The log file from the PHP .ini file
     * @return true, if we can use the log4j logger, false otherwise.
     * @throws Exception
     */
    private static boolean tryConfiguredChainsawLogger(String logFile) throws Exception {
	if(logFile!=null && logFile.length()>0 && logFile.charAt(0)=='@') {
	    logFile=logFile.substring(1, logFile.length());
	    int idx = logFile.indexOf(':');
	    int port = -1;
	    String host = null;
	    if(idx!=-1) {
		String p = logFile.substring(idx+1, logFile.length());
		if(p.length()>0) port = Integer.parseInt(p);
		host = logFile.substring(0, idx);
	    } else {
		if(logFile.length()>0) host = logFile;
	    }
	    ILogger logger = ConfiguredChainsawLogger.createLogger(host, port);
	    Util.setDefaultLogger(logger);
	    return true;
	}
	return false;
    }

    /**
     * Return the time in GMT
     * @param ms the time in milliseconds
     * @return The formatted date string
     */
    public static String formatDateTime(long ms) {
	java.sql.Timestamp t = new java.sql.Timestamp(ms);
	DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, Locale.ENGLISH);
	formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	String str =  formatter.format(t);
	return str;
    }
    static final boolean checkVM() {
	try {
	    return "libgcj".equals(System.getProperty("gnu.classpath.vm.shortname"));
	} catch (Throwable t) {
	    return false;
	}
    }
    /**
     * Return the thread context class loader
     * @return The context class loader
     */
    public static final ClassLoader getContextClassLoader() {
        ClassLoader loader = null;
        try {loader = Thread.currentThread().getContextClassLoader();} catch (SecurityException ex) {/*ignore*/}
	if(loader==null) loader = JavaBridge.class.getClassLoader();
        return loader;
    }
    public static final Class classForName(String name) throws ClassNotFoundException {
	return Class.forName(name, true, getContextClassLoader());
    }

    public static String getSimpleRedirectString(String webPath, String socketName, boolean isSecure) {
	try {
	    StringBuffer buf = new StringBuffer();
	    buf.append(socketName);
	    buf.append("/");
	    buf.append(webPath);
	    URI uri = new URI(isSecure?"s:127.0.0.1":"h:127.0.0.1", buf.toString(), null);
	    return (uri.toASCIIString()+".phpjavabridge");
	} catch (URISyntaxException e) {
	    Util.printStackTrace(e);
	}
	StringBuffer buf = new StringBuffer(isSecure?"s:127.0.0.1":"h:127.0.0.1:");
	buf.append(socketName); 
	buf.append('/');
	buf.append(webPath);
	buf.append(".phpjavabridge");
	return buf.toString();
    }
    /**
     * Destroy the thread associated with util.
     */
    public static void destroy () {
	try {
	    PHP_SCRIPT_ENGINE_THREAD_POOL.destroy();
	} catch (Exception e) {
	    Util.printStackTrace(e);
	}
	try {
	    if (fcgiConnectionPool!=null) fcgiConnectionPool.destroy();
	} catch (Exception e) {
	    Util.printStackTrace(e);
	}
	try {
	    JavaBridgeRunner.destroyRunner();
	} catch (Exception e) {
	    Util.printStackTrace(e);
	}
    }
}
