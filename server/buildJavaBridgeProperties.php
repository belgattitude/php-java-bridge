<?php
$version = trim(file_get_contents("../VERSION"));
file_put_contents($argv[1], preg_replace("|@BACKEND_VERSION@|", $version, file_get_contents($argv[2])));
?>
