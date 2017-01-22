/*-*- mode: Java; tab-width:8 -*-*/

package io.soluble.pjb.script;

import java.io.StringReader;

import javax.script.Invocable;
import javax.script.ScriptException;

/**
 * @author jostb
 *
 * Call this example with -Dfile.encoding=UTF-8
 */
public class TestUtf8 {

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
		int result = 0;

		System.setProperty("io.soluble.pjb.bridge.default_log_level", "4");
		System.setProperty("io.soluble.pjb.bridge.default_log_file", "");
		System.setProperty("io.soluble.pjb.bridge.php_exec", "php-cgi");

		String utf8 =
			"Cześć! -- שלום -- Grüß Gott -- Dobrý deň -- Dobrý den -- こんにちは, ｺﾝﾆﾁﾊ";

		String phpCode =
			"<?php \n" +
			"function toString() { \n" +
			"  $str=new java('java.lang.String','"+utf8+"'); \n" +
			"  echo $str; echo '\n'; \n" +
			"  return (string)$str; \n" +
			"} \n" +
			"java_context()->call(java_closure()); \n"+
			"?>";

		InvocablePhpScriptEngine engine = new InvocablePhpScriptEngine();
		StringReader reader = new StringReader(phpCode);
		engine.eval(reader);

		Invocable i = (Invocable)engine;
		String utf82 = i.getInterface(null).toString();

		if(!utf82.equals(utf8)) {
			System.out.println("ERROR");
			System.out.println(utf82);
			System.out.println(utf8);
			result = 1;
		}  else {
			System.out.println("test okay");
		}

		engine.release();
		System.exit(result);
    }
}
