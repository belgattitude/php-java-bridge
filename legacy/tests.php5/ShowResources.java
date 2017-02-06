import java.io.*; 
import java.net.*;
import java.util.*; 
import java.util.jar.*; 
 
public class ShowResources {
    public static void main(String args[]) throws Throwable { 
	List resources = listResources(ShowResources.class.getClassLoader()); 
	if (args.length == 0) { 
	    for (Iterator iter = resources.iterator(); iter.hasNext(); ) {
		System.out.println(((URL) iter.next()).toExternalForm()); 
	    } 
	} 
	else {
	    for (Iterator iter = resources.iterator(); iter.hasNext(); ) {
		URL url = (URL) iter.next();
		String urlString = url.toExternalForm();
		for (int i = 0; i < args.length; i++) { 
		    if (urlString.indexOf(args[i]) != -1) { 
			System.out.println("Found resource: " + urlString); 
			System.out.println("First few chars: " + readAFewChars(url)); 
			System.out.println(); 
			break;
		    } 
		} 
	    } 
	} 
    } 
 
 
    private static List listResources(ClassLoader cl) throws IOException, MalformedURLException { 
	List resources = new ArrayList(); 
	while (cl != null) {
	    if (cl instanceof URLClassLoader) { 
		URLClassLoader ucl = (URLClassLoader) cl; 
		URL[] urls = ucl.getURLs(); 
		for (int i = 0; i < urls.length; i++) { 
		    URL url = urls[i]; 
		    if (url.getFile().endsWith(".jar")) { 
			listJarResources(new URL("jar:" + url.toExternalForm() + "!/"), 
					 resources); 
		    } 
		    else if (url.getProtocol().equals("file")) {
			File file = new File(url.getFile());
			if (file.isDirectory()) { 
			    listDirResources(file, resources);
			} 
		    } 
		} 
	    } 
	    cl = cl.getParent();
	} 
	return resources; 
    } 
 
 
    private static void listDirResources(File dir, List resources)
	throws MalformedURLException {
	File[] files = dir.listFiles(); 
	for (int i = 0; i < files.length; i++) {
	    File file = files[i]; 
	    resources.add(file.toURL());
	    if (file.isDirectory()) { 
		listDirResources(file, resources);
	    } 
	} 
    } 
 
 
    private static void listJarResources(URL jarUrl, List resources)
	throws IOException, MalformedURLException { 
	JarURLConnection jarConnection =
	    (JarURLConnection) jarUrl.openConnection(); 
 
	JarFile file = null;
	try { 
	    file = jarConnection.getJarFile(); 
	} catch (java.util.zip.ZipException ex) { 
	    System.err.println("could not open: "+file); 
	    return; 
	}
	for (Enumeration entries = jarConnection.getJarFile().entries();
	     entries.hasMoreElements(); ) {
	    JarEntry entry = (JarEntry) entries.nextElement();
	    resources.add(new URL(jarUrl, entry.getName()));
	} 
    } 
 
 
    private static String readAFewChars(URL url) throws IOException { 
	StringBuffer buf = new StringBuffer(10);
	Reader reader = new InputStreamReader(url.openStream());
	for (int i = 0; i < 10; i++) {
	    int c = reader.read();
	    if (c == -1) {
		break;
	    } 
	    buf.append((char) c); 
	} 
	reader.close(); 
	return buf.toString();
    } 
} 
