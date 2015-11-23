<?php
define ("JAVA_PREFER_VALUES", true);
require_once ("java/Java.inc");

$system = new java("java.lang.System");
$t1=$system->currentTimeMillis();

$here=getcwd();
// load scheme interpreter
// try to load local ~/lib/kawa.jar, otherwise load it from sf.net
try { java_require("kawa.jar"); } catch (JavaException $e) {
  java_require("http://php-java-bridge.sourceforge.net/kawa.jar");
}

$s = new java("kawa.standard.Scheme");
for($i=0; $i<100; $i++) {
  $res=java_cast($s->eval("

(letrec
 ((f (lambda(v)
       (if 
	   (= v 0)
	   1
	 (*
	  (f
	   (- v 1))
	  v)))))
 (f $i))

"), "D");

  if($ex=java_last_exception_get()) $res=$ex->toString();
  java_last_exception_clear();
  echo "fact($i) ==> $res\n";
}
$t2=$system->currentTimeMillis();
$delta=($t2-$t1)/1000.0;
$now=new java("java.sql.Timestamp",$system->currentTimeMillis());
echo  "Evaluation took $delta s -- at: $now\n";
?>
