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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import php.java.bridge.Util;

/**
 * A connection pool. Example:<br><br>
 * <code>
 * ConnectionPool pool = new ConnectionPool("127.0.0.1", 8080, 20, 5000, new IOFactory());<br>
 * ConnectionPool.Connection conn = pool.openConnection();<br>
 * InputStream in =  conn.getInputStream();<br>
 * OutputStream out = conn.getOutputStream();<br>
 * ...<br>
 * in.close();<br>
 * out.close();<br>
 * ...<br>
 * pool.destroy();<br>
 * </code>
 * <p>Instead of using delegation (decorator pattern), it is possible to pass a factory 
 * which may create custom In- and OutputStreams. Example:<br><br>
 * <code>
 * new ConnectionPool(..., new IOFactory() {<br>
 * &nbsp;&nbsp;public InputStream getInputStream() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;return new DefaultInputStream() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * &nbsp;&nbsp;}<br>
 * }<br>
 * </code>
 * </p>
 * @author jostb
 *
 */
public class FCGIConnectionPool {

    private int limit;
    private long timeout;
    private int connections = 0;
    private List freeList = new LinkedList();
    private List connectionList = new LinkedList();
    private FCGIIOFactory factory;
    private int maxRequests;
    private FCGIConnectionFactory channelName;
    /**
     * Represents the connection kept by the pool.
     * 
     * @author jostb
     *
     */
    public final class Connection {
        protected int ostate, state; // bit0: input closed, bit1: output closed
	protected FCGIConnectionFactory channelName;
	protected FCGIConnection channel;
	private FCGIConnectionOutputStream outputStream;
	private FCGIConnectionInputStream inputStream;
	private boolean isClosed;
	private FCGIIOFactory factory;
	private int maxRequests;
	private int counter;
	
	protected void reset() {
            this.state = this.ostate = 0;
 	}
	protected void init() {
            inputStream = null;
            outputStream = null;
            counter = maxRequests; 
	    reset();
	}
	protected Connection reopen() throws FCGIConnectException {
            if(isClosed) this.channel = factory.connect(channelName);
            this.isClosed = false;
            return this;
	}
	protected Connection(FCGIConnectionFactory channelName, int maxRequests, FCGIIOFactory factory) {
            this.channelName = channelName;
            this.factory = factory;
            this.isClosed = true;
            this.maxRequests = maxRequests;
            init();
        }
	/** Set the closed/abort flag for this connection */
	public void setIsClosed() {
	    isClosed=true;
	}
	protected void close() throws FCGIConnectException {
	    // PHP child terminated: mark as closed, so that reopen() can allocate 
	    // a new connection for the new PHP child
	    if (maxRequests>0 && --counter==0) isClosed = true;
	    
	    if(isClosed) {
	        destroy();
	        init();
	    }
	    closeConnection(this);
        }

	private void destroy() {
	    try {
	        channel.close();
	    } catch (IOException e) {/*ignore*/}
	}
	/**
	 * Returns the OutputStream associated with this connection.
	 * @return The output stream.
	 * @throws FCGIConnectionException 
	 */
	public OutputStream getOutputStream() throws FCGIConnectionException {
	    if(outputStream != null) return outputStream;
	    FCGIConnectionOutputStream outputStream = (FCGIConnectionOutputStream) factory.createOutputStream();
	    outputStream.setConnection(this);
	    ostate |= 2;
	    return outputStream;
	}
	/**
	 * Returns the InputStream associated with this connection.
	 * @return The input stream.
	 * @throws FCGIConnectionException
	 */
	public InputStream getInputStream() throws FCGIConnectionException {
	    if(inputStream != null) return inputStream;
	    FCGIConnectionInputStream inputStream = (FCGIConnectionInputStream) factory.createInputStream();
	    inputStream.setConnection(this);
	    ostate |= 1;
	    return inputStream;
	}
    }
    /**
     * Create a new connection pool.
     * @param channelName The channel name
     * 
     * @param limit The max. number of physical connections
     * @param maxRequests 
     * @param factory A factory for creating In- and OutputStreams.
     * @throws FCGIConnectException 
     * @see FCGIIOFactory
     */
    private FCGIConnectionPool(FCGIConnectionFactory channelName, int limit, int maxRequests, FCGIIOFactory factory) throws FCGIConnectException {
	if(Util.logLevel>3) Util.logDebug("Creating new connection pool for: " +channelName);
        this.channelName = channelName;
        this.limit = limit;
        this.factory = factory;
        this.maxRequests = maxRequests;
        this.timeout = -1;
        channelName.test();
    }
    /**
     * Create a new connection pool.
     * @param channelName The channel name
     * 
     * @param limit The max. number of physical connections
     * @param maxRequests 
     * @param factory A factory for creating In- and OutputStreams.
     * @param timeout The pool timeout in milliseconds.
     * @throws FCGIConnectException 
     * @see FCGIIOFactory
     */
    public FCGIConnectionPool(FCGIConnectionFactory channelName, int limit, int maxRequests, FCGIIOFactory factory, long timeout) throws FCGIConnectException {
	this(channelName, limit, maxRequests, factory);
	this.timeout = timeout;
    }
    /* helper for openConnection() */
    private Connection createNewConnection() {
        Connection connection = new Connection(channelName, maxRequests, factory);
        connectionList.add(connection);
        connections++;
        return connection;
    }
    /**
     * Opens a connection to the back end.
     * @return The connection
     * @throws InterruptedException
     * @throws FCGIConnectException 
     */
    public synchronized Connection openConnection() throws InterruptedException, FCGIConnectException {
        Connection connection;
      	if(freeList.isEmpty() && connections<limit) {
      	    connection = createNewConnection();
      	} else {
      	    while(freeList.isEmpty()) {
      		if (timeout > 0) {
      		    long t1 = System.currentTimeMillis();
      		    wait(timeout);
      		    long t2 = System.currentTimeMillis();
      		    long t = t2 - t1;
      		    if (t >= timeout) throw new FCGIConnectException(new IOException("pool timeout "+timeout+" exceeded: "+t));
      		} else {
      		    wait();
      		}
      	    }
      	    connection = (Connection) freeList.remove(0);
      	    connection.reset();
      	}
      	return connection.reopen();
    }
    private synchronized void closeConnection(Connection connection) {
        freeList.add(connection);
        notify();
    }
    /**
     * Destroy the connection pool. 
     * 
     * It releases all physical connections.
     *
     */
    public synchronized void destroy() {
        for(Iterator ii = connectionList.iterator(); ii.hasNext();) {
            Connection connection = (Connection) ii.next();
            connection.destroy();
        }
        
    	if(channelName!=null) 
    	    channelName.destroy();
    }
}
