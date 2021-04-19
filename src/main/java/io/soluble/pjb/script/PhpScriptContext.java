/*-*- mode: Java; tab-width:8 -*-*/

package io.soluble.pjb.script;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import javax.script.ScriptContext;

import io.soluble.pjb.bridge.ILogger;
import io.soluble.pjb.bridge.JavaBridgeRunner;
import io.soluble.pjb.bridge.NotImplementedException;
import io.soluble.pjb.bridge.Util;
import io.soluble.pjb.bridge.http.ContextServer;
import io.soluble.pjb.bridge.http.HeaderParser;
import io.soluble.pjb.bridge.http.IContext;

/**
 * This class implements a simple script context for PHP. It starts a standalone
 * <code>JavaBridgeRunner</code> which listens for requests from php instances.<p>
 * <p>
 * In a servlet environment please use a <code>io.soluble.pjb.script.http.PhpSimpleHttpScriptContext</code> instead.
 *
 * @author jostb
 * @see io.soluble.pjb.script.PhpScriptContext
 * @see io.soluble.pjb.bridge.JavaBridgeRunner
 */

public final class PhpScriptContext extends AbstractPhpScriptContext {
    public PhpScriptContext(ScriptContext ctx) {
        super(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object init(Object callable) throws Exception {
        return io.soluble.pjb.bridge.http.Context.getManageable(callable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShutdown(Object closeable) {
        io.soluble.pjb.bridge.http.Context.handleManaged(closeable);
    }

    /**
     * Throws IllegalStateException
     *
     * @return none
     */
    @Override
    public Object getHttpServletRequest() {
        throw new IllegalStateException("PHP not running in a servlet environment");
    }

    /**
     * Throws IllegalStateException
     *
     * @return none
     */
    @Override
    public Object getServletContext() {
        throw new IllegalStateException("PHP not running in a servlet environment");
    }

    /**
     * Throws IllegalStateException
     *
     * @return none
     */
    @Override
    public Object getHttpServletResponse() {
        throw new IllegalStateException("PHP not running in a servlet environment");
    }

    /**
     * Throws IllegalStateException
     *
     * @return none
     */
    @Override
    public Object getServlet() {
        throw new IllegalStateException("PHP not running in a servlet environment");
    }

    /**
     * Throws IllegalStateException
     *
     * @return none
     */
    @Override
    public Object getServletConfig() {
        throw new IllegalStateException("PHP not running in a servlet environment");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealPath(String path) {
        return io.soluble.pjb.bridge.http.Context.getRealPathInternal(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String key) {
        return getBindings(IContext.ENGINE_SCOPE).get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, Object val) {
        getBindings(IContext.ENGINE_SCOPE).put(key, val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String key) {
        getBindings(IContext.ENGINE_SCOPE).remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map map) {
        getBindings(IContext.ENGINE_SCOPE).putAll(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map getAll() {
        return Collections.unmodifiableMap(getBindings(IContext.ENGINE_SCOPE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Continuation createContinuation(Reader reader, Map env,
                                           OutputStream out, OutputStream err, HeaderParser headerParser, ResultProxy result,
                                           ILogger logger, boolean isCompiled) {
        Continuation cont;

        if (isCompiled)
            cont = new FastCGIProxy(reader, env, out, err, headerParser, result, logger);
        else
            cont = new HttpProxy(reader, env, out, err, headerParser, result, logger);

        return cont;
    }

    private static JavaBridgeRunner httpServer;

    private static synchronized JavaBridgeRunner getHttpServer() {
        if (httpServer != null) return httpServer;
        try {
            return httpServer = JavaBridgeRunner.getRequiredInstance();
        } catch (IOException e) {
            Util.printStackTrace(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSocketName() {
        return getHttpServer().getSocket().getSocketName();
    }

    /**
     * @deprecated
     */
    @Override
    public String getRedirectString() {
        throw new NotImplementedException();
    }

    /**
     * @param webPath
     * @deprecated
     */
    @Override
    public String getRedirectString(String webPath) {
        throw new NotImplementedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRedirectURL(String webPath) {
        return "http://127.0.0.1:" + getSocketName() + webPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContextServer getContextServer() {
        return getHttpServer().getContextServer();
    }
}
