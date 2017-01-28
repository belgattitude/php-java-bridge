/*-*- mode: Java; tab-width:8 -*-*/

package io.soluble.pjb.bridge;

import java.io.IOException;
import java.io.InputStream;

import io.soluble.pjb.bridge.http.IContext;

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
 * Create new JavaBridge instances
 *
 * @author jostb
 * @see io.soluble.pjb.bridge.Session
 * @see io.soluble.pjb.bridge.http.Context
 * @see io.soluble.pjb.servlet.HttpContext
 * @see io.soluble.pjb.bridge.http.ContextFactory
 * @see io.soluble.pjb.servlet.ServletContextFactory
 * @see io.soluble.pjb.script.PhpScriptContextFactory
 */
public abstract class JavaBridgeFactory implements IJavaBridgeFactory {

    protected JavaBridge bridge = null;

    /**
     * Return a session for the JavaBridge
     *
     * @param name        The session name. If name is null, the name PHPSESSION will be used.
     * @param clientIsNew one of {@link ISession#SESSION_CREATE_NEW} {@link ISession#SESSION_GET_OR_CREATE} or {@link ISession#SESSION_GET}
     * @param timeout     timeout in seconds. If 0 the session does not expire.
     * @return The session
     * @see io.soluble.pjb.bridge.ISession
     */
    public abstract ISession getSession(String name, short clientIsNew, int timeout);

    /**
     * Return the associated JSR223 context
     *
     * @return Always null
     * @see io.soluble.pjb.bridge.http.ContextFactory#getContext()
     */
    public abstract IContext getContext();

    protected JavaBridge checkBridge() {
        return bridge;
    }

    /**
     * Return the JavaBridge.
     *
     * @return Returns the bridge.
     */
    public JavaBridge getBridge() {
        if (bridge != null) return bridge;
        bridge = new JavaBridge(this);
        if (Util.logLevel >= 4) Util.logDebug("created new bridge: " + bridge);
        return bridge;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNew() {
        return bridge == null;
    }

    /**
     * Recycle the factory for new reqests.
     */
    public void recycle() {
    }

    /**
     * Destroy the factory
     */
    public void destroy() {
        this.bridge = null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    public void parseHeader(Request req, InputStream in) throws IOException {

        in.read();

        byte option = (byte) (0xFF & in.read());
        if (option == (byte) 0xFF) throw new IllegalStateException("not within a JEE environment");
        req.init(option);
    }
}
