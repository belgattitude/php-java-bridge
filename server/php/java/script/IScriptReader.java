/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script;

/*
 * Copyright (C) 2003-2007 Jost Boekemeier and others.
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
import java.io.OutputStream;
import java.util.Map;

import php.java.bridge.Util;
import php.java.bridge.http.HeaderParser;

/**
 * Read data from a URL or from a servlet and write the result to the output stream and a header parser.
 * 
 * @author jostb
 *
 */
public interface IScriptReader {

    /** These header values appear in the environment map passed to PHP */
    public static final String[] HEADER = new String[]{Util.X_JAVABRIDGE_CONTEXT, Util.X_JAVABRIDGE_OVERRIDE_HOSTS, 
	Util.X_JAVABRIDGE_INCLUDE_ONLY, Util.X_JAVABRIDGE_INCLUDE, Util.X_JAVABRIDGE_REDIRECT, Util.X_JAVABRIDGE_OVERRIDE_HOSTS_REDIRECT};

    /**
     * Read from the URL and write the data to out.
     * @param env The environment, must contain values for X_JAVABRIDGE_CONTEXT. It may contain X_JAVABRIDGE_OVERRIDE_HOSTS.
     * @param out The OutputStream.
     * @param headerParser The header parser
     * @throws IOException
     * @throws ServletException 
     */
    public abstract void read(Map env, OutputStream out,
	    HeaderParser headerParser) throws IOException;

}