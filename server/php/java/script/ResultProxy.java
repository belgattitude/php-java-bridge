/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script;

import java.io.IOException;

import php.java.bridge.Util;

/*
 * Copyright (C) 2003-2007 Jost Boekemeier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER(S) OR AUTHOR(S) BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * Returned by {@link javax.script.ScriptEngine#eval(java.io.Reader)} this class holds a proxy
 * for the result code returned by PHP. Invoke any of its procedures to terminate the script
 * engine to receive its result code.
 * @author jostb
 */
public class ResultProxy extends Number {
    private static final long serialVersionUID = 9126953496638654790L;
    private int result;
    private IPhpScriptEngine engine;
    ResultProxy(IPhpScriptEngine engine) {
	this.engine = engine;
    }
    void setResult(int result) {
	this.result = result;
    }
    /**
     * Release the script engine and return the result code
     * @return the result code from PHP
     */
    public int getResult() {
	engine.release();
	return result;
    }
    /**
     * Release the script engine and return the result code
     * @return the result code from PHP
     */
    public String toString() {
	if (Util.logLevel>3) return "DEBUG WARNING: toString() did not terminate script because logLevel > 3!";
	return String.valueOf(getResult());
    }

    /**
     * Release the script engine and return the result code
     * @return the result code from PHP
     */
    public double doubleValue() {
	return (double)getResult();
    }

    /**
     * Release the script engine and return the result code
     * @return the result code from PHP
     */
    public float floatValue() {
	return (float)getResult();
    }

    /**
     * Release the script engine and return the result code
     * @return the result code from PHP
     */
    public int intValue() {
	return (int)getResult();
    }

    /**
     * Release the script engine and return the result code
     * @return the result code from PHP
     */
    public long longValue() {
	return (long)getResult();
    }

    /**
     * Release the script engine
     * @throws IOException 
     */
    public void close() throws IOException {
	engine.release();
    }
}
