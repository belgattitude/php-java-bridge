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

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A PrintWriter backed by an OutputStream.
 * @author jostb
 *
 */
public class PhpScriptWriter extends PrintWriter {

    OutputStream out;
	
    /**
     * Create a new PhpScriptWriter.
     * @param out The OutputStream
     */
    public PhpScriptWriter(OutputStream out) {
        super(out);
        if(out==null) throw new NullPointerException("out");
	this.out = out;
    }
	
    /**
     * Returns the OutputStream.
     * @return The OutputStream.
     */
    public OutputStream getOutputStream() {
	return out;
    }
    
    /**{@inheritDoc}*/
   public void close () {
	flush ();
    }
}
