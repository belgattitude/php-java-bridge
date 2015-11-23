<?php

/**
 * This test checks the JSR223 script API. The test must be run three
 * times, with the J2EE back end, the JavaBridgeRunner and standalone.
 *
 * In the J2EE environment the J2EE ContextRunner and ContextFactories
 * must not interfere with the ContextRunner and ContextFactories
 * created by the JavaBridgeRunner: check ContextRunner.runner, the
 * keys from the ContextServers (the part after the @...) must be
 * different, check if X_JAVABRIDGE_OVERRIDE_HOSTS works, so that PHP
 * scripts started by the JavaBridgeRunner connect back to the
 * JavaBridgeRunner and not to the default J2EE ContextRunner.
 *
 * When the JavaBridgeRunner is used, there must be exactly one
 * JavaBridge runner with 6 ContextRunner and ContextFactories.
 *
 * In the standalone setup there must be 6 ContextRunner and
 * ContextFactories and 5 CGIRunners.
 */

include_once("java/Java.inc");
/*
 * Call the Java continuation from the PHP continuation. If that
 * failed, start main().
 */
$IRunnable = java("java.lang.Runnable");
if (!java_values(java_context()->getAttribute("name", 100))) main();
else java_context()->call(java_closure(new Runnable(), null, $IRunnable));

/**
 * This class implements IRunnable. Its run method is called by each
 * of the 5 Java threads. Each PHP thread writes $nr to the filename
 * $name.out.
 */
class Runnable {
  function run() {
    $name = java_values(java_context()->getAttribute("name", 100)); // engine scope
    $out = new Java("java.io.FileOutputStream", "$name.out", true);
    $Thread = java("java.lang.Thread");
    $nr = java_values(java_context()->getAttribute("nr", 100));
    echo "started thread: $nr\n";
    for($i=0; $i<10; $i++) {
      $out->write(ord("$nr"));
      $Thread->yield();
    }
    $out->close();
  }
}

/**
 * Allocate a thread and an associated PHP continuation 
 *
 * @arg nr The data will be passed to the PHP run method
 * @arg name The current script name
 * @return the PHP continuation.
 */
function createRunnable($nr, $name) {
  global $IRunnable;
  $r = new Java("php.java.script.InvocablePhpScriptEngine");
  $r->put("name", $name);
  $r->eval(new Java("java.io.FileReader", $name));
  $r->put("nr", $nr);
  $r->put("thread",new java("java.lang.Thread",$r->getInterface($IRunnable)));
  return $r;
}

/**
 * Creates and starts count PHP threads concurrently writing to
 * SCRIPTNAME.out
 */
function main() {
  $count = 5;
  if (array_key_exists("argv", $_SERVER)) {
    $argv = ($_SERVER['argv']); 
    $name = realpath($argv[0]);
  } else {
    $name = $_SERVER["SCRIPT_FILENAME"];
  }
  @unlink("${name}.out");
  $here = dirname($name);
  // make sure php-script.jar and script-api.jar are in the classpath
  //java_require("$here/../modules/php-script.jar;$here/../modules/script-api.jar");
  $runnables = array();
  for($i=1; $i<=$count; $i++) {
    $runnables[$i]=createRunnable($i, $name);
  }
  for($i=1; $i<=$count; $i++) {
    $runnables[$i]->get("thread")->start();
  }
  for($i=1; $i<=$count; $i++) {
    $runnables[$i]->get("thread")->join();
    $runnables[$i]->release(); // release the PHP continuation
  }
  $result = system('cat '.$name.'.out | sed "s/./&\n/g" |sort | uniq -c | tr -d " \n"');
  echo "\n";
  if($result!="101102103104105") die($result);

  echo "test okay\n";
}


?>
