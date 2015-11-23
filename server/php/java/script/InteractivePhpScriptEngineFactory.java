/*-*- mode: Java; tab-width:8 -*-*/
package php.java.script;

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

import javax.script.ScriptEngine;

/**
 * Create a standalone interactive PHP script engines.
 */

public class InteractivePhpScriptEngineFactory extends InvocablePhpScriptEngineFactory {

    protected class Factory extends PhpScriptEngineFactory.Factory {
	public Factory(boolean hasCloseable) {
	    super(hasCloseable);
        }

	public ScriptEngine create () {
	    if (hasCloseable) {
		return new CloseableInteractivePhpScriptEngine(InteractivePhpScriptEngineFactory.this);
	    }
	    else {
		return new InteractivePhpScriptEngine(InteractivePhpScriptEngineFactory.this);
	    }
	}
    }
    
    /**
     * Create a new EngineFactory
     */
    public InteractivePhpScriptEngineFactory () {
	try {
	    Class.forName("java.io.Closeable");
	    factory = new Factory(true);
	} catch (ClassNotFoundException e) {
	    factory = new Factory(false);
	}
    }


  /**{@inheritDoc}*/
  public String getLanguageName() {
    return "php-interactive";
  }
}
