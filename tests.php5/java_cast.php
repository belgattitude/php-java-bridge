<?php

include_once ("java/Java.inc");

is_string(java_cast(1, "string")) || die("string");
is_long(java_cast("1", "long")) || die("long");
is_double(java_cast(1, "double")) || die("double");
is_bool(java_cast(1, "boolean")) || die("boolean");
is_object(java_cast(1, "object")) || die("object");
is_array(java_cast(1, "array")) || die("array");

echo "test okay\n";

?>
