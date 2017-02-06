<?php
// The following is the "Java" class definition, stripped down to fit
// into one line<br To use this sample start Java with: java -jar
// JavaBridge.jar INET:9267<br> Or enable java.so or php_java.dll,
// which automatically start the above<br> process. Then
// type: php sample.php<br>
//
class P {const Pc="<C v=\"%s\" p=\"I\">", PC="</C>"; const Pi="<I v=\"%d\" m=\"%s\" p=\"I\">", PI="</I>"; const Ps="<S v=\"%s\"/>", Pl="<L v=\"%d\" p=\"%s\"/>", Po="<O v=\"%d\"/>"; private $c; function str($s){fwrite($this->c, sprintf(self::Ps, $s));} function obj($s){fwrite($this->c, sprintf(self::Po, $s->java));} function __construct(){$this->c=fsockopen("127.0.0.1",9267);} function cBeg($s){fwrite($this->c, sprintf(self::Pc, $s));} function cEnd(){fwrite($this->c, self::PC);} function iBeg($o, $m){fwrite($this->c, sprintf(self::Pi, $o, $m));} function iEnd(){fwrite($this->c, self::PI);} function val($s){if(is_object($s))$this->obj($s);else $this->str((string)$s);} function res(){$r=sscanf(fread($this->c, 8192),"%s v=\"%[^\"]\"");return $r[1];}} function gP() {static $p; if(!$p) $p=new P(); return $p;} class Java {var $java, $p; function __construct() {if(!func_num_args()) return; $this->p=gP(); $ar=func_get_args(); $this->p->cBeg(array_shift($ar)); foreach($ar as $arg) $this->p->val($arg); $this->p->cEnd(); $ar = sscanf($this->p->res(), "%d"); $this->java=$ar[0];} function __call($meth, $args) {$this->p->iBeg($this->java, $meth); foreach($args as $arg) $this->p->val($arg); $this->p->iEnd(); $proxy = new Java(); $ar = sscanf($this->p->res(), "%d"); $proxy->java=$ar[0]; $proxy->p=$this->p; return $proxy;} function toString() {$this->p->iBeg("", "castToString"); $this->p->val($this); $this->p->iEnd(); return base64_decode($this->p->res());}}

// Test
$i1 = new Java("java.math.BigInteger",  "1");
$i2 = new Java("java.math.BigInteger",  "2");
$i3 = $i1->add($i2);
echo $i3->toString() . "\n";

?>
