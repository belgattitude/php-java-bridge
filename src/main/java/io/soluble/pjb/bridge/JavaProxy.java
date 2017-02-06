package php.java.bridge;
public class JavaProxy {
    private static final String data = "<?php \n"+
"/* wrapper for Java.inc */ \n"+
"\n"+
"if(!function_exists(\"java_get_base\")) require_once(\"Java.inc\"); \n"+
"\n"+
"if ($java_script_orig = $java_script = java_getHeader(\"X_JAVABRIDGE_INCLUDE\", $_SERVER)) {\n"+
"\n"+
"  if ($java_script!=\"@\") {\n"+
"    if (($_SERVER['REMOTE_ADDR']=='127.0.0.1') || (($java_script = realpath($java_script)) && (!strncmp($_SERVER['DOCUMENT_ROOT'], $java_script, strlen($_SERVER['DOCUMENT_ROOT']))))) {\n"+
"      chdir (dirname ($java_script));\n"+
"      require_once($java_script);\n"+
"    } else {\n"+
"      trigger_error(\"illegal access: \".$java_script_orig, E_USER_ERROR);\n"+
"    }\n"+
"  }\n"+
"\n"+
"  java_call_with_continuation();\n"+
"}\n"+
"?>\n"+
"";
    public static final byte[] bytes = data.getBytes(); 
}
