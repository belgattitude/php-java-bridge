<?php require_once("java/Java.inc");
$session = java_session();

/* The name of the remote document */
$name = "RMIdocument";

/* continue session? */
if(!java_values($doc=$session->get("$name")))
  $session->put("$name", $doc=createDocument("$name", array()));

try {
  /* add pages to the remote document */
  $doc->addPage(new java("Page", 0, "this is page 1"));
  $doc->addPage(new java("Page", 0, "this is page 2"));

  /* and print a summary */
  print java_values($doc->analyze()) . "\n";
} catch (JavaException $ex) {
  $cause = $ex->getCause();
  echo "Could not access remote document. <br>\n";
  echo "$cause <br>\nin file: {$ex->getFile()}<br>\nline:{$ex->getLine()}\n";
  $session->destroy();
  exit (2);
}
/* destroy the remote document and remove the session */
if($_GET['logout']) {
  echo "bye...\n";
  destroyDocument($doc);
  $session->destroy();
 }

/* Utility procedures */

/*
 * convenience function which connects to the AS server using the URL
 * $url, looks up the service $jndiname and returns a new remote
 * document.
 * @param jndiname The name of the remote document, see sun-ejb-jar.xml
 * @param serverArgs An array describing the connection parameters.
 */
function createDocument($jndiname, $serverArgs) {
  // find initial context
  $initial = new java("javax.naming.InitialContext", $serverArgs);
  
  try {
    // find the service
    $objref  = $initial->lookup("$jndiname");
    
    // access the home interface
    $home = java("javax.rmi.PortableRemoteObject")->narrow($objref, java("DocumentHome"));
    if(java_is_null($home)) throw new Exception("home");

    // create a new remote document and return it
    $doc = $home->create();
  } catch (JavaException $ex) {
    $cause = $ex->getCause();
    echo "Could not create remote document. Have you deployed documentBean.jar?<br>\n";
    echo "$cause <br>\nin file: {$ex->getFile()}<br>\nline:{$ex->getLine()}\n";
    exit (1);
  }
  
  return $doc;
}

/*
 * convenience function which destroys the reference to the remote
 * document
 * @param The remote document.
 */
function destroyDocument($doc) {
  $doc->remove();
}

?>
