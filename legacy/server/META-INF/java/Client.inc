<?php /*-*- mode: php; tab-width:4 -*-*/

  /** java_Client.php -- parser callbacks for the PHP/Java Bridge.
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

require_once(java_get_base()."/JavaProxy.inc");
require_once(java_get_base()."/Parser.inc");
require_once(java_get_base()."/Protocol.inc");
require_once(java_get_base()."/GlobalRef.inc");

/**
 * @access private
 */
class java_SimpleFactory {
  public $client;
  function java_SimpleFactory($client) {
	$this->client = $client;
  }
  function getProxy($result, $signature, $exception, $wrap) {
  	return $result;
  }
  function checkResult($result) {
  }
}
/**
 * @access private
 */
class java_ProxyFactory extends java_SimpleFactory {
  function create($result, $signature) {
	return new java_JavaProxy($result, $signature);
  }
  function createInternal($proxy) {
	return new java_InternalJava($proxy);
  }
  function getProxy($result, $signature, $exception, $wrap) {
	$proxy = $this->create($result, $signature);
	if($wrap) $proxy = $this->createInternal($proxy);
	return $proxy;
  }
}
/**
 * @access private
 */
class java_ArrayProxyFactory extends java_ProxyFactory {
  function create($result, $signature) {
	return new java_ArrayProxy($result, $signature);
  }	
}
/**
 * @access private
 */
class java_IteratorProxyFactory extends java_ProxyFactory {
  function create($result, $signature) {
	return new java_IteratorProxy($result, $signature);
  }	
}
/**
 * @access private
 */
class java_ExceptionProxyFactory extends java_SimpleFactory {
  function create($result, $signature) {
	return new java_ExceptionProxy($result, $signature);
  }
  function getProxy($result, $signature, $exception, $wrap) {
	$proxy = $this->create($result, $signature);
	if($wrap) $proxy = new java_InternalException($proxy, $exception);
	return $proxy;
  }
}
/**
 * @access private
 */
class java_ThrowExceptionProxyFactory extends java_ExceptionProxyFactory {
  function getProxy($result, $signature, $exception, $wrap) {
  	$proxy = $this->create($result, $signature);
	// don't check for $wrap, which may be wrong (type Java instead of
	// JavaException) when the user has managed to create an exception
	// from a Java constructor, e.g.: new Java("java.lang.String",
	// null). Since we'll discard the possibly wrong type anyway, we
	// can create a fresh proxy without any further checks:

	$proxy = new java_InternalException($proxy, $exception);
	return $proxy;
  }
  function checkResult($result) {
	if (JAVA_PREFER_VALUES || ($result->__hasDeclaredExceptions=='T'))
	  throw $result;
	else {
	  trigger_error("Unchecked exception detected: ".java_truncate($result->__toString()), E_USER_WARNING);
	}
  }
}

/**
 * @access private
 */
class java_CacheEntry {
  public $fmt, $signature, $factory, $java;
  public $resultVoid;

  function java_CacheEntry($fmt, $signature, $factory, $resultVoid) {
	$this->fmt = $fmt;
	$this->signature = $signature;
	$this->factory = $factory;
	$this->resultVoid = $resultVoid;
  }
}

/**
 * @access private
 */
class java_Arg {
  public $client;
  public $exception; 				// string representation for php4
  public $factory, $val;
  public $signature; // class type
  
  function java_Arg($client) {
	$this->client = $client;
	$this->factory = $client->simpleFactory;
  }
  function linkResult(&$val) {
	$this->val = &$val;
  }
  function setResult($val) {
	$this->val = &$val;
  }
  function getResult($wrap) {
	$rc = $this->factory->getProxy($this->val, $this->signature, $this->exception, $wrap);
	$factory = $this->factory;

	$this->factory = $this->client->simpleFactory;
	$factory->checkResult($rc);
	return $rc;
  }
  function setFactory($factory) {
	$this->factory = $factory;
  }
  function setException($string) {
	$this->exception = $string;
  }
  function setVoidSignature() {
	$this->signature = "@V";
	// update cache
	$key = $this->client->currentCacheKey;
	if($key && $key[0]!='~') {			// do not cache array(...) or non-java objects
	  $this->client->currentArgumentsFormat[6]="3";
	  if(JAVA_DEBUG) {echo "ignore further results:";  echo "\n";}
	  if(JAVA_DEBUG) {echo "updating cache $key, argformat: {$this->client->currentArgumentsFormat}, classType: {$this->signature}\n";}
	  $cacheEntry = new java_CacheEntry($this->client->currentArgumentsFormat, $this->signature, $this->factory, true);
	  $this->client->methodCache[$key]=$cacheEntry;
    }
  }
  function setSignature($signature) {
	$this->signature = $signature;
	// update cache
	$key = $this->client->currentCacheKey;
	if($key && $key[0]!='~') {			// do not cache array(...) or non-java objects
	  if(JAVA_DEBUG) {echo "updating cache $key, argformat: {$this->client->currentArgumentsFormat}, classType: {$this->signature}\n";}
	  $cacheEntry = new java_CacheEntry($this->client->currentArgumentsFormat, $this->signature, $this->factory, false);
	  $this->client->methodCache[$key]=$cacheEntry;
	}
  }
}
/**
 * @access private
 */
class java_CompositeArg extends java_Arg {
  public $parentArg;
  public $idx;						// position within $val;
  public $type;					// for A and X
  public $counter;

  function java_CompositeArg($client, $type) {
	parent::java_Arg($client);
	$this->type = $type;
	$this->val = array();
	$this->counter = 0;
  }
  function setNextIndex() {
	$this->idx = $this->counter++;
  }
  function setIndex($val) {
	$this->idx = $val;
  }
  function linkResult(&$val) {
	$this->val[$this->idx]=&$val;
  }
  function setResult($val) {
	$this->val[$this->idx]=$this->factory->getProxy($val, $this->signature, $this->exception, true);
	$this->factory = $this->client->simpleFactory;
  }
}
/**
 * @access private
 */
class java_ApplyArg extends java_CompositeArg {
  public $m, $p, $v, $n; 			// see PROTOCOL.TXT

  function java_ApplyArg($client, $type, $m, $p, $v, $n) {
	parent::java_CompositeArg($client, $type);
	$this->m = $m;
	$this->p = $p;
	$this->v = $v;
	$this->n = $n;
  }
}

/**
 * @access private
 */
class java_Client /* implements IDocHandler */ {
  public $RUNTIME;

  public $result, $exception;
  public $parser;

  public $simpleArg, $compositeArg;
  public $simpleFactory, 
	$proxyFactory, $iteratorProxyFacroty, 
	$arrayProxyFactory, $exceptionProxyFactory, $throwExceptionProxyFactory;
  
  public $arg;
  public $asyncCtx, $cancelProxyCreationCounter;
  public $globalRef;

  public $stack;

  // the cache
  public $defaultCache = array(), $asyncCache = array(), $methodCache;
  public $isAsync = 0;
  public $currentCacheKey, $currentArgumentsFormat;
  public $cachedJavaPrototype;
  
  
  // send buffer
  public $sendBuffer, $preparedToSendBuffer;

  // do not finish protocol if ags construction fails due to PHP OutOfMemory error
  public $inArgs;
  
  function java_Client() {
	$this->RUNTIME = array();
	$this->RUNTIME["NOTICE"]='***USE echo java_inspect(jVal) OR print_r(java_values(jVal)) TO SEE THE CONTENTS OF THIS JAVA OBJECT!***';

	$this->parser = new java_Parser($this);
	$this->protocol = new java_Protocol($this);

	$this->simpleFactory = new java_SimpleFactory($this);
	$this->proxyFactory = new java_ProxyFactory($this);
	$this->arrayProxyFactory = new java_ArrayProxyFactory($this);
	$this->iteratorProxyFactory = new java_IteratorProxyFactory($this);
	$this->exceptionProxyFactory = new java_ExceptionProxyFactory($this);
	$this->throwExceptionProxyFactory = new java_ThrowExceptionProxyFactory($this);

	$this->cachedJavaPrototype=new java_JavaProxyProxy($this);

	$this->simpleArg = new java_Arg($this);

	$this->globalRef = new java_GlobalRef();

	$this->asyncCtx = $this->cancelProxyCreationCounter = 0;
	
	$this->methodCache = $this->defaultCache;
	
	$this->inArgs = false;
  }

  function read($size) {
	return $this->protocol->read($size);
  }

  function setDefaultHandler() {
	$this->methodCache = $this->defaultCache;
  }

  function setAsyncHandler() {
	$this->methodCache = $this->asyncCache;
  }


  function handleRequests() {
	$tail_call = false;
	do {
	  $this->stack=array($this->arg=$this->simpleArg);
	  $this->idx = 0;
	  $this->parser->parse();

	  /* pull off A, if any */
	  if((count($this->stack)) > 1) {
		$arg = array_pop($this->stack);
		$this->apply($arg);
		$tail_call = true;			// we don't expect a result
	  } else {
		$tail_call = false;
	  }

	  $this->stack=null;
	} while($tail_call);
	return 1;
  }

  function getWrappedResult($wrap) {
	return $this->simpleArg->getResult($wrap);
  }
  function getInternalResult() {
	return $this->getWrappedResult(false);
  }
  function getResult() {
	return $this->getWrappedResult(true);
  }
  function getProxyFactory($type) {
	switch($type[0]) {
	case 'E':
	  $factory = $this->exceptionProxyFactory;
	  break;
	case 'C':
	  $factory = $this->iteratorProxyFactory;
	  break;
	case 'A':
	  $factory = $this->arrayProxyFactory;
	  break;
	default:
	case 'O':
	  $factory = $this->proxyFactory;
	}
	return $factory;
  }
  function link(&$arg, &$newArg) {
	$arg->linkResult($newArg->val);
	$newArg->parentArg = $arg;
  }
  function getExact($str) {
	return hexdec($str);
  }
  function getInexact($str) {
	$val = null;
  	sscanf($str, "%e", $val);
	return $val;
  }
  function begin($name, $st) {
	$arg = $this->arg;
    switch($name[0]) {
	case 'A':						/* receive apply args as normal array */
	  $object = $this->globalRef->get($this->getExact($st['v']));
	  $newArg = new java_ApplyArg($this, 'A',
								  $this->parser->getData($st['m']),
								  $this->parser->getData($st['p']),
								  $object,
								  $this->getExact($st['n']));
	  $this->link($arg, $newArg);
	  array_push($this->stack, $this->arg = $newArg);
	  break;
	case 'X': 
	  $newArg = new java_CompositeArg($this, $st['t']);
	  $this->link($arg, $newArg);
	  array_push($this->stack, $this->arg = $newArg);
	  break;
	case 'P':
	  if($arg->type=='H') { /* hash table */
		$s = $st['t'];
		if(JAVA_DEBUG) {echo "setresult prepare hash:"; echo sprintf("%s", $st['t']); echo "\n";}
		if($s[0]=='N') { /* number */
		  $arg->setIndex($this->getExact($st['v']));
		  if(JAVA_DEBUG) {echo "setresult array: index:"; echo sprintf("%s", $st['v']); echo "\n";}
		} else {
		  $arg->setIndex($this->parser->getData($st['v']));
		  if(JAVA_DEBUG) {echo "setresult hash: index:"; echo sprintf("%s", $this->parser->getData($st['v'])); echo "\n";}
		}
	  } else {					/* array */
		$arg->setNextIndex();
	  }
	  break;
	case 'S':
	  $arg->setResult($this->parser->getData($st['v']));
	  if(JAVA_DEBUG) {echo "setresult string:"; echo sprintf("%s", $this->parser->getData($st['v'])); echo "\n";}
	  break;
	case 'B':
	  $s=$st['v'];
	  $arg->setResult($s[0]=='T');
	  if(JAVA_DEBUG) {echo "setresult bool:"; echo sprintf("%s", $st['v']); echo "\n";}
	  break;
	case 'L':					// unsigned long
	  $sign = $st['p'];
	  $val = $this->getExact($st['v']);
	  if($sign[0]=='A') $val*=-1;
	  $arg->setResult($val);
	  if(JAVA_DEBUG) {echo "setresult long:"; echo sprintf("%s, sign: %s", $st['v'], $st['p']); echo "\n";}
	  break;
	case 'D':
	  $arg->setResult($this->getInexact($st['v']));
	  if(JAVA_DEBUG) {echo "setresult double:"; echo sprintf("%s", $st['v']); echo "\n";}
	  break;
	case 'V':
	  if ($st['n']!='T') {
		if(JAVA_DEBUG) {echo "setresult VOID:"; echo "\n";}
		$arg->setVoidSignature();
	  }
	  // fall through
	case 'N':
	  $arg->setResult(null);
	  if(JAVA_DEBUG) {echo "setresult null\n"; }
	  break;
	case 'F':
	  if(JAVA_DEBUG) {echo "comm. end\n"; }
	  break;
	case 'O': 
	  $arg->setFactory($this->getProxyFactory($st['p']));
	  $arg->setResult($this->asyncCtx=$this->getExact($st['v']));
	  if($st['n']!='T') $arg->setSignature($st['m']);
	  if(JAVA_DEBUG) {echo "setresult object:"; echo sprintf("%x", $this->asyncCtx); echo "\n";}
	  break;
	case 'E':
	  $arg->setFactory($this->throwExceptionProxyFactory);
	  if(JAVA_DEBUG) {echo "setresult exception:"; echo sprintf("%x", $this->asyncCtx); echo "\n";}
	  $arg->setException($st['m']);
	  $arg->setResult($this->asyncCtx=$this->getExact($st['v']));
	  break;
	default: 
	  $this->parser->parserError();
	}
  }
  function end($name) {
    switch($name[0]) {
	case 'X':
	  $frame = array_pop($this->stack);
	  $this->arg = $frame->parentArg;
	  break;
	}
  }
  function createParserString() {
	return new java_ParserString();
  }
  
  function writeArg($arg) {
	if(is_string($arg)) {
	  $this->protocol->writeString($arg);
	} else if(is_object($arg)) {
      if ((!$arg instanceof java_JavaType)) {
		error_log((string)new java_IllegalArgumentException($arg));
		trigger_error("argument '".get_class($arg)."' is not a Java object, using NULL instead", E_USER_WARNING);
		$this->protocol->writeObject(null);
      } else {
		$this->protocol->writeObject($arg->__java);
	  }
	} else if(is_null($arg)) {
	  $this->protocol->writeObject(null);
	} else if(is_bool($arg)) {
	  $this->protocol->writeBoolean($arg);
	} else if(is_integer($arg)) {
	  $this->protocol->writeLong($arg);
	} else if(is_float($arg)) {
	  $this->protocol->writeDouble($arg);
	} else if(is_array($arg)) {
	  $wrote_begin=false;
	  foreach($arg as $key=>$val) {
		if(is_string($key)) {
		  if(!$wrote_begin) {
			$wrote_begin=1;
			$this->protocol->writeCompositeBegin_h(); 		
		  }
		  $this->protocol->writePairBegin_s($key);
		  $this->writeArg($val);
		  $this->protocol->writePairEnd();
		} else {
		  if(!$wrote_begin) {
			$wrote_begin=1;
			$this->protocol->writeCompositeBegin_h();
		  }
		  $this->protocol->writePairBegin_n($key);
		  $this->writeArg($val);
		  $this->protocol->writePairEnd();
		}
	  }
	  if(!$wrote_begin) {
		$this->protocol->writeCompositeBegin_a();
	  }
	  $this->protocol->writeCompositeEnd();
	}
  }
  function writeArgs($args) {
  	$this->inArgs = true;
	$n = count($args);
	for($i=0; $i<$n; $i++) {
	  $this->writeArg($args[$i]);
	}
	$this->inArgs = false;
  }
  function createObject($name, $args) {
    $this->protocol->createObjectBegin($name);
    $this->writeArgs($args);
    $this->protocol->createObjectEnd();
	$val = $this->getInternalResult();
	return $val;
  }
  function referenceObject($name, $args) {
    $this->protocol->referenceBegin($name);
    $this->writeArgs($args);
    $this->protocol->referenceEnd();
	$val = $this->getInternalResult();
	return $val;
  }
  function getProperty($object, $property) {
	$this->protocol->propertyAccessBegin($object, $property);
	$this->protocol->propertyAccessEnd();
	return $this->getResult();
  }
  function setProperty($object, $property, $arg) {
	$this->protocol->propertyAccessBegin($object, $property);
	$this->writeArg($arg);
	$this->protocol->propertyAccessEnd();
	$this->getResult();
  }
  function invokeMethod($object, $method, $args) {
	$this->protocol->invokeBegin($object, $method);
	$this->writeArgs($args);
	$this->protocol->invokeEnd();
	$val = $this->getResult();
	return $val;
  }
  function unref($object) {
	if (isset($this->protocol)) $this->protocol->writeUnref($object);
  }
  function apply($arg) {
	$name = $arg->p;
	$object = $arg->v;
	$ob = ($object==null) ? $name : array(&$object, $name);

	// save the current state
	$isAsync                = $this->isAsync;
    $methodCache            = $this->methodCache;
    $currentArgumentsFormat = $this->currentArgumentsFormat;
    // the currentCacheKey is destroyed when the result is received
    // so that apply calls are not cached
        
	try {
  	$res = $arg->getResult(true);
	if((($object==null) && !function_exists($name)) || (!($object==null) && !method_exists($object, $name))) throw new JavaException("java.lang.NoSuchMethodError", "$name");
	  
	  $res = call_user_func_array($ob, $res);
      
      if (is_object($res) && (!($res instanceof java_JavaType))) {
          trigger_error("object returned from $name() is not a Java object", E_USER_WARNING);
 
          // correct this error by calling: $res=java_closure($res);
          $this->protocol->invokeBegin(0, "makeClosure");
          $this->protocol->writeULong($this->globalRef->add($res)); // proper PHP "long" -> Java 64 bit value conversion
          $this->protocol->invokeEnd();
          $res = $this->getResult();
      }

	  $this->protocol->resultBegin();
	  $this->writeArg($res);
	  $this->protocol->resultEnd();
	} catch (JavaException $e) {
	  $trace = $e->getTraceAsString();
	  $this->protocol->resultBegin();
	  $this->protocol->writeException($e->__java, $trace);
	  $this->protocol->resultEnd();
	} catch(Exception $ex) {
	  error_log($ex->__toString());
	  trigger_error("Unchecked exception detected in callback", E_USER_ERROR);
	  die (1);
	}	  

	// restore the state
	$this->isAsync                = $isAsync;
	$this->methodCache            = $methodCache;
	$this->currentArgumentsFormat = $currentArgumentsFormat;
  }
  function cast($object, $type) {
    switch($type[0]) {
	case 'S': case 's':
	  return $this->invokeMethod(0, "castToString", array($object));
	case 'B': case 'b':
	  return $this->invokeMethod(0, "castToBoolean", array($object));
	case 'L': case 'I': case 'l': case 'i':
	  return $this->invokeMethod(0, "castToExact", array($object));
	case 'D': case 'd': case 'F': case 'f':
	  return $this->invokeMethod(0, "castToInExact", array($object));
	case 'N': case 'n':
	  return null;
	case 'A': case 'a':
	  return $this->invokeMethod(0, "castToArray", array($object));
	case 'O': case 'o':			// eh?
	  return $object;
	default: 
	  throw new java_RuntimeException("$type illegal");
	}
  }
  function getContext() {
	static $cache = null;
	if (!is_null($cache)) return $cache;
	return $cache = $this->invokeMethod(0, "getContext", array());
  }
  function getSession($args) {
	return $this->invokeMethod(0, "getSession", $args);
  }
  function getServerName() {
	static $cache = null;
	if (!is_null($cache)) return $cache;
	return $cache = $this->protocol->getServerName();
  }
}
/**
 * @access private
 */
function java_shutdown() {
  global $java_initialized;
  if (!$java_initialized) return;

  if (session_id()) session_write_close();
  $client = __javaproxy_Client_getClient();
  if(JAVA_DEBUG) echo "the client destroyed\n";
  if (!isset($client->protocol) || $client->inArgs) return;
  if ($client->preparedToSendBuffer) 
    $client->sendBuffer.=$client->preparedToSendBuffer;
  $client->sendBuffer.=$client->protocol->getKeepAlive();
  $client->protocol->flush();
  $client->protocol->keepAlive();
}
register_shutdown_function("java_shutdown");

?>
