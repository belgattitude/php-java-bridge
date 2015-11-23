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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import php.java.bridge.Util;
import php.java.bridge.http.IFCGIProcess;

/**
 * Represents the FastCGI process.
 * 
 * @author jostb
 *
 */
class FCGIProcess extends Util.Process implements IFCGIProcess {
    String realPath;
    public FCGIProcess(String[] args, boolean includeJava, String cgiDir, String pearDir, String webInfDir, File homeDir, Map env, String realPath, boolean tryOtherLocations, boolean preferSystemPhp) throws IOException {
	super(args, includeJava, cgiDir, pearDir, webInfDir, homeDir, env, tryOtherLocations, preferSystemPhp);
	this.realPath = realPath;
    }
    protected String[] getArgumentArray(String[] php, String[] args) {
        LinkedList buf = new LinkedList();
        if(Util.USE_SH_WRAPPER) {
	    buf.add("/bin/sh");
	    buf.add(realPath+File.separator+"launcher.sh");
	    buf.addAll(java.util.Arrays.asList(php));
	    for(int i=1; i<args.length; i++) {
		buf.add(args[i]);
	    }
	    buf.addAll(java.util.Arrays.asList(Util.ALLOW_URL_INCLUDE));
        } else {
	    buf.add(realPath+File.separator+"launcher.exe");
	    buf.addAll(java.util.Arrays.asList(php));
	    for(int i=1; i<args.length; i++) {
		buf.add(args[i]);
	    }
	    buf.addAll(java.util.Arrays.asList(Util.ALLOW_URL_INCLUDE));
	}
        return (String[]) buf.toArray(new String[buf.size()]);
    }
    /* (non-Javadoc)
     * @see php.java.servlet.fastcgi.IFCGIProcess#start()
     */
    public void start() throws NullPointerException, IOException {
        super.start();
    }
}
