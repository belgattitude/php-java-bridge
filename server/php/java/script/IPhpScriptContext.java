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

import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

import javax.script.ScriptContext;

import php.java.bridge.ILogger;
import php.java.bridge.IManaged;
import php.java.bridge.Invocable;
import php.java.bridge.http.ContextServer;
import php.java.bridge.http.HeaderParser;
import php.java.bridge.http.IContext;

/**
 * Common methods for all PHP ScriptContexts
 * 
 * @author jostb
 *
 */
public interface IPhpScriptContext extends IManaged, Invocable, IContext, ScriptContext {
       
    /**
     * Set the php continuation
     * @param kont The continuation.
     */
    public void setContinuation(Continuation kont);
    /**
     * Get the php continuation
     * @return The HttpProxy
     */
    public Continuation getContinuation();

    /**
     * Create a continuation
     * @param reader the script reader
     * @param env the environment passed to php
     * @param out the fcgi output stream
     * @param err the fcgi error stream
     * @param headerParser fcgi header parser
     * @param result the result proxy
     * @param logger the logger
     * @param isCompiled create a continuation for a compiled or non-compiled script engine
     * @return the Continuation
     */
    public Continuation createContinuation(Reader reader, Map env, OutputStream out, OutputStream err, HeaderParser headerParser, ResultProxy result, ILogger logger, boolean isCompiled);
    
    /**
     * Start the current continuation using a context-specific thread pool
     */
    public void startContinuation();
    
    /**
     * Get the context server associated with this context, usually a HttpServer (JavaBridgeRunner) or a ContextServer from a ContextLoaderListener
     * @return the ContextServer
     */
    public ContextServer getContextServer();
}
