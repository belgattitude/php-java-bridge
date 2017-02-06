<?php /*-*- mode: php; tab-width:4 -*-*/

  /**
   * PHPDebugger.inc -- A PHP debugger for Eclipse for PHP Developers
   *
   * Copyright (C) 2009,2010 Jost Boekemeier.
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
   * Installation:
   *
   * Install "Eclipse for PHP Developers" version >= 3.5.2
   * 
   * - Open your project in PHP explorer and drag and drop this file to the PHP project folder.
   *
   * - Activate the PHPDebugger in your php.ini file. For example open Menu/Project/Properties, open "PHP Debug", select "Enable Project specific settings", select "Zend Debugger" if not already selected, click on "PHP executables ...", then "Edit" and select  a php.ini file with at least the following content:
   *
   *<code>
   * ;; activate the PHPDebugger in the php.ini
   * auto_prepend_file=PHPDebugger.inc
   *</code>
   *
   * - Debug your PHP scripts as usual. 
   *
   *
   * @category   java
   * @package    pdb
   * @author     Jost Boekemeier
   * @license    MIT
   * @version    1.0
   * @link       http://php-java-bridge.sf.net/phpdebugger
   * @see        PHPDebugger.php
   */


/** @access private */
define ("PDB_DEBUG", 0);
set_time_limit (0);

if (!class_exists("pdb_Parser")) {
  /**
   * The PHP parser
   * @access private
   */
  class pdb_Parser {
	const BLOCK = 1;
	const STATEMENT = 2;
	const EXPRESSION = 3;
	const FUNCTION_BLOCK = 4; // BLOCK w/ STEP() as last statement

	private $scriptName, $content;
	private $code;
	private $output;
	private $line, $currentLine;
	private $beginStatement, $inPhp, $inDQuote;
 
	/**
	 * Create a new PHP parser
	 * @param string the script name
	 * @param string the script content
	 * @access private
	 */
	public function pdb_Parser($scriptName, $content) {
	  $this->scriptName = $scriptName;
	  $this->content = $content;
	  $this->code = token_get_all($content);
	  $this->output = "";
	  $this->line = $this->currentLine = 0;
	  $this->beginStatement = $this->inPhp = $this->inDQuote = false;
	}

	private function toggleDQuote($chr) {
	  if ($chr == '"') $this->inDQuote = !$this->inDQuote;
	}

	private function each() {
	  $next = each ($this->code);
	  if ($next) {
		$cur = current($this->code);
		if (is_array($cur)) {
		  $this->currentLine = $cur[2] + ($cur[1][0] == "\n" ? substr_count($cur[1], "\n") : 0);
		  if ($this->isWhitespace($cur)) {
			$this->write($cur[1]);
			return $this->each();
		  }
		}
		else 
		  $this->toggleDQuote($cur);
	  }
	  return $next;
	}

	private function write($code) {
	  //echo "write:::".$code."\n";
	  $this->output.=$code;
	}

	private function writeInclude($once) {
	  $name = "";
	  while(1) {
		if (!$this->each()) die("parse error");
		$val = current($this->code);
		if (is_array($val)) {
		  $name.=$val[1];
		} else {
		  if ($val==';') break;
		  $name.=$val;
		}
	  }
	  if (PDB_DEBUG == 2) 
		$this->write("EVAL($name);");
	  else
		$this->write("eval('?>'.pdb_startInclude($name, $once)); pdb_endInclude();");
	}

	private function writeCall() {
	  while(1) {
		if (!$this->each()) die("parse error");
		$val = current($this->code);
		if (is_array($val)) {
		  $this->write($val[1]);
		} else {
		  $this->write($val);
		  if ($val=='{') break;
		}
	  }
	  $scriptName = addslashes($this->scriptName);
	  $this->write("\$__pdb_CurrentFrame=pdb_startCall(\"$scriptName\", {$this->currentLine});");
	}

	private function writeStep($pLevel) {
	  $token = current($this->code);
	  if ($this->inPhp && !$pLevel && !$this->inDQuote && $this->beginStatement && !$this->isWhitespace($token) && ($this->line != $this->currentLine)) {
		$line = $this->line = $this->currentLine;
		$scriptName = addslashes($this->scriptName);
		if (PDB_DEBUG == 2)
		  $this->write(";STEP($line);");
		else
		  $this->write(";pdb_step(\"$scriptName\", $line, pdb_getDefinedVars(get_defined_vars(), (isset(\$this) ? \$this : NULL)));");
	  }
	}

	private function writeNext() {
	  $this->next();
	  $token = current($this->code);
	  if (is_array($token)) $token = $token[1];
	  $this->write($token);
	}

	private function nextIs($chr) {
	  $i = 0;
	  while(each($this->code)) {
		$cur = current($this->code);
		$i++;
		if (is_array($cur)) {
		  switch ($cur[0]) {
		  case T_COMMENT:
		  case T_DOC_COMMENT:
		  case T_WHITESPACE:
			break;	/* skip */
		  default: 
			while($i--) prev($this->code);
			return false;	/* not found */
		  }
		} else {
		  while($i--) prev($this->code);
		  return $cur == $chr;	/* found */
		}
	  }
	  while($i--) prev($this->code);
	  return false;	/* not found */
	}

	private function nextTokenIs($ar) {
	  $i = 0;
	  while(each($this->code)) {
		$cur = current($this->code);
		$i++;
		if (is_array($cur)) {
		  switch ($cur[0]) {
		  case T_COMMENT:
		  case T_DOC_COMMENT:
		  case T_WHITESPACE:
			break;	/* skip */
		  default: 
			while($i--) prev($this->code);
			return (in_array($cur[0], $ar));
		  }
		} else {
		  break; /* not found */
		}
	  }
	  while($i--) prev($this->code);
	  return false;	/* not found */
	}

	private function isWhitespace($token) {
	  $isWhitespace = false;
	  switch($token[0]) {
	  case T_COMMENT:
	  case T_DOC_COMMENT:
	  case T_WHITESPACE:
		$isWhitespace = true;
		break;
	  }
	  return $isWhitespace;
	}
	private function next() {
	  if (!$this->each()) trigger_error("parse error", E_USER_ERROR);
	}

	private function parseBlock () {
	  $this->parse(self::BLOCK);
	}
	private function parseFunction () {
	  $this->parse(self::FUNCTION_BLOCK);
	}
	private function parseStatement () {
	  $this->parse(self::STATEMENT);
	}
	private function parseExpression () {
	  $this->parse(self::EXPRESSION);
	}

	private function parse ($type) {
	  pdb_Logger::debug("parse:::$type");

	  $this->beginStatement = true;
	  $pLevel = 0;

	  do {
		$token = current($this->code);
		if (!is_array($token)) {
		  pdb_Logger::debug(":::".$token);
		  if (!$pLevel && $type==self::FUNCTION_BLOCK && $token=='}') $this->writeStep($pLevel);
		  $this->write($token);
		  if ($this->inPhp && !$this->inDQuote) {
			$this->beginStatement = false; 
			switch($token) {
			case '(': 
			  $pLevel++;
			  break;
			case ')':
			  if (!--$pLevel && $type==self::EXPRESSION) return;
			  break;
			case '{': 
			  $this->next();
			  $this->parseBlock(); 
			  break;
			case '}': 
			  if (!$pLevel) return;
			  break;
			case ';':
			  if (!$pLevel) {
				if ($type==self::STATEMENT) return;
				$this->beginStatement = true; 
			  }
			  break;
			}
		  }
		} else {
		  pdb_Logger::debug(":::".$token[1].":(".token_name($token[0]).')');

		  if ($this->inDQuote) {
			$this->write($token[1]);
			continue;
		  }

		  switch($token[0]) {

		  case T_OPEN_TAG: 
		  case T_START_HEREDOC:
		  case T_OPEN_TAG_WITH_ECHO: 
			$this->beginStatement = $this->inPhp = true;
			$this->write($token[1]);
			break;

		  case T_END_HEREDOC:
		  case T_CLOSE_TAG: 
			$this->writeStep($pLevel);

			$this->write($token[1]);
			$this->beginStatement = $this->inPhp = false; 
			break;

		  case T_FUNCTION:
			$this->write($token[1]);
			$this->writeCall();
			$this->next();
			$this->parseFunction();
			$this->beginStatement = true;
			break;

		  case T_ELSE:
			$this->write($token[1]);
			if ($this->nextIs('{')) {
			  $this->writeNext();
			  $this->next();

			  $this->parseBlock();
			} else {
			  $this->next();

			  /* create an artificial block */
			  $this->write('{');
			  $this->beginStatement = true;
			  $this->writeStep($pLevel);
			  $this->parseStatement();
			  $this->write('}');

			}
			if ($type==self::STATEMENT) return;

			$this->beginStatement = true;
			break;

		  case T_DO:
			$this->writeStep($pLevel);
			$this->write($token[1]);
			if ($this->nextIs('{')) {
			  $this->writeNext();
			  $this->next();

			  $this->parseBlock();
			  $this->next();

			} else {
			  $this->next();

			  /* create an artificial block */
			  $this->write('{');
			  $this->beginStatement = true;
			  $this->writeStep($pLevel);
			  $this->parseStatement();
			  $this->next();
			  $this->write('}');
			}
			$token = current($this->code);
			$this->write($token[1]);

			if ($token[0]!=T_WHILE) trigger_error("parse error", E_USER_ERROR);
			$this->next();
			$this->parseExpression();

			if ($type==self::STATEMENT) return;

			$this->beginStatement = true;
			break;

		  case T_CATCH:
		  case T_IF:
		  case T_ELSEIF:
		  case T_FOR:
		  case T_FOREACH:
		  case T_WHILE:
			$this->writeStep($pLevel);

			$this->write($token[1]);
			$this->next();

			$this->parseExpression();

			if ($this->nextIs('{')) {
			  $this->writeNext();
			  $this->next();

			  $this->parseBlock();


			} else {
			  $this->next();
			  /* create an artificial block */
			  $this->write('{');
			  $this->beginStatement = true;
			  $this->writeStep($pLevel);
			  $this->parseStatement();
			  $this->write('}');
			}

			if ($this->nextTokenIs(array(T_ELSE, T_ELSEIF, T_CATCH))) {
			  $this->beginStatement = false;
			} else {
			  if ($type==self::STATEMENT) return;
			  $this->beginStatement = true;
			}
			break;

		  case T_REQUIRE_ONCE:
		  case T_INCLUDE_ONCE: 
		  case T_INCLUDE: 
		  case T_REQUIRE: 
			$this->writeStep($pLevel);
			$this->writeInclude((($token[0]==T_REQUIRE_ONCE) || ($token[0]==T_INCLUDE_ONCE)) ? 1 : 0);

			if ($type==self::STATEMENT) return;

			$this->beginStatement = true;
			break;

		  case T_CLASS:
			$this->write($token[1]);
			$this->writeNext();
			if ($this->nextIs('{')) {
			  $this->writeNext();
			  $this->next();
			  $this->parseBlock(); 
			  $this->beginStatement = true;
			} else {
			  $this->writeNext();
			  $this->beginStatement = false;
			}
			break;

		  case T_CASE:
		  case T_DEFAULT:
		  case T_PUBLIC:
		  case T_PRIVATE:
		  case T_PROTECTED:
		  case T_STATIC:
		  case T_CONST:
		  case T_GLOBAL:
		  case T_ABSTRACT:
			$this->write($token[1]);
			$this->beginStatement = false;
			break;

		  default:
			$this->writeStep($pLevel);
			$this->write($token[1]);
			$this->beginStatement = false;
			break;
	
		  }
		}
	  } while($this->each());
	}

	/**
	 * parse the given PHP script
	 * @return the parsed PHP script
	 * @access private
	 */
	public function parseScript() {
	  do {
		$this->parseBlock();
	  } while($this->each());

	  return $this->output;
	}
  }
}

/**
 * @access private
 */
class pdb_Logger {
  const FATAL = 1;
  const INFO = 2;
  const VERBOSE = 3;
  const DEBUG = 4;

  private static $logLevel = 0;
  private static $logFileName;

  private static function println($msg, $level) {
	if (!self::$logLevel) self::$logLevel=PDB_DEBUG?self::DEBUG:self::INFO;
	if ($level <= self::$logLevel) {
	  static $file = null;
	  if(!isset(self::$logFileName)) {
		self::$logFileName = $_SERVER['HOME'].DIRECTORY_SEPARATOR."pdb_PHPDebugger.inc.log";
	  }
	  if (!$file) $file = fopen(self::$logFileName, "ab") or die("fopen");
	  fwrite($file, time().": ");
	  fwrite($file, $msg."\n");
	  fflush($file);
	}
  }

  public static function logFatal($msg) {
	self::println($msg, self::FATAL);
  }
  public static function logInfo($msg) {
	self::println($msg, self::INFO);
  }
  public static function logMessage($msg) {
	self::println($msg, self::VERBOSE);
  }
  public static function logDebug($msg) {
	self::println($msg, self::DEBUG);
  }
  public static function debug($msg) {
	self::logDebug($msg);
  }
  public static function log($msg) {
	self::logMessage($msg);
  }
  public static function setLogLevel($level) {
	self::$logLevel=$level;
  }
  public static function setLogFileName($name) {
	self::$logFileName = $name;
  }
}

/**
 * @access private
 */
class pdb_Environment {
  public $filename, $stepNext;
  public $vars, $line, $firstLine;
  public $parent;

  public function pdb_Environment($parent, $filename, $stepNext, $firstLine) {
	$this->parent = $parent;
    $this->filename = $filename;
    $this->stepNext = $stepNext;
	$this->firstLine = $firstLine;
    $this->line = -1;
  }

  public function update ($line, &$vars) {
    $this->line = $line;
    $this->vars = &$vars;
  }
  public function __toString() {
	return "pdb_Environment: {$this->filename}, {$this->firstLine} - {$this->line}";
  }
}

/**
 * @access private
 */
abstract class pdb_Message {
  public $session;

  public abstract function getType();

  public function pdb_Message($session) {
    $this->session = $session;
  }

  public function serialize() {
    $this->session->out->writeShort($this->getType());
  }

  private static $messages = array();
  public static function register($message) {
    pdb_Message::$messages[$message->getType()] = $message;
  }
  public function getMessageById($id) {
    $message = pdb_Message::$messages[$id];
    return $message;
  }
  public function getMessage() {
    $id = $this->session->in->readShort();
    $message = $this->getMessageById($id);
    if (!$message) trigger_error("invalid message: $id", E_USER_ERROR);
    $message->deserialize();
    return $message;
  }

  protected function handleContinueProcessFile($message) {
	$code = $this->session->parseCode($this->currentFrame->filename, file_get_contents($this->currentFrame->filename));
	if (PDB_DEBUG) pdb_Logger::debug( "parse file:::" . $code ."\n");
	if (!PDB_DEBUG) ob_start();
	self::doEval ($code);
	$output = $this->getMessageById(pdb_OutputNotification::TYPE);
	if(!PDB_DEBUG) $output->setOutput(ob_get_contents());
	if(!PDB_DEBUG) ob_end_clean();
	$output->serialize();
	$this->status = 42; //FIXME
	$this->getMessageById(pdb_DebugScriptEndedNotification::TYPE)->serialize();
    return true;
  }
  private static function doEval($__pdb_Code) {
    return  eval ("?>".$__pdb_Code);
  }
  protected function handleStep($message) {
    return false;
  }
  protected function handleGo($message) {
    foreach ($this->session->allFrames as $frame) {
      $frame->stepNext = false;
    }
    return true; // exit
  }
  public function handleRequests () {
	$this->ignoreInterrupt = false;

    $this->serialize();
    while(1) {
      $message = $this->getMessage();
      switch ($message->getType()) {
      case pdb_SetProtocolRequest::TYPE:
		$message->ack();
		break;
      case pdb_StartRequest::TYPE:
		$message->ack();
		$this->getMessageById(pdb_StartProcessFileNotification::TYPE)->serialize();
		break;
      case pdb_ContinueProcessFileNotification::TYPE:
		if ($this->handleContinueProcessFile($message)) return pdb_ContinueProcessFileNotification::TYPE;
		break;
      case pdb_AddBreakpointRequest::TYPE:
		$message->ack();
		break;
      case pdb_RemoveBreakpointRequest::TYPE:
		$message->ack();
		break;
      case pdb_RemoveAllBreakpointsRequest::TYPE:
		$message->ack();
		break;
      case pdb_GetCallStackRequest::TYPE:
		$message->ack();
		break;
      case pdb_GetCWDRequest::TYPE:
		$message->ack();
		break;
      case pdb_GetVariableValueRequest::TYPE:
		$message->ack();
		break;
      case pdb_GoRequest::TYPE:
		$message->ack();
		if ($this->handleGo($message)) return pdb_GoRequest::TYPE;
		break;
      case pdb_StepOverRequest::TYPE:
		$message->ack();
		if ($this->handleStep($message)) return pdb_StepOverRequest::TYPE;
		break;
      case pdb_StepIntoRequest::TYPE:
		$message->ack();
		if ($this->handleStep($message)) return pdb_StepIntoRequest::TYPE;
		break;
      case pdb_StepOutRequest::TYPE:
		$message->ack();
		if ($this->handleStep($message)) return pdb_StepOutRequest::TYPE;
		break;
      case pdb_End::TYPE:
		$this->session->end();
      default: trigger_error("protocol error: $message", E_USER_ERROR);
      }
    }
  }
}
/**
 * @access private
 */
abstract class pdb_MessageRequest extends pdb_Message {
  public abstract function ack();
}

/**
 * @access private
 */
class pdb_Serializer {
  private $serial;
  private $depth;

  private function doSerialize ($o, $depth) {
    $serial = &$this->serial;

    switch(gettype($o)) {
    case 'object':
      $serial.="O:";
      $serial.=strlen(get_class($o));
      $serial.=":\"";
      $serial.=get_class($o);
      $serial.="\":";
      $serial.=count((array)$o);

	  if ($depth <= $this->depth) {
		$serial.=":{";
		foreach((array)$o as $k=>$v) {
		  $serial.=serialize($k);
		  $this->doSerialize($v, $depth+1);
		}
		$serial.="}";
	  } else {
		$serial .= ";";
	  }
      break;

    case 'array':
      $serial.="a:";
      $serial.=count($o);

	  if ($depth <= $this->depth) {
		$serial.=":{";
		foreach($o as $k=>$v) {
		  $serial.=serialize($k);
		  $this->doSerialize($v, $depth+1);
		}
		$serial.="}";
	  } else {
		$serial.=";";
	  }
      break;
    default:
      $serial.=serialize($o);
      break;
    }
  }

  public function serialize ($obj, $depth) {
    $this->serial = "";
	$this->depth = $depth;

    $this->doSerialize ($obj, 1);

    return $this->serial;
  }
}

/**
 * @access private
 */
class pdb_DebugSessionStart extends pdb_Message {
  const TYPE = 2005;

  public $status;
  public $end;

  private $breakFirstLine;
  private $enable;
  public $uri;
  public $query;
  public $options;
  
  public $in, $out;
  private $outputNotification;

  public $lines;
  public $breakpoints;

  public $currentTopLevelFrame, $currentFrame;
  public $allFrames; // should be a weak map so that frames could be gc'ed

  public $ignoreInterrupt; 

  public $serializer;

  public $includedScripts;

  public function getType() {
    return self::TYPE;
  }
  public function pdb_DebugSessionStart($options) {
    parent::pdb_Message($this);
	$this->end = true;
	if (isset($_SERVER["PATH_INFO"]) && isset($_SERVER["QUERY_STRING"])&&!extension_loaded("Zend Debugger")) {
	  $filename = $uri = $_SERVER["PATH_INFO"];
	  $queryStr = $_SERVER["QUERY_STRING"];
	} else {
	  $this->enable = false;
	  return;
	}

	$params = explode('&', $queryStr);
	$args = array();
	for ($i=0; $i<count($params); $i++) {
	  $arg=explode( '=', $params[$i]);
	  $args[$arg[0]] = $arg[1];
	}
	$this->enable = $args["start_debug"];
	$this->breakFirstLine = isset($args["debug_stop"]) ? $args["debug_stop"] : 0;
    $this->uri = $uri;
    $this->query = $queryStr;
    $this->options = $options;
    $this->breakpoints = $this->lines = array();

	$this->serializer = new pdb_Serializer();

	$this->currentTopLevelFrame = $this->currentFrame = new pdb_Environment(null, $filename, false, 1);
	$this->allFrames[] = $this->currentFrame;
	$this->ignoreInterrupt = false;
	$this->includedScripts = array();

    $errno = 0; $errstr = "";
    $io = fsockopen($args["debug_host"], $args['debug_port'], $errno, $errstr, 5) or trigger_error("fsockopen", E_USER_ERROR);
	$this->end = false;

    $this->in =new pdb_In($io, $this);
    $this->out=new pdb_Out($io, $this);
  }
  public function end() {
	$this->end = true;
	if (PDB_DEBUG) pdb_Logger::debug( "end() called");
	exit(0);
  }
  /**
   * @access private
   */
  public function flushOutput() {
	if (!isset($this->outputNotification))
	  $this->outputNotification = $this->getMessageById(pdb_OutputNotification::TYPE);

	$this->outputNotification->setOutput(ob_get_contents());
	if (!PDB_DEBUG) ob_clean();
	$this->outputNotification->serialize();
  }

  /**
   * @access private
   */
  public function resolveIncludePath($scriptName) {
	if (file_exists($scriptName)) return realpath($scriptName);
	$paths = explode(PATH_SEPARATOR, get_include_path());
	$name = $scriptName;
	foreach ($paths as $path) {
	  $scriptName = realpath("${path}${name}");
	  if ($scriptName) return $scriptName;
	}
	trigger_error("file $scriptName not found", E_USER_ERROR);
  }

  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt(2004102501);
    $out->writeString($this->currentFrame->filename);
    $out->writeString($this->uri);
    $out->writeString($this->query);
    $out->writeString($this->options);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function handleRequests () {
	if ($this->enable) { 
	  set_error_handler("pdb_error_handler");
	  register_shutdown_function("pdb_shutdown");
	  parent::handleRequests(); 
	  if (PDB_DEBUG) pdb_Logger::debug( "exit({$this->status})");
	  exit ($this->status); }
  }
  public function hasBreakpoint($scriptName, $line) {
	if ($this->breakFirstLine) {$this->breakFirstLine = false; return true;}

    if ($this->currentFrame->stepNext) return true;

    foreach ($this->breakpoints as $breakpoint) {
      if($breakpoint->type==1) {
		if ($breakpoint->file==$scriptName&&$breakpoint->line==$line) return true;
      }
    }

    return false;
  }
  function parseCode($filename, $contents) {
	$parser = new pdb_Parser($filename, $contents);
	return $parser->parseScript();
  }

  public function __toString() {
    return "pdb_DebugSessionStart: {$this->currentFrame->filename}";
  }
}


/**
 * @access private
 */
class pdb_HeaderOutputNotification extends pdb_Message {
  const TYPE = 2008;
  private $out;

  public function setOutput($out) {
    $this->out = $out;
  }
  protected function getAsciiOutput() {
    return $this->out;
  }
  protected function getEncodedOutput () {
    return $this->out; //FIXME
  }
  protected function getOutput() {
    return $this->getAsciiOutput();
  }
  public function getType() {
    return self::TYPE;
  }

  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeString($this->getOutput());
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_HeaderOutputNotification: ".$this->getOutput();
  }
}

/**
 * @access private
 */
class pdb_OutputNotification extends pdb_HeaderOutputNotification {
  const TYPE = 2004;

  public function getType() {
    return self::TYPE;
  }
  protected function getOutput() {
    return $this->getEncodedOutput();
  }
  public function __toString () {
    return "pdb_OutputNotification: ".$this->getAsciiOutput();
  }
}

/**
 * @access private
 */
class pdb_ErrorNotification extends pdb_Message {
  const TYPE = 2006;
  private $type, $filename, $lineno, $error;

  public function getType() {
    return self::TYPE;
  }
  public function setError($type, $filename, $lineno, $error) {
	$this->type = $type;
	$this->filename = $filename;
	$this->lineno = $lineno;
	$this->error = $error;
  }

  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->type);
	$out->writeString($this->filename);
	$out->writeInt($this->lineno);
	$out->writeString($this->error);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_ErrorNotification: {$this->error} at {$this->filename} line {$this->lineno}";
  }
}

/**
 * @access private
 */
class pdb_DebugScriptEndedNotification extends pdb_Message {
  const TYPE = 2002;

  public function getType() {
    return self::TYPE;
  }

  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeShort($this->session->status);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_DebugScriptEndedNotification: {$this->session->status}";
  }
}


/**
 * @access private
 */
class pdb_ReadyNotification extends pdb_Message {
  const TYPE = 2003;
  
  public function getType() {
    return self::TYPE;
  }

  protected function handleStep($message) {
    return true;
  }

  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeString($this->session->currentFrame->filename);
    $out->writeInt($this->session->currentFrame->line);
    $out->writeInt(0);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_ReadyNotification: {$this->session->currentFrame->filename}, {$this->session->currentFrame->line}";
  }
}

/**
 * @access private
 */
class pdb_SetProtocolRequest extends pdb_MessageRequest {
  const TYPE = 10000;
  public $id;
  public $protocolId;
  
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    $this->protocolId = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_SetProtocolResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_SetProtocolRequest: ". $this->protocolId;
  }
}

/**
 * @access private
 */
class pdb_SetProtocolResponse extends pdb_Message {
  const TYPE = 11000;
  private $req;
  
  public function pdb_SetProtocolResponse ($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);

    // use fixed id instead of $out->writeInt($this->req->protocolId);
	$out->writeInt(2006040705);

    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_SetProtocolResponse: ";
  }
}

/**
 * @access private
 */
class pdb_StartRequest extends pdb_MessageRequest {
  const TYPE = 1;
  public $id;
  public $protocolId;
  
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }

  public function ack() {
    $res = new pdb_StartResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_StartRequest: ";
  }
}

/**
 * @access private
 */
class pdb_StartResponse extends pdb_Message {
  const TYPE = 1001;
  private $req;
  
  public function pdb_StartResponse ($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    $out->writeInt(0);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_StartResponse: ";
  }
}
/**
 * @access private
 */
class pdb_StartProcessFileNotification extends pdb_Message {
  const TYPE = 2009;
  public function pdb_StartProcessFileNotification ($session) {
    parent::pdb_Message($session);
  }
  protected function handleContinueProcessFile($message) {
    return true; // next
  }
  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeString($this->session->currentFrame->filename);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_StartProcessFileNotification: {$this->session->currentFrame->filename}";
  }
}

/**
 * @access private
 */
class pdb_ContinueProcessFileNotification extends pdb_Message {
  const TYPE = 2010;
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_ContinueProcessFileNotification: ";
  }
}

/**
 * @access private
 */
class pdb_Breakpoint {
  public $type, $lifeTime, $file, $line, $condition;
  private $id;

  public function pdb_Breakpoint($type, $lifeTime, $file, $line, $condition, $id) {
    $this->type = $type;
    $this->lifeTime = $lifeTime;
    $this->file = $file;
    $this->line = $line;
    $this->condition = $condition;
    $this->id = $id;
  }
  public function __toString () {
    return "pdb_Breakpoint: ";
  }
}
/**
 * @access private
 */
class pdb_AddBreakpointResponse extends pdb_Message {
  const TYPE = 1021;
  private $req;
  private $id;

  private static function getId() {
    static $id = 0;
    return ++$id;
  }

  public function pdb_AddBreakpointResponse($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
    $this->id = self::getId();
    $this->session->breakpoints[$this->id] = new pdb_Breakpoint($req->type, $req->lifeTime, $req->file, $req->line, $req->condition, $this->id);
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    $out->writeInt(0);
    $out->writeInt($this->id);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_AddBreakpointResponse: {$this->id}";
  }
}

/**
 * @access private
 */
class pdb_RemoveBreakpointResponse extends pdb_Message {
  const TYPE = 1022;
  private $req;
  private $id;
  private $failure;

  public function pdb_RemoveBreakpointResponse($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;

	$this->remove();
  }

  protected function remove() {
	if (isset($this->session->breakpoints[$this->req->bpId])) {
	  unset($this->session->breakpoints[$this->req->bpId]);
	  $this->failure = 0;
	} else {
	  $this->failure = -1;
	}
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    $out->writeInt($this->failure);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_RemoveBreakpointResponse: {$this->id}";
  }
}
/**
 * @access private
 */
class pdb_RemoveAllBreakpointsResponse extends pdb_RemoveBreakpointResponse {
  const TYPE = 1023;
  public function pdb_RemoveAllBreakpointsResponse($req) {
    parent::pdb_RemoveBreakpointResponse($req);
  }

  protected function remove() {
	$keys = array_keys($this->session->breakpoints);
	foreach($keys as $key)
	  unset($this->session->breakpoints[$key]);
	
	$this->failure = 0;
  }

  public function getType() {
    return self::TYPE;
  }

  public function __toString () {
    return "pdb_RemoveAllBreakpoinstResponse: {$this->id}";
  }
}

/**
 * @access private
 */
class pdb_AddBreakpointRequest extends pdb_MessageRequest {
  const TYPE = 21;
  public $id;
  public $type;
  public $lifeTime;

  public $file;
  public $line;

  public $condition;

  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    $this->type = $in->readShort();
    $this->lifeType = $in->readShort();
    switch($this->type) {
    case 1: 
      $this->file = $in->readString();
      $this->line = $in->readInt();
      break;
    case 2:
      $this->condition = $in->readString();
      break;
    default: 
      trigger_error("invalid breakpoint", E_USER_ERROR);
    }
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_AddBreakpointResponse ($this);
    $res->serialize();
  }
  public function __toString () {
    if ($this->type == 1) 
      return "pdb_AddBreakpointRequest: {$this->file}, {$this->line}";
    else
      return "pdb_AddBreakpointRequest: {$this->condition}";
  }
}
/**
 * @access private
 */
class pdb_RemoveAllBreakpointsRequest extends pdb_MessageRequest {
  const TYPE = 23;
  public $id;

  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_RemoveAllBreakpointsResponse ($this);
    $res->serialize();
  }
  public function __toString () {
	return "pdb_RemoveAllBreakpointsRequest ";
  }
}
/**
 * @access private
 */
class pdb_RemoveBreakpointRequest extends pdb_RemoveAllBreakpointsRequest {
  const TYPE = 22;
  public $bpId;

  public function getType() {
    return self::TYPE;
  }

  public function deserialize() {
	parent::deserialize();
    $in = $this->session->in;
    $this->bpId = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_RemoveBreakpointResponse ($this);
    $res->serialize();
  }
  public function __toString () {
	return "pdb_RemoveBreakpointRequest: {$this->bpId}";
  }
}

/**
 * @access private
 */
class pdb_GetCallStackResponse extends pdb_Message {
  const TYPE = 1034;
  private $req;

  public function pdb_GetCallStackResponse($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
	for($frame=$this->session->currentFrame; $frame; $frame=$frame->parent)
	  $environments[] = $frame;

	$environments = array_reverse($environments);
    $n = count($environments);

    $out->writeInt($n);
    $out->writeInt(0);
    $out->writeInt(-1);
    $out->writeInt(0);

    for ($i=0; $i<$n; $i++) {
      if ($i>0) {
		$env = $environments[$i-1];
		$out->writeString($env->filename);
		$out->writeInt($env->line);
		$out->writeInt(0);
      }
	  $env = $environments[$i];
      $out->writeString($env->filename);
      $out->writeInt($env->firstLine);
      $out->writeInt(0);
	  
      $out->writeInt(0);
    }
	$out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_GetCallStackResponse: ";
  }
}
/**
 * @access private
 */
class pdb_GetCallStackRequest extends pdb_MessageRequest {
  const TYPE = 34;
  public $id;
	
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_GetCallStackResponse ($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_GetCallStackRequest: ";
  }
}


/**
 * @access private
 */
class pdb_GetCWDResponse extends pdb_Message {
  const TYPE = 1036;
  private $req;

  public function pdb_GetCWDResponse ($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    $out->writeInt(0);
    $out->writeString(getcwd());    
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_GetCWDResponse: ";
  }
}

/**
 * @access private
 */
class pdb_GetCWDRequest extends pdb_MessageRequest {
  const TYPE = 36;
  public $id;

  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_GetCWDResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_GetCWDRequest: ";
  }
}

/**
 * @access private
 */
class pdb_GetVariableValueResponse extends pdb_Message {
  const TYPE = 1032;
  private $req;

  public function pdb_GetVariableValueResponse ($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }

  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    if (PDB_DEBUG) pdb_Logger::debug( "evalcode:::".$this->req->code."\n");
	$error = 0;
    if ($this->req->code[0]=='$') {

	  $this->session->end = true;
	  $key = substr($this->req->code, 1);
	  if (isset($this->session->currentFrame->vars[$key])) {
		$var = $this->session->currentFrame->vars[$key];
		$paths = $this->req->paths;
		foreach ($paths as $path) {
		  if (is_object($var)) {
			$var = $var->$path;
		  } else {
			$var = $var[$path];
		  }
		}
	  } else {
		$var = "${key} not found!";
		$error = -1;
	  }
	  $ser = $this->session->serializer->serialize($var, $this->req->depth);
	  $this->session->end = false;

	  $out->writeInt($error);
      $out->writeString($ser);
	  if (PDB_DEBUG) pdb_Logger::debug("pdb_GetVariableValueResponse: ".print_r($var, true).": ${ser}, error: ${error}");
    } else {
	  if (PDB_DEBUG) pdb_Logger::debug(print_r($this->session->currentFrame->vars, true));

	  $this->session->end = true;
	  $vars = $this->session->currentFrame->vars;
	  $ser = $this->session->serializer->serialize($vars, $this->req->depth);
	  $this->session->end = false;

	  $out->writeInt($error);
	  $out->writeString($ser);
	  if (PDB_DEBUG) pdb_Logger::debug("pdb_GetVariableValueResponse: ".print_r($vars, true).": ${ser}, error: ${error}");
	}
    $out->flush();
  }
  public function __toString () {
    return "pdb_GetVariableValueResponse: ";
  }
}

/**
 * @access private
 */
class pdb_GetVariableValueRequest extends pdb_MessageRequest {
  const TYPE = 32;
  public $id;
  public $code;
  public $depth;
  public $paths;

  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    $this->code = $in->readString();
    $this->depth = $in->readInt();

    $this->paths = array();
    $length = $in->readInt();
    while($length--) {
      $this->paths[] = $in->readString();
    }
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_GetVariableValueResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_GetVariableValueRequest: {$this->code}, {$this->depth}, paths::".print_r($this->paths, true);
  }
}

/**
 * @access private
 */
class pdb_StepOverResponse extends pdb_Message {
  const TYPE = 1012;
  private $req;

  public function pdb_StepOverResponse($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->req->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    $out->writeInt(0);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_StepOverResponse: ";
  }
}

/**
 * @access private
 */
class pdb_StepOverRequest extends pdb_MessageRequest {
  const TYPE = 12;
  public $id;
  
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_StepOverResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_StepOverRequest: ";
  }
}

/**
 * @access private
 */
class pdb_StepIntoResponse extends pdb_StepOverResponse {
  const TYPE = 1011;
  public function getType() {
    return self::TYPE;
  }
  public function __toString () {
    return "pdb_StepIntoResponse: ";
  }
}

/**
 * @access private
 */
class pdb_StepIntoRequest extends pdb_StepOverRequest {
  const TYPE = 11;
  public function getType() {
    return self::TYPE;
  }
  public function ack() {
    $res = new pdb_StepIntoResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_StepIntoRequest: ";
  }
}

/**
 * @access private
 */
class pdb_StepOutResponse extends pdb_StepOverResponse {
  const TYPE = 1013;
  public function getType() {
    return self::TYPE;
  }
  public function __toString () {
    return "pdb_StepOutResponse: ";
  }
}

/**
 * @access private
 */
class pdb_StepOutRequest extends pdb_StepOverRequest {
  const TYPE = 13;
  public function getType() {
    return self::TYPE;
  }
  public function ack() {
    $res = new pdb_StepOutResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_OutIntoRequest: ";
  }
}

/**
 * @access private
 */
class pdb_GoResponse extends pdb_Message {
  const TYPE = 1014;
  private $req;

  public function pdb_GoResponse ($req) {
    parent::pdb_Message($req->session);
    $this->req = $req;
  }

  public function getType() {
    return self::TYPE;
  }
  public function serialize() {
    $out = $this->session->out;
    parent::serialize();
    $out->writeInt($this->req->id);
    $out->writeInt(0);
    $out->flush();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_GoResponse: ";
  }
}

/**
 * @access private
 */
class pdb_GoRequest extends pdb_MessageRequest {
  const TYPE = 14;
  public $id;
 
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    $in = $this->session->in;
    $this->id = $in->readInt();
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function ack() {
    $res = new pdb_GoResponse($this);
    $res->serialize();
  }
  public function __toString () {
    return "pdb_GoRequest: ";
  }
}
/**
 * @access private
 */
class pdb_End extends pdb_Message {
  const TYPE = 3;
  public $id;
 
  public function getType() {
    return self::TYPE;
  }
  public function deserialize() {
    if (PDB_DEBUG) pdb_Logger::debug( "$this");
  }
  public function __toString () {
    return "pdb_End: ";
  }
}

/**
 * @access private
 */
class pdb_In {
  private $in;
  private $len;
  private $session;

  public function pdb_In($in, $session) {
    $this->in = $in;
    $this->len = 0;
	$this->session = $session;
  }
  private function readBytes($n) {
    $str = "";
    while ($n) {
      $s = fread($this->in, $n);
	  if (feof($this->in)) $this->session->end();

      $n -= strlen($s);

      $str.=$s;
    }
    return $str;
  }
  public function read() {
    if(!$this->len) {
      $str = $this->readBytes(4);
      $lenDesc = unpack("N", $str);
      $this->len = array_pop($lenDesc);
    }
  }
  public function readShort() {
    $this->read();

    $this->len-=2;
    $str = $this->readBytes(2);
    $lenDesc = unpack("n", $str);
    return array_pop($lenDesc);
  }
  public function readInt() {
    $this->read();

    $this->len-=4;
    $str = $this->readBytes(4);
    $lenDesc = unpack("N", $str);
    return array_pop($lenDesc);
  }
  public function readString() {
    $this->read();

    $length = $this->readInt();
    $this->len-=$length;
    return $this->readBytes($length);
  }
  public function __toString () {
    return "pdb_In: ";
  }
}
/**
 * @access private
 */
class pdb_Out {
  private $out;
  private $buf;
  private $session;
  
  public function pdb_Out($out, $session) {
    $this->out = $out;
    $this->buf = "";
	$this->session = $session;
  }

  public function writeShort($val) {
    $this->buf.=pack("n", $val);
  }
  public function writeInt($val) {
    $this->buf.=pack("N", $val);
  }
  public function writeString($str) {
    $length = strlen($str);
    $this->writeInt($length);
    $this->buf.=$str;
  }
  public function writeUTFString($str) {
    $this->writeString(urlencode($str));
  }
  public function flush() {
    $length = strlen($this->buf);
    $this->buf = pack("N", $length).$this->buf;
    fwrite($this->out, $this->buf);
	if (feof($this->out)) $this->session->end();
    $this->buf = "";
  }
  public function __toString () {
    return "pdb_Out: ";
  }
}
$pdb_dbg = new pdb_DebugSessionStart("&debug_fastfile=1");
pdb_Message::register(new pdb_SetProtocolRequest($pdb_dbg));
pdb_Message::register(new pdb_StartRequest($pdb_dbg));
pdb_Message::register(new pdb_ContinueProcessFileNotification($pdb_dbg));
pdb_Message::register(new pdb_AddBreakpointRequest($pdb_dbg));
pdb_Message::register(new pdb_RemoveBreakpointRequest($pdb_dbg));
pdb_Message::register(new pdb_RemoveAllBreakpointsRequest($pdb_dbg));
pdb_Message::register(new pdb_GetCallStackRequest($pdb_dbg));
pdb_Message::register(new pdb_GetCWDRequest($pdb_dbg));
pdb_Message::register(new pdb_GetVariableValueRequest($pdb_dbg));
pdb_Message::register(new pdb_StepOverRequest($pdb_dbg));
pdb_Message::register(new pdb_StepIntoRequest($pdb_dbg));
pdb_Message::register(new pdb_StepOutRequest($pdb_dbg));
pdb_Message::register(new pdb_GoRequest($pdb_dbg));
pdb_Message::register(new pdb_End($pdb_dbg));

pdb_Message::register(new pdb_StartProcessFileNotification($pdb_dbg));
pdb_Message::register(new pdb_ReadyNotification($pdb_dbg));
pdb_Message::register(new pdb_DebugScriptEndedNotification($pdb_dbg));
pdb_Message::register(new pdb_HeaderOutputNotification($pdb_dbg));
pdb_Message::register(new pdb_OutputNotification($pdb_dbg));
pdb_Message::register(new pdb_ErrorNotification($pdb_dbg));

/**
 * @access private
 */
function pdb_getDefinedVars($vars1, $vars2) {
  //if(isset($vars2)) $vars1['pbd_This'] = $vars2;

  unset($vars1['__pdb_Code']);	     // see pdb_Message::doEval()

  return $vars1;   
}
/**
 * @access private
 */
function pdb_startCall($scriptName, $line) {
  global $pdb_dbg;

  $stepNext = $pdb_dbg->currentFrame->stepNext == pdb_StepIntoRequest::TYPE ? pdb_StepIntoRequest::TYPE : false;

  pdb_Logger::debug("startCall::$scriptName, $stepNext");

  $env = new pdb_Environment($pdb_dbg->currentFrame, $scriptName, $stepNext, $line);
  $pdb_dbg->allFrames[] = $env;

  return $env;
}

/**
 * @access private
 */
function pdb_startInclude($scriptName, $once) {
  global $pdb_dbg;

  $scriptName = $pdb_dbg->resolveIncludePath($scriptName);

  // include only from a top-level environment
  // initial line# and vars may be wrong due to a side-effect in step
  $pdb_dbg->session->currentFrame = $pdb_dbg->session->currentTopLevelFrame;

  $stepNext = $pdb_dbg->currentFrame->stepNext == pdb_StepIntoRequest::TYPE ? pdb_StepIntoRequest::TYPE : false;
  $pdb_dbg->currentFrame = new pdb_Environment($pdb_dbg->currentFrame, $scriptName, $stepNext, 1);
  $pdb_dbg->allFrames[] = $pdb_dbg->currentFrame;

  /* BEGIN: StartProcessFileNotification */
  $pdb_dbg->getMessageById(pdb_StartProcessFileNotification::TYPE)->handleRequests();
  /* ...  set breakpoints ... */
  /* END: ContinueProcessFileNotification */

  if ($once && isset($pdb_dbg->includedScripts[$scriptName]))
	$code = "<?php ?>";
  else
	$code = $pdb_dbg->parseCode(realpath($scriptName), file_get_contents($scriptName));

  $pdb_dbg->currentTopLevelFrame = $pdb_dbg->currentFrame;

  if (PDB_DEBUG) pdb_Logger::debug("include:::$code");

  if ($once) $pdb_dbg->includedScripts[$scriptName] = true;
  return $code; // eval -> pdb_step/MSG_READY or pdb_endInclude/MSG_READY OR FINISH
}
/**
 * @access private
 */
function pdb_endInclude() {
  global $pdb_dbg;

  $pdb_dbg->currentFrame = $pdb_dbg->currentTopLevelFrame = $pdb_dbg->currentTopLevelFrame->parent;
}


/**
 * @access private
 */
function pdb_step($filename, $line, $vars) {
  global $pdb_dbg;
  if ($pdb_dbg->ignoreInterrupt) return;

  $pdb_dbg->ignoreInterrupt = true;

  // pull the current frame from the stack or the top-level environment
  $pdb_dbg->currentFrame = (isset($vars['__pdb_CurrentFrame'])) ? $vars['__pdb_CurrentFrame'] : $pdb_dbg->currentTopLevelFrame;
  unset($vars['__pdb_CurrentFrame']);

  $pdb_dbg->currentFrame->update($line, $vars);

  if ($pdb_dbg->hasBreakpoint($filename, $line)) {
	$pdb_dbg->flushOutput();
	$stepNext = $pdb_dbg->getMessageById(pdb_ReadyNotification::TYPE)->handleRequests();
	pdb_Logger::logDebug("continue");
	/* clear all dynamic breakpoints */
	foreach ($pdb_dbg->allFrames as $currentFrame)
	  $currentFrame->stepNext = false;

	/* set new dynamic breakpoint */
	if ($stepNext != pdb_GoRequest::TYPE) {
	  $currentFrame = $pdb_dbg->currentFrame;

	  /* break in current frame or frame below */
	  if ($stepNext != pdb_StepOutRequest::TYPE)
		$currentFrame->stepNext = $stepNext;

	  /* or break in any parent */
	  while ($currentFrame = $currentFrame->parent) {
		$currentFrame->stepNext = $stepNext;
	  }
	}
  }
  $pdb_dbg->ignoreInterrupt = false;
}

/**
 * @access private
 */
function pdb_error_handler($errno, $errstr, $errfile, $errline) {
  global $pdb_dbg;
  if (PDB_DEBUG) pdb_Logger::debug("PHP error $errno: $errstr in $errfile line $errline");
  if ($pdb_dbg->end) return false;

  $msg = $pdb_dbg->getMessageById(pdb_ErrorNotification::TYPE);
  $msg->setError($errno, $errfile, $errline, $errstr);
  $msg->serialize();
  return true;
}

/**
 * @access private
 */
function pdb_shutdown() {
  global $pdb_dbg;
  if (PDB_DEBUG) pdb_Logger::debug("PHP error: ".print_r(error_get_last(), true));
  if ($pdb_dbg->end) return;

  $error = error_get_last();
  if ($error) {
	$msg = $pdb_dbg->getMessageById(pdb_ErrorNotification::TYPE);
	$msg->setError($error['type'], $error['file'], $error['line'], $error['message']);
	$msg->serialize();
  }
}

$pdb_dbg->handleRequests();

?>
