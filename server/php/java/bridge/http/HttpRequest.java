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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import php.java.bridge.NotImplementedException;

/**
 * A simple HTTP request implementation.
 * @author jostb
 *
 */
public class HttpRequest {
    private HashMap headers;
    private String method;
    private String uri;
    
    private InputStream in;

    private byte [] buf;
    private int bufStart = 0;
    private int bufEnd = 0;

    private int contentLength = -1;
    private int count = 0;
	
    /**
     * Create a new HTTP request
     * @param inputStream The InputStream
     */
    public HttpRequest(InputStream inputStream) {
	in = new HttpInputStream(new BufferedInputStream(inputStream));
	headers = new HashMap();
    }

    /**
     * Returns the header value
     * @param string The header
     * @return The header value
     */
    public String getHeader(String string) {
	return (String) headers.get(string);
    }

    /**
     * Return the request Method
     * @return GET, PUT or POST
     */
    public String getMethod() {
	return method;
    }
    /**
     *  Return the request URI
     * @return The request URI
     */
    public String getRequestURI() {
	return uri;
    }
    /**
     * Returns the InputStream
     * @return The InputStream
     */
    public InputStream getInputStream() {
	return in; 
    }

    /** 
     * Push back some bytes so that we can read them again.
     * @param buf The buffer
     * @param start The start position
     * @param length The number of bytes
     */
    public void pushBack(byte[] buf, int start, int length) {
	this.buf = buf;
	this.bufStart = start;
	this.bufEnd = length+start;
    }

    private class HttpInputStream extends InputStream {

	private InputStream in;
		
		
	public HttpInputStream (InputStream in) {
	    this.in = in;
	}
		
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
	    throw new NotImplementedException();
	}
		
	public int read(byte[] b, int start, int length) throws IOException {
	    if(contentLength > -1 && count==contentLength) return -1;
			
	    if(bufStart!=bufEnd) {
		if(bufEnd - bufStart < length) length = bufEnd - bufStart;
		System.arraycopy(buf, bufStart, b, start, length);
		bufStart += length;
		count += length;
		return length;
	    }
	    int n = in.read(b, start, length);
	    count += n;
	    return n;
	}
	public int read(byte[] b) throws IOException {
	    return read(b, 0, b.length);
	}
    }

    /**
     * Add a header
     * @param line A valid HTTP header, e.g. "Host: localhost"
     */
    public void addHeader(String line) {
	try {
	    headers.put
		(line.substring(0, line.indexOf(":")).trim(),
		 line.substring(line.indexOf(":") + 1).trim());
	}
	catch (StringIndexOutOfBoundsException e) { /* not a valid header, assume method */
	    int i1=-1, i2=-1;
	    i1 = line.indexOf(' ');
	    if(i1!=-1) {
		method = (line.substring(0, i1)).trim().toUpperCase().intern();
		i2 = line.indexOf(' ', i1+1); 
	    }
	    if(i2>i1) uri = line.substring(i1+1, i2);
	}
    }

    /**
     * Set the content length, it causes the InputStream to stop reading at some point.
     * @param contentLength The content length.
     * @see HttpRequest#getInputStream()
     */
    public void setContentLength(int contentLength) {
	this.count = 0;
	this.contentLength = contentLength;
    }
    /** Close the request 
     * @throws IOException */
    public void close() throws IOException {
	try {
	    if(in!=null) in.close();
	} finally {
	    in = null;
	}
    }
}
