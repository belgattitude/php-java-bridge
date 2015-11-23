/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

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
 * A cloneable CompiledScript
 */
public class CompiledPhpScript extends CompiledScript implements CloneableScript {
    /**
     * 
     */
    private final AbstractPhpScriptEngine abstractPhpScriptEngine;
    protected CompiledPhpScript(AbstractPhpScriptEngine abstractPhpScriptEngine) {
        super();
        this.abstractPhpScriptEngine = abstractPhpScriptEngine;
    }
    /** {@inheritDoc} */
    public Object eval(ScriptContext context) throws ScriptException {
        try {
    	return this.abstractPhpScriptEngine.evalCompiledPhp(AbstractPhpScriptEngine.DUMMY_READER, context);
        } catch (Exception e) {
    	throw new ScriptException(e);
        }
    }
    /** {@inheritDoc} */
    public ScriptEngine getEngine() {
        return this.abstractPhpScriptEngine;
    }
    /** {@inheritDoc} */
    public Object clone() {
        return new CompiledPhpScript((AbstractPhpScriptEngine) this.abstractPhpScriptEngine.clone());
    }
}