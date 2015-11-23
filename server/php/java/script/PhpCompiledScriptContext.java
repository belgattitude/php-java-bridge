/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script;

import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

import php.java.bridge.ILogger;
import php.java.bridge.http.HeaderParser;

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
 * A decorator for compiled script engines. 
 * Only for internal use.
 * @author jostb
 *
 */
public class PhpCompiledScriptContext extends PhpScriptContextDecorator {

    /**
     * Create a new PhpCompiledScriptContext using an existing
     * PhpScriptContext
     * @param ctx the script context to be decorated
     */
    public PhpCompiledScriptContext(IPhpScriptContext ctx) {
	super(ctx);
    }
    /**{@inheritDoc}*/
    public Continuation createContinuation(Reader reader, Map env,
            OutputStream out, OutputStream err, HeaderParser headerParser, ResultProxy result,
            ILogger logger, boolean isCompiled) {
		Continuation cont;
		if (isCompiled) {
		    cont = new FastCGIProxy(reader, env, out,  err, headerParser, result, logger);
		} else {
		    cont = super.createContinuation(reader, env, out, err, headerParser, result, logger, isCompiled);
		}
    		return cont;
    }
}
