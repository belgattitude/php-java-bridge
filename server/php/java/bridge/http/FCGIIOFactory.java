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

import java.io.InputStream;
import java.io.OutputStream;


/**
 * FastCGI In-/OutputStream factory.
 * 
 * Override this class if you want to use your own streams.
 * 
 * @author jostb
 *
 */
public abstract class FCGIIOFactory {
    /**
     * Create a new socket and connect
     * it to the given host/port
     * @param name The channel name.
     * @return The socket
     * @throws FCGIConnectException
     */
    public abstract FCGIConnection connect(FCGIConnectionFactory name) throws FCGIConnectException;
    /** 
     * Create a new InputStream.
     * @return The input stream. 
     * @throws FCGIConnectionException 
     */
    public InputStream createInputStream() throws FCGIConnectionException {
	FCGIConnectionInputStream in = new FCGIConnectionInputStream();
	return in;
    }
    /**
     * Create a new OutputStream.
     * @return The output stream.
     * @throws FCGIConnectionException
     */
    public OutputStream createOutputStream() throws FCGIConnectionException {
        FCGIConnectionOutputStream out = new FCGIConnectionOutputStream();
        return out;
    }
}