/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import php.java.bridge.Util;

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
 * A procedure class which can be used to capture the HTTP header strings.
 * Example:<br>
 * <code>
 * Util.parseBody(buf, natIn, out, new Util.HeaderParser() {protected void parseHeader(String header) {System.out.println(header);}});<br>
 * </code>
 * @author jostb
 * @see HeaderParser#parseBody(byte[], InputStream, OutputStreamFactory, HeaderParser)
 */
public abstract class HeaderParser {
    /**
     * The default CGI header parser. The default implementation discards everything.
     */
    public static final HeaderParser DEFAULT_HEADER_PARSER = new SimpleHeaderParser();
    /**
     * Parse a header
     * @param header The header string to parse
     */
    public abstract void parseHeader(String header);
    /**
     * Add a header
     * @param key the key
     * @param val the value
     */
    public abstract void addHeader (String key, String val);
    /**
     * Discards all header fields from a HTTP connection and write the body to the OutputStream
     * @param buf A buffer, for example new byte[BUF_SIZE]
     * @param natIn The InputStream
     * @param out The OutputStream
     * @param parser The header parser
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static void parseBody(byte[] buf, InputStream natIn, OutputStreamFactory out, HeaderParser parser) throws UnsupportedEncodingException, IOException {
	int i = 0, n, s = 0;
	boolean eoh = false;
	boolean rn = false;
	String remain = null;
	String line;

	// the header and content
	while((n = natIn.read(buf)) !=-1 ) {
	    //System.err.println("HEADER:::"+new String(buf, 0, n, "ASCII"));
	    int N = i + n;
	    // header
	    while(!eoh && i<N) {
		switch(buf[i++]) {

		case '\n':
		    if(rn) {
			eoh=true;
		    } else {
			if (remain != null) {
			    line = remain + new String(buf, s, i-s, Util.ASCII);
			    line = line.substring(0, line.length()-2);
			    remain = null;
			} else {
			    line = new String(buf, s, i-s-2, Util.ASCII);
			}
			parser.parseHeader(line);
			s=i;
		    }
		    rn=true;
		    break;

		case '\r': break;

		default: rn=false;	

		}
	    }
	    // body
	    if(eoh) {
		if(i<N) {
		    //System.err.println("BODY:::"+new String(buf, i, N-i, "ASCII")); 
		    out.getOutputStream().write(buf, i, N-i);
		}
	    }  else { 
		if (remain != null) {
		    remain += new String(buf, s, i-s, Util.ASCII);
		} else {
		    remain = new String(buf, s, i-s, Util.ASCII);
		}
	    }
	    s = i = 0;
	}
    }
}