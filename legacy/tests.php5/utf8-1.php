#!/usr/bin/php

# For the following test start the java VM with: 
# java -Dfile.encoding=ASCII -jar JavaBridge.jar INET:0 4 ""
# and run the test as follows:
# php utf8-1.inc | grep '^Gr' | od -c
# =>
# 0000000   G   r 303 274 303 237       G   o   t   t  \n   G   r   ?   ?
# 0000020       G   o   t   t  \n   G   r 374 337       G   o   t   t  \n



<?php
     include_once ("java/Java.inc");

java_set_file_encoding("ISO-8859-1");

// the following is rewritten into new String(..., "ISO-8859-1");
$string = new Java("java.lang.String", "Grüß Gott"); 

// decode string into UTF-8
print java_values($string->getBytes("UTF-8")) . "\n";

// decode string using System.getProperty("file.encoding");
print java_values($string->getBytes()) . "\n";

// decode string using file encoding
print java_values($string->toString()) . "\n";


// the following is rewritten into new String(..., "ISO-8859-1");
$string = new Java("java.lang.String", "Grüß Gott", 0, 9); 

// decode string into UTF-8
print java_values($string->getBytes("UTF-8")) . "\n";

// decode string using System.getProperty("file.encoding");
print java_values($string->getBytes()) . "\n";

// decode string using file encoding
print java_values($string->toString()) . "\n";


?>
