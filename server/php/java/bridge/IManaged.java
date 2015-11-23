/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge;


/*
 * Copyright (C) 2003-2009 Jost Boekemeier
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
 * Classes which implement this interface receive a notification before their container terminates.
 * This usually happens when the  ContextLoaderListener.contextDestroyed(javax.servlet.ServletContextEvent)
 * is called or right before the VM terminates.
 * 
 * @author jostb
 *
 */
public interface IManaged {
    
    /** Initialize a library. This method may be called via java_context()->init(...) 
     * to initialize a library. Within init() onShutdown() may be called to
     * register a shutdown hook for the library.
     * @param callable Its call() method will be called synchronized.
     * @return The result of the call() invocation.
     * @throws Exception The result of the call() invocation.
     */
    public Object init(Object callable) throws Exception;
    
    /**
     * Register a shutdown hook for the library. This method may be called via java_context()->onShutdown(...)
     * to register a shutdown hook during init().
     * @param closeable Its close() method will be called before the context or the VM terminates.
     */
    public void onShutdown(Object closeable);
}
