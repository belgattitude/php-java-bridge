<?php /*-*- mode: php; tab-width:4 -*-*/

  /**
   * Copyright (C) 2003-2007 Jost Boekemeier.
   *
   * Permission is hereby granted, free of charge, to any person
   * obtaining a copy of this file (the "Software"), to deal in the
   * Software without restriction, including without limitation the
   * rights to use, copy, modify, merge, publish, distribute,
   * sublicense, and/or sell copies of the Software, and to permit
   * persons to whom the Software is furnished to do so, subject to the
   * following conditions:
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
   *
   * @author     Jost Boekemeier
   * @license    GPL
   * @link       http://php-java-bridge.sf.net
   */


/**
 * Helper function: Extract the URL from base and
 * and set host, port and servlet accordingly.
 * For example when the user has called:
 * require_once("http://localhost:8080/JavaBridge/java/Java.inc");
 * the JAVA_HOSTS is set to localhost:8080 and
 * JAVA_SERVLET to /JavaBridge/JavaBridge.phpjavabridge.
 * @access private
 */
function java_defineHostFromInitialQuery($java_base) {
  if($java_base!="java") {
	$url = parse_url($java_base);
	if(isset($url["scheme"]) && ($url["scheme"]=="http" || $url["scheme"]=="https")) {
	  $scheme = $url["scheme"] == "https" ? "ssl://" : "";
	  $host = $url["host"];
	  $port = $url["port"];
	  $path = $url["path"];
	  define ("JAVA_HOSTS", "${scheme}${host}:${port}");
	  $dir = dirname($path);
	  define ("JAVA_SERVLET", "$dir/servlet.phpjavabridge"); // On ;; On or User
	  return true;
	}
  }
  return false;
}

/**
 * The version number of this PHP library.
 */
define ("JAVA_PEAR_VERSION", "6.1");

if(!defined("JAVA_SEND_SIZE")) 
  define("JAVA_SEND_SIZE", 8192);

if(!defined("JAVA_RECV_SIZE")) 
  define("JAVA_RECV_SIZE", 8192);

/**
 * The address of the HTTP back end. 
 * 
 * For example "127.0.0.1:8080"
 * @see JAVA_SERVLET
 */
if(!defined("JAVA_HOSTS")) {
  if(!java_defineHostFromInitialQuery(java_get_base())) {
	if ($java_ini=get_cfg_var("java.hosts")) define("JAVA_HOSTS", $java_ini); 
	else                                define("JAVA_HOSTS", "127.0.0.1:8080"); // host1:port1;host2:port2;...
  }
}

/**
 * Rewrite rules for incoming HTTP requests.
 * 
 *Used in conjunction with
 * JAVA_HOSTS and a servlet/JEE back end 
 * <ul>
 * <li>
 * "On" or 1; Hard-codes the context to "JavaBridge":
 * http://foo.com/test.php => http://host1:port1/JavaBridge/test.phpjavabridge
 * cookie path: always "/"
 *<li>
 * "bar/JavaBridge.phpjavabridge"; Hard-codes the context to "bar":
 * http://foo.com/test.php => http://host1:port1/bar/test.phpjavabridge
 * cookie path: always "/"
 *<li>
 * "User"; Separates different web apps:
 * http://foo.com/mApp1/test.php => http://host1:port1/mApp1/test.phpjavabridge
 * cookie path: /mApp1
 * http://foo.com/mApp2/test.php => http://host1:port1/mApp2/test.phpjavabridge
 * cookie path: /mApp2
 *<li>
 * "Off" or 0 doesn't use a web context at all (no cookies are generated, no
 * PHP/Java session sharing). Back-end must have been started with
 * INET:PORT or INET_LOCAL:PORT, no Servlet engine, no JEE server.
 * </ul>
 */
 if(!defined("JAVA_SERVLET")) {
   if (!(($java_ini=get_cfg_var("java.servlet"))===false)) define("JAVA_SERVLET", $java_ini); 
   else                                               define("JAVA_SERVLET", 1); // Off|0, On|1 or User
}

/** The request log level between 0 (log off) and 4 (log debug). 
 * 
 * The
 * default request log level is initialized with the value from to the
 * Java system property "php.java.bridge.default_log_level".  The
 * servlet's init-param: servlet_log_level (see WEB-INF/web.xml)
 * overrides this value. The default level is 2.
 */
if(!defined("JAVA_LOG_LEVEL"))
  if (!(($java_ini=get_cfg_var("java.log_level"))===false)) define("JAVA_LOG_LEVEL", (int)$java_ini); 
  else                                                 define("JAVA_LOG_LEVEL", null); // integer between 0 and 4


/** Set to 1 for compatibility with earlier versions.
 * 
 * 
 * When this flag is set, a value (null, int, ...) is returned
 * immediately and java.lang.RuntimeException/java.lang.Error is
 * thrown as a normal PHP JavaException. Otherwise a proxy
 * (RuntimeExceptionErrorMarker, Request$PHPNULL, Integer,
 * Boolean, ...) is returned and PHP must fetch Java values using
 * java_values($proxy) or with (string)$proxy. java.lang.Exceptions are thrown as usual. 
 * But java.lang.RuntimeException/Errors raise a warning and terminate the script with
 * a fatal PHP error, unless the error condition is fetched and cleared within the
 * current try/catch block using java_last_exception_get()/java_last_exception_clear().
 */
if (!defined("JAVA_PREFER_VALUES"))
  if ($java_ini=get_cfg_var("java.prefer_values")) define("JAVA_PREFER_VALUES", $java_ini); 
  else                                        define("JAVA_PREFER_VALUES", 0);
  
/**
 * Debug mode for the client. 
 * 
 * This debug flag is for PHP only. To enable the VMBridge log restart
 * Java with the -Dphp.java.bridge.default_log_level=... option.
 */
if(!defined("JAVA_DEBUG")) 
  if ($java_ini=get_cfg_var("java.debug")) define("JAVA_DEBUG", $java_ini); 
  else                                define("JAVA_DEBUG", false);
