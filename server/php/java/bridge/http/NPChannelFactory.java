/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.util.Map;

import php.java.bridge.Util;
import php.java.bridge.Util.Process;

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
 * A factory which creates FastCGI "named pipe" channels.
 * @author jostb
 */

public class NPChannelFactory extends FCGIConnectionFactory {
    /**
     * The named pipe prefix
     */
    public static final String PREFIX="\\\\.\\pipe\\";
    
    private String raPath;
    private String testRaPath;
    
    /**
     * Create a new factory using a given processFactory
     * @param processFactory the FCGIProcessFactory
     */
    public NPChannelFactory(IFCGIProcessFactory processFactory) {
	super(processFactory);
    }
    /**
     * Tests if the channel will be available.
     * @throws FCGIConnectException
     */
    public void test() throws FCGIConnectException {
	if(!new File(raPath).canWrite()){
	    String reason = "File " + raPath + " not writable";
	    if (lastException != null) {
		throw new FCGIConnectException(reason, lastException);
	    }
	    throw new FCGIConnectException(new IOException(reason));
	}
    }
    private NPChannel doConnect() throws FCGIConnectException {
	try {
	    return new NPChannel(new RandomAccessFile(raPath, "rw"));
	} catch (IOException e) {
	    throw new FCGIConnectException(e);
	}
    }
    /**
     * Create a FCGIConnection
     * @throws FCGIConnectException
     */
    public FCGIConnection connect() throws FCGIConnectException {
	return doConnect();
    }

    protected Process doBind(Map env, String php, boolean includeJava) throws IOException {
        if(proc!=null) return null;
	if(raPath==null) throw new IOException("No pipe name available.");
	// Set override hosts so that php does not try to start a VM.
	// The value itself doesn't matter, we'll pass the real value
	// via the (HTTP_)X_JAVABRIDGE_OVERRIDE_HOSTS header field
	// later.
	File home = null;
	if(php!=null) try { home = ((new File(php)).getParentFile()); } catch (Exception e) {Util.printStackTrace(e);}
	proc = processFactory.createFCGIProcess(new String[]{php, raPath}, includeJava, home, env);
	proc.start();
	return (Process)proc;
    }
    protected void waitForDaemon() throws UnknownHostException, InterruptedException {
	Thread.sleep(5000);
    }
    
    /**
     * Return the OS command used to start the FCGI process
     * @return the fcgi start command.
     */
    public String getFcgiStartCommand(String base, String php_fcgi_max_requests) {
	String msg =
	    "cd \"" + base + File.separator + Util.osArch + "-" + Util.osName+ "\"\n" + 
	    "set %REDIRECT_STATUS%=200\n"+ 
	    "set %X_JAVABRIDGE_OVERRIDE_HOSTS%=/\n"+ 
	    "set %PHP_FCGI_CHILDREN%=5\n"+ 
	    "set %PHP_FCGI_MAX_REQUESTS%=\""+php_fcgi_max_requests+"\"\n"+
	    "\"c:\\Program Files\\PHP\\php-cgi.exe\" -v\n"+
	    ".\\launcher.exe \"c:\\Program Files\\PHP\\php-cgi.exe\" \"" + getPath() +"\"\n\n";
        return msg;
    }
    /**
     * Find a free socket port. After that {@link #setDynamicPort()} should be called
     * @param select wether or not some hard-coded path should be used
     */
    public void findFreePort(boolean select) {
	try {
	    if(select) {
		File testRafile = File.createTempFile("JavaBridge", ".socket");
		testRaPath = PREFIX+testRafile.getCanonicalPath();
		testRafile.delete();
	    } else {
		testRaPath  = FCGIUtil.FCGI_PIPE;
	    }
	} catch (IOException e) {
	    Util.printStackTrace(e);
	}
    }
    /**
     * Set the a default port, overriding the port selected by {@link #findFreePort(boolean)}
     */
    public void setDefaultPort() {
	raPath=FCGIUtil.FCGI_PIPE;
    }
    /**
     * Set the dynamic port selected by {@link #findFreePort(boolean)}
     */
    public void setDynamicPort() {
	raPath=testRaPath;
    }
    /**
     * Return the path selected by {@link #setDefaultPort()} or {@link #setDynamicPort()}
     * @return the path
     */
    public String getPath() {
	return raPath;
    }
    /** 
     * Return the channel name 
     * @return the channel name
     * 
     */
    public String toString() {
	return "ChannelName@" + (getPath()==null ? "<not initialized>" : getPath());
    }
}
