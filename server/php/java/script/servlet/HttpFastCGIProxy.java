/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script.servlet;

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
import java.io.OutputStream;
import java.util.Map;

import php.java.bridge.Util;
import php.java.bridge.http.FCGIConnectionPool;
import php.java.bridge.http.FCGIInputStream;
import php.java.bridge.http.FCGIOutputStream;
import php.java.bridge.http.FCGIUtil;
import php.java.bridge.http.HeaderParser;
import php.java.bridge.http.OutputStreamFactory;
import php.java.script.Continuation;
import php.java.script.ResultProxy;

/**
  * This class can be used to connect to a FastCGI server.
 * 
 * @author jostb
 * @see php.java.script.FastCGIProxy
 */

public class HttpFastCGIProxy extends Continuation {
    private FCGIConnectionPool fcgiConnectionPool;

    public HttpFastCGIProxy(Map env, OutputStream out,
            OutputStream err, HeaderParser headerParser,
            ResultProxy resultProxy, FCGIConnectionPool fcgiConnectionPool) {
	super(env, out, err, headerParser, resultProxy);
	this.fcgiConnectionPool = fcgiConnectionPool;
    }

    protected void doRun() throws IOException, Util.Process.PhpException {
	byte[] buf = new byte[FCGIUtil.FCGI_BUF_SIZE];
	
	FCGIInputStream natIn = null;
	FCGIOutputStream natOut = null;

	FCGIConnectionPool.Connection connection = null;
	
	try {
	    connection = fcgiConnectionPool.openConnection();
	    natOut = (FCGIOutputStream) connection.getOutputStream();
	    natIn = (FCGIInputStream) connection.getInputStream();

	    natOut.writeBegin();
	    natOut.writeParams(env);
	    natOut.write(FCGIUtil.FCGI_STDIN, FCGIUtil.FCGI_EMPTY_RECORD);
	    natOut.close();
	    HeaderParser.parseBody(buf, natIn, new OutputStreamFactory() { public OutputStream getOutputStream() throws IOException {return out;}}, headerParser);
	    natIn.close();
	} catch (InterruptedException e) {
	    /*ignore*/
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
