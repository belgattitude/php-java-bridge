#!/usr/bin/php

<?php
include_once ("java/Java.inc");

$name=java_get_server_name();

if(!$name) {
  echo "No servers available, please start one.\n";
} else {
  echo "connected to the server: $name\n";
}
?>
