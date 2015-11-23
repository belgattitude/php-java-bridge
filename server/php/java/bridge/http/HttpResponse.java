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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import php.java.bridge.NotImplementedException;
import php.java.bridge.Util;

/**
 * A simple HTTP response implementation.
 * @author jostb
 *
 */
public class HttpResponse {
	
    private HashMap headers;
    private OutputStream outputStream;
	
    private boolean headersWritten;

    /**
     * Create a new HTTP response with the given OutputStream
     * @param outputStream The OutputStream.
   	/*  */
    public HttpResponse(OutputStream outputStream) {
	this.headers = new HashMap();
	
	this.outputStream = new BufferedOutputStream (outputStream){ 
	    public void write (byte buf[], int pos, int len) throws IOException {
		if (!headersWritten) {
		    headersWritten = true;
		    writeHeaders();
		}
		super.write(buf, pos, len);
	    }
	};
    }
    /**
     * Set the response header
     * @param string The header key
     * @param val The header value.
     */
    public void setHeader(String string, String val) {
	headers.put(string, val);
    }

    /**
     * Returns the OutputStream of the response. setContentLength() must be called before.
     * @return The OutputStream
     * @see HttpRequest#setContentLength(int)
     */
    public OutputStream getOutputStream() {
	return outputStream;
    }

    /**
     * Set the response status. Not implemented.
     * @param code
     */
    public void setStatus(int code) {
	throw new NotImplementedException();
    }

    /**
     * Add a response header, in this implementation identical to setHeader
     * @param string The header
     * @param string2 The header value
     * @see HttpResponse#setHeader(String, String)
     */
    public void addHeader(String string, String string2) {
	setHeader(string, string2);
    }

    private final byte[] h1 = Util.toBytes("HTTP/1.0 200 OK\r\n"); 
    private final byte[] h2 = Util.toBytes("Host: localhost\r\nConnection: close\r\n"); 
    private final byte[] h4 = Util.toBytes(": ");
    private void writeHeaders() throws IOException {
    	java.io.ByteArrayOutputStream out = new ByteArrayOutputStream();
    	out.write(h1);
    	out.write(h2);
    	for(Iterator ii = headers.keySet().iterator(); ii.hasNext(); ) {
	    Object key = ii.next();
	    Object val = headers.get(key);
	    out.write(Util.toBytes((String)key));
	    out.write(h4);
	    out.write(Util.toBytes((String)val));
	    out.write(Util.RN);
    	}
    	out.write(Util.RN);
    	out.writeTo(outputStream);
    }
    /**
     * Set the content length of the response. Sets the "Content-Length" header value.
     * @param length The content length
     * @throws IOException
     * @see HttpResponse#getOutputStream()
     */
    public void setContentLength(int length) throws IOException {
	setHeader("Content-Length", String.valueOf(length));
    }

    /** Close the response 
     * @throws IOException */
    public void close() throws IOException {
	try {
	    if(outputStream!=null) outputStream.close();
	} finally {
	    outputStream = null;
	}
    }
    public void flushBuffer() throws IOException {
	if (outputStream != null) outputStream.flush();
    }
}
