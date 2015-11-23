/*-*- mode: Java; tab-width:8 -*-*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Used only for release tests
 */
public class TestInstallation implements Runnable {
    // See Util.DEFAULT_CGI_LOCATIONS
    static final String DEFAULT_CGI_LOCATIONS[] = new String[] {"/usr/bin/php-cgi", "c:/Program Files/PHP/php-cgi.exe"};
    private static String socket;
    private Process proc;
    private static Process runner;
    private static File base;

    TestInstallation() {
    }
    TestInstallation(Process proc) {
	this.proc = proc;
    }
    static class SimpleBrowser implements Runnable,  HyperlinkListener {
	private String port;
	SimpleBrowser(String port) {
	    this.port = port;
	}
	public void hyperlinkUpdate(HyperlinkEvent e) {
	    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		JEditorPane pane = (JEditorPane) e.getSource();
		if (e instanceof HTMLFrameHyperlinkEvent) {
		    HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
		    HTMLDocument doc = (HTMLDocument)pane.getDocument();
		    doc.processHTMLFrameHyperlinkEvent(evt);
		} else {
		    try {
			URL url = e.getURL();
			if(url.getFile().endsWith("/"))
			    pane.setPage(e.getURL());
			else {
			    JFrame frame = new JFrame(url.toExternalForm());
			    JEditorPane p = null;
			    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			    p = new JEditorPane(url);
			    p.setEditable(false);
			    p.addHyperlinkListener(this);
			    JScrollPane scroll = new JScrollPane(p);
			    frame.getContentPane().add(scroll);
			    frame.setSize(800,600);
			    frame.setVisible(true);
			}	        	  
		    } catch (Throwable t) {
			t.printStackTrace();
		    }
		}
	    }
	}
	   
	public void run() {
	    try {
		URL url = new URL("http://127.0.0.1:"+port+"/server/tests.php5/");
	    	JEditorPane p = null;
	    	JFrame frame = new JFrame(url.toExternalForm());
	    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		p = new JEditorPane(url);
		p.setEditable(false);
		p.addHyperlinkListener(this);
		JScrollPane scroll = new JScrollPane(p);
		frame.getContentPane().add(scroll);
		frame.setSize(800,600);
	 	 
		frame.setVisible(true);
	    } catch (IOException e) {
		System.exit(0);
	    }
	}

    }
    private static String findSocket() {
	for(int i=8080; i<8080+200; i++) {
	    try {
		ServerSocket s = new ServerSocket(i);
		s.close();
		return socket = String.valueOf(i);
	    } catch (Exception e) {/* ignore */}
	}
	return null;
    }
    private static final Class[] STRING_PARAM = new Class[]{String.class};
    private static final Class[] EMPTY_PARAM = new Class[0];
    private static final Object[] EMPTY_ARG = new Object[0];
    private static final File winnt = new File("c:/winnt");
    private static final File windows = new File("c:/windows");
    private static final Map COMMON_ENVIRONMENT = getCommonEnvironment();
    private static final HashMap processEnvironment = getProcessEnvironment();
    
    private static String[] hashToStringArray(Map h)
	throws NullPointerException {
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
    private static HashMap getProcessEnvironment() {
	HashMap defaultEnv = new HashMap();
	String val = null;
	// Bug in WINNT and WINXP.
	// If SystemRoot is missing, php cannot access winsock.
	if(winnt.isDirectory()) val="c:\\winnt";
	else if(windows.isDirectory()) val = "c:\\windows";
	try {
	    String s = System.getenv("SystemRoot"); 
	    if(s!=null) val=s;
	} catch (Throwable t) {/*ignore*/}
	try {
	    String s = System.getProperty("Windows.SystemRoot");
	    if(s!=null) val=s;
	} catch (Throwable t) {/*ignore*/}
	if(val!=null) defaultEnv.put("SystemRoot", val);
	try {
	    Method m = System.class.getMethod("getenv", EMPTY_PARAM);
	    Map map = (Map) m.invoke(System.class, EMPTY_ARG);
	    defaultEnv.putAll(map);
	} catch (Exception e) {
	    defaultEnv.putAll(COMMON_ENVIRONMENT);
	}
	return defaultEnv;
    }

    /**
     * A map containing common environment values for JDK <= 1.4:
     * "PATH", "LD_LIBRARY_PATH", "LD_ASSUME_KERNEL", "USER", "TMP", "TEMP", "HOME", "HOMEPATH", "LANG", "TZ", "OS"
     * They can be set with e.g.: <code>java -DPATH="$PATH" -DHOME="$HOME" -jar JavaBridge.jar</code> or
     * <code>java -DPATH="%PATH%" -jar JavaBridge.jar</code>. 
     */
    private static HashMap getCommonEnvironment() {
	String entries[] = {
	    "PATH", "LD_LIBRARY_PATH", "LD_ASSUME_KERNEL", "USER", "TMP", "TEMP", "HOME", "HOMEPATH", "LANG", "TZ", "OS"
	};
	HashMap map = new HashMap(entries.length+10);
	String val;
        Method m = null;
        try {m = System.class.getMethod("getenv", STRING_PARAM);} catch (Exception e) {/*ignore*/}
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
	    if(val!=null) map.put(entries[i], val);
	}
	return map;
    }
    private void readError() throws IOException {
	int c;
	InputStream in = proc.getErrorStream();
	while((c=in.read())!=-1) System.err.write(c);
	in.close();
    }
    private void startRunner() throws IOException {
	String[] cmd = new String[] {(String.valueOf(new File(System.getProperty("java.home"), "bin"+File.separator+"java"))),
				     "-jar",
				     base+File.separator+"ext"+File.separator+"JavaBridge.jar",
				     "SERVLET_LOCAL:"+socket};
	System.err.println("Starting a simple servlet engine: " + Arrays.asList(cmd));
	Process p;
	try {
	    p = runner = Runtime.getRuntime().exec(cmd);
	} catch (java.io.IOException ex) {
	    throw new RuntimeException("Could not run "+Arrays.asList(cmd)+".", ex);
	}
	(new Thread(new TestInstallation(p))).start();
	InputStream i = p.getInputStream();
	int c;
	while((c=i.read())!=-1) System.out.write(c);
	System.out.flush();
	System.err.flush();
    }
    /**{@inheritDoc}*/
    public void run() {
	try {
	    if(proc==null) startRunner();
	    else readError();
	} catch (Exception e) {
	    e.printStackTrace();
	}
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
     * 
     * @param args
     */
    public static void main(String[] args) {
	try { // Hack for Unix: execute the standalone container using the default SUN VM
	    if(args.length==0 && (new File("/usr/java/default/bin/java")).exists() && checkGNUVM() && (System.getProperty("php.java.bridge.exec_sun_vm", "true").equals("true"))) {
		Process p = Runtime.getRuntime().exec(new String[] {"/usr/java/default/bin/java", "-Dphp.java.bridge.exec_sun_vm=false", "-classpath", System.getProperty("java.class.path"), "TestInstallation"});
		(new Thread() { 
		    InputStream in;
		    public Thread init(InputStream in) { this.in = in; return this; }
		    public void run() { int c; try { while((c=in.read())!=-1) System.out.write(c); } catch (IOException e) { e.printStackTrace(); } }
		}).init(p.getInputStream()).start();
		(new Thread() { 
		    InputStream in;
		    public Thread init(InputStream in) { this.in = in; return this; }
		    public void run() { int c; try { while((c=in.read())!=-1) System.err.write(c); } catch (IOException e) { e.printStackTrace(); } }
		}).init(p.getErrorStream()).start();
		
		if(p != null) System.exit(p.waitFor());
	    }
	} catch (Throwable t) {/*ignore*/}

	try {
	    start(args);
	} catch (Throwable t) {
	    t.printStackTrace();
	    if(runner!=null) runner.destroy();
	    System.exit(1);
	}
    }
    static void start(String[] args) throws Exception {
	String socket = findSocket();
	String os = null;
	String separator = "/-+.,;: ";
	try {
	    String val = System.getProperty("os.name").toLowerCase();
	    StringTokenizer t = new StringTokenizer(val, separator);
	    os = t.nextToken();
	} catch (Throwable t) {/*ignore*/}
	if(os==null) os="unknown";
        File ext = null;
        try {
            ext = (args.length==0) ? new File(new File(System.getProperty("java.class.path")).getParentFile().getAbsoluteFile(), "ext") : new File(args[0], "ext");
	    ext = ext.getAbsoluteFile();
        } catch (Throwable t) {
            ext = (args.length==0) ? new File("ext") : new File(args[0], "ext");         
	    ext = ext.getAbsoluteFile();
        }
	if(!ext.isDirectory()) ext.mkdirs();
	base = ext.getParentFile();
	File java = new File(base, "java");
	if(!java.isDirectory()) java.mkdirs();
		
	ClassLoader loader = TestInstallation.class.getClassLoader();
	InputStream in = loader.getResourceAsStream("WEB-INF/lib/JavaBridge.jar");
	extractFile(in, new File(ext, "JavaBridge.jar").getAbsoluteFile());
	in.close();
	in = loader.getResourceAsStream("WEB-INF/lib/php-script.jar");
	extractFile(in, new File(ext, "php-script.jar").getAbsoluteFile());
	in.close();
	in = loader.getResourceAsStream("WEB-INF/lib/php-servlet.jar");
	extractFile(in, new File(ext, "php-servlet.jar").getAbsoluteFile());
	in.close();
	in = loader.getResourceAsStream("WEB-INF/lib/script-api.jar");
	extractFile(in, new File(ext, "script-api.jar").getAbsoluteFile());
	in.close();
	in = loader.getResourceAsStream("test.php");
	extractFile(in, new File(base, "test.php").getAbsoluteFile());
	in.close();
	
	// start back end
	(new Thread(new TestInstallation())).start();
	int count = 20;
	while(count-->0) {
	    Thread.sleep(500);
	    try {Socket s = new Socket("127.0.0.1", Integer.parseInt(socket)); if(s!=null) s.close(); break;} catch (IOException e) {/* ignore */}
	}
	if(count==0) throw new IOException("Could not start test servlet engine");
	// Fetch the Java.inc file
	URL url = new URL("http://127.0.0.1:"+socket+"/JavaBridge/java/Java.inc");
	URLConnection conn = url.openConnection();
	conn.connect();
	in = conn.getInputStream();
	extractFile(in, new File(java, "Java.inc").getAbsoluteFile());
	in.close();

	FileOutputStream o = new FileOutputStream(new File(base,"RESULT.html"));
	String php = "php-cgi";
	for(int i=0; i<DEFAULT_CGI_LOCATIONS.length; i++) {
	    File location = new File(DEFAULT_CGI_LOCATIONS[i]);
	    if(location.exists()) {php = location.getAbsolutePath(); break;}
	}
		
	// start front end
	String[] cmd = new String[] {php, "-n", "-d","allow_url_include=On",String.valueOf(new File(base,"test.php"))};
	System.err.println("Invoking php: " + Arrays.asList(cmd));
	HashMap env = (HashMap) processEnvironment.clone();
	env.put("SERVER_PORT", socket);
	env.put("X_JAVABRIDGE_OVERRIDE_HOSTS", "h:127.0.0.1:"+socket+"//JavaBridge/test.phpjavabridge");
	Process p;
	try {
	    p = Runtime.getRuntime().exec(cmd, hashToStringArray(env));
	} catch (java.io.IOException ex) {
	    throw new RuntimeException("Could not run PHP ("+Arrays.asList(cmd)+"), please check if php-cgi is in the path.", ex);
	}
	(new Thread(new TestInstallation(p))).start();
		
	InputStream i = p.getInputStream();
	int c;
	while((c=i.read())!=-1) o.write(c);
	i.close();
	p.getOutputStream().close();
	o.close();

	//p.destroy(); if(runner!=null) runner.destroy();
	
	System.out.flush();
	System.err.flush();
	System.out.println("\nNow check the " + new File(base, "RESULT.html."));
	System.out.println("Read the INSTALL.J2EE and/or INSTALL.J2SE documents.");

	try {
	    SwingUtilities.invokeAndWait(new SimpleBrowser(socket));
	} catch (Throwable err) {System.exit(0);}
    }
    private static void extractFile(InputStream in, File target) throws IOException {
	byte[] buf = new byte[8192];
	FileOutputStream out = new FileOutputStream(target);
	int c;
  	while((c = in.read(buf))!=-1) {
	    out.write(buf, 0, c);
	}
	out.close();
    }
}
