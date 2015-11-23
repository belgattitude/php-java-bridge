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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;


/**
 * Create a standalone invocable PHP script engines.
 */
public class InvocablePhpScriptEngineFactory extends PhpScriptEngineFactory {

    protected class Factory extends PhpScriptEngineFactory.Factory {
	public Factory(boolean hasCloseable) {
	    super(hasCloseable);
        }

	public ScriptEngine create () {
	    if (hasCloseable) {
		return new CloseableInvocablePhpScriptEngine(InvocablePhpScriptEngineFactory.this);
	    }
	    else {
		return new InvocablePhpScriptEngine(InvocablePhpScriptEngineFactory.this);
	    }
	}
    }
    
    /**
     * Create a new EngineFactory
     */
    public InvocablePhpScriptEngineFactory () {
	try {
	    Class.forName("java.io.Closeable");
	    factory = new Factory(true);
	} catch (ClassNotFoundException e) {
	    factory = new Factory(false);
	}
    }

    /**{@inheritDoc}*/
  public String getLanguageName() {
    return "php-invocable";
  }

  private List names;
  /**{@inheritDoc}*/
  public List getNames() {
      if (names!=null) return names;
      return names=Arrays.asList(new String[]{getLanguageName()});
  }

  private static final List ext = Collections.unmodifiableList(new LinkedList());
  /**{@inheritDoc}*/
  public List getExtensions() {
      return ext;
    }
}
