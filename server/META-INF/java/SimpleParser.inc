<?php /*-*- mode: php; tab-width:4 -*-*/

  /** java_SimpleParser.php -- PHP/Java Bridge protocol parser.
   *
   *   Copyright (C) 2003-2007 Jost Boekemeier
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

require_once(java_get_base()."/Client.inc");

/**
 * @access private
 */
class java_ParserString {
  public $string, $off, $length;
  function toString() {
	return $this->getString();
  }
  function getString() {
	return substr($this->string, $this->off, $this->length);
  }
}
/**
 * @access private
 */
class java_ParserTag {
  public $n, $strings;
  function java_ParserTag() {
	$this->strings = array();
	$this->n = 0;
  }
}
/**
 * @access private
 */
class java_SimpleParser {
  public $SLEN=256; // initial length of the parser string

  public $handler;
  
  public $tag, $buf, $len, $s;
  public $type;

  function java_SimpleParser($handler) {
    $this->handler = $handler;
    $this->tag = array(new java_ParserTag(), new java_ParserTag(), new java_ParserTag());
	$this->len = $this->SLEN;
	$this->s = str_repeat(" ", $this->SLEN);
	$this->type = $this->VOJD;
  }
  
  // VOJD is VOID for f... windows (VOID is in winsock2.h)
  public $BEGIN=0, $KEY=1, $VAL=2, $ENTITY=3, $VOJD=5, $END=6; 
  public $level=0, $eor=0; public $in_dquote, $eot=false;
  public $pos=0, $c=0, $i=0, $i0=0, $e;

  function RESET() {
	$this->type=$this->VOJD;
	$this->level=0;
	$this->eor=0;
	$this->in_dquote=false;
	$this->i=0;
	$this->i0=0;
  }
	
  function APPEND($c) {
	if($this->i>=$this->len-1) {
	  $this->s=str_repeat($this->s,2);
	  $this->len*=2;
	} 
	$this->s[$this->i++]=$c; 
  }
  function CALL_BEGIN() {
    $pt=&$this->tag[1]->strings;
	$st=&$this->tag[2]->strings;
    $t=&$this->tag[0]->strings[0];
    $name=$t->string[$t->off];
	$n = $this->tag[2]->n;
	$ar = array();
	for($i=0; $i<$n; $i++) {
	  $ar[$pt[$i]->getString()] = $st[$i]->getString();
	}
	$this->handler->begin($name, $ar);
  }
  function CALL_END() {
    $t=&$this->tag[0]->strings[0];
    $name=$t->string[$t->off];
	$this->handler->end($name);
  }
  function PUSH($t) { 
	$str = &$this->tag[$t]->strings;
	$n = &$this->tag[$t]->n;
	$this->s[$this->i]='|';
	if(!isset($str[$n])){$h=$this->handler; $str[$n]=$h->createParserString();}
	$str[$n]->string=&$this->s;
	$str[$n]->off=$this->i0;
	$str[$n]->length=$this->i-$this->i0;
	++$this->tag[$t]->n;
	$this->APPEND('|');
	$this->i0=$this->i;
  }
  function parse() {
	while($this->eor==0) {
	  if($this->c>=$this->pos) { 
		
		$this->buf=$this->handler->read(JAVA_RECV_SIZE); 
		if(is_null($this->buf) || strlen($this->buf) == 0) 
		  $this->handler->protocol->handler->shutdownBrokenConnection("protocol error. Check the back end log for OutOfMemoryErrors.");
		$this->pos=strlen($this->buf);
		if($this->pos==0) break;
		$this->c=0; 
	  }
	  switch(($ch=$this->buf[$this->c])) 
		{/* --- This block must be compilable with an ansi C compiler or javac --- */
		case '<': if($this->in_dquote) {$this->APPEND($ch); break;}
		  $this->level+=1;
		  $this->type=$this->BEGIN;
		  break;
		case '\t': case '\f': case '\n': case '\r': case ' ': if($this->in_dquote) {$this->APPEND($ch); break;}
		  if($this->type==$this->BEGIN) {
			$this->PUSH($this->type); 
			$this->type = $this->KEY; 
		  }
		  break;
		case '=': if($this->in_dquote) {$this->APPEND($ch); break;}
		  $this->PUSH($this->type);
		  $this->type=$this->VAL;
		  break;
		case '/': if($this->in_dquote) {$this->APPEND($ch); break;}
		  if($this->type==$this->BEGIN) { $this->type=$this->END; $this->level-=1; }
		  $this->level-=1;
		  $this->eot=true; // used for debugging only
		  break;
		case '>': if($this->in_dquote) {$this->APPEND($ch); break;}
		  if($this->type==$this->END){
			$this->PUSH($this->BEGIN);
			$this->CALL_END();
		  } else {
			if($this->type==$this->VAL) $this->PUSH($this->type);
			$this->CALL_BEGIN();
		  }
		  $this->tag[0]->n=$this->tag[1]->n=$this->tag[2]->n=0; $this->i0=$this->i=0;      		/* RESET */
		  $this->type=$this->VOJD;
		  if($this->level==0) $this->eor=1; 
		  break;
		case ';':
		  if($this->type==$this->ENTITY) {
			switch ($this->s[$this->e+1]) {
			case 'l': $this->s[$this->e]='<'; $this->i=$this->e+1; break; /* lt */
			case 'g': $this->s[$this->e]='>'; $this->i=$this->e+1; break; /* gt */
			case 'a': $this->s[$this->e]=($this->s[$this->e+2]=='m'?'&':'\''); $this->i=$this->e+1; break; /* amp, apos */
			case 'q': $this->s[$this->e]='"'; $this->i=$this->e+1; break; /* quot */
			default: $this->APPEND($ch);
			}
			$this->type=$this->VAL; //& escapes may only appear in values
		  } else {
			$this->APPEND($ch);
		  }
		  break;
		case '&': 
		  $this->type = $this->ENTITY;
		  $this->e=$this->i;
		  $this->APPEND($ch);
		  break;
		case '"':
		  $this->in_dquote = !$this->in_dquote;
		  if(!$this->in_dquote && $this->type==$this->VAL) {
			$this->PUSH($this->type);
			$this->type = $this->KEY;
		  }
		  break;
		default:
		  $this->APPEND($ch);
		} /* ------------------ End of ansi C block ---------------- */
	  $this->c+=1;
	}
   	$this->RESET();
  }

  function getData($str) {
	return $str;
  }

  function parserError() {
	$this->handler->protocol->handler->shutdownBrokenConnection(
        sprintf("protocol error: %s. Check the back end log for details.", $this->s));
  }
}
?>
