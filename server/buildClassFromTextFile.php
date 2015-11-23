<?php

$s = preg_replace('/\\\\/', '\\\\\\\\', preg_replace('/\r/', '', file_get_contents($argv[3])));
$s = preg_replace('/"/', '\\\\"', $s);
$s = preg_replace('/^.*$/m', '"${0}\\\\n"+', $s);
$s.='""';

$str=<<<EOF
package php.java.bridge;
public class $argv[2] {
    private static final String data = ${s};
    public static final byte[] bytes = data.getBytes(); 
}

EOF;

file_put_contents($argv[1], $str);

?>
