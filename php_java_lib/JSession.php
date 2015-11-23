<?php

/**
 * The JSessionAdapter makes it possible to store java values into the
 * $_SESSION variable. 
 *
 * Example:
 * $vector = new JSessionAdapter(new Java("java.util.Vector"));
 * $vector->addElement(...);
 * $_SESSION["v"]=$vector;
 */


class JSessionProxy {
  var $java;
  var $serialID;

  function __construct($java){ 
    $this->java=$java; 
    $this->serialID = uniqid(""); 
  }
  function __sleep() {
    $session=java_get_session("PHPSESSION".session_id());
    $session->put($this->serialID, $this->java);
    return array("serialID");
  }
  function __wakeup() {
    $session=java_get_session("PHPSESSION".session_id());
    $this->java = $session->get($this->serialID);
  }
  function getJava() {
    return $this->java;
  }
  function __destruct() { 
    if($this->java) return $this->java->__destruct(); 
  }
}

class JSessionAdapter extends JSessionProxy {
  function __get($arg)       { if(!is_null($this->java)) return $this->java->__get($arg); }
  function __set($key, $val) { if(!is_null($this->java)) return $this->java->__set($key, $val); }
  function __call($m, $a)    { if(!is_null($this->java)) return $this->java->__call($m,$a); }
  function __toString()      { if(!is_null($this->java)) return $this->java->__toString(); }
}

?>
