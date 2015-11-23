/*-*- mode: Java; tab-width:8 -*-*/

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

package php.java.bridge;

import java.io.FileNotFoundException;
import java.util.Date;

/**
 * The classic PHP/Java Bridge logger, doesn't need any external libraries.
 */
class FileLogger implements ILogger { 
    static boolean haveDateFormat=true;
    private static Object _form;
    private boolean isInit = false;
    private void init() {
	if(Util.logStream==null) {
	    if((Util.DEFAULT_LOG_FILE==null) || (Util.DEFAULT_LOG_FILE.trim().length()==0))  Util.logStream = System.err;
	    else 
		try {
		    Util.logStream=new java.io.PrintStream(new java.io.FileOutputStream(Util.DEFAULT_LOG_FILE));
		} catch (FileNotFoundException e1) {Util.logStream=System.err;}
	}
	isInit = true;
    }
    /**
     * Create a String containing the current date/time.
     * @return The date/time as a String
     */
    public String now() {
	if(!haveDateFormat) return String.valueOf(System.currentTimeMillis());
	try {
	    if(_form==null)
		_form = new java.text.SimpleDateFormat("MMM dd HH:mm:ss", java.util.Locale.ENGLISH);
	    return ((java.text.SimpleDateFormat)_form).format(new Date());
	} catch (Throwable t) {
	    haveDateFormat=false;
	    return now();
	}
    }
    /**
     * Log a message
     * @param s  The message
     */
    public void log(String s) {
	if(!isInit) init();
	byte[] bytes = null;
	try {
	    bytes = s.getBytes(Util.UTF8);
	} catch (java.io.UnsupportedEncodingException e) {
	    Util.printStackTrace(e);
	    bytes = s.getBytes();
	}
	Util.logStream.write(bytes, 0, bytes.length);
	Util.logStream.println("");
	Util.logStream.flush();
    }
   	
    /* (non-Javadoc)
     * @see php.java.bridge.ILogger#printStackTrace(java.lang.Throwable)
     */
    public void printStackTrace(Throwable t) {
	if(!isInit) init();
   	if(Util.logLevel > 0) {
   	    if (t instanceof Error) {
   	        Util.println(1, "An error occured: " + t);
   	    } else if (Util.logLevel > 1) {
   	        Util.println(2, "An exception occured: " + t);
   	    }
   	    t.printStackTrace(Util.logStream);
   	}
    }
    /* (non-Javadoc)
     * @see php.java.bridge.ILogger#log(int, java.lang.String)
     */
    public void log(int level, String msg) {
	StringBuffer b = new StringBuffer(now());
	b.append(" "); b.append(Util.EXTENSION_NAME); b.append(" ");
	switch(level) {
	case 1: b.append("FATAL"); break;
	case 2: b.append("ERROR"); break;
	case 3: b.append("INFO "); break;
	case 4: b.append("DEBUG"); break;
	default: b.append(level); break;
	}
	b.append(": ");
	b.append(msg);
	log(b.toString());
    }
    public void warn(String msg) {
	StringBuffer b = new StringBuffer(now());
	b.append(" "); b.append(Util.EXTENSION_NAME); b.append(" ");
	b.append("WARNING");
	b.append(": ");
	b.append(msg);
	log(b.toString());	  
    }
    public String toString() {
	return "DefaultLogger";
    }
}
