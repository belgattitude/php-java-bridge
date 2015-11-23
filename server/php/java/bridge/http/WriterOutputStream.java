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

import java.io.IOException;
import java.io.Writer;

import php.java.bridge.Util;

/**
 * A PrintWriter backed by an OutputStream.
 * @author jostb
 *
 */
public class WriterOutputStream extends DefaultCharsetWriterOutputStream {
    
    protected String charsetName = Util.DEFAULT_ENCODING;
    private boolean written = false;
    
    /**
     * The encoding used for char[] -&gt; byte[] conversion
     * @param charsetName
     */
    public void setEncoding(String charsetName) {
	if(written) throw new IllegalStateException("setEncoding");
	this.charsetName = charsetName;
    }
    /**
     * Create a new PhpScriptWriter.
     * @param out The OutputStream
     */
    public WriterOutputStream(Writer out) {
	super(out);
    }
    /**{@inheritDoc}*/
    public void write(byte b[], int off, int len) throws IOException {
	written = true;
	String s = new String (b, off, len, charsetName);
	out.write(s);
    }
 }
