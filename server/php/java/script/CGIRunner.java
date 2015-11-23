/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script;

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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import php.java.bridge.ILogger;
import php.java.bridge.Util;
import php.java.bridge.http.HeaderParser;
import php.java.bridge.http.OutputStreamFactory;

/**
 * This class can be used to run a PHP CGI binary. Used only when
 * running local php scripts.  To allocate and invoke remote scripts
 * please use a HttpProxy and a URLReader instead.
 *  
 * @author jostb
 *
 * @see php.java.bridge.http.HttpServer
 * @see php.java.script.URLReader
 * @see php.java.script.HttpProxy
 */

public class CGIRunner extends Continuation {

    protected Reader reader;
    protected CGIRunner(Reader reader, Map env, OutputStream out,
            OutputStream err, HeaderParser headerParser,
            ResultProxy resultProxy, ILogger logger) {
	super(env, out, err, headerParser, resultProxy);
	this.reader = reader;
    }

    private Writer writer;
    protected void doRun() throws IOException, Util.Process.PhpException {
        Util.Process proc = Util.ProcessWithErrorHandler.start(new String[] {null}, false, null, null, null, null, env, true, true, err);

	InputStream natIn = null;
	try {
	natIn = proc.getInputStream();
	OutputStream natOut = proc.getOutputStream();
	writer = new BufferedWriter(new OutputStreamWriter(natOut));

	(new Thread() { // write the script asynchronously to avoid deadlock
	    public void doRun() throws IOException {
		char[] cbuf = new char[Util.BUF_SIZE]; 
		int n;    
		while((n = reader.read(cbuf))!=-1) {
		    //System.err.println("SCRIPT:::"+new String(cbuf, 0, n));
		    writer.write(cbuf, 0, n);
		}
	    }
	    public void run() { 
		    try {
			    doRun(); 
		    } catch (IOException e) {
			    Util.printStackTrace(e);
		    } finally {
			    try {
				    writer.close();
			    } catch (IOException ex) {
				    /*ignore*/
			    }
		    }
	    }
	}).start();

	byte[] buf = new byte[Util.BUF_SIZE];
	HeaderParser.parseBody(buf, natIn, new OutputStreamFactory() { public OutputStream getOutputStream() throws IOException {return out;}}, headerParser);
	proc.waitFor();
	resultProxy.setResult(proc.exitValue());
	} catch (IOException e) {
	    Util.printStackTrace(e);
	    throw e;
	} catch (InterruptedException e) {
		/*ignore*/
	} finally {
	    if(natIn!=null) try {natIn.close();} catch (IOException ex) {/*ignore*/}
	    try {proc.destroy(); } catch (Exception e) { Util.printStackTrace(e); }
	}
	
	proc.checkError();
    }
}
