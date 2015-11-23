/*-*- mode: Java; tab-width:8 -*-*/

package php.java.servlet;

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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * A servlet response which writes its output to an internal buffer. The buffer can be fetched using
 * "getBufferContents()". May be used by remote PHP scripts (those accessing PhpJavaServlet) through the "java_context()->getHttpServletResponse()" API. 
 * Also used by the "java_virtual()" API.
 * 
 * @author jostb
 *
 */
public class RemoteHttpServletResponse extends HttpServletResponseWrapper implements BufferedResponse {
    
    private ByteArrayOutputStream buffer;

    public RemoteHttpServletResponse(HttpServletResponse res) {
	super(res);
	this.buffer = new ByteArrayOutputStream();
    }
    public byte[] getBufferContents() throws IOException {
	committed = true;
	flushBuffer();
	return buffer.toByteArray();
    }
    public void flushBuffer() throws IOException {
	getWriter().flush();
    }

    public int getBufferSize() {
	return buffer.size();
    }

    private ServletOutputStream out = null;
    public ServletOutputStream getOutputStream() throws IOException {
	if (out!=null) return out;
	return out = new ServletOutputStream() {
	    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		buffer.write(arg0, arg1, arg2);
	    }
	    public void write(int arg0) throws IOException {
		buffer.write(arg0);
	    }};
    }
    private PrintWriter writer = null;
    public PrintWriter getWriter() throws IOException {
	if (writer != null) return writer;
        return writer = new PrintWriter(getOutputStream());
    }

    private boolean committed; 
    public boolean isCommitted() {
	return committed;
    }

    public void reset() {
	buffer.reset();
    }

    public void resetBuffer() {
	reset();
    }

    public void setBufferSize(int arg0) {
    }
}