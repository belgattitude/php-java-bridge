/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge;


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
import java.io.InputStream;

import php.java.bridge.http.IContext;

/**
 * Create {@link JavaBridge}, {@link IContext} and {@link ISession} instances.
 */
public interface IJavaBridgeFactory {
    
    /**
     * Return a session for the JavaBridge
     * @param name The session name. If name is null, the name PHPSESSION will be used.
     * @param clientIsNew one of {@link ISession#SESSION_CREATE_NEW} {@link ISession#SESSION_GET_OR_CREATE} or {@link ISession#SESSION_GET}
     * @param timeout timeout in seconds. If 0 the session does not expire.
     * @return The session
     * @see php.java.bridge.ISession
     */
    public ISession getSession(String name, short clientIsNew, int timeout);
    /**
     * Return the associated JSR223 context
     * @return The JSR223 context, if supported by the environment or null.
     * @see php.java.bridge.http.ContextFactory#getContext()
     */
    public IContext getContext();

    /** 
     * Return true if this factory has already created a bridge or not.
     * @return true if this factory is new, false otherwise
     */
    public boolean isNew ();
    
    /**
     * Return the JavaBridge.
     * @return Returns the bridge.
     */
    public JavaBridge getBridge();

    /**
     * Recycle the factory for new reqests.
     */
    public void recycle();

    /**
     * Destroy the factory
     */
    public void destroy();
    
    /**
     * Called for the request header
     * 
     * @param req the current request
     * @param in the input stream
     * @throws IOException
     */
    public void parseHeader(Request req, InputStream in) throws IOException;

    /**
     * Flush the response buffer
     * @throws IOException 
     */
   public void flushBuffer() throws IOException;

   /**
    * Hook is called at the end of the life cycle. 
    * Either from destroy(), recycle() or from destroyOrphaned().
    * @see #destroy()
    * @see #recycle()
    */
   public void invalidate();
}
