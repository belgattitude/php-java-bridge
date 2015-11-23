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


/**
 * Constants and common procedures for FastCGI
 * @author jostb
 *
 */
public class FCGIUtil {

    /**
     * IO buffer size
     */
    public static final int FCGI_BUF_SIZE = 65535;

    /**
     * header length
     */
    public static final int FCGI_HEADER_LEN = 8;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_BEGIN_REQUEST =      1;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_ABORT_REQUEST =      2;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_END_REQUEST   =      3;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_PARAMS        =      4;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_STDIN         =      5;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_STDOUT        =      6;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_STDERR        =      7;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_DATA          =      8;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_GET_VALUES    =      9;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_GET_VALUES_RESULT = 10;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_UNKNOWN_TYPE      = 11;
    /**
     * Values for type component of FCGI_Header
     */
    public static final byte[] FCGI_EMPTY_RECORD = new byte[0];
    
    /**
     * Mask for flags component of FCGI_BeginRequestBody
     */
    public static final int FCGI_KEEP_CONN  = 1;

    /**
     * Values for role component of FCGI_BeginRequestBody
     */
    public static final int FCGI_RESPONDER  = 1;
    /**
     * Values for role component of FCGI_BeginRequestBody
     */
    public static final int FCGI_AUTHORIZER = 2;
    /**
     * Values for role component of FCGI_BeginRequestBody
     */
    public static final int FCGI_FILTER     = 3;

    /**
     * The Fast CGI default port
     */ 
    public static final int FCGI_PORT = 9667;

    /**
     * This controls how many child processes the PHP process spawns.
     * Default is 5. The value should be less than THREAD_POOL_MAX_SIZE
     * @see php.java.bridge.Util#THREAD_POOL_MAX_SIZE
     */
    public static final String PHP_FCGI_CONNECTION_POOL_SIZE = "5"; // should be less than Util.THREAD_POOL_MAX_SIZE;

    /**
     * This controls how long the pool waits for a PHP script to terminate.
     * Default is -1, which means: "wait forever".
     */
    public static final String PHP_FCGI_CONNECTION_POOL_TIMEOUT = "-1"; // no timeout

    /**
     * This controls how many requests each child process will handle before
     * exitting. When one process exits, another will be created. Default is 5000.
     */
    public static final String PHP_FCGI_MAX_REQUESTS = "5000";

    /**
     * The default channel name on Windows
     */
    public static final String FCGI_PIPE = NPChannelFactory.PREFIX +"JavaBridge@9667";
}
