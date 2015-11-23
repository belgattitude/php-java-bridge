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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import php.java.bridge.NotImplementedException;
import php.java.bridge.Util;

/**
 * A FastCGI OutputStream
 * @author jostb
 *
 */
public class FCGIOutputStream extends FCGIConnectionOutputStream {
    public void write(int type, byte buf[]) throws FCGIConnectionException {
        write(type, buf, buf.length);
    }
    /**
     * Write a FCGI packet
     * @param type the packet type
     * @param buf the output buffer
     * @param length the packet length
     * @throws FCGIConnectionException
     */
    public void write(int type, byte buf[], int length) throws FCGIConnectionException {
        int requestId = 1;
        byte[] header = new byte[] {
	    1, (byte)type, 
	    (byte)((requestId >> 8) & 0xff), (byte)((requestId) & 0xff),
	    (byte)((FCGIUtil.FCGI_BUF_SIZE >> 8) & 0xff), (byte)((FCGIUtil.FCGI_BUF_SIZE) & 0xff),
	    0, //padding
	    0};
        int contentLength = length;
        int pos=0;
        while(pos + FCGIUtil.FCGI_BUF_SIZE <= contentLength) {
	    write(header);
	    write(buf, pos, FCGIUtil.FCGI_BUF_SIZE);
	    pos += FCGIUtil.FCGI_BUF_SIZE;
        }
        contentLength = length % FCGIUtil.FCGI_BUF_SIZE;
        header[4] = (byte)((contentLength >> 8) & 0xff);
        header[5] = (byte)((contentLength) & 0xff);
        write(header);
        write(buf, pos, contentLength);
    }
    /**
     * Start the FCGI_RESPONDER conversation
     * @throws FCGIConnectionException
     */
    public void writeBegin() throws FCGIConnectionException {
        int role = FCGIUtil.FCGI_RESPONDER;
        byte[] body = new byte[] {
	    (byte)((role >> 8) & 0xff), (byte)((role) & 0xff),
	    FCGIUtil.FCGI_KEEP_CONN,
	    0,0,0,0,0};
            
        write(FCGIUtil.FCGI_BEGIN_REQUEST, body);
    }
    private void writeLength(ByteArrayOutputStream out, int keyLen) throws IOException {
        if (keyLen < 0x80) {
	    out.write((byte)keyLen);
        }else {
	    byte[] b = new byte[] {
		(byte)(((keyLen >> 24) | 0x80) & 0xff),
		(byte)((keyLen >> 16) & 0xff),
		(byte)((keyLen >> 8) & 0xff),
		(byte)keyLen};
		out.write(b);
        }
    }
    /**
     * Write FCGI Params according to FCGI spec
     * @param props
     * @throws FCGIConnectionException
     */
    public void writeParams(Map props) throws FCGIConnectionException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(Iterator ii = props.keySet().iterator(); ii.hasNext();) {
	    Object k = ii.next();
	    Object v = props.get(k);
	    String key = String.valueOf(k);
	    String val = String.valueOf(v);
	    int keyLen = key.length();
	    int valLen = val.length();
	    if(keyLen==0 || valLen==0) continue;
	    try {
	    	writeLength(out, keyLen);
	    	writeLength(out, valLen);
		out.write(key.getBytes(Util.ASCII)); 	
		out.write(val.getBytes(Util.ASCII));
	    } catch (IOException e) {
		throw new FCGIConnectionException(connection, e);
	    }
        }
        write(FCGIUtil.FCGI_PARAMS, out.toByteArray());
        write(FCGIUtil.FCGI_PARAMS, FCGIUtil.FCGI_EMPTY_RECORD);
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) {
        throw new NotImplementedException();
    }

}