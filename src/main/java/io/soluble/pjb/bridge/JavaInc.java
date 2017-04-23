package io.soluble.pjb.bridge;
public class JavaInc {
    private static final String data = "<?php\n"+
"# Java.inc -- The PHP/Java Bridge PHP library. Compiled from JavaBridge.inc.\n"+
"# Copyright (C) 2003-2009 Jost Boekemeier.\n"+
"# Distributed under the MIT license, see Options.inc for details.\n"+
"# Customization examples:\n"+
"# define (\"JAVA_HOSTS\", 9267); define (\"JAVA_SERVLET\", false);\n"+
"# define (\"JAVA_HOSTS\", \"127.0.0.1:8787\");\n"+
"# define (\"JAVA_HOSTS\", \"ssl://my-secure-host.com:8443\");\n"+
"# define (\"JAVA_SERVLET\", \"/MyWebApp/servlet.phpjavabridge\");\n"+
"# define (\"JAVA_PREFER_VALUES\", 1);\n"+
"\n"+
"if(!function_exists(\"java_get_base\")) {\n"+
"1.0E512;\n"+
"function java_get_base() {\n"+
"$ar=get_required_files();\n"+
"$arLen=sizeof($ar);\n"+
"if($arLen>0) {\n"+
"$thiz=$ar[$arLen-1];\n"+
"return dirname($thiz);\n"+
"} else {\n"+
"return \"java\";\n"+
"}\n"+
"}\n"+
"function java_truncate($str) {\n"+
"if (strlen($str)>955)\n"+
"return substr($str,0,475).'[...]'.substr($str,-475);\n"+
"return $str;\n"+
"}\n"+
"class java_JavaException extends Exception {\n"+
"function __toString() {return $this->getMessage();}\n"+
"};\n"+
"class java_RuntimeException extends java_JavaException {};\n"+
"class java_IOException extends java_JavaException {};\n"+
"class java_ConnectException extends java_IOException {};\n"+
"class java_IllegalStateException extends java_RuntimeException {};\n"+
"class java_IllegalArgumentException extends java_RuntimeException {\n"+
"function __construct($ob) {\n"+
"parent::__construct(\"illegal argument: \".gettype($ob));\n"+
"}\n"+
"};\n"+
"function java_autoload_function5($x) {\n"+
"$s=str_replace(\"_\",\".\",$x);\n"+
"$c=__javaproxy_Client_getClient();\n"+
"if(!($c->invokeMethod(0,\"typeExists\",array($s)))) return false;\n"+
"$i=\"class ${x} extends Java {\".\n"+
"\"static function type(\\$sub=null){if(\\$sub) \\$sub='\\$'.\\$sub; return java('${s}'.\\\"\\$sub\\\");}\".\n"+
"'function __construct() {$args=func_get_args();'.\n"+
"'array_unshift($args,'.\"'$s'\".'); parent::__construct($args);}}';\n"+
"eval (\"$i\");\n"+
"return true;\n"+
"}\n"+
"function java_autoload_function($x) {\n"+
"$idx=strrpos($x,\"\\\\\"); if (!$idx) return java_autoload_function5($x);\n"+
"$str=str_replace(\"\\\\\",\".\",$x);\n"+
"$client=__javaproxy_Client_getClient();\n"+
"if(!($client->invokeMethod(0,\"typeExists\",array($str)))) return false;\n"+
"$package=substr($x,0,$idx);\n"+
"$name=substr($x,1+$idx);\n"+
"$instance=\"namespace $package; class ${name} extends \\\\Java {\".\n"+
"\"static function type(\\$sub=null){if(\\$sub) \\$sub='\\$'.\\$sub;return \\\\java('${str}'.\\\"\\$sub\\\");}\".\n"+
"\"static function __callStatic(\\$procedure,\\$args) {return \\\\java_invoke(\\\\java('${str}'),\\$procedure,\\$args);}\".\n"+
"'function __construct() {$args=func_get_args();'.\n"+
"'array_unshift($args,'.\"'$str'\".'); parent::__construct($args);}}';\n"+
"eval (\"$instance\");\n"+
"return true;\n"+
"}\n"+
"if(!defined(\"JAVA_DISABLE_AUTOLOAD\") && function_exists(\"spl_autoload_register\")) spl_autoload_register(\"java_autoload_function\");\n"+
"function java_autoload($libs=null) {\n"+
"trigger_error('Please use <a href=\"http://php-java-bridge.sourceforge.net/pjb/webapp.php>tomcat or jee hot deployment</a> instead',E_USER_WARNING);\n"+
"}\n"+
"function java_virtual($path,$return=false) {\n"+
"$req=java_context()->getHttpServletRequest();\n"+
"$req=new java(\"php.java.servlet.VoidInputHttpServletRequest\",$req);\n"+
"$res=java_context()->getHttpServletResponse();\n"+
"$res=new java(\"php.java.servlet.RemoteHttpServletResponse\",$res);\n"+
"$req->getRequestDispatcher($path)->include($req,$res);\n"+
"if ($return) return $res->getBufferContents();\n"+
"echo $res->getBufferContents();\n"+
"return true;\n"+
"}\n"+
"function Java($name) {\n"+
"static $classMap=array();\n"+
"if(array_key_exists($name,$classMap)) return $classMap[$name];\n"+
"return $classMap[$name]=new JavaClass($name);\n"+
"}\n"+
"function java_get_closure() {return java_closure_array(func_get_args());}\n"+
"function java_wrap() {return java_closure_array(func_get_args());}\n"+
"function java_get_values($arg) { return java_values($arg); }\n"+
"function java_get_session() {return java_session_array(func_get_args());}\n"+
"function java_get_context() {return java_context(); }\n"+
"function java_get_server_name() { return java_server_name(); }\n"+
"function java_isnull($value) { return is_null (java_values ($value)); }\n"+
"function java_is_null($value) { return is_null (java_values ($value)); }\n"+
"function java_istrue($value) { return (boolean)(java_values ($value)); }\n"+
"function java_is_true($value) { return (boolean)(java_values ($value)); }\n"+
"function java_isfalse($value) { return !(java_values ($value)); }\n"+
"function java_is_false($value) { return !(java_values ($value)); }\n"+
"function java_set_encoding($enc) { return java_set_file_encoding ($enc); }\n"+
"function java_call_with_continuation($kontinuation=null) {\n"+
"if (java_getHeader(\"X_JAVABRIDGE_INCLUDE\",$_SERVER) && !java_getHeader(\"X_JAVABRIDGE_INCLUDE_ONLY\",$_SERVER)) {\n"+
"if (is_null($kontinuation))\n"+
"java_context()->call(java_closure());\n"+
"elseif (is_string($kontinuation))\n"+
"java_context()->call(call_user_func($kontinuation));\n"+
"elseif ($kontinuation instanceof java_JavaType)\n"+
"java_context()->call($kontinuation);\n"+
"else\n"+
"java_context()->call(java_closure($kontinuation));\n"+
"}\n"+
"}\n"+
"function java_defineHostFromInitialQuery($java_base) {\n"+
"if($java_base!=\"java\") {\n"+
"$url=parse_url($java_base);\n"+
"if(isset($url[\"scheme\"]) && ($url[\"scheme\"]==\"http\" || $url[\"scheme\"]==\"https\")) {\n"+
"$scheme=$url[\"scheme\"]==\"https\" ? \"ssl://\" : \"\";\n"+
"$host=$url[\"host\"];\n"+
"$port=$url[\"port\"];\n"+
"$path=$url[\"path\"];\n"+
"define (\"JAVA_HOSTS\",\"${scheme}${host}:${port}\");\n"+
"$dir=dirname($path);\n"+
"define (\"JAVA_SERVLET\",\"$dir/servlet.phpjavabridge\");\n"+
"return true;\n"+
"}\n"+
"}\n"+
"return false;\n"+
"}\n"+
"define(\"JAVA_PEAR_VERSION\",\"\");\n"+
"if(!defined(\"JAVA_SEND_SIZE\"))\n"+
"define(\"JAVA_SEND_SIZE\",8192);\n"+
"if(!defined(\"JAVA_RECV_SIZE\"))\n"+
"define(\"JAVA_RECV_SIZE\",8192);\n"+
"if(!defined(\"JAVA_HOSTS\")) {\n"+
"if(!java_defineHostFromInitialQuery(java_get_base())) {\n"+
"if ($java_ini=get_cfg_var(\"java.hosts\")) define(\"JAVA_HOSTS\",$java_ini);\n"+
"else define(\"JAVA_HOSTS\",\"127.0.0.1:8080\");\n"+
"}\n"+
"}\n"+
"if(!defined(\"JAVA_SERVLET\")) {\n"+
"if (!(($java_ini=get_cfg_var(\"java.servlet\"))===false)) define(\"JAVA_SERVLET\",$java_ini);\n"+
"else define(\"JAVA_SERVLET\",1);\n"+
"}\n"+
"if(!defined(\"JAVA_LOG_LEVEL\"))\n"+
"if (!(($java_ini=get_cfg_var(\"java.log_level\"))===false)) define(\"JAVA_LOG_LEVEL\",(int)$java_ini);\n"+
"else define(\"JAVA_LOG_LEVEL\",null);\n"+
"if (!defined(\"JAVA_PREFER_VALUES\"))\n"+
"if ($java_ini=get_cfg_var(\"java.prefer_values\")) define(\"JAVA_PREFER_VALUES\",$java_ini);\n"+
"else define(\"JAVA_PREFER_VALUES\",0);\n"+
"class java_SimpleFactory {\n"+
"public $client;\n"+
"function __construct($client) {\n"+
"$this->client=$client;\n"+
"}\n"+
"function getProxy($result,$signature,$exception,$wrap) {\n"+
"return $result;\n"+
"}\n"+
"function checkResult($result) {\n"+
"}\n"+
"}\n"+
"class java_ProxyFactory extends java_SimpleFactory {\n"+
"function create($result,$signature) {\n"+
"return new java_JavaProxy($result,$signature);\n"+
"}\n"+
"function createInternal($proxy) {\n"+
"return new java_InternalJava($proxy);\n"+
"}\n"+
"function getProxy($result,$signature,$exception,$wrap) {\n"+
"$proxy=$this->create($result,$signature);\n"+
"if($wrap) $proxy=$this->createInternal($proxy);\n"+
"return $proxy;\n"+
"}\n"+
"}\n"+
"class java_ArrayProxyFactory extends java_ProxyFactory {\n"+
"function create($result,$signature) {\n"+
"return new java_ArrayProxy($result,$signature);\n"+
"}\n"+
"}\n"+
"class java_IteratorProxyFactory extends java_ProxyFactory {\n"+
"function create($result,$signature) {\n"+
"return new java_IteratorProxy($result,$signature);\n"+
"}\n"+
"}\n"+
"class java_ExceptionProxyFactory extends java_SimpleFactory {\n"+
"function create($result,$signature) {\n"+
"return new java_ExceptionProxy($result,$signature);\n"+
"}\n"+
"function getProxy($result,$signature,$exception,$wrap) {\n"+
"$proxy=$this->create($result,$signature);\n"+
"if($wrap) $proxy=new java_InternalException($proxy,$exception);\n"+
"return $proxy;\n"+
"}\n"+
"}\n"+
"class java_ThrowExceptionProxyFactory extends java_ExceptionProxyFactory {\n"+
"function getProxy($result,$signature,$exception,$wrap) {\n"+
"$proxy=$this->create($result,$signature);\n"+
"$proxy=new java_InternalException($proxy,$exception);\n"+
"return $proxy;\n"+
"}\n"+
"function checkResult($result) {\n"+
"if (JAVA_PREFER_VALUES || ($result->__hasDeclaredExceptions=='T'))\n"+
"throw $result;\n"+
"else {\n"+
"trigger_error(\"Unchecked exception detected: \".java_truncate($result->__toString()),E_USER_WARNING);\n"+
"}\n"+
"}\n"+
"}\n"+
"class java_CacheEntry {\n"+
"public $fmt,$signature,$factory,$java;\n"+
"public $resultVoid;\n"+
"function __construct($fmt,$signature,$factory,$resultVoid) {\n"+
"$this->fmt=$fmt;\n"+
"$this->signature=$signature;\n"+
"$this->factory=$factory;\n"+
"$this->resultVoid=$resultVoid;\n"+
"}\n"+
"}\n"+
"class java_Arg {\n"+
"public $client;\n"+
"public $exception;\n"+
"public $factory,$val;\n"+
"public $signature;\n"+
"function __construct($client) {\n"+
"$this->client=$client;\n"+
"$this->factory=$client->simpleFactory;\n"+
"}\n"+
"function linkResult(&$val) {\n"+
"$this->val=&$val;\n"+
"}\n"+
"function setResult($val) {\n"+
"$this->val=&$val;\n"+
"}\n"+
"function getResult($wrap) {\n"+
"$rc=$this->factory->getProxy($this->val,$this->signature,$this->exception,$wrap);\n"+
"$factory=$this->factory;\n"+
"$this->factory=$this->client->simpleFactory;\n"+
"$factory->checkResult($rc);\n"+
"return $rc;\n"+
"}\n"+
"function setFactory($factory) {\n"+
"$this->factory=$factory;\n"+
"}\n"+
"function setException($string) {\n"+
"$this->exception=$string;\n"+
"}\n"+
"function setVoidSignature() {\n"+
"$this->signature=\"@V\";\n"+
"$key=$this->client->currentCacheKey;\n"+
"if($key && $key[0]!='~') {\n"+
"$this->client->currentArgumentsFormat[6]=\"3\";\n"+
"$cacheEntry=new java_CacheEntry($this->client->currentArgumentsFormat,$this->signature,$this->factory,true);\n"+
"$this->client->methodCache[$key]=$cacheEntry;\n"+
"}\n"+
"}\n"+
"function setSignature($signature) {\n"+
"$this->signature=$signature;\n"+
"$key=$this->client->currentCacheKey;\n"+
"if($key && $key[0]!='~') {\n"+
"$cacheEntry=new java_CacheEntry($this->client->currentArgumentsFormat,$this->signature,$this->factory,false);\n"+
"$this->client->methodCache[$key]=$cacheEntry;\n"+
"}\n"+
"}\n"+
"}\n"+
"class java_CompositeArg extends java_Arg {\n"+
"public $parentArg;\n"+
"public $idx;\n"+
"public $type;\n"+
"public $counter;\n"+
"function __construct($client,$type) {\n"+
"parent::__construct($client);\n"+
"$this->type=$type;\n"+
"$this->val=array();\n"+
"$this->counter=0;\n"+
"}\n"+
"function setNextIndex() {\n"+
"$this->idx=$this->counter++;\n"+
"}\n"+
"function setIndex($val) {\n"+
"$this->idx=$val;\n"+
"}\n"+
"function linkResult(&$val) {\n"+
"$this->val[$this->idx]=&$val;\n"+
"}\n"+
"function setResult($val) {\n"+
"$this->val[$this->idx]=$this->factory->getProxy($val,$this->signature,$this->exception,true);\n"+
"$this->factory=$this->client->simpleFactory;\n"+
"}\n"+
"}\n"+
"class java_ApplyArg extends java_CompositeArg {\n"+
"public $m,$p,$v,$n;\n"+
"function __construct($client,$type,$m,$p,$v,$n) {\n"+
"parent::__construct($client,$type);\n"+
"$this->m=$m;\n"+
"$this->p=$p;\n"+
"$this->v=$v;\n"+
"$this->n=$n;\n"+
"}\n"+
"}\n"+
"class java_Client {\n"+
"public $RUNTIME;\n"+
"public $result,$exception;\n"+
"public $parser;\n"+
"public $simpleArg,$compositeArg;\n"+
"public $simpleFactory,\n"+
"$proxyFactory,$iteratorProxyFacroty,\n"+
"$arrayProxyFactory,$exceptionProxyFactory,$throwExceptionProxyFactory;\n"+
"public $arg;\n"+
"public $asyncCtx,$cancelProxyCreationTag;\n"+
"public $globalRef;\n"+
"public $stack;\n"+
"public $defaultCache=array(),$asyncCache=array(),$methodCache;\n"+
"public $isAsync=0;\n"+
"public $currentCacheKey,$currentArgumentsFormat;\n"+
"public $cachedJavaPrototype;\n"+
"public $sendBuffer,$preparedToSendBuffer;\n"+
"public $inArgs;\n"+
"function __construct() {\n"+
"$this->RUNTIME=array();\n"+
"$this->RUNTIME[\"NOTICE\"]='***USE echo java_inspect(jVal) OR print_r(java_values(jVal)) TO SEE THE CONTENTS OF THIS JAVA OBJECT!***';\n"+
"$this->parser=new java_Parser($this);\n"+
"$this->protocol=new java_Protocol($this);\n"+
"$this->simpleFactory=new java_SimpleFactory($this);\n"+
"$this->proxyFactory=new java_ProxyFactory($this);\n"+
"$this->arrayProxyFactory=new java_ArrayProxyFactory($this);\n"+
"$this->iteratorProxyFactory=new java_IteratorProxyFactory($this);\n"+
"$this->exceptionProxyFactory=new java_ExceptionProxyFactory($this);\n"+
"$this->throwExceptionProxyFactory=new java_ThrowExceptionProxyFactory($this);\n"+
"$this->cachedJavaPrototype=new java_JavaProxyProxy($this);\n"+
"$this->simpleArg=new java_Arg($this);\n"+
"$this->globalRef=new java_GlobalRef();\n"+
"$this->asyncCtx=$this->cancelProxyCreationTag=0;\n"+
"$this->methodCache=$this->defaultCache;\n"+
"$this->inArgs=false;\n"+
"}\n"+
"function read($size) {\n"+
"return $this->protocol->read($size);\n"+
"}\n"+
"function setDefaultHandler() {\n"+
"$this->methodCache=$this->defaultCache;\n"+
"}\n"+
"function setAsyncHandler() {\n"+
"$this->methodCache=$this->asyncCache;\n"+
"}\n"+
"function handleRequests() {\n"+
"$tail_call=false;\n"+
"do {\n"+
"$this->stack=array($this->arg=$this->simpleArg);\n"+
"$this->idx=0;\n"+
"$this->parser->parse();\n"+
"if((count($this->stack)) > 1) {\n"+
"$arg=array_pop($this->stack);\n"+
"$this->apply($arg);\n"+
"$tail_call=true;\n"+
"} else {\n"+
"$tail_call=false;\n"+
"}\n"+
"$this->stack=null;\n"+
"} while($tail_call);\n"+
"return 1;\n"+
"}\n"+
"function getWrappedResult($wrap) {\n"+
"return $this->simpleArg->getResult($wrap);\n"+
"}\n"+
"function getInternalResult() {\n"+
"return $this->getWrappedResult(false);\n"+
"}\n"+
"function getResult() {\n"+
"return $this->getWrappedResult(true);\n"+
"}\n"+
"function getProxyFactory($type) {\n"+
"switch($type[0]) {\n"+
"case 'E':\n"+
"$factory=$this->exceptionProxyFactory;\n"+
"break;\n"+
"case 'C':\n"+
"$factory=$this->iteratorProxyFactory;\n"+
"break;\n"+
"case 'A':\n"+
"$factory=$this->arrayProxyFactory;\n"+
"break;\n"+
"default:\n"+
"case 'O':\n"+
"$factory=$this->proxyFactory;\n"+
"}\n"+
"return $factory;\n"+
"}\n"+
"function link(&$arg,&$newArg) {\n"+
"$arg->linkResult($newArg->val);\n"+
"$newArg->parentArg=$arg;\n"+
"}\n"+
"function getExact($str) {\n"+
"return hexdec($str);\n"+
"}\n"+
"function getInexact($str) {\n"+
"$val=null;\n"+
"sscanf($str,\"%e\",$val);\n"+
"return $val;\n"+
"}\n"+
"function begin($name,$st) {\n"+
"$arg=$this->arg;\n"+
"switch($name[0]) {\n"+
"case 'A':\n"+
"$object=$this->globalRef->get($this->getExact($st['v']));\n"+
"$newArg=new java_ApplyArg($this,'A',\n"+
"$this->parser->getData($st['m']),\n"+
"$this->parser->getData($st['p']),\n"+
"$object,\n"+
"$this->getExact($st['n']));\n"+
"$this->link($arg,$newArg);\n"+
"array_push($this->stack,$this->arg=$newArg);\n"+
"break;\n"+
"case 'X':\n"+
"$newArg=new java_CompositeArg($this,$st['t']);\n"+
"$this->link($arg,$newArg);\n"+
"array_push($this->stack,$this->arg=$newArg);\n"+
"break;\n"+
"case 'P':\n"+
"if($arg->type=='H') {\n"+
"$s=$st['t'];\n"+
"if($s[0]=='N') {\n"+
"$arg->setIndex($this->getExact($st['v']));\n"+
"} else {\n"+
"$arg->setIndex($this->parser->getData($st['v']));\n"+
"}\n"+
"} else {\n"+
"$arg->setNextIndex();\n"+
"}\n"+
"break;\n"+
"case 'S':\n"+
"$arg->setResult($this->parser->getData($st['v']));\n"+
"break;\n"+
"case 'B':\n"+
"$s=$st['v'];\n"+
"$arg->setResult($s[0]=='T');\n"+
"break;\n"+
"case 'L':\n"+
"$sign=$st['p'];\n"+
"$val=$this->getExact($st['v']);\n"+
"if($sign[0]=='A') $val*=-1;\n"+
"$arg->setResult($val);\n"+
"break;\n"+
"case 'D':\n"+
"$arg->setResult($this->getInexact($st['v']));\n"+
"break;\n"+
"case 'V':\n"+
"if ($st['n']!='T') {\n"+
"$arg->setVoidSignature();\n"+
"}\n"+
"case 'N':\n"+
"$arg->setResult(null);\n"+
"break;\n"+
"case 'F':\n"+
"break;\n"+
"case 'O':\n"+
"$arg->setFactory($this->getProxyFactory($st['p']));\n"+
"$arg->setResult($this->asyncCtx=$this->getExact($st['v']));\n"+
"if($st['n']!='T') $arg->setSignature($st['m']);\n"+
"break;\n"+
"case 'E':\n"+
"$arg->setFactory($this->throwExceptionProxyFactory);\n"+
"$arg->setException($st['m']);\n"+
"$arg->setResult($this->asyncCtx=$this->getExact($st['v']));\n"+
"break;\n"+
"default:\n"+
"$this->parser->parserError();\n"+
"}\n"+
"}\n"+
"function end($name) {\n"+
"switch($name[0]) {\n"+
"case 'X':\n"+
"$frame=array_pop($this->stack);\n"+
"$this->arg=$frame->parentArg;\n"+
"break;\n"+
"}\n"+
"}\n"+
"function createParserString() {\n"+
"return new java_ParserString();\n"+
"}\n"+
"function writeArg($arg) {\n"+
"if(is_string($arg)) {\n"+
"$this->protocol->writeString($arg);\n"+
"} else if(is_object($arg)) {\n"+
"if ((!$arg instanceof java_JavaType)) {\n"+
"error_log((string)new java_IllegalArgumentException($arg));\n"+
"trigger_error(\"argument '\".get_class($arg).\"' is not a Java object,using NULL instead\",E_USER_WARNING);\n"+
"$this->protocol->writeObject(null);\n"+
"} else {\n"+
"$this->protocol->writeObject($arg->__java);\n"+
"}\n"+
"} else if(is_null($arg)) {\n"+
"$this->protocol->writeObject(null);\n"+
"} else if(is_bool($arg)) {\n"+
"$this->protocol->writeBoolean($arg);\n"+
"} else if(is_integer($arg)) {\n"+
"$this->protocol->writeLong($arg);\n"+
"} else if(is_float($arg)) {\n"+
"$this->protocol->writeDouble($arg);\n"+
"} else if(is_array($arg)) {\n"+
"$wrote_begin=false;\n"+
"foreach($arg as $key=>$val) {\n"+
"if(is_string($key)) {\n"+
"if(!$wrote_begin) {\n"+
"$wrote_begin=1;\n"+
"$this->protocol->writeCompositeBegin_h();\n"+
"}\n"+
"$this->protocol->writePairBegin_s($key);\n"+
"$this->writeArg($val);\n"+
"$this->protocol->writePairEnd();\n"+
"} else {\n"+
"if(!$wrote_begin) {\n"+
"$wrote_begin=1;\n"+
"$this->protocol->writeCompositeBegin_h();\n"+
"}\n"+
"$this->protocol->writePairBegin_n($key);\n"+
"$this->writeArg($val);\n"+
"$this->protocol->writePairEnd();\n"+
"}\n"+
"}\n"+
"if(!$wrote_begin) {\n"+
"$this->protocol->writeCompositeBegin_a();\n"+
"}\n"+
"$this->protocol->writeCompositeEnd();\n"+
"}\n"+
"}\n"+
"function writeArgs($args) {\n"+
"$this->inArgs=true;\n"+
"$n=count($args);\n"+
"for($i=0; $i<$n; $i++) {\n"+
"$this->writeArg($args[$i]);\n"+
"}\n"+
"$this->inArgs=false;\n"+
"}\n"+
"function createObject($name,$args) {\n"+
"$this->protocol->createObjectBegin($name);\n"+
"$this->writeArgs($args);\n"+
"$this->protocol->createObjectEnd();\n"+
"$val=$this->getInternalResult();\n"+
"return $val;\n"+
"}\n"+
"function referenceObject($name,$args) {\n"+
"$this->protocol->referenceBegin($name);\n"+
"$this->writeArgs($args);\n"+
"$this->protocol->referenceEnd();\n"+
"$val=$this->getInternalResult();\n"+
"return $val;\n"+
"}\n"+
"function getProperty($object,$property) {\n"+
"$this->protocol->propertyAccessBegin($object,$property);\n"+
"$this->protocol->propertyAccessEnd();\n"+
"return $this->getResult();\n"+
"}\n"+
"function setProperty($object,$property,$arg) {\n"+
"$this->protocol->propertyAccessBegin($object,$property);\n"+
"$this->writeArg($arg);\n"+
"$this->protocol->propertyAccessEnd();\n"+
"$this->getResult();\n"+
"}\n"+
"function invokeMethod($object,$method,$args) {\n"+
"$this->protocol->invokeBegin($object,$method);\n"+
"$this->writeArgs($args);\n"+
"$this->protocol->invokeEnd();\n"+
"$val=$this->getResult();\n"+
"return $val;\n"+
"}\n"+
"function setExitCode($code) {\n"+
"if (isset($this->protocol)) $this->protocol->writeExitCode($code);\n"+
"}\n"+
"function unref($object) {\n"+
"if (isset($this->protocol)) $this->protocol->writeUnref($object);\n"+
"}\n"+
"function apply($arg) {\n"+
"$name=$arg->p;\n"+
"$object=$arg->v;\n"+
"$ob=($object==null) ? $name : array(&$object,$name);\n"+
"$isAsync=$this->isAsync;\n"+
"$methodCache=$this->methodCache;\n"+
"$currentArgumentsFormat=$this->currentArgumentsFormat;\n"+
"try {\n"+
"$res=$arg->getResult(true);\n"+
"if((($object==null) && !function_exists($name)) || (!($object==null) && !method_exists($object,$name))) throw new JavaException(\"java.lang.NoSuchMethodError\",\"$name\");\n"+
"$res=call_user_func_array($ob,$res);\n"+
"if (is_object($res) && (!($res instanceof java_JavaType))) {\n"+
"trigger_error(\"object returned from $name() is not a Java object\",E_USER_WARNING);\n"+
"$this->protocol->invokeBegin(0,\"makeClosure\");\n"+
"$this->protocol->writeULong($this->globalRef->add($res));\n"+
"$this->protocol->invokeEnd();\n"+
"$res=$this->getResult();\n"+
"}\n"+
"$this->protocol->resultBegin();\n"+
"$this->writeArg($res);\n"+
"$this->protocol->resultEnd();\n"+
"} catch (JavaException $e) {\n"+
"$trace=$e->getTraceAsString();\n"+
"$this->protocol->resultBegin();\n"+
"$this->protocol->writeException($e->__java,$trace);\n"+
"$this->protocol->resultEnd();\n"+
"} catch(Exception $ex) {\n"+
"error_log($ex->__toString());\n"+
"trigger_error(\"Unchecked exception detected in callback\",E_USER_ERROR);\n"+
"die (1);\n"+
"}\n"+
"$this->isAsync=$isAsync;\n"+
"$this->methodCache=$methodCache;\n"+
"$this->currentArgumentsFormat=$currentArgumentsFormat;\n"+
"}\n"+
"function cast($object,$type) {\n"+
"switch($type[0]) {\n"+
"case 'S': case 's':\n"+
"return $this->invokeMethod(0,\"castToString\",array($object));\n"+
"case 'B': case 'b':\n"+
"return $this->invokeMethod(0,\"castToBoolean\",array($object));\n"+
"case 'L': case 'I': case 'l': case 'i':\n"+
"return $this->invokeMethod(0,\"castToExact\",array($object));\n"+
"case 'D': case 'd': case 'F': case 'f':\n"+
"return $this->invokeMethod(0,\"castToInExact\",array($object));\n"+
"case 'N': case 'n':\n"+
"return null;\n"+
"case 'A': case 'a':\n"+
"return $this->invokeMethod(0,\"castToArray\",array($object));\n"+
"case 'O': case 'o':\n"+
"return $object;\n"+
"default:\n"+
"throw new java_RuntimeException(\"$type illegal\");\n"+
"}\n"+
"}\n"+
"function getContext() {\n"+
"static $cache=null;\n"+
"if (!is_null($cache)) return $cache;\n"+
"return $cache=$this->invokeMethod(0,\"getContext\",array());\n"+
"}\n"+
"function getSession($args) {\n"+
"return $this->invokeMethod(0,\"getSession\",$args);\n"+
"}\n"+
"function getServerName() {\n"+
"static $cache=null;\n"+
"if (!is_null($cache)) return $cache;\n"+
"return $cache=$this->protocol->getServerName();\n"+
"}\n"+
"}\n"+
"function java_shutdown() {\n"+
"global $java_initialized;\n"+
"if (!$java_initialized) return;\n"+
"if (session_id()) session_write_close();\n"+
"$client=__javaproxy_Client_getClient();\n"+
"if (!isset($client->protocol) || $client->inArgs) return;\n"+
"if ($client->preparedToSendBuffer)\n"+
"$client->sendBuffer.=$client->preparedToSendBuffer;\n"+
"$client->sendBuffer.=$client->protocol->getKeepAlive();\n"+
"$client->protocol->flush();\n"+
"$client->protocol->keepAlive();\n"+
"}\n"+
"register_shutdown_function(\"java_shutdown\");\n"+
"class java_GlobalRef {\n"+
"public $map;\n"+
"function __construct() {\n"+
"$this->map=array();\n"+
"}\n"+
"function add($object) {\n"+
"if(is_null($object)) return 0;\n"+
"return array_push($this->map,$object);\n"+
"}\n"+
"function get($id) {\n"+
"if(!$id) return 0;\n"+
"return $this->map[--$id];\n"+
"}\n"+
"}\n"+
"class java_NativeParser {\n"+
"public $parser,$handler;\n"+
"public $level,$event;\n"+
"public $buf;\n"+
"function __construct($handler) {\n"+
"$this->handler=$handler;\n"+
"$this->parser=xml_parser_create();\n"+
"xml_parser_set_option($this->parser,XML_OPTION_CASE_FOLDING,0);\n"+
"xml_set_object($this->parser,$this);\n"+
"xml_set_element_handler($this->parser,\"begin\",\"end\");\n"+
"xml_parse($this->parser,\"<F>\");\n"+
"$this->level=0;\n"+
"}\n"+
"function begin($parser,$name,$param) {\n"+
"$this->event=true;\n"+
"switch($name) {\n"+
"case 'X': case 'A': $this->level+=1;\n"+
"}\n"+
"$this->handler->begin($name,$param);\n"+
"}\n"+
"function end($parser,$name) {\n"+
"$this->handler->end($name);\n"+
"switch($name) {\n"+
"case 'X': case 'A': $this->level-=1;\n"+
"}\n"+
"}\n"+
"function getData($str) {\n"+
"return base64_decode($str);\n"+
"}\n"+
"function parse() {\n"+
"do {\n"+
"$this->event=false;\n"+
"$buf=$this->buf=$this->handler->read(JAVA_RECV_SIZE);\n"+
"$len=strlen($buf);\n"+
"if(!xml_parse($this->parser,$buf,$len==0)) {\n"+
"$this->handler->protocol->handler->shutdownBrokenConnection(\n"+
"sprintf(\"protocol error: %s,%s at col %d. Check the back end log for OutOfMemoryErrors.\",\n"+
"$buf,\n"+
"xml_error_string(xml_get_error_code($this->parser)),\n"+
"xml_get_current_column_number($this->parser)));\n"+
"}\n"+
"} while(!$this->event || $this->level>0);\n"+
"}\n"+
"function parserError() {\n"+
"$this->handler->protocol->handler->shutdownBrokenConnection(\n"+
"sprintf(\"protocol error: %s. Check the back end log for details.\",$this->buf));\n"+
"}\n"+
"}\n"+
"class java_Parser {\n"+
"public $parser;\n"+
"function __construct($handler) {\n"+
"if(function_exists(\"xml_parser_create\")) {\n"+
"$this->parser=new java_NativeParser($handler);\n"+
"$handler->RUNTIME[\"PARSER\"]=\"NATIVE\";\n"+
"} else {\n"+
"$this->parser=new java_SimpleParser($handler);\n"+
"$handler->RUNTIME[\"PARSER\"]=\"SIMPLE\";\n"+
"}\n"+
"}\n"+
"function parse() {\n"+
"$this->parser->parse();\n"+
"}\n"+
"function getData($str) {\n"+
"return $this->parser->getData($str);\n"+
"}\n"+
"function parserError() {\n"+
"$this->parser->parserError();\n"+
"}\n"+
"}\n"+
"function java_getHeader($name,$array) {\n"+
"if (array_key_exists($name,$array)) return $array[$name];\n"+
"$name=\"HTTP_$name\";\n"+
"if (array_key_exists($name,$array)) return $array[$name];\n"+
"return null;\n"+
"}\n"+
"function java_checkCliSapi() {\n"+
"$sapi=substr(php_sapi_name(),0,3);\n"+
"return ((($sapi=='cgi') && !get_cfg_var(\"java.session\")) || ($sapi=='cli'));\n"+
"}\n"+
"function java_getCompatibilityOption($client) {\n"+
"static $compatibility=null;\n"+
"if ($compatibility) return $compatibility;\n"+
"$compatibility=$client->RUNTIME[\"PARSER\"]==\"NATIVE\"\n"+
"? (0103-JAVA_PREFER_VALUES)\n"+
": (0100+JAVA_PREFER_VALUES);\n"+
"if(is_int(JAVA_LOG_LEVEL)) {\n"+
"$compatibility |=128 | (7 & JAVA_LOG_LEVEL)<<2;\n"+
"}\n"+
"$compatibility=chr ($compatibility);\n"+
"return $compatibility;\n"+
"}\n"+
"class java_EmptyChannel {\n"+
"protected $handler;\n"+
"private $res;\n"+
"function __construct($handler) {\n"+
"$this->handler=$handler;\n"+
"}\n"+
"function shutdownBrokenConnection () {}\n"+
"function fwrite($data) {\n"+
"return $this->handler->fwrite($data);\n"+
"}\n"+
"function fread($size) {\n"+
"return $this->handler->fread($size);\n"+
"}\n"+
"function getKeepAliveA() {\n"+
"return \"<F p=\\\"A\\\" />\";\n"+
"}\n"+
"function getKeepAliveE() {\n"+
"return \"<F p=\\\"E\\\" />\";\n"+
"}\n"+
"function getKeepAlive() {\n"+
"return $this->getKeepAliveE();\n"+
"}\n"+
"function error() {\n"+
"trigger_error(\"An unchecked exception occured during script execution. Please check the server log files for details.\",E_USER_ERROR);\n"+
"}\n"+
"function checkA($peer) {\n"+
"$val=$this->res[6];\n"+
"if ($val !='A') fclose($peer);\n"+
"if ($val !='A' && $val !='E') {\n"+
"$this->error();\n"+
"}\n"+
"}\n"+
"function checkE() {\n"+
"$val=$this->res[6];\n"+
"if ($val !='E') {\n"+
"$this->error();\n"+
"}\n"+
"}\n"+
"function keepAliveS() {\n"+
"$this->res=$this->fread(10);\n"+
"}\n"+
"function keepAliveSC() {\n"+
"$this->res=$this->fread(10);\n"+
"$this->fwrite(\"\");\n"+
"$this->fread(JAVA_RECV_SIZE);\n"+
"}\n"+
"function keepAliveH() {\n"+
"$this->res=$this->handler->read(10);\n"+
"}\n"+
"function keepAlive() {\n"+
"$this->keepAliveH();\n"+
"$this->checkE();\n"+
"}\n"+
"}\n"+
"abstract class java_SocketChannel extends java_EmptyChannel {\n"+
"public $peer,$host;\n"+
"function __construct($peer,$host) {\n"+
"$this->peer=$peer;\n"+
"$this->host=$host;\n"+
"}\n"+
"function fwrite($data) {\n"+
"return fwrite($this->peer,$data);\n"+
"}\n"+
"function fread($size) {\n"+
"return fread($this->peer,$size);\n"+
"}\n"+
"function shutdownBrokenConnection () {\n"+
"fclose($this->peer);\n"+
"}\n"+
"}\n"+
"class java_SocketChannelP extends java_SocketChannel {\n"+
"function getKeepAlive() {return $this->getKeepAliveA();}\n"+
"function keepAlive() { $this->keepAliveS(); $this->checkA($this->peer); }\n"+
"}\n"+
"class java_ChunkedSocketChannel extends java_SocketChannel {\n"+
"function fwrite($data) {\n"+
"$len=dechex(strlen($data));\n"+
"return fwrite($this->peer,\"${len}\\r\\n${data}\\r\\n\");\n"+
"}\n"+
"function fread($size) {\n"+
"$length=hexdec(fgets($this->peer,JAVA_RECV_SIZE));\n"+
"$data=\"\";\n"+
"while ($length > 0) {\n"+
"$str=fread($this->peer,$length);\n"+
"if (feof ($this->peer)) return null;\n"+
"$length -=strlen($str);\n"+
"$data .=$str;\n"+
"}\n"+
"fgets($this->peer,3);\n"+
"return $data;\n"+
"}\n"+
"function keepAlive() { $this->keepAliveSC(); $this->checkE(); fclose ($this->peer); }\n"+
"}\n"+
"class java_SocketHandler {\n"+
"public $protocol,$channel;\n"+
"function __construct($protocol,$channel) {\n"+
"$this->protocol=$protocol;\n"+
"$this->channel=$channel;\n"+
"}\n"+
"function write($data) {\n"+
"return $this->channel->fwrite($data);\n"+
"}\n"+
"function fwrite($data) {return $this->write($data);}\n"+
"function read($size) {\n"+
"return $this->channel->fread($size);\n"+
"}\n"+
"function fread($size) {return $this->read($size);}\n"+
"function redirect() {}\n"+
"function getKeepAlive() {\n"+
"return $this->channel->getKeepAlive();\n"+
"}\n"+
"function keepAlive() {\n"+
"$this->channel->keepAlive();\n"+
"}\n"+
"function dieWithBrokenConnection($msg) {\n"+
"unset($this->protocol->client->protocol);\n"+
"trigger_error ($msg?$msg:\"unknown error: please see back end log for details\",E_USER_ERROR);\n"+
"}\n"+
"function shutdownBrokenConnection ($msg) {\n"+
"$this->channel->shutdownBrokenConnection();\n"+
"$this->dieWithBrokenConnection($msg);\n"+
"}\n"+
"}\n"+
"class java_SimpleHttpHandler extends java_SocketHandler {\n"+
"public $headers,$cookies;\n"+
"public $context,$ssl,$port;\n"+
"public $host;\n"+
"function createChannel() {\n"+
"$channelName=java_getHeader(\"X_JAVABRIDGE_REDIRECT\",$_SERVER);\n"+
"$context=java_getHeader(\"X_JAVABRIDGE_CONTEXT\",$_SERVER);\n"+
"$len=strlen($context);\n"+
"$len0=java_getCompatibilityOption($this->protocol->client);\n"+
"$len1=chr($len&0xFF); $len>>=8;\n"+
"$len2=chr($len&0xFF);\n"+
"$this->channel=new java_EmptyChannel($this);\n"+
"$this->channel=$this->getChannel($channelName);\n"+
"$this->protocol->socketHandler=new java_SocketHandler($this->protocol,$this->channel);\n"+
"$this->protocol->write(\"\\177${len0}${len1}${len2}${context}\");\n"+
"$this->context=sprintf(\"X_JAVABRIDGE_CONTEXT: %s\\r\\n\",$context);\n"+
"$this->protocol->handler=$this->protocol->socketHandler;\n"+
"$this->protocol->handler->write($this->protocol->client->sendBuffer)\n"+
"or $this->protocol->handler->shutdownBrokenConnection(\"Broken local connection handle\");\n"+
"$this->protocol->client->sendBuffer=null;\n"+
"$this->protocol->handler->read(1)\n"+
"or $this->protocol->handler->shutdownBrokenConnection(\"Broken local connection handle\");\n"+
"}\n"+
"function __construct($protocol,$ssl,$host,$port) {\n"+
"$this->cookies=array();\n"+
"$this->protocol=$protocol;\n"+
"$this->ssl=$ssl;\n"+
"$this->host=$host;\n"+
"$this->port=$port;\n"+
"$this->createChannel();\n"+
"}\n"+
"function getCookies() {\n"+
"$str=\"\";\n"+
"$first=true;\n"+
"foreach($_COOKIE as $k=> $v) {\n"+
"$str .=($first ? \"Cookie: $k=$v\":\"; $k=$v\");\n"+
"$first=false;\n"+
"}\n"+
"if(!$first) $str .=\"\\r\\n\";\n"+
"return $str;\n"+
"}\n"+
"function getContextFromCgiEnvironment() {\n"+
"$ctx=java_getHeader('X_JAVABRIDGE_CONTEXT',$_SERVER);\n"+
"return $ctx;\n"+
"}\n"+
"function getContext() {\n"+
"static $context=null;\n"+
"if($context) return $context;\n"+
"$ctx=$this->getContextFromCgiEnvironment();\n"+
"$context=\"\";\n"+
"if($ctx) {\n"+
"$context=sprintf(\"X_JAVABRIDGE_CONTEXT: %s\\r\\n\",$ctx);\n"+
"}\n"+
"return $context;\n"+
"}\n"+
"function getWebAppInternal() {\n"+
"$context=$this->protocol->webContext;\n"+
"if(isset($context)) return $context;\n"+
"return (JAVA_SERVLET==\"User\" &&\n"+
"array_key_exists('PHP_SELF',$_SERVER) &&\n"+
"array_key_exists('HTTP_HOST',$_SERVER))\n"+
"? $_SERVER['PHP_SELF'].\"javabridge\"\n"+
": null;\n"+
"}\n"+
"function getWebApp() {\n"+
"$context=$this->getWebAppInternal();\n"+
"if(is_null($context)) $context=JAVA_SERVLET;\n"+
"if(is_null($context) || $context[0]!=\"/\")\n"+
"$context=\"/JavaBridge/JavaBridge.phpjavabridge\";\n"+
"return $context;\n"+
"}\n"+
"function write($data) {\n"+
"return $this->protocol->socketHandler->write($data);\n"+
"}\n"+
"function doSetCookie($key,$val,$path) {\n"+
"$path=trim($path);\n"+
"$webapp=$this->getWebAppInternal(); if(!$webapp) $path=\"/\";\n"+
"setcookie($key,$val,0,$path);\n"+
"}\n"+
"function read($size) {\n"+
"return $this->protocol->socketHandler->read($size);\n"+
"}\n"+
"function getChannel($channelName) {\n"+
"$errstr=null; $errno=null;\n"+
"$peer=pfsockopen($this->host,$channelName,$errno,$errstr,20);\n"+
"if (!$peer) throw new java_IllegalStateException(\"No ContextServer for {$this->host}:{$channelName}. Error: $errstr ($errno)\\n\");\n"+
"stream_set_timeout($peer,-1);\n"+
"return new java_SocketChannelP($peer,$this->host);\n"+
"}\n"+
"function keepAlive() {\n"+
"parent::keepAlive();\n"+
"}\n"+
"function redirect() {}\n"+
"}\n"+
"class java_SimpleHttpTunnelHandler extends java_SimpleHttpHandler {\n"+
"public $socket;\n"+
"protected $hasContentLength=false;\n"+
"function createSimpleChannel () {\n"+
"$this->channel=new java_EmptyChannel($this);\n"+
"}\n"+
"function createChannel() {\n"+
"$this->createSimpleChannel();\n"+
"}\n"+
"function shutdownBrokenConnection ($msg) {\n"+
"fclose($this->socket);\n"+
"$this->dieWithBrokenConnection($msg);\n"+
"}\n"+
"function checkSocket($socket,&$errno,&$errstr) {\n"+
"if (!$socket) {\n"+
"$msg=\"Could not connect to the JEE server {$this->ssl}{$this->host}:{$this->port}. Please start it.\";\n"+
"$msg.=java_checkCliSapi()\n"+
"?\" Or define('JAVA_HOSTS',9267); define('JAVA_SERVLET',false); before including 'Java.inc' and try again. Error message: $errstr ($errno)\\n\"\n"+
":\" Error message: $errstr ($errno)\\n\";\n"+
"throw new java_ConnectException($msg);\n"+
"}\n"+
"}\n"+
"function open() {\n"+
"$errno=null; $errstr=null;\n"+
"$socket=fsockopen(\"{$this->ssl}{$this->host}\",$this->port,$errno,$errstr,20);\n"+
"$this->checkSocket($socket,$errno,$errstr);\n"+
"stream_set_timeout($socket,-1);\n"+
"$this->socket=$socket;\n"+
"}\n"+
"function fread($size) {\n"+
"$length=hexdec(fgets($this->socket,JAVA_RECV_SIZE));\n"+
"$data=\"\";\n"+
"while ($length > 0) {\n"+
"$str=fread($this->socket,$length);\n"+
"if (feof ($this->socket)) return null;\n"+
"$length -=strlen($str);\n"+
"$data .=$str;\n"+
"}\n"+
"fgets($this->socket,3);\n"+
"return $data;\n"+
"}\n"+
"function fwrite($data) {\n"+
"$len=dechex(strlen($data));\n"+
"return fwrite($this->socket,\"${len}\\r\\n${data}\\r\\n\");\n"+
"}\n"+
"function close() {\n"+
"fwrite($this->socket,\"0\\r\\n\\r\\n\");\n"+
"fgets($this->socket,JAVA_RECV_SIZE);\n"+
"fgets($this->socket,3);\n"+
"fclose($this->socket);\n"+
"}\n"+
"function __construct($protocol,$ssl,$host,$port) {\n"+
"parent::__construct($protocol,$ssl,$host,$port);\n"+
"$this->open();\n"+
"}\n"+
"function read($size) {\n"+
"if(is_null($this->headers)) $this->parseHeaders();\n"+
"if (isset($this->headers[\"http_error\"])) {\n"+
"if (isset($this->headers[\"transfer_chunked\"])) {\n"+
"$str=$this->fread(JAVA_RECV_SIZE);\n"+
"} elseif (isset($this->headers['content_length'])) {\n"+
"$len=$this->headers['content_length'];\n"+
"for($str=fread($this->socket,$len); strlen($str)<$len; $str.=fread($this->socket,$len-strlen($str)))\n"+
"if (feof ($this->socket)) break;\n"+
"} else {\n"+
"$str=fread($this->socket,JAVA_RECV_SIZE);\n"+
"}\n"+
"$this->shutdownBrokenConnection($str);\n"+
"}\n"+
"return $this->fread(JAVA_RECV_SIZE);\n"+
"}\n"+
"function getBodyFor ($compat,$data) {\n"+
"$len=dechex(2+strlen($data));\n"+
"return \"Cache-Control: no-cache\\r\\nPragma: no-cache\\r\\nTransfer-Encoding: chunked\\r\\n\\r\\n${len}\\r\\n\\177${compat}${data}\\r\\n\";\n"+
"}\n"+
"function write($data) {\n"+
"$compat=java_getCompatibilityOption($this->protocol->client);\n"+
"$this->headers=null;\n"+
"$socket=$this->socket;\n"+
"$webapp=$this->getWebApp();\n"+
"$cookies=$this->getCookies();\n"+
"$context=$this->getContext();\n"+
"$res=\"PUT \";\n"+
"$res .=$webapp;\n"+
"$res .=\" HTTP/1.1\\r\\n\";\n"+
"$res .=\"Host: {$this->host}:{$this->port}\\r\\n\";\n"+
"$res .=$context;\n"+
"$res .=$cookies;\n"+
"$res .=$this->getBodyFor($compat,$data);\n"+
"$count=fwrite($socket,$res) or $this->shutdownBrokenConnection(\"Broken connection handle\");\n"+
"fflush($socket) or $this->shutdownBrokenConnection(\"Broken connection handle\");\n"+
"return $count;\n"+
"}\n"+
"function parseHeaders() {\n"+
"$this->headers=array();\n"+
"$line=trim(fgets($this->socket,JAVA_RECV_SIZE));\n"+
"$ar=explode (\" \",$line);\n"+
"$code=((int)$ar[1]);\n"+
"if ($code !=200) $this->headers[\"http_error\"]=$code;\n"+
"while (($str=trim(fgets($this->socket,JAVA_RECV_SIZE)))) {\n"+
"if($str[0]=='X') {\n"+
"if(!strncasecmp(\"X_JAVABRIDGE_REDIRECT\",$str,21)) {\n"+
"$this->headers[\"redirect\"]=trim(substr($str,22));\n"+
"} else if(!strncasecmp(\"X_JAVABRIDGE_CONTEXT\",$str,20)) {\n"+
"$this->headers[\"context\"]=trim(substr($str,21));\n"+
"}\n"+
"} else if($str[0]=='S') {\n"+
"if(!strncasecmp(\"SET-COOKIE\",$str,10)) {\n"+
"$str=substr($str,12);\n"+
"$this->cookies[]=$str;\n"+
"$ar=explode(\";\",$str);\n"+
"$cookie=explode(\"=\",$ar[0]);\n"+
"$path=\"\";\n"+
"if(isset($ar[1])) $p=explode(\"=\",$ar[1]);\n"+
"if(isset($p)) $path=$p[1];\n"+
"$this->doSetCookie($cookie[0],$cookie[1],$path);\n"+
"}\n"+
"} else if($str[0]=='C') {\n"+
"if(!strncasecmp(\"CONTENT-LENGTH\",$str,14)) {\n"+
"$this->headers[\"content_length\"]=trim(substr($str,15));\n"+
"$this->hasContentLength=true;\n"+
"} else if(!strncasecmp(\"CONNECTION\",$str,10) && !strncasecmp(\"close\",trim(substr($str,11)),5)) {\n"+
"$this->headers[\"connection_close\"]=true;\n"+
"}\n"+
"} else if($str[0]=='T') {\n"+
"if(!strncasecmp(\"TRANSFER-ENCODING\",$str,17) && !strncasecmp(\"chunked\",trim(substr($str,18)),7)) {\n"+
"$this->headers[\"transfer_chunked\"]=true;\n"+
"}\n"+
"}\n"+
"}\n"+
"}\n"+
"function getSimpleChannel() {\n"+
"return new java_ChunkedSocketChannel($this->socket,$this->protocol,$this->host);\n"+
"}\n"+
"function redirect() {\n"+
"$this->isRedirect=isset($this->headers[\"redirect\"]);\n"+
"if ($this->isRedirect)\n"+
"$channelName=$this->headers[\"redirect\"];\n"+
"$context=$this->headers[\"context\"];\n"+
"$len=strlen($context);\n"+
"$len0=chr(0xFF);\n"+
"$len1=chr($len&0xFF); $len>>=8;\n"+
"$len2=chr($len&0xFF);\n"+
"if ($this->isRedirect) {\n"+
"$this->protocol->socketHandler=new java_SocketHandler($this->protocol,$this->getChannel($channelName));\n"+
"$this->protocol->write(\"\\177${len0}${len1}${len2}${context}\");\n"+
"$this->context=sprintf(\"X_JAVABRIDGE_CONTEXT: %s\\r\\n\",$context);\n"+
"$this->close ();\n"+
"$this->protocol->handler=$this->protocol->socketHandler;\n"+
"$this->protocol->handler->write($this->protocol->client->sendBuffer)\n"+
"or $this->protocol->handler->shutdownBrokenConnection(\"Broken local connection handle\");\n"+
"$this->protocol->client->sendBuffer=null;\n"+
"$this->protocol->handler->read(1)\n"+
"or $this->protocol->handler->shutdownBrokenConnection(\"Broken local connection handle\");\n"+
"} else {\n"+
"$this->protocol->handler=$this->protocol->socketHandler=new java_SocketHandler($this->protocol,$this->getSimpleChannel());\n"+
"}\n"+
"}\n"+
"}\n"+
"class java_HttpTunnelHandler extends java_SimpleHttpTunnelHandler {\n"+
"function fread($size) {\n"+
"if ($this->hasContentLength)\n"+
"return fread($this->socket,$this->headers[\"content_length\"]);\n"+
"else\n"+
"return parent::fread($size);\n"+
"}\n"+
"function fwrite($data) {\n"+
"if ($this->hasContentLength)\n"+
"return fwrite($this->socket,$data);\n"+
"else\n"+
"return parent::fwrite($data);\n"+
"}\n"+
"function close() {\n"+
"if ($this->hasContentLength) {\n"+
"fwrite($this->socket,\"0\\r\\n\\r\\n\");\n"+
"fclose($this->socket);\n"+
"} else {\n"+
"parent::fclose($this->socket);\n"+
"}\n"+
"}\n"+
"}\n"+
"class java_Protocol {\n"+
"public $client;\n"+
"public $webContext;\n"+
"public $serverName;\n"+
"function getOverrideHosts() {\n"+
"if(array_key_exists('X_JAVABRIDGE_OVERRIDE_HOSTS',$_ENV)) {\n"+
"$override=$_ENV['X_JAVABRIDGE_OVERRIDE_HOSTS'];\n"+
"if(!is_null($override) && $override!='/') return $override;\n"+
"}\n"+
"return\n"+
"java_getHeader('X_JAVABRIDGE_OVERRIDE_HOSTS_REDIRECT',$_SERVER);\n"+
"}\n"+
"static function getHost() {\n"+
"static $host=null;\n"+
"if(is_null($host)) {\n"+
"$hosts=explode(\";\",JAVA_HOSTS);\n"+
"$host=explode(\":\",$hosts[0]);\n"+
"while(count ($host) < 3) array_unshift($host,\"\");\n"+
"if (substr($host[1],0,2)==\"//\") $host[1]=substr($host[1],2);\n"+
"}\n"+
"return $host;\n"+
"}\n"+
"function createHttpHandler() {\n"+
"$overrideHosts=$this->getOverrideHosts();\n"+
"$ssl=\"\";\n"+
"if($overrideHosts) {\n"+
"$s=$overrideHosts;\n"+
"if((strlen($s)>2) && ($s[1]==':')) {\n"+
"if($s[0]=='s')\n"+
"$ssl=\"ssl://\";\n"+
"$s=substr($s,2);\n"+
"}\n"+
"$webCtx=strpos($s,\"//\");\n"+
"if($webCtx)\n"+
"$host=substr($s,0,$webCtx);\n"+
"else\n"+
"$host=$s;\n"+
"$idx=strpos($host,':');\n"+
"if($idx) {\n"+
"if($webCtx)\n"+
"$port=substr($host,$idx+1,$webCtx);\n"+
"else\n"+
"$port=substr($host,$idx+1);\n"+
"$host=substr($host,0,$idx);\n"+
"} else {\n"+
"$port=\"8080\";\n"+
"}\n"+
"if($webCtx) $webCtx=substr($s,$webCtx+1);\n"+
"$this->webContext=$webCtx;\n"+
"} else {\n"+
"$hostVec=java_Protocol::getHost();\n"+
"if ($ssl=$hostVec[0]) $ssl .=\"://\";\n"+
"$host=$hostVec[1];\n"+
"$port=$hostVec[2];\n"+
"}\n"+
"$this->serverName=\"${ssl}${host}:$port\";\n"+
"if ((array_key_exists(\"X_JAVABRIDGE_REDIRECT\",$_SERVER)) ||\n"+
"(array_key_exists(\"HTTP_X_JAVABRIDGE_REDIRECT\",$_SERVER)))\n"+
"return new java_SimpleHttpHandler($this,$ssl,$host,$port);\n"+
"return new java_HttpTunnelHandler($this,$ssl,$host,$port);\n"+
"}\n"+
"function createSimpleHandler($name,$again=true) {\n"+
"$channelName=$name;\n"+
"$errno=null; $errstr=null;\n"+
"if(is_numeric($channelName)) {\n"+
"$peer=@pfsockopen($host=\"127.0.0.1\",$channelName,$errno,$errstr,5);\n"+
"} else {\n"+
"$type=$channelName[0];\n"+
"list($host,$channelName)=explode(\":\",$channelName);\n"+
"$peer=pfsockopen($host,$channelName,$errno,$errstr,20);\n"+
"if (!$peer)\n"+
"throw new java_ConnectException(\"No Java server at $host:$channelName. Error message: $errstr ($errno)\");\n"+
"}\n"+
"if (!$peer) {\n"+
"$java=file_exists(ini_get(\"extension_dir\").\"/JavaBridge.jar\")?ini_get(\"extension_dir\").\"/JavaBridge.jar\":(java_get_base().\"/JavaBridge.jar\");\n"+
"if (!file_exists($java))\n"+
"throw new java_IOException(\"Could not find $java in \".getcwd().\". Download it from http://sf.net/projects/php-java-bridge/files/Binary%20package/php-java-bridge_\".JAVA_PEAR_VERSION.\"/exploded/JavaBridge.jar/download and try again.\");\n"+
"$java_cmd=\"java -Dphp.java.bridge.daemon=true -jar \\\"${java}\\\" INET_LOCAL:$channelName 0\";\n"+
"if (!$again)\n"+
"throw new java_ConnectException(\"No Java back end! Please run it with: $java_cmd. Error message: $errstr ($errno)\");\n"+
"if (!java_checkCliSapi())\n"+
"trigger_error(\"This PHP SAPI requires a JEE or SERVLET back end. Start it,define ('JAVA_SERVLET',true); define('JAVA_HOSTS',...); and try again.\",E_USER_ERROR);\n"+
"system ($java_cmd);\n"+
"return $this->createSimpleHandler($name,false);\n"+
"}\n"+
"stream_set_timeout($peer,-1);\n"+
"$handler=new java_SocketHandler($this,new java_SocketChannelP($peer,$host));\n"+
"$compatibility=java_getCompatibilityOption($this->client);\n"+
"$this->write(\"\\177$compatibility\");\n"+
"$this->serverName=\"127.0.0.1:$channelName\";\n"+
"return $handler;\n"+
"}\n"+
"function java_get_simple_channel() {\n"+
"return (JAVA_HOSTS&&(!JAVA_SERVLET||(JAVA_SERVLET==\"Off\"))) ? JAVA_HOSTS : null;\n"+
"}\n"+
"function createHandler() {\n"+
"if(!java_getHeader('X_JAVABRIDGE_OVERRIDE_HOSTS',$_SERVER)&&\n"+
"((function_exists(\"java_get_default_channel\")&&($defaultChannel=java_get_default_channel())) ||\n"+
"($defaultChannel=$this->java_get_simple_channel())) ) {\n"+
"return $this->createSimpleHandler($defaultChannel);\n"+
"} else {\n"+
"return $this->createHttpHandler();\n"+
"}\n"+
"}\n"+
"function __construct ($client) {\n"+
"$this->client=$client;\n"+
"$this->handler=$this->createHandler();\n"+
"}\n"+
"function redirect() {\n"+
"$this->handler->redirect();\n"+
"}\n"+
"function read($size) {\n"+
"return $this->handler->read($size);\n"+
"}\n"+
"function sendData() {\n"+
"$this->handler->write($this->client->sendBuffer);\n"+
"$this->client->sendBuffer=null;\n"+
"}\n"+
"function flush() {\n"+
"$this->sendData();\n"+
"}\n"+
"function getKeepAlive() {\n"+
"return $this->handler->getKeepAlive();\n"+
"}\n"+
"function keepAlive() {\n"+
"$this->handler->keepAlive();\n"+
"}\n"+
"function handle() {\n"+
"$this->client->handleRequests();\n"+
"}\n"+
"function write($data) {\n"+
"$this->client->sendBuffer.=$data;\n"+
"}\n"+
"function finish() {\n"+
"$this->flush();\n"+
"$this->handle();\n"+
"$this->redirect();\n"+
"}\n"+
"function referenceBegin($name) {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$signature=sprintf(\"<H p=\\\"1\\\" v=\\\"%s\\\">\",$name);\n"+
"$this->write($signature);\n"+
"$signature[6]=\"2\";\n"+
"$this->client->currentArgumentsFormat=$signature;\n"+
"}\n"+
"function referenceEnd() {\n"+
"$this->client->currentArgumentsFormat.=$format=\"</H>\";\n"+
"$this->write($format);\n"+
"$this->finish();\n"+
"$this->client->currentCacheKey=null;\n"+
"}\n"+
"function createObjectBegin($name) {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$signature=sprintf(\"<K p=\\\"1\\\" v=\\\"%s\\\">\",$name);\n"+
"$this->write($signature);\n"+
"$signature[6]=\"2\";\n"+
"$this->client->currentArgumentsFormat=$signature;\n"+
"}\n"+
"function createObjectEnd() {\n"+
"$this->client->currentArgumentsFormat.=$format=\"</K>\";\n"+
"$this->write($format);\n"+
"$this->finish();\n"+
"$this->client->currentCacheKey=null;\n"+
"}\n"+
"function propertyAccessBegin($object,$method) {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$this->write(sprintf(\"<G p=\\\"1\\\" v=\\\"%x\\\" m=\\\"%s\\\">\",$object,$method));\n"+
"$this->client->currentArgumentsFormat=\"<G p=\\\"2\\\" v=\\\"%x\\\" m=\\\"${method}\\\">\";\n"+
"}\n"+
"function propertyAccessEnd() {\n"+
"$this->client->currentArgumentsFormat.=$format=\"</G>\";\n"+
"$this->write($format);\n"+
"$this->finish();\n"+
"$this->client->currentCacheKey=null;\n"+
"}\n"+
"function invokeBegin($object,$method) {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$this->write(sprintf(\"<Y p=\\\"1\\\" v=\\\"%x\\\" m=\\\"%s\\\">\",$object,$method));\n"+
"$this->client->currentArgumentsFormat=\"<Y p=\\\"2\\\" v=\\\"%x\\\" m=\\\"${method}\\\">\";\n"+
"}\n"+
"function invokeEnd() {\n"+
"$this->client->currentArgumentsFormat.=$format=\"</Y>\";\n"+
"$this->write($format);\n"+
"$this->finish();\n"+
"$this->client->currentCacheKey=null;\n"+
"}\n"+
"function resultBegin() {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$this->write(\"<R>\");\n"+
"}\n"+
"function resultEnd() {\n"+
"$this->client->currentCacheKey=null;\n"+
"$this->write(\"</R>\");\n"+
"$this->flush();\n"+
"}\n"+
"function writeString($name) {\n"+
"$this->client->currentArgumentsFormat.=$format=\"<S v=\\\"%s\\\"/>\";\n"+
"$this->write(sprintf($format,htmlspecialchars($name,ENT_COMPAT,\"ISO-8859-1\")));\n"+
"}\n"+
"function writeBoolean($boolean) {\n"+
"$this->client->currentArgumentsFormat.=$format=\"<T v=\\\"%s\\\"/>\";\n"+
"$this->write(sprintf($format,$boolean));\n"+
"}\n"+
"function writeLong($l) {\n"+
"$this->client->currentArgumentsFormat.=\"<J v=\\\"%d\\\"/>\";\n"+
"if($l<0) {\n"+
"$this->write(sprintf(\"<L v=\\\"%x\\\" p=\\\"A\\\"/>\",-$l));\n"+
"} else {\n"+
"$this->write(sprintf(\"<L v=\\\"%x\\\" p=\\\"O\\\"/>\",$l));\n"+
"}\n"+
"}\n"+
"function writeULong($l) {\n"+
"$this->client->currentArgumentsFormat.=$format=\"<L v=\\\"%x\\\" p=\\\"O\\\"/>\";\n"+
"$this->write(sprintf($format,$l));\n"+
"}\n"+
"function writeDouble($d) {\n"+
"$this->client->currentArgumentsFormat.=$format=\"<D v=\\\"%.14e\\\"/>\";\n"+
"$this->write(sprintf($format,$d));\n"+
"}\n"+
"function writeObject($object) {\n"+
"$this->client->currentArgumentsFormat.=$format=\"<O v=\\\"%x\\\"/>\";\n"+
"$this->write(sprintf($format,$object));\n"+
"}\n"+
"function writeException($object,$str) {\n"+
"$this->write(sprintf(\"<E v=\\\"%x\\\" m=\\\"%s\\\"/>\",$object,htmlspecialchars($str,ENT_COMPAT,\"ISO-8859-1\")));\n"+
"}\n"+
"function writeCompositeBegin_a() {\n"+
"$this->write(\"<X t=\\\"A\\\">\");\n"+
"}\n"+
"function writeCompositeBegin_h() {\n"+
"$this->write(\"<X t=\\\"H\\\">\");\n"+
"}\n"+
"function writeCompositeEnd() {\n"+
"$this->write(\"</X>\");\n"+
"}\n"+
"function writePairBegin_s($key) {\n"+
"$this->write(sprintf(\"<P t=\\\"S\\\" v=\\\"%s\\\">\",htmlspecialchars($key,ENT_COMPAT,\"ISO-8859-1\")));\n"+
"}\n"+
"function writePairBegin_n($key) {\n"+
"$this->write(sprintf(\"<P t=\\\"N\\\" v=\\\"%x\\\">\",$key));\n"+
"}\n"+
"function writePairBegin() {\n"+
"$this->write(\"<P>\");\n"+
"}\n"+
"function writePairEnd() {\n"+
"$this->write(\"</P>\");\n"+
"}\n"+
"function writeUnref($object) {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$this->write(sprintf(\"<U v=\\\"%x\\\"/>\",$object));\n"+
"}\n"+
"function writeExitCode($code) {\n"+
"$this->client->sendBuffer.=$this->client->preparedToSendBuffer;\n"+
"$this->client->preparedToSendBuffer=null;\n"+
"$this->write(sprintf(\"<Z v=\\\"%x\\\"/>\",0xffffffff&$code));\n"+
"}\n"+
"function getServerName() {\n"+
"return $this->serverName;\n"+
"}\n"+
"}\n"+
"class java_ParserString {\n"+
"public $string,$off,$length;\n"+
"function toString() {\n"+
"return $this->getString();\n"+
"}\n"+
"function getString() {\n"+
"return substr($this->string,$this->off,$this->length);\n"+
"}\n"+
"}\n"+
"class java_ParserTag {\n"+
"public $n,$strings;\n"+
"function __construct() {\n"+
"$this->strings=array();\n"+
"$this->n=0;\n"+
"}\n"+
"}\n"+
"class java_SimpleParser {\n"+
"public $SLEN=256;\n"+
"public $handler;\n"+
"public $tag,$buf,$len,$s;\n"+
"public $type;\n"+
"function __construct($handler) {\n"+
"$this->handler=$handler;\n"+
"$this->tag=array(new java_ParserTag(),new java_ParserTag(),new java_ParserTag());\n"+
"$this->len=$this->SLEN;\n"+
"$this->s=str_repeat(\" \",$this->SLEN);\n"+
"$this->type=$this->VOJD;\n"+
"}\n"+
"public $BEGIN=0,$KEY=1,$VAL=2,$ENTITY=3,$VOJD=5,$END=6;\n"+
"public $level=0,$eor=0; public $in_dquote,$eot=false;\n"+
"public $pos=0,$c=0,$i=0,$i0=0,$e;\n"+
"function RESET() {\n"+
"$this->type=$this->VOJD;\n"+
"$this->level=0;\n"+
"$this->eor=0;\n"+
"$this->in_dquote=false;\n"+
"$this->i=0;\n"+
"$this->i0=0;\n"+
"}\n"+
"function APPEND($c) {\n"+
"if($this->i>=$this->len-1) {\n"+
"$this->s=str_repeat($this->s,2);\n"+
"$this->len*=2;\n"+
"}\n"+
"$this->s[$this->i++]=$c;\n"+
"}\n"+
"function CALL_BEGIN() {\n"+
"$pt=&$this->tag[1]->strings;\n"+
"$st=&$this->tag[2]->strings;\n"+
"$t=&$this->tag[0]->strings[0];\n"+
"$name=$t->string[$t->off];\n"+
"$n=$this->tag[2]->n;\n"+
"$ar=array();\n"+
"for($i=0; $i<$n; $i++) {\n"+
"$ar[$pt[$i]->getString()]=$st[$i]->getString();\n"+
"}\n"+
"$this->handler->begin($name,$ar);\n"+
"}\n"+
"function CALL_END() {\n"+
"$t=&$this->tag[0]->strings[0];\n"+
"$name=$t->string[$t->off];\n"+
"$this->handler->end($name);\n"+
"}\n"+
"function PUSH($t) {\n"+
"$str=&$this->tag[$t]->strings;\n"+
"$n=&$this->tag[$t]->n;\n"+
"$this->s[$this->i]='|';\n"+
"if(!isset($str[$n])){$h=$this->handler; $str[$n]=$h->createParserString();}\n"+
"$str[$n]->string=&$this->s;\n"+
"$str[$n]->off=$this->i0;\n"+
"$str[$n]->length=$this->i-$this->i0;\n"+
"++$this->tag[$t]->n;\n"+
"$this->APPEND('|');\n"+
"$this->i0=$this->i;\n"+
"}\n"+
"function parse() {\n"+
"while($this->eor==0) {\n"+
"if($this->c>=$this->pos) {\n"+
"$this->buf=$this->handler->read(JAVA_RECV_SIZE);\n"+
"if(is_null($this->buf) || strlen($this->buf)==0)\n"+
"$this->handler->protocol->handler->shutdownBrokenConnection(\"protocol error. Check the back end log for OutOfMemoryErrors.\");\n"+
"$this->pos=strlen($this->buf);\n"+
"if($this->pos==0) break;\n"+
"$this->c=0;\n"+
"}\n"+
"switch(($ch=$this->buf[$this->c]))\n"+
"{\n"+
"case '<': if($this->in_dquote) {$this->APPEND($ch); break;}\n"+
"$this->level+=1;\n"+
"$this->type=$this->BEGIN;\n"+
"break;\n"+
"case '\\t': case '\\f': case '\\n': case '\\r': case ' ': if($this->in_dquote) {$this->APPEND($ch); break;}\n"+
"if($this->type==$this->BEGIN) {\n"+
"$this->PUSH($this->type);\n"+
"$this->type=$this->KEY;\n"+
"}\n"+
"break;\n"+
"case '=': if($this->in_dquote) {$this->APPEND($ch); break;}\n"+
"$this->PUSH($this->type);\n"+
"$this->type=$this->VAL;\n"+
"break;\n"+
"case '/': if($this->in_dquote) {$this->APPEND($ch); break;}\n"+
"if($this->type==$this->BEGIN) { $this->type=$this->END; $this->level-=1; }\n"+
"$this->level-=1;\n"+
"$this->eot=true;\n"+
"break;\n"+
"case '>': if($this->in_dquote) {$this->APPEND($ch); break;}\n"+
"if($this->type==$this->END){\n"+
"$this->PUSH($this->BEGIN);\n"+
"$this->CALL_END();\n"+
"} else {\n"+
"if($this->type==$this->VAL) $this->PUSH($this->type);\n"+
"$this->CALL_BEGIN();\n"+
"}\n"+
"$this->tag[0]->n=$this->tag[1]->n=$this->tag[2]->n=0; $this->i0=$this->i=0;\n"+
"$this->type=$this->VOJD;\n"+
"if($this->level==0) $this->eor=1;\n"+
"break;\n"+
"case ';':\n"+
"if($this->type==$this->ENTITY) {\n"+
"switch ($this->s[$this->e+1]) {\n"+
"case 'l': $this->s[$this->e]='<'; $this->i=$this->e+1; break;\n"+
"case 'g': $this->s[$this->e]='>'; $this->i=$this->e+1; break;\n"+
"case 'a': $this->s[$this->e]=($this->s[$this->e+2]=='m'?'&':'\\''); $this->i=$this->e+1; break;\n"+
"case 'q': $this->s[$this->e]='\"'; $this->i=$this->e+1; break;\n"+
"default: $this->APPEND($ch);\n"+
"}\n"+
"$this->type=$this->VAL;\n"+
"} else {\n"+
"$this->APPEND($ch);\n"+
"}\n"+
"break;\n"+
"case '&':\n"+
"$this->type=$this->ENTITY;\n"+
"$this->e=$this->i;\n"+
"$this->APPEND($ch);\n"+
"break;\n"+
"case '\"':\n"+
"$this->in_dquote=!$this->in_dquote;\n"+
"if(!$this->in_dquote && $this->type==$this->VAL) {\n"+
"$this->PUSH($this->type);\n"+
"$this->type=$this->KEY;\n"+
"}\n"+
"break;\n"+
"default:\n"+
"$this->APPEND($ch);\n"+
"}\n"+
"$this->c+=1;\n"+
"}\n"+
"$this->RESET();\n"+
"}\n"+
"function getData($str) {\n"+
"return $str;\n"+
"}\n"+
"function parserError() {\n"+
"$this->handler->protocol->handler->shutdownBrokenConnection(\n"+
"sprintf(\"protocol error: %s. Check the back end log for details.\",$this->s));\n"+
"}\n"+
"}\n"+
"interface java_JavaType {};\n"+
"$java_initialized=false;\n"+
"function __javaproxy_Client_getClient() {\n"+
"static $client=null;\n"+
"if(!is_null($client)) return $client;\n"+
"if (function_exists(\"java_create_client\")) $client=java_create_client();\n"+
"else {\n"+
"global $java_initialized;\n"+
"$client=new java_Client();\n"+
"$java_initialized=true;\n"+
"}\n"+
"return $client;\n"+
"}\n"+
"function java_last_exception_get() {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"getLastException\",array());\n"+
"}\n"+
"function java_last_exception_clear() {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"$client->invokeMethod(0,\"clearLastException\",array());\n"+
"}\n"+
"function java_values_internal($object) {\n"+
"if(!$object instanceof java_JavaType) return $object;\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"getValues\",array($object));\n"+
"}\n"+
"function java_invoke($object,$method,$args) {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"$id=($object==null) ? 0 : $object->__java;\n"+
"return $client->invokeMethod($id,$method,$args);\n"+
"}\n"+
"function java_unwrap ($object) {\n"+
"if(!$object instanceof java_JavaType) throw new java_IllegalArgumentException($object);\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->globalRef->get($client->invokeMethod(0,\"unwrapClosure\",array($object)));\n"+
"}\n"+
"function java_values($object) {\n"+
"return java_values_internal($object);\n"+
"}\n"+
"function java_reset() {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"reset\",array());\n"+
"}\n"+
"function java_inspect_internal($object) {\n"+
"if(!$object instanceof java_JavaType) throw new java_IllegalArgumentException($object);\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"inspect\",array($object));\n"+
"}\n"+
"function java_inspect($object) {\n"+
"return java_inspect_internal($object);\n"+
"}\n"+
"function java_set_file_encoding($enc) {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"setFileEncoding\",array($enc));\n"+
"}\n"+
"function java_instanceof_internal($ob,$clazz) {\n"+
"if(!$ob instanceof java_JavaType) throw new java_IllegalArgumentException($ob);\n"+
"if(!$clazz instanceof java_JavaType) throw new java_IllegalArgumentException($clazz);\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"instanceOf\",array($ob,$clazz));\n"+
"}\n"+
"function java_instanceof($ob,$clazz) {\n"+
"return java_instanceof_internal($ob,$clazz);\n"+
"}\n"+
"function java_cast_internal($object,$type) {\n"+
"if(!$object instanceof java_JavaType) {\n"+
"switch($type[0]) {\n"+
"case 'S': case 's':\n"+
"return (string)$object;\n"+
"case 'B': case 'b':\n"+
"return (boolean)$object;\n"+
"case 'L': case 'I': case 'l': case 'i':\n"+
"return (integer)$object;\n"+
"case 'D': case 'd': case 'F': case 'f':\n"+
"return (float) $object;\n"+
"case 'N': case 'n':\n"+
"return null;\n"+
"case 'A': case 'a':\n"+
"return (array)$object;\n"+
"case 'O': case 'o':\n"+
"return (object)$object;\n"+
"}\n"+
"}\n"+
"return $object->__cast($type);\n"+
"}\n"+
"function java_cast($object,$type) {\n"+
"return java_cast_internal($object,$type);\n"+
"}\n"+
"function java_require($arg) {\n"+
"trigger_error('java_require() not supported anymore. Please use <a href=\"http://php-java-bridge.sourceforge.net/pjb/webapp.php>tomcat or jee hot deployment</a> instead',E_USER_WARNING);\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->invokeMethod(0,\"updateJarLibraryPath\",\n"+
"array($arg,ini_get(\"extension_dir\")));\n"+
"}\n"+
"function java_get_lifetime ()\n"+
"{\n"+
"$session_max_lifetime=ini_get(\"session.gc_maxlifetime\");\n"+
"return $session_max_lifetime ? (int)$session_max_lifetime : 1440;\n"+
"}\n"+
"function java_session_array($args) {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"if(!isset($args[0])) $args[0]=null;\n"+
"if(!isset($args[1]))\n"+
"$args[1]=0;\n"+
"elseif ($args[1]===true)\n"+
"$args[1]=1;\n"+
"else\n"+
"$args[1]=2;\n"+
"if(!isset($args[2])) {\n"+
"$args[2]=java_get_lifetime ();\n"+
"}\n"+
"return $client->getSession($args);\n"+
"}\n"+
"function java_session() {\n"+
"return java_session_array(func_get_args());\n"+
"}\n"+
"function java_server_name() {\n"+
"try {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->getServerName();\n"+
"} catch (java_ConnectException $ex) {\n"+
"return null;\n"+
"}\n"+
"}\n"+
"function java_context() {\n"+
"$client=__javaproxy_Client_getClient();\n"+
"return $client->getContext();\n"+
"}\n"+
"function java_closure_array($args) {\n"+
"if(isset($args[2]) && ((!($args[2] instanceof java_JavaType))&&!is_array($args[2])))\n"+
"throw new java_IllegalArgumentException($args[2]);\n"+
"$client=__javaproxy_Client_getClient();\n"+
"$args[0]=isset($args[0]) ? $client->globalRef->add($args[0]) : 0;\n"+
"$client->protocol->invokeBegin(0,\"makeClosure\");\n"+
"$n=count($args);\n"+
"$client->protocol->writeULong($args[0]);\n"+
"for($i=1; $i<$n; $i++) {\n"+
"$client->writeArg($args[$i]);\n"+
"}\n"+
"$client->protocol->invokeEnd();\n"+
"$val=$client->getResult();\n"+
"return $val;\n"+
"}\n"+
"function java_closure() {\n"+
"return java_closure_array(func_get_args());\n"+
"}\n"+
"function java_begin_document() {\n"+
"}\n"+
"function java_end_document() {\n"+
"}\n"+
"class java_JavaProxy implements java_JavaType {\n"+
"public $__serialID,$__java;\n"+
"public $__signature;\n"+
"public $__client;\n"+
"public $__tempGlobalRef;\n"+
"function __construct($java,$signature){\n"+
"$this->__java=$java;\n"+
"$this->__signature=$signature;\n"+
"$this->__client=__javaproxy_Client_getClient();\n"+
"}\n"+
"function __cast($type) {\n"+
"return $this->__client->cast($this,$type);\n"+
"}\n"+
"function __sleep() {\n"+
"$args=array($this,java_get_lifetime());\n"+
"$this->__serialID=$this->__client->invokeMethod(0,\"serialize\",$args);\n"+
"$this->__tempGlobalRef=$this->__client->globalRef;\n"+
"return array(\"__serialID\",\"__tempGlobalRef\");\n"+
"}\n"+
"function __wakeup() {\n"+
"$args=array($this->__serialID,java_get_lifetime());\n"+
"$this->__client=__javaproxy_Client_getClient();\n"+
"if($this->__tempGlobalRef)\n"+
"$this->__client->globalRef=$this->__tempGlobalRef;\n"+
"$this->__tempGlobalRef=null;\n"+
"$this->__java=$this->__client->invokeMethod(0,\"deserialize\",$args);\n"+
"}\n"+
"function __destruct() {\n"+
"if(isset($this->__client))\n"+
"$this->__client->unref($this->__java);\n"+
"}\n"+
"function __get($key) {\n"+
"return $this->__client->getProperty($this->__java,$key);\n"+
"}\n"+
"function __set($key,$val) {\n"+
"$this->__client->setProperty($this->__java,$key,$val);\n"+
"}\n"+
"function __call($method,$args) {\n"+
"return $this->__client->invokeMethod($this->__java,$method,$args);\n"+
"}\n"+
"function __toString() {\n"+
"try {\n"+
"return $this->__client->invokeMethod(0,\"ObjectToString\",array($this));\n"+
"} catch (JavaException $ex) {\n"+
"trigger_error(\"Exception in Java::__toString(): \". java_truncate((string)$ex),E_USER_WARNING);\n"+
"return \"\";\n"+
"}\n"+
"}\n"+
"}\n"+
"class java_objectIterator implements Iterator {\n"+
"private $var;\n"+
"function __construct($javaProxy) {\n"+
"$this->var=java_cast ($javaProxy,\"A\");\n"+
"}\n"+
"function rewind() {\n"+
"reset($this->var);\n"+
"}\n"+
"function valid() {\n"+
"return $this->current() !==false;\n"+
"}\n"+
"function next() {\n"+
"return next($this->var);\n"+
"}\n"+
"function key() {\n"+
"return key($this->var);\n"+
"}\n"+
"function current() {\n"+
"return current($this->var);\n"+
"}\n"+
"}\n"+
"class java_IteratorProxy extends java_JavaProxy implements IteratorAggregate {\n"+
"function getIterator() {\n"+
"return new java_ObjectIterator($this);\n"+
"}\n"+
"}\n"+
"class java_ArrayProxy extends java_IteratorProxy implements ArrayAccess {\n"+
"function offsetExists($idx) {\n"+
"$ar=array($this,$idx);\n"+
"return $this->__client->invokeMethod(0,\"offsetExists\",$ar);\n"+
"}\n"+
"function offsetGet($idx) {\n"+
"$ar=array($this,$idx);\n"+
"return $this->__client->invokeMethod(0,\"offsetGet\",$ar);\n"+
"}\n"+
"function offsetSet($idx,$val) {\n"+
"$ar=array($this,$idx,$val);\n"+
"return $this->__client->invokeMethod(0,\"offsetSet\",$ar);\n"+
"}\n"+
"function offsetUnset($idx) {\n"+
"$ar=array($this,$idx);\n"+
"return $this->__client->invokeMethod(0,\"offsetUnset\",$ar);\n"+
"}\n"+
"}\n"+
"class java_ExceptionProxy extends java_JavaProxy {\n"+
"function __toExceptionString($trace) {\n"+
"$args=array($this,$trace);\n"+
"return $this->__client->invokeMethod(0,\"ObjectToString\",$args);\n"+
"}\n"+
"}\n"+
"abstract class java_AbstractJava implements IteratorAggregate,ArrayAccess,java_JavaType {\n"+
"public $__client;\n"+
"public $__delegate;\n"+
"public $__serialID;\n"+
"public $__factory;\n"+
"public $__java,$__signature;\n"+
"public $__cancelProxyCreationTag;\n"+
"function __createDelegate() {\n"+
"$proxy=$this->__delegate=\n"+
"$this->__factory->create($this->__java,$this->__signature);\n"+
"$this->__java=$proxy->__java;\n"+
"$this->__signature=$proxy->__signature;\n"+
"}\n"+
"function __cast($type) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"return $this->__delegate->__cast($type);\n"+
"}\n"+
"function __sleep() {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"$this->__delegate->__sleep();\n"+
"return array(\"__delegate\");\n"+
"}\n"+
"function __wakeup() {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"$this->__delegate->__wakeup();\n"+
"$this->__java=$this->__delegate->__java;\n"+
"$this->__client=$this->__delegate->__client;\n"+
"}\n"+
"function __get($key) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"return $this->__delegate->__get($key);\n"+
"}\n"+
"function __set($key,$val) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"$this->__delegate->__set($key,$val);\n"+
"}\n"+
"function __call($method,$args) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"return $this->__delegate->__call($method,$args);\n"+
"}\n"+
"function __toString() {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"return $this->__delegate->__toString();\n"+
"}\n"+
"function getIterator() {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"if(func_num_args()==0) return $this->__delegate->getIterator();\n"+
"$args=func_get_args(); return $this->__call(\"getIterator\",$args);\n"+
"}\n"+
"function offsetExists($idx) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"if(func_num_args()==1) return $this->__delegate->offsetExists($idx);\n"+
"$args=func_get_args(); return $this->__call(\"offsetExists\",$args);\n"+
"}\n"+
"function offsetGet($idx) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"if(func_num_args()==1) return $this->__delegate->offsetGet($idx);\n"+
"$args=func_get_args(); return $this->__call(\"offsetGet\",$args);\n"+
"}\n"+
"function offsetSet($idx,$val) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"if(func_num_args()==2) return $this->__delegate->offsetSet($idx,$val);\n"+
"$args=func_get_args(); return $this->__call(\"offsetSet\",$args);\n"+
"}\n"+
"function offsetUnset($idx) {\n"+
"if(!isset($this->__delegate)) $this->__createDelegate();\n"+
"if(func_num_args()==1) return $this->__delegate->offsetUnset($idx);\n"+
"$args=func_get_args(); return $this->__call(\"offsetUnset\",$args);\n"+
"}\n"+
"}\n"+
"class Java extends java_AbstractJava {\n"+
"function __construct() {\n"+
"$client=$this->__client=__javaproxy_Client_getClient();\n"+
"$args=func_get_args();\n"+
"$name=array_shift($args);\n"+
"if(is_array($name)) {$args=$name; $name=array_shift($args);}\n"+
"$sig=\"&{$this->__signature}@{$name}\";\n"+
"$len=count($args);\n"+
"$args2=array();\n"+
"for($i=0; $i<$len; $i++) {\n"+
"switch(gettype($val=$args[$i])) {\n"+
"case 'boolean': array_push($args2,$val); $sig.='@b'; break;\n"+
"case 'integer': array_push($args2,$val); $sig.='@i'; break;\n"+
"case 'double': array_push($args2,$val); $sig.='@d'; break;\n"+
"case 'string': array_push($args2,htmlspecialchars($val,ENT_COMPAT,\"ISO-8859-1\")); $sig.='@s'; break;\n"+
"case 'array':$sig=\"~INVALID\"; break;\n"+
"case 'object':\n"+
"if($val instanceof java_JavaType) {\n"+
"array_push($args2,$val->__java);\n"+
"$sig.=\"@o{$val->__signature}\";\n"+
"}\n"+
"else {\n"+
"$sig=\"~INVALID\";\n"+
"}\n"+
"break;\n"+
"case 'resource': array_push($args2,$val); $sig.='@r'; break;\n"+
"case 'NULL': array_push($args2,$val); $sig.='@N'; break;\n"+
"case 'unknown type': array_push($args2,$val); $sig.='@u'; break;\n"+
"default: throw new java_IllegalArgumentException($val);\n"+
"}\n"+
"}\n"+
"if(array_key_exists($sig,$client->methodCache)) {\n"+
"$cacheEntry=&$client->methodCache[$sig];\n"+
"$client->sendBuffer.=$client->preparedToSendBuffer;\n"+
"if(strlen($client->sendBuffer)>=JAVA_SEND_SIZE) {\n"+
"if($client->protocol->handler->write($client->sendBuffer)<=0)\n"+
"throw new java_IllegalStateException(\"Connection out of sync,check backend log for details.\");\n"+
"$client->sendBuffer=null;\n"+
"}\n"+
"$client->preparedToSendBuffer=vsprintf($cacheEntry->fmt,$args2);\n"+
"$this->__java=++$client->asyncCtx;\n"+
"$this->__factory=$cacheEntry->factory;\n"+
"$this->__signature=$cacheEntry->signature;\n"+
"$this->__cancelProxyCreationTag=++$client->cancelProxyCreationTag;\n"+
"} else {\n"+
"$client->currentCacheKey=$sig;\n"+
"$delegate=$this->__delegate=$client->createObject($name,$args);\n"+
"$this->__java=$delegate->__java;\n"+
"$this->__signature=$delegate->__signature;\n"+
"}\n"+
"}\n"+
"function __destruct() {\n"+
"if(!isset($this->__client)) return;\n"+
"$client=$this->__client;\n"+
"$preparedToSendBuffer=&$client->preparedToSendBuffer;\n"+
"if($preparedToSendBuffer &&\n"+
"$client->cancelProxyCreationTag==$this->__cancelProxyCreationTag) {\n"+
"$preparedToSendBuffer[6]=\"3\";\n"+
"$client->sendBuffer.=$preparedToSendBuffer;\n"+
"$preparedToSendBuffer=null;\n"+
"$client->asyncCtx -=1;\n"+
"} else {\n"+
"if(!isset($this->__delegate)) {\n"+
"$client->unref($this->__java);\n"+
"}\n"+
"}\n"+
"}\n"+
"function __call($method,$args) {\n"+
"$client=$this->__client;\n"+
"$sig=\"@{$this->__signature}@$method\";\n"+
"$len=count($args);\n"+
"$args2=array($this->__java);\n"+
"for($i=0; $i<$len; $i++) {\n"+
"switch(gettype($val=$args[$i])) {\n"+
"case 'boolean': array_push($args2,$val); $sig.='@b'; break;\n"+
"case 'integer': array_push($args2,$val); $sig.='@i'; break;\n"+
"case 'double': array_push($args2,$val); $sig.='@d'; break;\n"+
"case 'string': array_push($args2,htmlspecialchars($val,ENT_COMPAT,\"ISO-8859-1\")); $sig.='@s'; break;\n"+
"case 'array':$sig=\"~INVALID\"; break;\n"+
"case 'object':\n"+
"if($val instanceof java_JavaType) {\n"+
"array_push($args2,$val->__java);\n"+
"$sig.=\"@o{$val->__signature}\";\n"+
"}\n"+
"else {\n"+
"$sig=\"~INVALID\";\n"+
"}\n"+
"break;\n"+
"case 'resource': array_push($args2,$val); $sig.='@r'; break;\n"+
"case 'NULL': array_push($args2,$val); $sig.='@N'; break;\n"+
"case 'unknown type': array_push($args2,$val); $sig.='@u'; break;\n"+
"default: throw new java_IllegalArgumentException($val);\n"+
"}\n"+
"}\n"+
"if(array_key_exists($sig,$client->methodCache)) {\n"+
"$cacheEntry=&$client->methodCache[$sig];\n"+
"$client->sendBuffer.=$client->preparedToSendBuffer;\n"+
"if(strlen($client->sendBuffer)>=JAVA_SEND_SIZE) {\n"+
"if($client->protocol->handler->write($client->sendBuffer)<=0)\n"+
"throw new java_IllegalStateException(\"Out of sync. Check backend log for details.\");\n"+
"$client->sendBuffer=null;\n"+
"}\n"+
"$client->preparedToSendBuffer=vsprintf($cacheEntry->fmt,$args2);\n"+
"if($cacheEntry->resultVoid) {\n"+
"$client->cancelProxyCreationTag +=1;\n"+
"return null;\n"+
"} else {\n"+
"$result=clone($client->cachedJavaPrototype);\n"+
"$result->__factory=$cacheEntry->factory;\n"+
"$result->__java=++$client->asyncCtx;\n"+
"$result->__signature=$cacheEntry->signature;\n"+
"$result->__cancelProxyCreationTag=++$client->cancelProxyCreationTag;\n"+
"return $result;\n"+
"}\n"+
"} else {\n"+
"$client->currentCacheKey=$sig;\n"+
"$retval=parent::__call($method,$args);\n"+
"return $retval;\n"+
"}\n"+
"}\n"+
"}\n"+
"class java_InternalJava extends Java {\n"+
"function __construct($proxy) {\n"+
"$this->__delegate=$proxy;\n"+
"$this->__java=$proxy->__java;\n"+
"$this->__signature=$proxy->__signature;\n"+
"$this->__client=$proxy->__client;\n"+
"}\n"+
"}\n"+
"class java_class extends Java {\n"+
"function __construct() {\n"+
"$this->__client=__javaproxy_Client_getClient();\n"+
"$args=func_get_args();\n"+
"$name=array_shift($args);\n"+
"if(is_array($name)) { $args=$name; $name=array_shift($args); }\n"+
"$delegate=$this->__delegate=$this->__client->referenceObject($name,$args);\n"+
"$this->__java=$delegate->__java;\n"+
"$this->__signature=$delegate->__signature;\n"+
"}\n"+
"}\n"+
"class JavaClass extends java_class{}\n"+
"class java_exception extends Exception implements java_JavaType {\n"+
"public $__serialID,$__java,$__client;\n"+
"public $__delegate;\n"+
"public $__signature;\n"+
"public $__hasDeclaredExceptions;\n"+
"function __construct() {\n"+
"$this->__client=__javaproxy_Client_getClient();\n"+
"$args=func_get_args();\n"+
"$name=array_shift($args);\n"+
"if(is_array($name)) { $args=$name; $name=array_shift($args); }\n"+
"if (count($args)==0)\n"+
"Exception::__construct($name);\n"+
"else\n"+
"Exception::__construct($args[0]);\n"+
"$delegate=$this->__delegate=$this->__client->createObject($name,$args);\n"+
"$this->__java=$delegate->__java;\n"+
"$this->__signature=$delegate->__signature;\n"+
"$this->__hasDeclaredExceptions='T';\n"+
"}\n"+
"function __cast($type) {\n"+
"return $this->__delegate->__cast($type);\n"+
"}\n"+
"function __sleep() {\n"+
"$this->__delegate->__sleep();\n"+
"return array(\"__delegate\");\n"+
"}\n"+
"function __wakeup() {\n"+
"$this->__delegate->__wakeup();\n"+
"$this->__java=$this->__delegate->__java;\n"+
"$this->__client=$this->__delegate->__client;\n"+
"}\n"+
"function __get($key) {\n"+
"return $this->__delegate->__get($key);\n"+
"}\n"+
"function __set($key,$val) {\n"+
"$this->__delegate->__set($key,$val);\n"+
"}\n"+
"function __call($method,$args) {\n"+
"return $this->__delegate->__call($method,$args);\n"+
"}\n"+
"function __toString() {\n"+
"return $this->__delegate->__toExceptionString($this->getTraceAsString());\n"+
"}\n"+
"}\n"+
"class JavaException extends java_exception {}\n"+
"class java_InternalException extends JavaException {\n"+
"function __construct($proxy,$exception) {\n"+
"$this->__delegate=$proxy;\n"+
"$this->__java=$proxy->__java;\n"+
"$this->__signature=$proxy->__signature;\n"+
"$this->__client=$proxy->__client;\n"+
"$this->__hasDeclaredExceptions=$exception;\n"+
"}\n"+
"}\n"+
"class java_JavaProxyProxy extends Java {\n"+
"function __construct($client) {\n"+
"$this->__client=$client;\n"+
"}\n"+
"}\n"+
"}\n"+
"?>\n"+
"";
    public static final byte[] bytes = data.getBytes(); 
}
