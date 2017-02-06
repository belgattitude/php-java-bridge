<?
/*
* This file contains various methods to compile and load a java project
* tree from java, using "Jikes" the free java compiler and the PHP-Java-Bridge
*
*
*  See also:
*  PHP-Java Bridge - http://sourceforge.net/projects/php-java-bridge
*  Jikes Java Compiler - http://sourceforge.net/projects/jikes/
*
*  IMPORTANT: a jikes executable is neccessary for this to work.
*  Download from http://sourceforge.net/projects/jikes/
*
*  Jikes needs to be installed either on the Command-Path, or in the same directory as this
*  file in order to work
*
* Author: Kai Londenberg (kl@librics.de / http://www.librics,de/)
*
*/

require_once('java_helper.php');

/**
* Finds all files matching a given pattern in a given directory.
*/
function dir_pattern_scan($dir,$pattern,$separator,$depth) {
  	if ($depth<0) return;
  	$oldcwd = getcwd();
  	$result = array();
  	chdir($dir);
  	$dh = opendir($dir);
  	while ($fname= readdir($dh)) {
  		if (($fname!='.') && ($fname!='..')) {
  			if (is_dir($dir.$separator.$fname)) {
  			    $preres = dir_pattern_scan($dir.$separator.$fname,$pattern,$separator, $depth-1);
  			    if (is_array($preres)) {
  					$result = array_merge($result,$preres);
  				}
  			} else {
  				$fpath = $dir.$separator.$fname;
  				if (eregi($pattern, $fpath)) {
  					$result[] = $dir.$separator.$fname;
  				}
  			}
  		}
  	}
  	closedir($dh);
  	chdir($oldcwd);
  	return $result;
}

// Ensures a given path exists
if (!function_exists("mkpath")) {
	  function mkpath($path,$mode=0700) {
	  	$dirs = explode('/',$path);
	  	$path = '';
	  	for($i = 0;$i < count($dirs);$i++) {
	  	if ($i>0) $path.='/';
	        $path .= $dirs[$i];
	        if ($path=='') continue;
	          if(!is_dir($path)) {
	        	@mkdir($path,$mode);
	          }
	    }
   }
}

// Removes source files form the list which have not been modified since the last compilation
function filter_unmodified(&$files, $src_dir, $class_dir) {
	$result = array();
	$pl = strlen($src_dir); // Cut from start of filename
	$el = -strlen(".java"); // Cut from end of filename
	foreach ($files as $src_file) {
		$class_file = $class_dir.substr($src_file, $pl, $el).'.class';
		if (!file_exists($class_file)) {
			$result[] = $src_file;
			continue;
		}
		if (filemtime($src_file)>filemtime($class_file)) { // Compare modification dates
			$result[] = $src_file;
		}
	}
	return $result;
}

// Self explaining
class JavaCompilationException extends Exception {
	public $command;
	public $output;
	public $retval;

	public function __construct($command, $output, $retval) {
		$this->command = $command;
		$this->output = $output;
		$this->retval = $retval;
	}

	public function __toString() {
		return "<br><PRE>".$this->toString()."</PRE><br>";
	}

	public function toString() {
		return "Java Compilation Exception:\nCommand: '$this->command'\nOutput:\n$this->output\n\nReturn value: $this->retval";
	}
}

// Compiles all java files in a given directory, and writes them to the target directory
// if $verbose is true, the error messages also include detailed progress info.
// This method might throw a JavaCompilationException
function jikes_compile($src_path, $target_path, $verbose, $extra_classpath='') {
		// Initialize path to jikes. First assume jikes is in the same directory as this script
		$jikes_pp = explode('/',__FILE__);
		$jikes_pp[count($jikes_pp)-1]='jikes';
		$jikes_cmd = implode('/', $jikes_pp);
		if (!file_exists($jikes_cmd)) {
			$jikes_cmd = $jikes_cmd.'.exe';
			if (!file_exists($jikes_cmd)) {
				$jikes_cmd = 'jikes'; // Try path
			}
		}
		$src_path = realpath($src_path);
		mkpath($target_path, 0750);
		$target_path=realpath($target_path);
		$System = new JavaClass("java.lang.System");
		$Properties = $System->getProperties();
		$java_home = $Properties->get("java.home");
		$classpath = $Properties->get("java.class.path");
		if ($extra_classpath!='') {
			$classpath .= ":$extra_classpath";
		}


		$bootclasspath = $Properties->get("sun.boot.class.path");
		$target_version = $Properties->get("java.specification.version");
        clearstatcache(); // Neccessary, otherwise file modification dates and calls to "file_exists" get cached.
        $files = dir_pattern_scan($src_path, "\.java$", '/', 6);
		$files = filter_unmodified($files, $src_path, $target_path);
		if (count($files)==0) {
			return 0;
		}
		$filearg = implode(' ', $files);
		$output = array();
		if ($verbose) {
			$command  = "$jikes_cmd -Xstdout -verbose ";
		} else {
			$command  = "$jikes_cmd -Xstdout ";
		}
		$command .= "-bootclasspath $bootclasspath -classpath $classpath:$target_path -target $target_version -source $target_version ";
		$command .= "-sourcepath $src_path -d $target_path $filearg";
		$retval = -99999;
		exec($command, $output, $retval);
		if ($retval==126) throw new JavaCompilationException($command, "No execute permission for '$jikes_cmd'", $retval);
		if ($retval==127) throw new JavaCompilationException($command, "Jikes Executable '$jikes_cmd' not found", $retval);

		if ($retval!=0) throw new JavaCompilationException($command, implode("\n", $output), $retval);
		return count($files);
}

/**
* Provides a jikes based automatic build system using jikes and the php java bridge
* Just call this method at the start of your script, and it will ensure that
* all modified java files will be compiled, and the neccessary libraries are
* in your classpath.
* Params:
* $src_path - The base directory where the java files reside
* $target_path - [Optional] Target directory for class files
* $extra_classpath - [Optional] additional jar files / classpaths used in the project.
* Paths separated by ';'
*/
function java_autoproject($src_path, $target_path='', $extra_classpath='', $extra_lib_dirs='') {
	if ($target_path=='') $target_path=$src_path;
	$compiled_count = 0;
	$ecp = '';
	if ($extra_classpath!='') {
		$ec = explode(';', $extra_classpath);
		foreach($ec as $cp) {
			$cpl = '';
			$cpl = @realpath($cp);
			if ($cpl=='') $cpl = getcwd().'/'.$cp;
			$ecp .= $cpl.':';
		}
	}
	if ($extra_lib_dirs!='') {
		$libdirs = explode(';',$extra_lib_dirs);
		foreach ($libdirs as $libdir) {
		    $glibdir='';
			$glibdir = @realpath($libdir);
			if ($glibdir=='') $glibdir = getcwd().'/'.$libdir;
			$ecp .= ':'.implode(':',dir_pattern_scan($glibdir, '\.jar$', '/', 1));
		}
	}
	if ($ecp!='') {
		$extra_classpath = strtr($ecp, ':;', ';;');
	}
	if ($extra_classpath!='') {
		$cp = $target_path.";$extra_classpath";
	} else {
		$cp = $target_path;
	}
	try {
		$compiled_count = jikes_compile($src_path, $target_path, true, $ecp, $extra_lib_dirs);

		if ($compiled_count>0) {
     		java_invalidate_library_path($cp);
		}
	} catch (JavaCompilationException $jce) {
		echo $jce;
	}
	java_require($cp);
	return $compiled_count;
}


?>