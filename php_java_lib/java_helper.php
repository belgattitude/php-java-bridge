<?

/*
* This file contains various helper functions and classes for usage with the php-java bridge
* See also: http://sourceforge.net/projects/php-java-bridge
*/

// Dumps a very detailed error message for a given java_exception
function dump_java_exception($ex) {
   $trace = new java("java.io.ByteArrayOutputStream");
   echo "Java Exception in File '".$ex->getFile()."' Line:". $ex->getLine(). " - Message: " .$ex->getCause()->toString();
   $cause = $ex->getCause();
   $cause->printStackTrace(new java("java.io.PrintStream", $trace));
   echo "<PRE>Java Stack Trace:\n".$trace->toString()."\n</PRE>";
}

/**
 * Marks a given classpath as invalid, i.E. modified - which causes that all cached classes are
 * removed from the cache.
 *
 * Note, that this does not mean they are unloaded. Old versions might still be in use if they
 * are referenced from elsewhere. But new instantiations will use the fresh classes
 *
 * Usage of method is useless if the Classloader is not Dynamic
 */
function java_invalidate_library_path($cp) {
	$DC = new JavaClass('php.java.bridge.DynamicClassLoader');
	$DC->invalidate($cp);
}

?>