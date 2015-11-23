/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
 * Implementing classes are able to use the FastCGI machinery.
 * Currently implemented by {@link php.java.servlet.fastcgi.FastCGIServlet} and {@link php.java.script.FastCGIProxy}
 */
public interface IFCGIProcessFactory {
    /** 
     * Log message through preferred log mechanism, for example servlet.log() 
     * @param msg The message to log
     */
    public void log(String msg);

    /**
     * Create a FastCGI Process
     * @param args The PHP arguments
     * @param webInfDir The web inf dir
     * @param pearDir The bear dir
     * @param cgiDir The cgi dir
     * @param includeJava automatically include Java.inc in each script
     * @param home The PHP home dir or null
     * @param env The process environment
     * @return a FastCGI process object
     * @throws IOException
     */
    public IFCGIProcess createFCGIProcess(String[] args, boolean includeJava, File home, Map env) throws IOException;

    /**
     * Get the connection pool size, usually FCGIUtil#PHP_FCGI_CONNECTION_POOL_SIZE
     * @return The connection pool size
     */
    public String getPhpConnectionPoolSize();

    /**
     * Get the max requests value, usually {@link FCGIUtil#PHP_FCGI_MAX_REQUESTS}
     * @return The connection pool size
     */
    public String getPhpMaxRequests();

    /**
     * Get the path to the PHP binary. For example "/usr/bin/php-cgi".
     * @return The path or the name of the PHP FastCGI binary or null (defaults to php-cgi or php-cgi.exe on the PATH)
     */
    public String getPhp();

    /**
     * Get the value of the php_include_java option from the WEB-INF/web.xml. Should return true in most cases.
     * @return the php_include_java option
     */
    public boolean getPhpIncludeJava();

    /**
     * Get the process environment map used for PHP.
     * @return the process environment.
     */
    public HashMap getEnvironment();

    /**
     * Used for debugging only. Should always return true.
     * @return true
     */
    public boolean canStartFCGI();

    /**
     * The full path to the pear dir. Defaults to WEB-INF/cgi. Use TMPDIR for a standalone runner.
     * @return the full path to the cgi dir
     */
    public String getCgiDir();

    /**
     * The full path to the pear dir. Defaults to WEB-INF/pear. Use TMPDIR for a standalone runner.
     * @return the full path to the pear dir
     */
    public String getPearDir();

    /**
     * The full path to the web-inf dir. Defaults to WEB-INF. Use TMPDIR for a standalone runner.
     * @return the full path to the WEB-INF dir
     */
    public String getWebInfDir();
}
