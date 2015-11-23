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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import php.java.bridge.NotImplementedException;
import php.java.bridge.http.FCGIConnectionPool.Connection;

/**
 * Default OutputStream used by the connection pool.
 * 
 * @author jostb
 *
 */
public class FCGIConnectionOutputStream extends OutputStream {
    protected Connection connection;
    private BufferedOutputStream out;
    
    protected void setConnection(Connection connection) throws FCGIConnectionException {
        this.connection = connection;
        try {
	    this.out = new BufferedOutputStream(connection.channel.getOutputStream());
        } catch (IOException e) {
	    throw new FCGIConnectionException(connection, e);
        }
    }
    /**{@inheritDoc}*/  
    public void write(byte buf[]) throws FCGIConnectionException {
        write(buf, 0, buf.length);
    }
    /**{@inheritDoc}*/  
    public void write(byte buf[], int off, int buflength) throws FCGIConnectionException {
	try {
	    out.write(buf, off, buflength);
	} catch (IOException ex) {
	    throw new FCGIConnectionException(connection, ex);
	}
    }
    /**{@inheritDoc}*/  
    public void write(int b) throws FCGIConnectionException {
        throw new NotImplementedException();
    }
    /**{@inheritDoc}*/  
    public void close() throws FCGIConnectionException {
        try { 
            flush();
        } finally {
            connection.state|=2;
            if(connection.state==connection.ostate)
		try {
		    connection.close();
		} catch (IOException e) {
		    throw new FCGIConnectionException(connection, e);
		}
        }
    }
    /**{@inheritDoc}*/  
    public void flush() throws FCGIConnectionException {
        try {
            out.flush();
        } catch (IOException ex) {
            throw new FCGIConnectionException(connection, ex);
        }
    }
}