<?php include_once("java/Java.inc");

$here=getcwd();
java_require("$here");

class cg {
  function toString() {return "cg";}
}
class TestClosure {
  function f() { return new java("java.lang.String", "hello"); }
}
class TestClosure1 {
  function g() { return new cg();  }
}
$c1 = java_closure(new TestClosure(), null, java("TestClosure"));
echo $c1->f();

$c1 = java_closure(new TestClosure1(), null, java("TestClosure1"));
echo $c1->g();

?>
