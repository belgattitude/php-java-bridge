<?php /*-*- mode: php; tab-width:4 -*-*/

  /**
   *  PHPDebugger.php -- The PHP debugger (JavaScript GUI)
   * 
   * Copyright (C) 2009 Jost Boekemeier
   * 
   * The PHPDebugger ("the library") is free software; you can
   * redistribute it and/or modify it under the terms of the GNU General
   * Public License as published by the Free Software Foundation; either
   * version 2, or (at your option) any later version.
   * 
   * The library is distributed in the hope that it will be useful, but
   * WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * General Public License for more details.
   * 
   * You should have received a copy of the GNU General Public License
   * along with the PHPDebugger; see the file COPYING. If not, write to the
   * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
   * 02111-1307 USA.
   * 
   * Linking this file statically or dynamically with other modules is
   * making a combined work based on this library. Thus, the terms and
   * conditions of the GNU General Public License cover the whole
   * combination.
   * 
   * As a special exception, the copyright holders of this library give you
   * permission to link this library with independent modules to produce an
   * executable, regardless of the license terms of these independent
   * modules, and to copy and distribute the resulting executable under
   * terms of your choice, provided that you also meet, for each linked
   * independent module, the terms and conditions of the license of that
   * module. An independent module is a module which is not derived from
   * or based on this library. If you modify this library, you may extend
   * this exception to your version of the library, but you are not
   * obligated to do so. If you do not wish to do so, delete this
   * exception statement from your version. 
   *
   * Usage:
   * 
   * - include() this file at the beginning of your script
   *
   * - browse to your script using firefox 
   *
   * - set breakpoints using the JavaScript GUI, click on any variable or included file to visit that variable or file
   * 
   * - click on the stop button to terminate the debug session
   *
   * @category   java
   * @package    pdb
   * @author     Jost Boekemeier
   * @license    GPL
   * @version    1.0
   * @link       http://php-java-bridge.sf.net/phpdebugger
   * @see        PHPDebugger.inc
   */

/** @access private */
define ("PDB_DEBUG", 0);
set_time_limit (0);

$pdb_version = phpversion();
if ((version_compare("5.3.0", $pdb_version, ">")))
  trigger_error("<br><strong>PHP ${pdb_version} too old.</strong><br>\nPlease set the path to a PHP >= 5.3 executable, see php_exec in the WEB-INF/web.xml", E_USER_ERROR);

 
/**
 * a simple logger
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
		self::$logFileName = $_SERVER['DOCUMENT_ROOT'].DIRECTORY_SEPARATOR."pdb_PHPDebugger.log";
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
interface pdb_Queue {

  /** 
   * Read a string from the queue
   * @return string a json encoded string of values
   * @access private
   */
  public function read();

  /**
   * Write a string to the queue
   * @param string a json encoded string of values or TRUE
   * @access private
   */
  public function write($val);

  /**
   * Set the script output before server shutdown
   * @access private
   */
  public function setOutput($output);

  /**
   * Get the script output
   * @access private
   */
  public function getOutput();

  /**
   * Mark the channel as dead. If marked, read will return boolean
   * TRUE, write will do nothing.
   * @access private
   */
  public function shutdown();
}

/**
 * This class represents the debugger back end connection. It
 * communicates with the debugger front end using a shared-memory queue.
 * It is slow, but it does not require any special library.
 * @access private
 */
class pdb_PollingServerConnection implements pdb_Queue {
  protected $id;
  protected $role, $to;
  protected $chanTrm; // "back end terminated" flag
  protected $output;
  const TIMER_DURATION = 200000; // every 200ms

  /**
   * Create a new communication using a unique id
   * @access private
   */
  public function pdb_PollingServerConnection($id) {
	$this->id = $id;
	$this->chanTrm = "pdb_trmserver{$this->id}";
	$this->output = "<missing>";

	$this->prepareCookies();
	$this->init();
  }

  protected function checkTrm() {
	return false!==$_SESSION[$this->chanTrm];
  }

  protected function prepareCookies() {
	ini_set("session.use_cookies", true);
	session_start();
	session_write_close();

	/* avoid PHP bug, which repeats set-cookie header for each
	   iteration of session_start/session_write_close */
	ini_set("session.use_cookies", false);
  }

  protected function init() {
	session_start();

	$this->role = "client";
	$this->to  = "server";

	$chanCtr = "pdb_ctr{$this->role}{$this->id}";
	$chan = "pdb_{$this->role}{$this->id}";
	unset ($_SESSION[$chan]);
	unset ($_SESSION[$chanCtr]);

	$this->role = "server";
	$this->to  = "client";

	$chanCtr = "pdb_ctr{$this->role}{$this->id}";
	$chan = "pdb_{$this->role}{$this->id}";
	unset ($_SESSION[$chan]);
	unset ($_SESSION[$chanCtr]);

	if (isset($_SESSION[$this->chanTrm]) && !$this->checkTrm()) {
	  $_SESSION[$this->chanTrm] = true;
	  session_write_close();
	  sleep(1);
	  session_start();
	}
	$_SESSION[$this->chanTrm] = false;
	session_write_close();
  }

  protected function poll() {
	$val = "";
	$chanCtr = "pdb_ctr{$this->role}{$this->id}";
	$chan = "pdb_{$this->role}{$this->id}";
	session_start();
	if (!($val = $this->checkTrm())) {
	  if(!isset($_SESSION[$chanCtr])) { 
		$_SESSION[$chan] = array(); 
		$_SESSION[$chanCtr]=0; 
	  }
	  $seq = $_SESSION[$chanCtr];
	  $seqNext = count($_SESSION[$chan]);
	  if (PDB_DEBUG) pdb_Logger::debug("...{$this->role}, {$this->id} poll next # ${seqNext} (${seq}) ...");
	  if ($seqNext > $seq) {
		$val = json_decode($_SESSION[$chan][$seq]);
		$_SESSION[$chan][$seq]=null;
		$_SESSION[$chanCtr]++;
		if (PDB_DEBUG) pdb_Logger::debug("...{$this->role}, {$this->id} polled next # ${seqNext} (${seq}), got: {$val->seq}");
	  }
	}
	session_write_close();
	return $val;
  }
  protected function send($val) {
	
	$seq = $val->seq;
	$chan = "pdb_{$this->to}{$this->id}";
	session_start();
	if (!$this->checkTrm()) $_SESSION[$chan][$seq]=json_encode($val);
	if (PDB_DEBUG) pdb_Logger::debug("...{$this->role}, {$this->id} send: ${seq} ...");
	session_write_close();
  }

  /**
   * read a new value from the read queue
   * @access private
   */
  public function read() {
	$val = null;
	$cntr = 0;
	while(!($val=$this->poll())) {
	  if ($cntr<=20) {
		$cntr++;
		usleep(self::TIMER_DURATION);
	  } else {
		usleep(self::TIMER_DURATION*5);
	  }
	}
	return $val === true ? null : $val;
  }

  /**
   * write a new value to the write queue
   * @access private
   */
  public function write($val) {
	$this->send((object)$val);
  }

  /**
   * Set the script output
   * @access private
   */
  public function setOutput($output) {
	$this->output = $output;
  }
  /**
   * Get the script output
   * @access private
   */
  public function getOutput() {
	return $_SESSION[$this->chanTrm];
  }

  /**
   * shut down the communication channel
   */
  public function shutdown() {
	if (PDB_DEBUG) pdb_Logger::debug("session terminated: {$this->chanTrm}");
	session_start();
	$_SESSION[$this->chanTrm] = $this->output;
	session_write_close();
  }
}  

/**
 * This class represents the debugger front end connection. It
 * communicates with the debugger back end using a shared-memory queue.
 * It is slow, but it does not require any special library.
 * @access private
 */
class pdb_PollingClientConnection extends pdb_PollingServerConnection {
  private $seq;

  protected function init() {
	$this->role = "client";
	$this->to  = "server";
  }

  protected function poll() {
	$chan = "pdb_{$this->role}{$this->id}";
	session_start();
	if (!($val = $this->checkTrm())) {
	  if (PDB_DEBUG) pdb_Logger::debug("...{$this->role}, {$this->id} poll for {$this->seq} ...");
	  if (isset($_SESSION[$chan][$this->seq])) {
		$val = json_decode($_SESSION[$chan][$this->seq]);
		if (PDB_DEBUG) pdb_Logger::debug("...{$this->role}, {$this->id} polled for {$this->seq}, got: {$val->seq}");
		unset($_SESSION[$chan][$this->seq]);
	  }
	}
	session_write_close();
	return $val;
  }

  /**
   * write a new value to the write queue
   * @access private
   */
  public function write($val) {
	$this->seq = $val->seq;

	parent::write($val);
  }

  /**
   * shut down the communication channel
   * @access private
   */
  public function shutdown() {}
}

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
	  if (PDB_DEBUG) pdb_Logger::debug("parse:::$type");

	  $this->beginStatement = true;
	  $pLevel = 0;

	  do {
		$token = current($this->code);
		if (!is_array($token)) {
		  if (PDB_DEBUG) pdb_Logger::debug(":::".$token);
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
		  if (PDB_DEBUG) pdb_Logger::debug(":::".$token[1].":(".token_name($token[0]).')');

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
 * This structure represents the debugger front-end. It is used by the
 * JavaScript code to communicate with the debugger back end.
 * @access private
 */
class pdb_JSDebuggerClient {
  private static function getDebuggerFilename() {
	$script = __FILE__;
	$scriptName = basename($script);
	return realpath($scriptName == "PHPDebugger.php" ? $script :"java/PHPDebugger.php");
  }
	
  private static function getCurrentRootDir() {
	$scriptName = $_SERVER['SCRIPT_NAME'];
	$scriptFilename = $_SERVER['SCRIPT_FILENAME'];
 
	$scriptDirName = dirname($scriptName);
	$scriptDir   = dirname($scriptFilename);
 
	if ((strlen($scriptDirName)>1) && ($scriptDirName[1]=='~')) {
	  $scriptDirName = ltrim($scriptDirName, "/");
	  $idx = strpos($scriptDirName, '/');
	  $scriptDirName = $idx===false ? '' : substr($scriptDirName, $idx);
	} elseif ((strlen($scriptDirName)==1) && (($scriptDirName[0]=='/') || ($scriptDirName[0]=='\\'))) {
	  $scriptDirName = '';
	}
	if (PDB_DEBUG) pdb_Logger::debug("scriptDir: $scriptDir, scriptDirName: $scriptDirName");

	if ((strlen($scriptDir) < strlen($scriptDirName)) || ($scriptDirName &&
														  (substr($scriptDir, -strlen($scriptDirName)) != $scriptDirName)))
	  return null;
	else
	  return substr($scriptDir, 0, strlen($scriptDir)-strlen($scriptDirName));
  }
  /*
   * Return the script name
   * Example: %2Fopt%2Fappserv%2Fapache-tomcat-6.0.14%2Fwebapps%2FJavaBridge%2Ftest.php"
   *
   * @return An urlencoded file name.
   */
  public static function getDebugScriptName() {
	return urlencode($_SERVER['SCRIPT_FILENAME']);
  }
  /**
   * Return the debugger URL. 
   * Example: "/JavaBridge/java/PHPDebugger.php?source=settings.php"
   *
   * @return The debugger URL.
   * @access private
   */
  public static function getDebuggerURL() {
	$path = self::getDebuggerFilename();
	if (!$path) 
	  trigger_error("java/PHPDebugger.php not found in document root", E_USER_ERROR);

	$root = self::getCurrentRootDir();

	$scriptName = $_SERVER['SCRIPT_NAME'];

	$scriptDirName = dirname($scriptName);
	$prefix = '';
	if ((strlen($scriptDirName)>1) && ($scriptDirName[1]=='~')) {
	  $scriptDirName = ltrim($scriptDirName, "/");
	  $idx = strpos($scriptDirName, '/');
	  $prefix = '/' . ($idx ? substr($scriptDirName, 0, $idx): $scriptDirName);
	}
  
	if (PDB_DEBUG) pdb_Logger::debug("serverRoot: $root - path: $path");
	if ($root && (strlen($root) < strlen($path)) && (!strncmp($path, $root, strlen($root))))
	  $path = "${prefix}" . str_replace('\\', '/', substr($path, strlen($root)));
	else // could not calculate debugger path
	  $path = dirname($_SERVER['SCRIPT_NAME']) . "/java/PHPDebugger.php";

	$pathInfo = isset($_SERVER['PATH_INFO']) ? $_SERVER['PATH_INFO'] : "";
	$query = isset($_SERVER['QUERY_STRING']) ? $_SERVER['QUERY_STRING'] : "";

	$url = "${path}${pathInfo}";
	if ($query) $url .= "?${query}";
	return $url;
  }

  public static function getPostData() {
	$str = '';
	foreach ($_POST as $key => $value) {
	  if ($str) $str .= '&';
	  $str .= $key . '=' . urlencode($value);
	}
	return $str;
  }

  /**
   * Get the server's uniqe session ID
   * @return a uniqe session ID
   * @access private
   */ 
  public static function getServerID() {
	// TODO: allow more than one debug session
	return 1;
  }
 
  private static function getConnection($id) {
	return new pdb_PollingClientConnection($id);
  }
  private static function stripslashes($value) {
	 $value = is_array($value) ?
	   array_map("self::stripslashes", $value) :
	   stripslashes($value);
	 
	 return $value;
   }

  /**
   * Pass the command and arguments to the debug back end and
   * output the response to JavaScript.
   *
   * @arg array The command arguments
   * @access private
   */
  public static function handleRequest($vars) {
		if (get_magic_quotes_gpc()) $vars = self::stripslashes($vars);
	$msg = (object)$vars;
	
	if ($msg->cmd == "begin") sleep(1); // wait for the server to settle
	if (PDB_DEBUG) pdb_Logger::debug("beginHandleRequest: ".$msg->cmd);
	$conn = self::getConnection($msg->serverID);
	$conn->write($msg);

	if (!($response = $conn->read())) 
	  $output = json_encode(array("cmd"=>"term", "output"=>$conn->getOutput()));
	else
	  $output = json_encode($response);
	
	echo "($output)";
	if (PDB_DEBUG) pdb_Logger::debug("endHandleRequest");
  }
}

/**
 * The current view frame. Contains the current script name. May be
 * selected by clicking on a include() hyperlink Allows users to set
 * breakpoints for this file.
 *
 * View will be discarded when go, step, end is invoked.
 * @access private
 */
class pdb_View {
  /** The current script name */
  public $scriptName;
  /** Back-link to the parent or null */
  public $parent;

  protected $bpCounter, $lineCounter, $code;
  /**
   * Create a new view frame
   * @param object the parent frame
   * @param string the script name
   */
  public function pdb_View($parent, $scriptName) {
	$this->parent = $parent;
	$this->scriptName = $scriptName;

	$this->bpCounter = $this->lineCounter = 1;
	$this->code = null;
  }

  private function lineCB ($val) {
	return $val.(string)$this->lineCounter++;
  }
  private function breakpointCB ($val) {
	return 	$val.'id="bp_'.(string)$this->bpCounter++.'"';
  }
  function replaceCallback($code, $split, $cb) {
	$ar = explode($split, $code);
	$last = array_pop($ar);
	$ar = array_map($cb, $ar);
	array_push($ar, $last);
	return implode($ar);
  }


  /**
   * Return a HTML representation of the current script
   * @return string the clickable HTML representation of the current script
   */
  public function getHtmlScriptSource() {
	if (!$this->code) {
	  $c=
		'<span class="breakpoint" id="bp_" onmousedown="return toggleBreakpoint(this, event);">'.
		'<span class="currentlineIndicator normal"></span>'.
		'<span class="linenumber">line#</span>'.
		'<span class="breakpointIndicator normal"></span>'.
		'</span><br />';

	  $code=show_source($this->scriptName, true);

	  // br => span id=pb_ ...
	  $code = str_replace('<br />', $c, $code);
	  // handle incomplete last line, identical to preg: '|(?<!<br >)</span>\n</span>\n</code>$|'
	  $code = ereg_replace('"(([^>])|([^/]>)|([^ ]/>)|([^r] />)|([^b]r />)|([^<]br />))(</span>\n</span>\n</code>)"', '\1 $c \8', $code);

	  $code = $this->replaceCallback($code, 'id="bp_"', array($this, "breakpointCB"));
	  $code = $this->replaceCallback($code, 'line#', array($this, "lineCB"));

	  $this->code = $code;
	}

	return $this->code;
  }
}
/**
 * The current view. Used to show the contents of a variable
 * @access private
 */
class pdb_VariableView extends pdb_View {
  /**
   * Create a new variable view
   * @param object the parent frame
   * @param string the variable name
   * @param string the variable value
   */
  public function pdb_VariableView($parent, $name, $value) {
	parent::pdb_View($parent, $name);
	$this->value = $value;
  }
  /**
   * {@inheritDoc}
   */
  public function getHtmlScriptSource() {
	return (highlight_string(print_r($this->value, true), true));
  }
}
/**
 * The current execution frame. Contains the current run-time script
 * name along with its state
 * @access private
 */
class pdb_Environment extends pdb_View {
  /** bool true if a dynamic breakpoint should be inserted at the next line, false otherwise */
  public $stepNext;
  /** The execution vars */
  public $vars;
  /** The current line */
  public $line;

  /**
   * Create a new execution frame
   * @param string the script name
   * @param bool true if a dynamic breakpoint should be inserted at the next line, false otherwise
   */
  public function pdb_Environment($parent, $scriptName, $stepNext) {
	parent::pdb_View($parent, $scriptName);
	$this->stepNext = $stepNext;
	$this->line = -1;
  }
  /**
   * Update the execution frame with the current state
   * @param string the current script name
   * @param int the current execution line
   * @param mixed the current variables
   */
  public function update ($line, &$vars) {
	$this->line = $line;
	$this->vars = $vars;
  }

  public function __toString() {
	return "pdb_Environment: {$this->scriptName}, {$this->line}";
  }
}
/**
 * Represents a breakpoint
 * @access private
 */
class pdb_Breakpoint {
  /** The script name */
  public $scriptName;
  /** The current line */
  public $line;
  /** The breakpointName as seen by JavaScript */
  public $breakpoint;
  /* The breakpoint type (not used yet) */
  public $type;

  /**
   * Create a new breakpoint
   * @param string the breakpoint name
   * @param string the script name
   * @param int the line
   */
  public function pdb_Breakpoint($breakpointName, $scriptName, $line) {
	$this->breakpoint = $breakpointName;
	$this->scriptName = $scriptName;
	$this->line = $line;

	$this->type = 1;
  }
  /**
   * @return the string representation of the breakpoint
   */
  public function __toString() {
	return "{$this->line}@{$this->scriptName}, js name: ({$this->breakpoint}, type: {$this->type})";
  }
}
/**
 * The current debug session. Contains the current environment stack,
 * script output and all breakpoints set by the client. An optional
 * view is set by the switchView command.
 * @access private
 */
final class pdb_Session {
  /** The collection of breakpoints */
  public $breakpoints;

  /** List of all frames */
  public $allFrames;
  /** The current top level frame */
  public $currentTopLevelFrame;
  /** The current execution frame */
  public $currentFrame;
  /** The current view */
  public $currentView;
  /** The script output */
  public $output;

  /**
   * Create a new debug session for a given script
   * @param string the script name
   */
  public function pdb_Session($scriptName) {
	$this->breakpoints = $this->lines = array();
	$this->currentTopLevelFrame = $this->currentFrame = new pdb_Environment(null, $scriptName, true);
	$this->allFrames[] = $this->currentFrame;

	$this->currentView = null;
  }
  /**
   * Return the clickable HTML script source, either from the cusom view or from the current frame
   * @return string the HTML script source
   */
  public function getCurrentViewHtmlScriptSource () {
	return $this->currentView ? $this->currentView->getHtmlScriptSource() : $this->currentFrame->getHtmlScriptSource();
  }   
  /**
   * Return the current frame script name
   * @return string the script name of the current frame
   */
  public function getScriptName () {
	return $this->currentFrame->scriptName;
  }
  /**
   * Return the current script name, either from the view or from the current frame
   * @return string the current script name
   */
  public function getCurrentViewScriptName () {
	return $this->currentView ? $this->currentView->scriptName : $this->getScriptName();
  }   
  /**
   * Return the breakpoints for the current script
   * @return object the breakpoints
   */
  public function getBreakpoints () {
	$bp = array();
	foreach ($this->breakpoints as $breakpoint) {
	  if ($this->getCurrentViewScriptName() != $breakpoint->scriptName) continue;
	  array_push($bp, $breakpoint->breakpoint);
	}
	return $bp;
  }
  /**
   * toggle and write breakpoint reply
   * @param object the current comm. channel
   * @param object the breakpoint
   */
  public function toggleBreakpoint($breakpoint) {
	$id = $breakpoint."@".$this->getCurrentViewScriptName();
	if (!isset($this->breakpoints[$id])) {
	  $this->breakpoints[$id] = new pdb_Breakpoint($breakpoint, $this->getCurrentViewScriptName(), substr($breakpoint, 3));
	  return false;
	} else {
	  $bp = $this->breakpoints[$id];
	  unset ($this->breakpoints[$id]);
	  return $bp;
	}
  }
  /**
   * check if there's a breakpoint
   * @param string the script name
   * @param int the line within the script 
   * @return true if a breakpoint exists at line, false otherwise
   */
  public function hasBreakpoint($scriptName, $line) {
	if ($this->currentFrame->stepNext) return true;
  
	foreach ($this->breakpoints as $breakpoint) {
	  if (PDB_DEBUG) pdb_Logger::debug("process breakpoint::: $scriptName, $line:: $breakpoint");
	  if($breakpoint->type==1) {
		if ($breakpoint->scriptName==$scriptName&&$breakpoint->line==$line) return true;
	  }
	}
	return false;
  }
  /**
   * parse code
   * @param string the script name
   * @param string the content
   * @return the parsed script
   */
  public function parseCode($scriptName, $content) {
	$parser = new pdb_Parser($scriptName, $content);
	return $parser->parseScript();
  }
  private static function doEval($__pdb_Code) {
	return eval ("?>".$__pdb_Code);
  }
  /**
   * parse and execute script
   * @return the script output
   */
  public function evalScript() {
	$code = $this->parseCode($this->getScriptName(), file_get_contents($this->getScriptName()));

	if (PDB_DEBUG) pdb_Logger::debug("eval:::$code,".$this->getScriptName()."\n");
	ob_start();
	self::doEval ($code);
	$this->output = ob_get_contents();
	ob_end_clean();

	return $this->output;
  }
}

/**
 * The java script debugger server daemon. Contains a debug session
 * and handles debug requests from the client.
 * @access private
 */
class pdb_JSDebugger {
  /** The pdb_Session */
  public $session;
  private $id;
 
  public $end;
  private $includedScripts;
  private $conn;
  private $ignoreInterrupt;

  const STEP_INTO = 1;
  const STEP_OVER = 2;
  const STEP_OUT = 3;
  const GO    = 4;


  private function getConnection($id) {
	return new pdb_PollingServerConnection($id);
  }

  /**
   * Create new PHP debugger using a given comm. ID
   * @param int the communication address
   */
  public function pdb_JSDebugger($id) {

	$this->id = $id;
	$this->conn = $this->getConnection($id);

	$this->end = false;
	$this->session = null;

	$this->includedScripts = array();

	$this->ignoreInterrupt = false;
	set_error_handler("pdb_error_handler");
	register_shutdown_function("pdb_shutdown");
  }
  public function setError($errno, $errfile, $errline, $errstr) {
	highlight_string("PHP error $errno: $errstr in $errfile line $errline");
  }
  /**
   * Return the current comm. ID
   * @return int the communication address
   */
  public function getServerID() {
	return $this->id;
  }
  /**
   * Read data from the front end
   * @return object the data 
   */
  public function read() {
	return $this->conn->read();
  }
  /**
   * Write data to the front end
   * @param object the data
   */
  public function write($data) {
	$data["serverID"] = $this->getServerID();
	if (PDB_DEBUG) pdb_Logger::debug("->".print_r($data, true));
	return $this->conn->write($data);
  }
  private function ack() {
	$this->write(array("cmd"=>$this->packet->cmd,
					   "seq"=>$this->packet->seq));
  }

  private function getOutput() {
	if (!$this->session) return "";

	if (!$this->end) $output = $this->session->output = ob_get_contents();
	return $this->session->output;
  }

  /**
   * Handle requests from the front end
   */
  public function handleRequests() {
	$this->ignoreInterrupt = false;

	while(!$this->end) {
	  if (PDB_DEBUG) pdb_Logger::debug("handleRequests: accept");
   
	  if (!($this->packet = $this->read())) break; // ignore __destructors after shutdown

	  if (PDB_DEBUG) pdb_Logger::debug("handleRequests: done accept ".$this->packet->cmd);

	  switch($this->packet->cmd) {
	  case "status":
		$this->write(array("cmd"=>$this->packet->cmd,
						   "seq"=>$this->packet->seq, 
						   "line"=>$this->session->currentFrame->line, 
						   "scriptName"=>$this->session->getCurrentViewScriptName(), 
						   "breakpoints"=>$this->session->getBreakpoints()));
		break;
	  case "extendedStatus":
		$this->write(array("cmd"=>$this->packet->cmd,
						   "seq"=>$this->packet->seq, 
						   "line"=>$this->session->currentFrame->line, 
						   "scriptName"=>$this->session->getCurrentViewScriptName(), 
						   "script"=>$this->session->getCurrentViewHtmlScriptSource(),
						   "breakpoints"=>$this->session->getBreakpoints()));
		break;
	  case "begin":
		chdir (urldecode($this->packet->cwd));
		$this->session = new pdb_Session(urldecode($this->packet->scriptName));
		$this->write(array("cmd"=>$this->packet->cmd,
						   "seq"=>$this->packet->seq, 
						   "scriptName"=>$this->packet->scriptName, 
						   "script"=>$this->session->getCurrentViewHtmlScriptSource()));

		$this->session->evalScript();
		$this->end = true;
		break;
	  case "stepNext":
		if ($this->end) break;
		$this->session->currentView = null;
		$this->ack();
		return self::STEP_INTO;
	  case "stepOver":
		if ($this->end) break;
		$this->session->currentView = null;
		$this->ack();
		return self::STEP_OVER;
	  case "go":
		if ($this->end) break;
		$this->session->currentView = null;
		$this->ack();
		return self::GO;
	  case "stepOut":
		if ($this->end) break;
		$this->session->currentView = null;
		$this->ack();
		return self::STEP_OUT;
	  case "toggleBreakpoint":
		$bp = $this->session->toggleBreakpoint($this->packet->breakpoint);
		$this->write($bp ? 
					 (array("cmd"=>"unsetBreakpoint", 
							"seq"=>$this->packet->seq,
							"scriptName"=>$bp->scriptName, 
							"breakpoint"=>$bp->breakpoint)) :
					 (array("cmd"=>"setBreakpoint", 
							"seq"=>$this->packet->seq,
							"scriptName"=>$this->session->getCurrentViewScriptName(), 
							"breakpoint"=>$this->packet->breakpoint)));
		break;
	  case "toolTip":
		$name = urldecode($this->packet->item);
		$value = "";
		if ($name[0]=='$') {
		  $idx = substr($name, 1);
		  $env = (object) $this->session->currentFrame->vars;
		  $code = "return \$env->$idx;";
		  $value = eval($code);
		  if (is_object($value)) {
			$value = get_class($value) . " object";
		  } elseif (is_array($value)) {
			$value = "array[".count($value)."]";
		  } elseif (!isset($value)) {
			$value = "<undefined>";
		  } else {
			$value = print_r($value, true);
		  }
		} else {
		  $value = $this->packet->item;
		}
		$this->write(array("cmd"=>$this->packet->cmd,
						   "seq"=>$this->packet->seq,
						   "item"=>$this->packet->item,
						   "value"=>$value));
		break;
	  case "switchView":
		if (PDB_DEBUG) pdb_Logger::debug("switchView here");
		$name = urldecode($this->packet->scriptName);
		if (PDB_DEBUG) pdb_Logger::debug("switchView $name");
		if ($name[0]=='$') {
		  $idx = substr($name, 1);
		  $env = (object) $this->session->currentFrame->vars;
		  $code = "return \$env->$idx;";

		  $pdb_dbg->end = true;
		  $value = eval($code);
		  $pdb_dbg->end = false;

		  $this->session->currentView = new pdb_VariableView($this->session->currentView, $name, $value);
		} else {
		  $this->end = true;
		  $value = self::resolveIncludePath(eval("return ${name};"));
		  $this->end = false;

		  $this->session->currentView = new pdb_View($this->session->currentView, realpath($value));
		}
		$this->ack();
		break;
	  case "backView":
		if ($this->session->currentView)
		  $this->session->currentView = $this->session->currentView->parent;
		$this->ack();
		break;
	  case "output":
		if ($this->session) {
		  $this->write(array("cmd"=>$this->packet->cmd,
							 "seq"=>$this->packet->seq, 
							 "output"=>$this->getOutput()));
		} else {
		  $this->ack();
		}
		break;
	  case "end":
		$this->end();
		break;
	  default:
		if (PDB_DEBUG) pdb_Logger::debug("illegal packet: " . print_r($this->packet, true));
		exit(1);
	  }
	}
	return self::GO;
  }
  public function end() {
	$this->session->currentView = null;
	$this->write(array("cmd"=>"end",
					   "seq"=>$this->packet->seq, 
					   "output"=>$this->getOutput()));
	$this->end = true;
  }
  /**
   * shut down the current comm. channel
   */
  public function shutdown() {
	$this->conn->setOutput($this->getOutput());
	$this->conn->shutdown();
  }

  /**
   * called at run-time for each frame
   * @return the current frame
   */
  public function startCall($scriptName) {
	/* check for stepOver and stepOut */
	$stepNext = $this->session->currentFrame->stepNext == pdb_JSDebugger::STEP_INTO ? pdb_JSDebugger::STEP_INTO : false;
	
	if (PDB_DEBUG) pdb_Logger::debug("startCall::$scriptName, $stepNext");
	$env = new pdb_Environment($this->session->currentFrame, $scriptName, $stepNext);
	$this->session->allFrames[] = $env;
	return $env;
  }

  /**
   * @access private
   */
  protected function resolveIncludePath($scriptName) {
	if (file_exists($scriptName)) return realpath($scriptName);

	$paths = explode(PATH_SEPARATOR, get_include_path());
	$name = $scriptName;
	foreach ($paths as $path) {
	  $x = substr($path, -1);
	  if ($x != "/" && $x != DIRECTORY_SEPARATOR) $path.=DIRECTORY_SEPARATOR;
	  $scriptName = realpath("${path}${name}");
	  if ($scriptName) return $scriptName;
	}
	trigger_error("file $scriptName not found", E_USER_ERROR);
  }
  /**
   * called at run-time for each included file
   * @param string the script name
   * @return string the code
   */
  public function startInclude($scriptName, $once) {
	$isDebugger = (basename($scriptName) == "PHPDebugger.php");
	if (!$isDebugger)
	  $scriptName = $this->resolveIncludePath($scriptName);

	if (PDB_DEBUG) pdb_Logger::debug("scriptName::$scriptName, $isDebugger");

	if ($once && isset($this->includedScripts[$scriptName]))
	  $isDebugger = true;

	// include only from a top-level environment
	// initial line# and vars may be wrong due to a side-effect in step
	$this->session->currentFrame = $this->session->currentTopLevelFrame;

	$stepNext = $this->session->currentFrame->stepNext == pdb_JSDebugger::STEP_INTO ? pdb_JSDebugger::STEP_INTO : false;
	$this->session->currentFrame = new pdb_Environment($this->session->currentFrame, $scriptName, $stepNext);
	$this->session->allFrames[] = $this->session->currentFrame;

	if ($isDebugger) // do not debug self
	  $code = "<?php ?>";
	else
	  $code = $this->session->parseCode(realpath($scriptName), file_get_contents($scriptName));

	$this->session->currentTopLevelFrame = $this->session->currentFrame;

	if (PDB_DEBUG) pdb_Logger::debug("startInclude:::".$this->session->currentTopLevelFrame . " parent: " . $this->session->currentTopLevelFrame->parent . " code: ".$code);

	if ($once) $this->includedScripts[$scriptName] = true;
	return $code;
  }

  /**
   * called at run-time after the script has been included
   */
  public function endInclude() {
	if (PDB_DEBUG) pdb_Logger::debug("endInclude:::".$this->session->currentTopLevelFrame . "parent: ".$this->session->currentTopLevelFrame->parent);

	$this->session->currentFrame = $this->session->currentTopLevelFrame = 
	  $this->session->currentTopLevelFrame->parent;
  }

  /**
   * called at run-time for each line
   * @param string the script name
   * @param int the current line
   * @param mixed the execution variables
   */
  public function step($scriptName, $line, $vars) {
	if ($this->ignoreInterrupt) return; // avoid spurious step calls from __destruct() method
	$this->ignoreInterrupt = true;

	if (PDB_DEBUG) pdb_Logger::logDebug("step: $scriptName @ $line");
	// pull the current frame from the stack or the top-level environment
	$this->session->currentFrame = (isset($vars['__pdb_CurrentFrame'])) ? $vars['__pdb_CurrentFrame'] : $this->session->currentTopLevelFrame;
	unset($vars['__pdb_CurrentFrame']);

	$this->session->currentFrame->update($line, $vars);

	if ($this->session->hasBreakpoint($scriptName, $line)) {
	  $stepNext = $this->handleRequests();
	  if (PDB_DEBUG) pdb_Logger::logDebug("continue");

	  /* clear all dynamic breakpoints */
	  foreach ($this->session->allFrames as $currentFrame)
		$currentFrame->stepNext = false;

	  /* set new dynamic breakpoint */
	  if ($stepNext != pdb_JSDebugger::GO) {
		$currentFrame = $this->session->currentFrame;

		/* break in current frame or frame below */
		if ($stepNext != pdb_JSDebugger::STEP_OUT)
		  $currentFrame->stepNext = $stepNext;

		/* or break in any parent */
		while ($currentFrame = $currentFrame->parent) {
		  $currentFrame->stepNext = $stepNext;
		}
	  }
	}

	$this->ignoreInterrupt = false;
	if (PDB_DEBUG) pdb_Logger::logDebug("endStep: $scriptName @ $line");
  }
}

/**
 * Convenience function called by the executor
 * @access private
 */
function pdb_getDefinedVars($vars1, $vars2) {
  if(isset($vars2)) $vars1['pbd_This'] = $vars2;

  unset($vars1['__pdb_Code']);	   // see pdb_Message::doEval()

  return $vars1;  
}

/**
 * Convenience function called by the executor
 * @access private
 */
function pdb_startCall($scriptName, $line) {
  global $pdb_dbg;
  if (isset($pdb_dbg)) return $pdb_dbg->startCall($scriptName);
}

/**
 * Convenience function called by the executor
 * @access private
 */
function pdb_startInclude($scriptName, $once) {
  global $pdb_dbg;
  if (isset($pdb_dbg)) return $pdb_dbg->startInclude($scriptName, $once);
  else return "";
}

/**
 * Convenience function called by the executor
 * @access private
 */
function pdb_endInclude() {
  global $pdb_dbg;
  if (isset($pdb_dbg)) $pdb_dbg->endInclude();
}

/**
 * Convenience function called by the executor
 * @access private
 */
function pdb_step($scriptName, $line, $vars) {
  global $pdb_dbg;
  if (isset($pdb_dbg)) $pdb_dbg->step($scriptName, $line, $vars);
}

/**
 * @access private
 */
function pdb_error_handler($errno, $errstr, $errfile, $errline) {
  global $pdb_dbg;
  if (PDB_DEBUG) pdb_Logger::debug("PHP error $errno: $errstr in $errfile line $errline");
  if ($pdb_dbg->end) return true;
 
 if (strncmp(basename($errfile),"PHPDebugger", 11)) 
   $pdb_dbg->setError($errno, $errfile, $errline, $errstr);

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
	$pdb_dbg->setError($error['type'], $error['file'], $error['line'], $error['message']);
	$pdb_dbg->end();
	$pdb_dbg->shutdown();
  }
}

if (PDB_DEBUG==2) {
  $parser = new pdb_Parser($argv[1], file_get_contents($argv[1]));
 echo $parser->parseScript();
 exit (2);
}

/* * The JavaScript part, invoked after the debugger has been included() * */
if (!isset($_SERVER['HTTP_XPDB_DEBUGGER'])) {

  session_start();
  header("Expires: Sat, 1 Jan 2005 00:00:00 GMT");
  header("Last-Modified: ".gmdate( "D, d M Y H:i:s")." GMT");
  header("Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
  header("Pragma: no-cache");
  header("Content-Type: text/html");
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>
PHPDebugger version 1.0
</title>
<style type="text/css">
#tooltip {
  background:#EFEFEF none repeat scroll 0 0;
  height:auto;
  width:auto;
  min-width: 1px;
  min-height: 1.5ex;
  display: block;
  border:1px solid black;
  background-color:gray; color:white;
  position:absolute;
  text-align: center;
  z-index:75;
}
.tt {
  visibility: hidden;
}
.ttHover {
  visibility: visible;
}

#run {
  height: 13px;
  width: 17px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  background:green url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAANCAYAAABPeYUaAAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwcXGXdF1DwAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAABEUlEQVQoz62SoWvDUBDGfx0Rry6VzzUyg4lEVgYmRulfMVUKVWOqTFVWDULVbNxsTSETg8jOzm+QwcSL28ECmwhJGpqwwnrmfXfv7nvf3b2e+TQ//NMsgPRlQfoeQ14E9TBAe0uC9QyAeBoeQfK6IZjuqmC89tHeEoDFxP+TzKqQJK0JksPNlQ/QSWYBiJQVKShd+4DJa+f68hyA0f0M7fR5nKyaSiQTlHwgatB4JXkzB+q8C12RJfNwr522Vr4hy7PjtgOgbAWZKs6SRASt+gfZ8bPBcQck83B/Jl/Fre3S8AGlarx5Kuazu33o2I4atUq1LYi27cUNEj0cE925VdBxxxWOttJZXFrvFN/+jBPYL4uJYFb2zCiIAAAAAElFTkSuQmCC') no-repeat;
}
#terminate {
  height: 13px;
  width: 13px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  background:red url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA0AAAANCAYAAABy6+R8AAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwYrNECrm4EAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAAAwUlEQVQoz52SLQ7CQBCFvyaI4nYlciWSI3CEFkVQGAyOcBU0ClCARCKRHGFlN0HsuK4D0RRasWnKJOPel/fmJ/Ev/6ZnDQD89YzcL51iNc3R2ayC5HZkslx3Qs/97gcBEEBOh7jLfNGOh5RY8egQd7HiQcoG9HULPRZRwthagvdR4chapBUPQELVsZKAL8HUkBZApVVHN5FWuhoKzlEYg06HUaYwhuBcO55RGjbxWynANWdSWc5jte3+iCwHIPnn9z7J30SR7ayFNQAAAABJRU5ErkJggg==') no-repeat;
}
#stepInto {
  height: 13px;
  width: 17px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  background: yellow url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAANCAYAAABPeYUaAAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwYtD6f61SMAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAAA6ElEQVQoz2N8//r9fwYKAQuMcWy6JlYFmq4lDIIqyYQNOTZdk8Gr8jpWBdvaNRkUPz9nkDSswWkII8w72Fyi6JjMoGmRw7Ct3ZDBKvM6Ye9gU3RsuiaDpkkywTBhIqjizwfiAxbdBQhDfqCIYXMxVkN+fGdgCCqaCzXkPYNXJoS9ri+ZeO84FV1nWNYUDfEKFC9rimZwKrpOWph41d1nWNaWzcDw5zvDsrZsBq+6+/ijOHn+MQbBzxBF73kVGeYmWiHSSZMiigHY1LIwMDAw/Di/lOHaC4giQYljDAxIhqC7AJtaRmrkHQBYX2Q4HZvQSwAAAABJRU5ErkJggg==') no-repeat;
}
#stepOver {
  height: 13px;
  width: 17px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  background: yellow url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAANCAYAAABPeYUaAAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwYsOAZcQW0AAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAABSklEQVQoz62TP0jDQBTGv5QMdSgYcEjGgEvHdsyYsW6Kg27SKeJki1PpEAShqJPYKTilHUS3dOyYbu1mhpZmERqIcIEOyRA4h9qaeCcK+uA43uPj9/7cO4GEhOKPJmYdMrMQTJ5A3nxGKO2okCsHkHbr30PIzII/slA9vMO2XGWEUTDG+PEMyXIBpdJiIWuAbjiIXkcYXJUZiHZ0A91wMOzuoVhS8hWRkFDHlCmN55RMbeqYMiUhYY5jypRMbUrjOaMpbGhpBLffgGZ43OFphge33wDSiD+TJAaQJqubY243016a5GKa4UFYP/HAVFFr+1zI8LaM/dNLoKh8BpMFnu9b0M8zkJ9sYKo4vrgGxC0gjdHrNDdJC79dqFrbR6/TZAAA+JXUH1xIy5WIlFRYJ1quoq9ti7ysycTGS/CxqbILZCC8uQn/8XfeAUbdtbmYIYl8AAAAAElFTkSuQmCC') no-repeat;
}
#stepOut {
  height: 13px;
  width: 17px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  background: yellow url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAANCAYAAABPeYUaAAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwYsF62NfDQAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAABAUlEQVQoz62Tv2vCQBTHP5GsDoJDV8f8C44ZM+Y/EOnSgoOCQ4YOxVE6BH9MoZMdM3TI6CLcJNzmLULGCg4p6HBgoB0SFH9gKvYtj7v3fR/ufe/OSNbJD3dG6RaxGFuIsXUfhB04j0OEfwwyi/q+ZI94NjlsVG2cpyGRb1FvKQCMa54kywAZ9XE9eV78FoSjZ+yWun4S+dnH9aao+QAVBQC43hS2ktDvYLfVXz3RqCjYN5wCiiEpkOos5+vw7RhQbGyqIU2yDNhddfs70TlIFwxsAjTfBZVNnN1IuUbQqJ+i9nFJawJoOWGxykSVBwE5xPFiPl5rOC/xAXlBa/zH3/kFlslu4rTXaTUAAAAASUVORK5CYII=') no-repeat;
}
#output {
  height: 13px;
  width: 13px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  visibility: visible;
  background: blue url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA0AAAANCAYAAABy6+R8AAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLCQYlB4EnoCoAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAABCklEQVQoz42Rv0oDQRCHv4MJbCCFgoUHvkBKXyGPEDufwUokhdFGokWwt7IRbISkTHmvcIUPcUKK3eJgp1g4i92Ld4gxWw2z329+8ye7vF81pcs59J0fVUjpcsZaHCwq3QQBD8Ddcv6vYDF7BDxCACMGgPFov8iIgQACChKTVw9rvCrqPK52+FopPjodCBA0Og1H0el5Nv1VXcNPPDQGHAhBMcnpZrlOJFi1aK1orXhVNm/zxGnbXnRaXE//nEchcm17ZpB+ggImIaaDx9gMaBfhQeD95bVfWiLQi4W48vzkmM1ndfBx89OczG5t001ePBVUX1UPWt1OesLMbm2zA4Puhk3n7LucxQLfZ4Norb3ftQMAAAAASUVORK5CYII=') no-repeat;
}
#backView {
  height: 13px;
  width: 16px;
  display: inline-block;
  position: relative;
  margin: 1px 10px 1px 10px;
  visibility: visible;
  background: yellow url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAANCAYAAACgu+4kAAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwc3GnvIoSQAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAABH0lEQVQoz5WToU4DQRCGvzZLspUn+wpIJLLyJDiQEBLMJZDAA1QgTlRUU1tXCY+A5BGoPHFiKkhukm4yiD3CNr3S9k8mu/l3Z+af2dme1GJ04HM6hBPP2f2S/9DvIj/KjNG4gUbZh36Xc15GURr2B3Abzi8Z+cQABTyqkdu8BTjP+WMV91KLSS22eMDMGjOrzNZfZuvKzCSxJlnN3p4wqcVclD3kYlJBWEJQCACrJLNvVwXnwWVoSErQbwUEwgq0+ZMa2JYfBuAV1aSJo7EwvzsFmpj515y2/WjNKbim5SN66Ry8P2dcTRexDGBWXDNwHSoc5GPZfoW8FGZFxs30FYCBi9xRc3BZCrPiNoZ2/rg5SIPMi+GO0030dv2FQ/EDM7CYPt7DsBMAAAAASUVORK5CYII=') no-repeat;
}
 
.normal {
  background: transparent;
}
.selected {
  background: yellow url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA0AAAANCAYAAABy6+R8AAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwYQB8PJFa8AAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAABNElEQVQoz4WPsUsCYRjGf8YNNzTo7vJBDgUNHTSc4KBbro6ORy5KU5tN0VhjS+FfEDXaEFxbY9cQuQSfQ3BCwyck3At9YIOieZo98MAL7/vjed6MMWYMcH6aQxUqePsBaqvKOm0A6PcQpaDoh4QPdcL7E8xQr4EsmC9BLKg8NA8hm72kc+Xx9Hi2GhIryMgAIHZibw9aDZDRBZ3rIs9RZ7neTDK3C1RKUC336L0cc3tTR/fDFGTByLLdTagegMp36d7VAHAWQuSPzy0Mh5BM987vxSoojiGKQBUCgkYzBQGSzOdkBNEr4Pj45QBvtza7XkhCILGg+6A1FEttPL+F67oL6RPIcZFveJse72wHBEdtctncyhcz5tOM40GM1iHxR4jnN1F5L1U8DRkzFhGwTGo4/KsfXjWDCQ6TmRMAAAAASUVORK5CYII=') no-repeat;
}
.breakpointSet {
  background: red url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA0AAAANCAYAAABy6+R8AAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9kLBwYOGJqAJ4UAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAAA+0lEQVQoz5XSMUvDUBSG4bcXhyM4pFsudBE6GEfB5UKGZLOjq5uCIE7iPyjdxEXQoZC/4OgipIOQbqZDMW6OcbsZBO8Q0MEWkWJJ3vl7lsPpWGu/WOQqy8s44X2S8ukcmyL4Uczu6QnidZczOktUPqY8HB/Bh2OlLWEwTtAHg19kXwvuQwM1/7cBh08Z3Z0ABZCena8HAPViB6hymlHNcppUzXLKaYaq5gVtquYFitq1QtQO5fWDVsbrBygdGqTnNwLS89GhQSFCfJc0QvHNLYj8nFyHBjMcrQVmOEJH8d+PALD5M/nVNW+TFJwDEbajmODiEr2/t/pGbfoGcP1aToLr9OAAAAAASUVORK5CYII=') no-repeat;
}

#navigationBar {
  display: block;
  position: fixed;
  top: 0px;
  left: 0px;
  height: 20px;
  width: 100%;
  z-index: 100;
  background: #efefef;
}
#code {
  display: block;
  position: absolute;
  left: 92px;
  top: 22px;
  z-index: 50;
}

.breakpoint {
  display: inline;
  position: absolute;
  left: -92px;
  width: 90px;
  height: 13px;
  background: #efefef;
  overflow: hidden;
}

.linenumber {
  display: inline-block;
  position: relative;
  width: 59px;
  height: 13px;
  text-align: right;
  margin-right: 5px;
  float: right;
  color: black;
  
}
.currentlineIndicator {
  display: inline-block;
  position: relative;
  width: 13px;
  height: 13px;
  float: left;
}
.breakpointIndicator {
  display: inline-block;
  position: relative;
  width: 13px;
  height: 13px;
  float: left;
}
</style>

<script type="text/javascript">
var http = createRequestObject();
var httpCtrl = createRequestObject();
var serverID = <?php echo pdb_JSDebuggerClient::getServerID(); ?>;
var scriptName = "<?php echo pdb_JSDebuggerClient::getDebugScriptName(); ?>";
var currentScriptName = "";
var cwd = "<?php echo urlencode(getcwd()); ?>";
var debuggerURL = "<?php echo pdb_JSDebuggerClient::getDebuggerURL(); ?>";
var date = "<?php echo gmdate( 'D, d M Y H:i:s').' GMT'; ?>";
var seq = 1;

function getSeq() {
 return seq++;
}
function getServerID() {
  return serverID;
}
function createRequestObject() {
  var req;
  var browser = navigator.appName;
  if (browser == "Microsoft Internet Explorer") {
	req = new ActiveXObject("Microsoft.XMLHTTP");
  } else {
	req = new XMLHttpRequest();
  }
  return req;
}
function doCmd(text) {
  switch(text.cmd) {
  case 'stepNext': 
  case 'stepOver': 
  case 'go':
  case 'switchView':
  case 'backView':
  case 'begin':
  case 'stepOut':	getStatusCB(text); break;

  case 'output':	showOutputCB(text); break;

  case 'status':	showStatusCB(text); break;

  case 'extendedStatus': showExtendedStatusCB(text); break;

  case 'setBreakpoint':	setBreakpointCB(text); break;

  case 'unsetBreakpoint':	unsetBreakpointCB(text); break;

  case 'term':
  case 'end':		endCB(text); break;
  case 'toolTip':   showToolTipCB(text); break;

  default: alert("illegal cmd: " + text.cmd); break;
  }
}
function hasClassName(element, className) {
  var elementClassName = element.className;
  if (elementClassName.length == 0) return false;
  if (elementClassName == className ||
    elementClassName.match(new RegExp("(^|\\s)" + className + "(\\s|$)")))
   return true;
  return false;
}
if (document.getElementsByClassName == undefined) {
 document.getElementsByClassName = function(className) {
	var children = document.body.getElementsByTagName('*');
	var elements = [], child;
	for (var i = 0, length = children.length; i < length; i++) {
	 child = children[i];
	 
	 if (hasClassName(child, className)) {
	elements.push(child);
	 }
	}
	return elements;
 }
}
function sendCmd(cmd) {
  var url = debuggerURL;
  data = cmd+"&serverID="+getServerID()+"&seq="+getSeq();

  /* use synchronous requests to avoid out of order execution of 
	 step -> status -> extended status sequences */
  http.open("POST", url, false);
  http.setRequestHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
  http.setRequestHeader("Last-Modified", date);
  http.setRequestHeader("Pragma", "no-cache");
  http.setRequestHeader("Expires", "Sat, 1 Jan 2005 00:00:00 GMT");
  http.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  http.setRequestHeader("Content-Length", data.length);
  http.setRequestHeader("XPDB-DEBUGGER", 0);
  /*
  http.onreadystatechange = function() {
	 if(http.readyState == 4 && http.status == 200) {
	   doCmd(eval(http.responseText));
	 }
  }
  */

  http.send(data);

  doCmd(eval(http.responseText));
}
function startServer() { 
  var url = debuggerURL;
	data = "<?php echo pdb_JSDebuggerClient::getPostData(); ?>";
	method = "<?php echo $_SERVER['REQUEST_METHOD']; ?>";
  httpCtrl.open(method, url, true);
  httpCtrl.setRequestHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
  httpCtrl.setRequestHeader("Last-Modified", date);
  httpCtrl.setRequestHeader("Pragma", "no-cache");
  httpCtrl.setRequestHeader("Expires", "Sat, 1 Jan 2005 00:00:00 GMT");
  httpCtrl.setRequestHeader("Content-Length", data.length);
  httpCtrl.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  httpCtrl.setRequestHeader("XPDB-DEBUGGER", getServerID());
  httpCtrl.onreadystatechange = function() {
	 if(httpCtrl.readyState == 4 && httpCtrl.status == 200) {
	  //alert("debugger exited. Debugger debug output: " +httpCtrl.responseText);
	 }
  }
  httpCtrl.send(data);
}
function stepNext() {
  sendCmd("cmd=stepNext");
}
function stepOver() {
  sendCmd("cmd=stepOver");
}
function stepOut() {
  sendCmd("cmd=stepOut");
}
function getStatusCB(cmd) {
	sendCmd("cmd=status");
}

function loaded() {
  startServer();
  sendCmd("cmd=begin&scriptName="+encodeURI(scriptName)+"&cwd="+encodeURI(cwd));
}
function toggleBreakpoint(el, event) {
  sendCmd("cmd=toggleBreakpoint&breakpoint="+encodeURI(el.id));
  return false;
}
function setBreakpointCB(cmd) {
  document.getElementById(cmd.breakpoint).lastChild.className="breakpointIndicator breakpointSet";
}
function unsetBreakpointCB(cmd) {
  document.getElementById(cmd.breakpoint).lastChild.className="breakpointIndicator normal";
}
function showOutputCB(cmd) {
  currentScriptName = "";
  document.getElementById("code").innerHTML = cmd.output;
  window.scrollTo(0, 0);
}

function doShowStatusCB(cmd) {
  lines = document.getElementsByClassName("currentlineIndicator");
   if (lines.length == 0) {
     window.scrollTo (0, 0);
     return; // no lines to mark
   }

  lines = document.getElementsByClassName("currentlineIndicator selected");
  for (i=0; i<lines.length; i++) {
	line = lines[i];
	line.className = "currentlineIndicator normal";
  }
  for (i=0; i<cmd.breakpoints.length; i++) {
	breakpoint = cmd.breakpoints[i];
	document.getElementById(breakpoint).lastChild.className="breakpointIndicator breakpointSet";
  }
  currentLine = document.getElementById("bp_"+(cmd.line)).firstChild;

	currentLine.className="currentlineIndicator selected";

	codeDiv = document.getElementById("code");
	codeLine = currentLine.parentNode;
	toolsHeight = 30;
	if (codeLine.offsetTop < window.pageYOffset || codeLine.offsetTop + codeLine.clientHeight +toolsHeight > window.pageYOffset+window.innerHeight) {
	  window.scrollTo(0, codeLine.offsetTop + toolsHeight - window.innerHeight/2);
	}
}
function showStatusCB(cmd) {
	if (currentScriptName == cmd.scriptName)
	 doShowStatusCB(cmd);
	else
	 sendCmd("cmd=extendedStatus"); // another round-trip 
}
function showExtendedStatusCB(cmd) {
	currentScriptName = cmd.scriptName;
	document.getElementById("code").innerHTML = cmd.script;
	document.title = cmd.scriptName;

	doShowStatusCB(cmd);
}

function stepInto(el, event) {
  sendCmd("cmd=stepNext");
  return false;
}
function stepOver(el, event) {
  sendCmd("cmd=stepOver");
  return false;
}
function stepOut(el, event) {
  sendCmd("cmd=stepOut");
  return false;
}
function end(el, event) {
  sendCmd("cmd=end");
  return false;
}
function endCB(cmd) {
  document.body.innerHTML = cmd.output;
}
function go(el, event) {
  sendCmd("cmd=go");
  return false;
}
function switchView(data) {
  data = encodeURI(data);
  sendCmd("cmd=switchView&scriptName="+data);
  return false;
}
function backView(el, event) {
  sendCmd("cmd=backView");
  return false;
}
function output(el, event) {
  sendCmd("cmd=output");
  return false;
}

var currentEl, tooltipItem;
var currentX, currentY;
var timer;
var tt;
function getToolTip() {
  if(tt) return tt;
  return tt = document.getElementById("tooltip");
}
function mouseout(el, event) {
  getToolTip().className = "tt";
  if (timer) {
	window.clearTimeout(timer);
	timer = null;
  }
  return true;
}
function mousemove(el, event) {
  if (timer) {
	window.clearTimeout(timer);
	getToolTip().className = "tt";
	timer = null;
  }
  if (getEventSource(event).parentNode.className=="breakpoint") return true;
  el = getEventSource(event);
  if (el.nodeName !="SPAN") return true;

  currentEl = el;
  currentX = getEventX(event) + window.pageXOffset;
  currentY = getEventY(event) + window.pageYOffset;
  timer = window.setTimeout("showToolTip()", 600);
  return false;
}
function toolTipCmd(el) {
  el=encodeURI(el);
  sendCmd("cmd=toolTip&item="+el);
  return false;
}
function showToolTip() {
  el = currentEl;
  if (el && el.firstChild && el.firstChild.data) {
	data = trim(el.firstChild.data);
	while (el && el.previousSibling && el.previousSibling.firstChild && el.previousSibling.firstChild.data && trim(el.previousSibling.firstChild.data)=='->') {
	  el = el.previousSibling.previousSibling;
	  if (el && el.firstChild && el.firstChild.data) data = trim(el.firstChild.data)+"->"+data;
	}
  }
    toolTipCmd(data);
}
function showToolTipCB(cmd) {
  tt = getToolTip();
  if(tooltipItem) tt.removeChild(tooltipItem);
  tooltipItem = (document.createTextNode(cmd.value));
  tt.appendChild(tooltipItem);
  tt.style.top=(currentY+10) + "px";
  tt.style.left=(currentX+10) + "px";
  tt.className = "ttHover";

}
function trim(str) {
  nbspChar = String.fromCharCode(160);
  return str.replace(/^\s*/, "").replace(/\s*$/, "").replace(nbspChar,"");
}
function getEventSource(event) {
 if (event.target != undefined) return event.target;
 return event.srcElement;
}
function getEventX(event) {
  if (event.clientX != undefined) return event.clientX;
  return event.offsetX;
}
function getEventY(event) {
  if (event.clientY != undefined) return event.clientY;
  return event.offsetY;
}
function mousedown(el, event) {
  getToolTip().className = "tt";

  if (getEventSource(event).parentNode.className=="breakpoint") return true;
  el = getEventSource(event);
  if (el && el.firstChild && el.firstChild.data) {
	data = trim(el.firstChild.data);
	while (el && el.previousSibling && el.previousSibling.firstChild && el.previousSibling.firstChild.data && trim(el.previousSibling.firstChild.data)=='->') {
	  el = el.previousSibling.previousSibling;
	  if (el && el.firstChild && el.firstChild.data) data = trim(el.firstChild.data)+"->"+data;
	}
	switchView(data);
  }
  return false;
}
</script>

</head>
<body>

<div id="navigationBar">
<a id="run" onmousedown="return go(this, event);"></a>
<a id="terminate" onmousedown="return end(this, event);"></a>
<a id="stepInto" onmousedown="return stepInto(this, event);"></a>
<a id="stepOver" onmousedown="return stepOver(this, event);"></a>
<a id="stepOut" onmousedown="return stepOut(this, event);"></a>
<a id="output" onmousedown="return output(this, event);"></a>
<a id="backView" onmousedown="return backView(this, event);"></a>
</div>
 <div onmouseout="return mouseout(this, event);" onmousemove="return mousemove(this, event);" onmousedown="return mousedown(this, event);">
 <div id="code">
<span>loading...
<noscript>failed! Please enable JavaScript and try again.</noscript>
</span>
</div>
<div id="tooltip" class="tt" />
</div>
<script type="text/javascript">
 loaded();
</script>
</body>
</html>
<?php
 exit (0);
} elseif (($serverID = $_SERVER['HTTP_XPDB_DEBUGGER']) != "0") {
/* * The back end part, invoked from JavaScript using a json call. $serverID is a uniq ID generated from JavaScript * */
 header("Expires: Sat, 1 Jan 2005 00:00:00 GMT");
 header("Last-Modified: ".gmdate( "D, d M Y H:i:s")." GMT");
 header("Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
 header("Pragma: no-cache");
 header("Content-Encoding: identity");
 $pdb_dbg = new pdb_JSDebugger((int)$serverID);
 $pdb_dbg->handleRequests();
 $pdb_dbg->shutdown();
 if (PDB_DEBUG) pdb_Logger::debug("SERVER TERMINATED!");
 exit(0);
} else {
/* * The front end part, invoked from JavaScript using json calls * */
 header("Expires: Sat, 1 Jan 2005 00:00:00 GMT");
 header("Last-Modified: ".gmdate( "D, d M Y H:i:s")." GMT");
 header("Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
 header("Pragma: no-cache");
 header("Content-Encoding: identity");
 pdb_JSDebuggerClient::handleRequest ($_POST);
 exit(0);
}
?>
