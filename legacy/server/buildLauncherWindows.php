<?php

function createOutfile($name, $path) {
  $filePath = "${path}/${name}.java";
  $file = fopen($filePath, "wb") or die("fopen");
  $str =<<<HEAD_OUTFILE
package io.soluble.pjb.bridge;
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

$file = fopen($argv[2], "rb") or die("fopen");
for ($byte=fread($file,1); !feof($file); $byte=fread($file, 1)) {
  if ($counter%FILE_LENGTH==0) {
    if(isset($outfile)) finishOutfile($outfile);
    $outfile = createOutfile($names[(int)($counter/FILE_LENGTH)], $argv[1]);
    $linectr = 1;
  }
  fprintf($outfile, "(byte)0%o,%s", ord($byte), $linectr%16?" ":"\n");

  $linectr++;
  $counter++;
}
finishOutfile($outfile);
fclose($file) or die("fclose");
?>