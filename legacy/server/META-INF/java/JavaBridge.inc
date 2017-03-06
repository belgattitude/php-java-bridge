<?php /*-*- mode: php; tab-width:4 -*-*/

  /** java_JavaBridge.php -- provides the PHP/Java Bridge PHP API.
   * 
   * Copyright (C) 2003-2007 Jost Boekemeier
   * 
   * This file is part of the PHP/Java Bridge.
   * 
   * The PHP/Java Bridge ("the library") is free software; you can
   * redistribute it and/or modify it under the terms of the GNU General
   * Public License as published by the Free Software Foundation; either
   * version 2, or (at your option) any later version.
   * 
   * The library is distributed in the hope that it will be useful, but
   * WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   * General Public License for more details.
   * 
   * You should have received a copy of the GNU General Public License
   * along with the PHP/Java Bridge; see the file COPYING.  If not, write to the
   * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
   * 02111-1307 USA.
   * 
   * Linking this file statically or dynamically with other modules is
   * making a combined work based on this library.  Thus, the terms and
   * conditions of the GNU General Public License cover the whole
   * combination.
   * 
   * As a special exception, the copyright holders of this library give you
   * permission to link this library with independent modules to produce an
   * executable, regardless of the license terms of these independent
   * modules, and to copy and distribute the resulting executable under
   * terms of your choice, provided that you also meet, for each linked
   * independent module, the terms and conditions of the license of that
   * module.  An independent module is a module which is not derived from
   * or based on this library.  If you modify this library, you may extend
   * this exception to your version of the library, but you are not
   * obligated to do so.  If you do not wish to do so, delete this
   * exception statement from your version. 
   *
   * @author     Jost Boekemeier
   * @license    GPL
   * @link       http://php-java-bridge.sf.net
   */

if(!function_exists("java_get_base")) {

  1.0E512; // Workaround pfsockopen bug; initialize the socket subsystem

  /**
   * @access private
   */
  function java_get_base() {
	$ar = get_required_files();
	$arLen = sizeof($ar);
	if($arLen>0) {
	  $thiz = $ar[$arLen-1];
	  return dirname($thiz);
	} else {
	  return "java";
	}
  }
  /**
   * @access private
   */
  function java_truncate($str) {
    if (strlen($str)>955)
      return substr($str, 0, 475).'[...]'.substr($str,-475);
    return $str;
  }
  require_once(java_get_base()."/Options.inc");
  require_once(java_get_base()."/JavaProxy.inc");

  /**
   * @access private
   */
  class java_JavaException  extends Exception {
	function __toString() {return $this->getMessage();}
  };
  /**
   * @access private
   */
  class java_RuntimeException extends java_JavaException {};
  /**
   * @access private
   */
  class java_IOException extends java_JavaException {};
  /**
   * @access private
   */
  class java_ConnectException extends java_IOException {};
  /**
   * @access private
   */
  class java_IllegalStateException extends java_RuntimeException {};
  /**
   * @access private
   */
  class java_IllegalArgumentException extends java_RuntimeException {
	function __construct($ob) {
	  parent::__construct("illegal argument: ".gettype($ob));
	}
  };

  /**
   * @access private
   */
  function java_autoload_function5($x) {
	$s=str_replace("_", ".", $x);

    $c=__javaproxy_Client_getClient();
    if(!($c->invokeMethod(0, "typeExists", array($s)))) return false;

	$i = "class ${x} extends Java {".
	  "static function type(\$sub=null){if(\$sub) \$sub='\$'.\$sub; return java('${s}'.\"\$sub\");}".
	  'function __construct() {$args = func_get_args();'.
	  'array_unshift($args, '."'$s'".'); parent::__construct($args);}}';
	eval ("$i");
	return true;
  }
  /**
   * @access private
   */
  function java_autoload_function($x) {
    $idx = strrpos($x, "\\"); if (!$idx) return java_autoload_function5($x);

    $str=str_replace("\\", ".", $x);

    $client=__javaproxy_Client_getClient();
    if(!($client->invokeMethod(0, "typeExists", array($str)))) return false;

    $package = substr($x,0, $idx);
    $name = substr($x, 1+$idx);
    $instance = "namespace $package; class ${name} extends \\Java {".
	  "static function type(\$sub=null){if(\$sub) \$sub='\$'.\$sub;return \\java('${str}'.\"\$sub\");}".
	  "static function __callStatic(\$procedure, \$args) {return \\java_invoke(\\java('${str}'), \$procedure, \$args);}".
	  'function __construct() {$args = func_get_args();'.
	  'array_unshift($args, '."'$str'".'); parent::__construct($args);}}';
	eval ("$instance");
	return true;
  }
  /** Autoload Java classes if not disabled via define("JAVA_DISABLE_AUTOLOAD", true); */
  if(!defined("JAVA_DISABLE_AUTOLOAD") && function_exists("spl_autoload_register")) spl_autoload_register("java_autoload_function");

  /**
   * @access private
   */
  function java_autoload($libs=null) {
	trigger_error('Please use <a href="http://php-java-bridge.sourceforge.net/pjb/webapp.php>tomcat or jee hot deployment</a> instead', E_USER_WARNING);
  }

  /**
   * Include the output of some Java resource.

   * java_virtual() uses the PHP/Java Bridge protocol to render .jsp,
   * .jsf or other Java resources through PHP scripts running within
   * Apache, IIS or as a JEE sub component.
   *
   * Example: 
   * <code>
   * <?php require_once("java/Java.inc");
   * java_session();
   * echo java_virtual("/test.jsp", true);
   * ?>
   * </code>
   *
   * @access public
   * @param string The local resource
   * @param bool return the response
   * @return mixed The response or true
   * @see http://php.net/manual/en/function.virtual.php
   * @see java_session();
   */
  function java_virtual($path, $return=false) {

	$req = java_context()->getHttpServletRequest();
	$req = new java("php.java.servlet.VoidInputHttpServletRequest", $req);

	$res = java_context()->getHttpServletResponse();
	$res = new java("php.java.servlet.RemoteHttpServletResponse", $res);

	$req->getRequestDispatcher($path)->include($req, $res);
	if ($return) return $res->getBufferContents();

	echo $res->getBufferContents();
	return true;
  }
  /**
   * Access the java type with the given name. 
   *
   * This procedure can be
   * used to access constants or procedures within a class.  <br>
   *
   * To access class features, use the java constructor instead.<br>
   *
   * Example: <code> java("java.lang.Long")->MAX_VALUE </code>
   *
   *<br> Any declared exception can be caught by PHP code. <br>
   * Exceptions derived from java.lang.RuntimeException or Error must
   * not be caught unless declared in the methods throws clause -- OutOfMemoryErrors cannot be caught at all,
   * even if declared.
   *
   * @access public
   * @param string The type name
   * @see Java
   */
  function Java($name) { 
	static $classMap = array();
	if(array_key_exists($name, $classMap)) return $classMap[$name];
	return $classMap[$name]=new JavaClass($name);
  }

  /**
   * Alias for java_closure();
   * @access private
   * @see java_closure();
   */
  function java_get_closure() {return java_closure_array(func_get_args());}

  /**
   * Alias for java_closure();
   * @access private
   * @see java_closure();
   */
  function java_wrap() {return java_closure_array(func_get_args());}
  
  /**
   * Alias for java_values();
   * @access private
   * @see java_values();
   */
  function java_get_values($arg) { return java_values($arg); }

  /**
   * Alias for java_session();
   * @access private
   * @see java_session();
   */
  function java_get_session() {return java_session_array(func_get_args());}

  /**
   * Alias for java_context();
   * @access private
   * @see java_context();
   */
  function java_get_context() {return java_context(); }

  /**
   * Alias for java_server_name();
   * @access private
   * @see java_server_name();
   */
  function java_get_server_name() { return java_server_name(); }

  /**
   * Alias for java_is_null();
   * @access private
   * @see java_is_null();
   */
  function java_isnull($value) { return is_null (java_values ($value)); }

  /**
   * Checks whether a value is null or not.
   *
   * Example: <code> java_is_null(java("java.lang.System")->getProperty("foo")) </code>
   *
   * @access public
   * @param mixed A Java object or a PHP value
   * @return true if $value is the PHP or Java null value.
   */
  function java_is_null($value) { return is_null (java_values ($value)); }

  /**
   * Alias for java_is_true();
   * @access private
   * @see java_is_true();
   */
  function java_istrue($value) { return  (boolean)(java_values ($value)); }

  /**
   * Checks whether a value is not NULL, 0, "" or false. 

   * Note that if
   * you want to check if a value is boolean true, use the following
   * test: <code>java_values($value)===true</code>
   * 
   * Example:
   * <code>
   * java_is_true(new java.lang.String("12")) => true
   * </code>
   *
   * Example:
   * <code>
   * java_is_true(new java.lang.String("")) => false
   * </code>
   *
   * Example:
   * <code>
   * java_is_true(1==1) => true
   * </code>
   *
   * Example: <code> java_is_true(java("java.lang.System")->getProperty("foo")) </code>
   *
   * @access public
   * @param mixed A Java object or a PHP value
   * @return true if the PHP or Java value is true.
   */
  function java_is_true($value) { return (boolean)(java_values ($value)); }

  /**
   * Alias for java_is_false();
   * @access private
   * @see java_is_false();
   */
  function java_isfalse($value) { return !(java_values ($value)); }

  /**
   * Checks whether a value is NULL, 0, "" or false. 
   *
   * Note that if you want to check if a value is boolean false, use
   * the following test: <code>java_values($value)===false</code>
   * 
   * Example:
   * <code>
   * java_is_false(new java.lang.String("12")) => false
   * </code>
   *
   * Example:
   * <code>
   * java_is_false(new java.lang.String("")) => true
   * </code>
   *
   * Example:
   * <code>
   * java_is_false(1==1) => false
   * </code>
   *
   * Example: <code> java_is_false(java("java.lang.System")->getProperty("foo")) </code>
   *
   * @access public
   * @param mixed A Java object or a PHP value
   * @return true if the PHP or Java value is false.
   */
  function java_is_false($value) { return !(java_values ($value)); }

  /**
   * Alias for java_set_file_encoding();
   * @access private
   * @see java_set_file_encoding();
   */
  function java_set_encoding($enc) { return java_set_file_encoding ($enc); }

  /**
   * Call the current Java continuation with the closed-over PHP environment
   * $kontinuation as its argument.
   *
   * This procedure can be used to transfer control back to a Java
   * continuation so that Java can call PHP procedures defined within
   * the passed environment. If no current Java continuation exists,
   * the procedure does nothing.
   *
   *
   * Example PHP script:
   * <code>
   * <?php require_once("java/Java.inc");
   *
   * // ... calculate sales for a given month ...
   * function calculateSales($month) {return ((int)(string)$month)+1;}
   *
   * write_response($response);
   *
   * // prepare for out-of-band data: make our top-level environment
   * // available to Java. But only if it has been requested by Java:
   * java_call_with_continuation();
   * ?>
   * </code>
   *
   * If the above PHP script is named
   * "calculateSales.php", the JSR 223 API can be used to invoke its
   * functions or methods, to debug or test certain
   * functionality from Java while keeping the script running in a
   * production environment. The Java code follows:
   * <code>
   * import javax.script.*;
   * import java.net.*;
   * import java.io.*;
   * class SalesTest {
   *     public static void test(Integer month) throws Exception {
   *         ScriptEngine e = new ScriptEngineManager().getEngineByName("php-invocable");
   *		 ByteArrayOutputStream out;
   *		 OutputStreamWriter writer;
   *	     e.getContext().setWriter(writer=new OutputStreamWriter(out=new ByteArrayOutputStream()));
   *         Object res=e.eval(new php.java.script.URLReader(new URL("http://localhost/calculateSales.php")));
   *         System.err.println(((Invocable)e).invokeFunction("calculateSales", new Object[]{month}));
   *         ((Closeable)e).close();
   *         System.err.println("PHP exit() code:" + String.valueOf(res));
   *         System.err.println("PHP output:" + String.valueOf(out));
   *      }
   *      public static void main(String s[]) throws Exception {
   *         test(new Integer(12));
   *      }
   * }
   * </code>
   *
   * The above Java program opens a URL connection to the PHP script,
   * invokes the calculateSales() function from the passed PHP 
   * environment and then outputs the exit code and the HTML response
   * of the PHP script.
   *
   * @access public
   * @param mixed May either be null, a PHP function name, a PHP object or a java_closure. If a PHP object is provided, the object is passed to Java. If a function name is provided, the function must return a java_closure. Otherwise the current environment is used.
   */
  function java_call_with_continuation($kontinuation=null) {
	if (java_getHeader("X_JAVABRIDGE_INCLUDE", $_SERVER) && !java_getHeader("X_JAVABRIDGE_INCLUDE_ONLY", $_SERVER)) {
	  if (is_null($kontinuation))
		java_context()->call(java_closure());
	  elseif (is_string($kontinuation)) 
		java_context()->call(call_user_func($kontinuation));
	  elseif ($kontinuation instanceof java_JavaType) 
		java_context()->call($kontinuation);
	  else
		java_context()->call(java_closure($kontinuation));
	}
  }
 } //!java_defined  DO NOT REMOVE THIS LINE
?>
