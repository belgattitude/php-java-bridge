<?php
 
function createOutfile($name) {
  $file = fopen("php/java/bridge/${name}.java", "wb") or die("fopen");
  $str =<<<HEAD_OUTFILE
package php.java.bridge;
public class ${name} {
  public static final byte[] bytes = new byte[]{

HEAD_OUTFILE;
  fwrite($file, $str);
  return $file;
}
function finishOutfile($file) {
  $str =<<<TAIL_OUTFILE
  };
}

TAIL_OUTFILE;
  fwrite($file, $str);
  fclose($file) or die("fclose");
}

define ("FILE_LENGTH", 8192);
$names = array("LauncherWindows", "LauncherWindows2",
	       "LauncherWindows3","LauncherWindows4");
$counter = 0;
$linectr = 1;

$file = fopen($argv[1], "rb") or die("fopen");
for ($byte=fread($file,1); !feof($file); $byte=fread($file, 1)) {
  if ($counter%FILE_LENGTH==0) {
    if(isset($outfile)) finishOutfile($outfile);
    $outfile = createOutfile($names[(int)($counter/FILE_LENGTH)]);
    $linectr = 1;
  }
  fprintf($outfile, "(byte)0%o,%s", ord($byte), $linectr%16?" ":"\n");

  $linectr++;
  $counter++;
}
finishOutfile($outfile);
fclose($file) or die("fclose");
?>