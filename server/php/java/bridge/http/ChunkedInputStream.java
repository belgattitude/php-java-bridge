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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import php.java.bridge.Util;

/**
 * An output stream which reads data in HTTP chunks.
 * 
 * @author jostb
 */

public class ChunkedInputStream extends FilterInputStream {

    /**
     * Maps ASCII to HEX code.
     */
    public static final int[] ASCII_HEX = getAscii();
    private boolean eof = false;
    
    private static int[] getAscii() {
	int[] ascii = new int[103];
	for (int i=48; i<58; i++) {
	    ascii[i]=i-48;
	};
	for (int i=65; i<71; i++) {
	    ascii[i]=i-55;
	};
	for (int i=97; i<103; i++) {
	    ascii[i]=i-87;
	};
	return ascii;
    }
    /**
     * Create a new chunked input stream
     * @param in The input stream
     */
    public ChunkedInputStream(InputStream in) {
	super(new BufferedInputStream(in));
    }
    private byte[] rn = new byte[2];
    private byte[] remaining;
    private int remainPos, remainLen;
   
    /**{@inheritDoc}*/
   public int read(byte[] buf, int pos, int len) throws IOException {
	int c, i;
	int count;

	if (len <= 0) return len;
	if (eof) return -1;

	// check remaining
	if (remaining != null) {
	    if (len < remainLen) {
		System.arraycopy(remaining, remainPos, buf, pos, len);
		remainPos += len;
		remainLen -= len;
		return len;
	    } else {
		System.arraycopy(remaining, remainPos, buf, pos, remainLen);
            remaining = null;
            return remainLen;
	    }
	}

	// read next packet
	int packetLen = readPacketLength();
	if (packetLen == 0) eof = true;
	
	
	remaining = new byte[packetLen]; remainLen = 0;
	for(c=0; (i=in.read(remaining, c, packetLen-c)) > 0; c+=i)
	    ;
	if ((c != packetLen)) throw new IOException("read chunked");

	for(c=0; (i=in.read(rn, c, 2-c)) > 0; c+=i)
	    ;
	if ((c != 2)) throw new IOException("read \r\n");

	
	// store remaining
	if (len >= packetLen) {
	    count = packetLen;
	    System.arraycopy(remaining, 0, buf, pos, packetLen);
	    remaining = null;
	} else {
	    count = len;
	    System.arraycopy(remaining, 0, buf, pos, len);
	    remainPos = len;
	    remainLen = packetLen - len;
	}
	return count;
    }
    private int readPacketLength() throws IOException {
	int n = 0;
	int c;
	
	while ((c = in.read()) != '\r') {
	    if (c == -1) throw new IllegalStateException ("read chunked packet length");
	    if (c == 32) continue; // skip white space
	    n <<= 4;
	    n += ASCII_HEX[c];
	}
	if (in.read() == -1) throw new IOException ("read chunked packet length");

	return n;
    }
    private byte[] buf = new byte[Util.BUF_SIZE];

    /**
     * read trailing 0\r\n
     * @throws IOException
     */
    public void eof() throws IOException {
	// consume remaining 0\r\n
	read(buf, 0, buf.length);
    }
}
