/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge;

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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

class SSLServerSocketHelper {
    private static final char[] KEYSTORE_PASSWORD = "123456".toCharArray();
    public static final ISocketFactory bind(final int port, final int backlog, final boolean isLocal) throws IOException {
	ServerSocketFactory ssocketFactory = null;
	if (System.getProperty("javax.net.ssl.keyStore", null) == null) {
    	    try {
    		SSLContext sslContext = SSLContext.getInstance( "TLS" );
    		KeyManagerFactory km = KeyManagerFactory.getInstance("SunX509");
    		KeyStore ks = KeyStore.getInstance("JKS");
    		InputStream in = SSLServerSocketHelper.class.getClassLoader().getResourceAsStream("META-INF/SSLServerSocketHelperKeystore");
    		ks.load(in, KEYSTORE_PASSWORD);
    		km.init(ks, KEYSTORE_PASSWORD);
    		sslContext.init(km.getKeyManagers(), null, null);
    		ssocketFactory = sslContext.getServerSocketFactory();
    	    } catch (Exception e) {
    		e.printStackTrace();
    	    }
	}
	if (ssocketFactory == null)
	    ssocketFactory = SSLServerSocketFactory.getDefault(); 
	
	
	final ServerSocket ssocket = 
	    isLocal ? 
		    ssocketFactory.createServerSocket(port, backlog, InetAddress.getByName("127.0.0.1")) :
			ssocketFactory.createServerSocket(port, backlog);
			
	return new ISocketFactory() {
	    
	    /**{@inheritDoc}*/
	    public String getSocketName() {
		return String.valueOf(port);
	    }
	    
	    /**{@inheritDoc}*/
	    public void close() throws IOException {
		ssocket.close();
	    }
	    
	    /**{@inheritDoc}*/
	    public Socket accept() throws IOException {
		return ssocket.accept();
	    }
	    
	    /**{@inheritDoc}*/
	    public String toString() {
		return (isLocal?"HTTP_LOCAL:":"HTTPS:") +getSocketName();
	    }
	};
    }
}
