/*-*- mode: Java; tab-width:8 -*-*/
package php.java.bridge;

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

/** numbers are base 16 */
class HexOutputBuffer extends ByteArrayOutputStream {
	/**
     * 
     */
    private final JavaBridge bridge;

    HexOutputBuffer(JavaBridge bridge) {
        this.bridge = bridge;
    }

    /*
     * Return up to 256 bytes. Useful for logging.
     */
    protected byte[] getFirstBytes() {
        int c = super.count;
        byte[] append = (c>256) ? Response.append_for_OutBuf_getFirstBytes : Response.append_none_for_OutBuf_getFirstBytes;
        if(c>256) c=256;
        byte[] ret = new byte[c+append.length];
        System.arraycopy(super.buf, 0, ret, 0, c);
        System.arraycopy(append, 0, ret, ret.length-append.length, append.length);
        return ret;
    }

	protected void append(byte[] s) {
        try {
    	write(s);
        } catch (IOException e) {/*not possible*/}
    }

	protected void appendQuoted(byte[] s) {
        for(int i=0; i<s.length; i++) {
    	byte ch;
    	switch(ch=s[i]) {
    	case '&':
    	    append(Response.amp);
    	    break;
    	case '\"':
    	    append(Response.quote);
    	    break;
    	default:
    	    write(ch);
    	}
        }
	}
	protected void appendQuoted(String s) {
        appendQuoted(bridge.options.getBytes(s));
	}

	private byte[] buf = new byte[16];
	/** append an unsigned long number */
	protected void append(long i) {
        int pos = 16;
	    do {
	        buf[--pos] = Util.HEX_DIGITS[(int)(i & 0xF)];
	        i >>>= 4;
	    } while (i != 0);
	    write(buf, pos, 16-pos);
    }

	/** append a double value, base 10 for now (not every C compiler supports the C99 "a" conversion) */ 
    protected void append(double d) {
      append(Double.toString(d).getBytes());
    }

    /** append a long number */
    protected void appendLong(long l) {
        append(Response.L);
        if(l<0) {
            append(-l);
            append(Response.pa);
        } else {
            append(l);
            append(Response.po);
        }
    }
    /** append a string */
    protected void appendString(byte s[]) {
        append(Response.S);
        appendQuoted(s);
    }
}
