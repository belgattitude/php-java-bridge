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

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;

/**
 * Abstract class for ScriptContexts. The abstract class itself provides default methods that pass 
 * all requests to the contained ScriptContext. Subclasses of ScriptContextDecorator should override some of
 * these methods and may also provide additional methods and fields. 
 * 
 * @author jostb
 */
public abstract class ScriptContextDecorator implements ScriptContext {

    protected ScriptContext ctx;

    public ScriptContextDecorator(ScriptContext ctx) {
	this.ctx = ctx;
    }
    /**{@inheritDoc}*/
    public Object getAttribute(String name) throws IllegalArgumentException {
	return ctx.getAttribute(name);
    }

    /**{@inheritDoc}*/
    public Object getAttribute(String name, int scope)
	    throws IllegalArgumentException {
	return ctx.getAttribute(name, scope);
    }

    /**{@inheritDoc}*/
    public int getAttributesScope(String name) {
	return ctx.getAttributesScope(name);
    }

    /**{@inheritDoc}*/
    public Bindings getBindings(int scope) {
	return ctx.getBindings(scope);
    }

    /**{@inheritDoc}*/
    public Writer getErrorWriter() {
	return ctx.getErrorWriter();
    }

    /**{@inheritDoc}*/
    public Reader getReader() {
	return ctx.getReader();
    }

    /**{@inheritDoc}*/
    public List getScopes() {
	return ctx.getScopes();
    }

    /**{@inheritDoc}*/
    public Writer getWriter() {
	return ctx.getWriter();
    }

    /**{@inheritDoc}*/
    public Object removeAttribute(String name, int scope) {
    	return ctx.removeAttribute(name, scope);
    }

    /**{@inheritDoc}*/
    public void setAttribute(String key, Object value, int scope)
	    throws IllegalArgumentException {
	ctx.setAttribute(key, value, scope);
    }

    /**{@inheritDoc}*/
    public void setBindings(Bindings namespace, int scope)
	    throws IllegalArgumentException {
	ctx.setBindings(namespace, scope);
    }

    /**{@inheritDoc}*/
    public void setErrorWriter(Writer writer) {
	ctx.setErrorWriter(writer);
    }

    /**{@inheritDoc}*/    
    public void setReader(Reader reader) {
	ctx.setReader(reader);
    }

    /**{@inheritDoc}*/    
    public void setWriter(Writer writer) {
	ctx.setWriter(writer);
    }
}
