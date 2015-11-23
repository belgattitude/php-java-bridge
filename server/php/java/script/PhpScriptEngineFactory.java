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
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import php.java.bridge.Util;

/**
 * Create a standalone PHP script engines.
 */
public class PhpScriptEngineFactory implements ScriptEngineFactory {

    protected class Factory {
	protected boolean hasCloseable;
	public Factory (boolean hasCloseable) {
	    this.hasCloseable = hasCloseable;
	}
	public ScriptEngine create () {
	    if (hasCloseable) {
		return new CloseablePhpScriptEngine(PhpScriptEngineFactory.this);	    }
	    else {
		return new PhpScriptEngine(PhpScriptEngineFactory.this);
	    }
	}
    }
    protected Factory factory;
    
    /**
     * Create a new EngineFactory
     */
    public PhpScriptEngineFactory () {
	try {
	    Class.forName("java.io.Closeable");
	    factory = new Factory(true);
	} catch (ClassNotFoundException e) {
	    factory = new Factory(false);
	}
    }
    private static final String ENGINE_NAME=Util.EXTENSION_NAME + " php script engine for Java";
    /**{@inheritDoc}*/
    public String getEngineName() {
	return ENGINE_NAME;
    }

    /**{@inheritDoc}*/
    public String getEngineVersion() {
	return Util.VERSION;
    }

    /**{@inheritDoc}*/
    public String getLanguageName() {
	return "php";
    }

    /**{@inheritDoc}*/
    public String getLanguageVersion() {
	return "6";
    }

    /**{@inheritDoc}*/
    public List getExtensions() {
	return getNames();
    }

    /**{@inheritDoc}*/
    public List getMimeTypes() {
	return Arrays.asList(new String[]{});
    }
    List names;
    /**{@inheritDoc}*/
    public List getNames() {
	if(names==null) names = Arrays.asList(new String[]{getLanguageName(), "phtml", "php4", "php5", "php6"});
	return names;
    }

    /**{@inheritDoc}*/
    public ScriptEngine getScriptEngine() {
	return factory.create();
    }

    /**{@inheritDoc}*/
    public Object getParameter(String key) {
        if(key.equals("javax.script.name"))
            return getLanguageName();
        if(key.equals("javax.script.engine"))
            return getEngineName();
        if(key.equals("javax.script.engine_version"))
            return getEngineVersion();
        if(key.equals("javax.script.language"))
            return getLanguageName();
        if(key.equals("javax.script.language_version"))
            return getLanguageVersion();
        if(key.equals("THREADING"))
            return "STATELESS";
        else
            throw new IllegalArgumentException("key");
    }

    /**{@inheritDoc}*/
    public String getMethodCallSyntax(String obj, String m, String[] args) {
	StringBuffer b = new StringBuffer();
	b.append("$");
	b.append(obj);
	b.append("->");
	b.append(m);
	b.append("(");
	int i;
	for(i=0; i<args.length-1; i++) {
	    b.append(args[i]);
	    b.append(",");
	}
	b.append(args[i]);
	b.append(")");
	return b.toString();
    }

    /**{@inheritDoc}*/
    public String getOutputStatement(String toDisplay) {
	return "echo("+toDisplay+")";
    }

    /**{@inheritDoc}*/
    public String getProgram(String[] statements) {
	int i=0;
	StringBuffer b = new StringBuffer("<?php ");
      
	for(i=0; i<statements.length; i++) {
	    b.append(statements[i]);
	    b.append(";");
	}
	b.append("?>");
	return b.toString();
    }
}
