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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import php.java.bridge.JavaBridge;
import php.java.bridge.Request;
import php.java.bridge.Util;
import php.java.bridge.http.AbstractChannel;
import php.java.bridge.http.IContextFactory;

/**
 * @author jostb
 *
 */
final class HttpContextRunner implements Serializable {
    private static final long serialVersionUID = 6280393106090501730L;
    private Request request;
    private InputStream in;
    private OutputStream out;
    private AbstractChannel channel;
    private IContextFactory ctx;
    
    public HttpContextRunner(AbstractChannel channel, IContextFactory ctx) {
	this.channel = channel;
	this.ctx = ctx;
    }
    private byte shortPathHeader;
    private int readLength() throws IOException{
	byte buf[] = new byte[1];
	in.read(buf);
	shortPathHeader = (byte) (0xFF&buf[0]);
	
	buf = new byte[2];
	in.read(buf);
	return (0xFF&buf[0]) | (0xFF00&(buf[1]<<8));
    }
    private String readString(int length) throws IOException {
	byte buf[] = new byte[length];
	in.read(buf);
	return new String(buf, Util.ASCII);
    }

    private String readName() throws IOException {
	return readString(readLength());
    }
    /**
     * Sets a new Input/OutputStream into the bridge
     * @param bridge the JavaBridge
     * @param in the new InputStream
     * @param out the new OutputStream
     */
    private void setIO(JavaBridge bridge, InputStream in, OutputStream out) {
	bridge.request.reset();
    	bridge.in=in;
    	bridge.out=out;	
    }

    private boolean init() throws IOException {
	if(Util.logLevel>4) Util.logDebug("starting a new ContextRunner " + this);
	out = channel.getOuptutStream();
	in = channel.getInputStream();

	int c = in.read();
	if(c!=0177) {
	    
	    if(c==-1) return false; // client has closed the connection
	    
	    try {out.write(0); }catch(IOException e){}
	    throw new IOException("Protocol violation");
	}
	out.write(0); out.flush(); // dummy write: avoid ack delay
	readName();
    	JavaBridge bridge = ctx.getBridge();
	
	if (shortPathHeader != (byte) 0xFF) { // short path S1: no PUT request
	    bridge.request = new Request(bridge);
	    bridge.request.init(shortPathHeader);
	}
	setIO(bridge, in, out);
	this.request = bridge.request;
	
	ctx.initialize();
	return true;
    }

    /**{@inheritDoc}*/  
    public void run() {
	try {
	    if(init())
		request.handleRequests();
	    else
		Util.warn("context runner init failed");
	} catch (IOException e) {
	    if(Util.logLevel>4) Util.printStackTrace(e);
        } catch (Exception e) {
    	    Util.printStackTrace(e);
        } finally {
	    if(ctx!=null) {
		ctx.destroy();
	    }
	    channel.shutdown();
	}
    }
    
}
