/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

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

import php.java.bridge.IJavaBridgeFactory;
import php.java.bridge.ISession;

/**
 * Interface that the ContextFactories must implement.
 * 
 * @author jostb
 *
 */
public interface IContextFactory extends IJavaBridgeFactory {

  /**
   * <p>
   * Update the context factory with the new JavaBridge obtained from the servlet
   * </p>
   * @param id The fresh id
   * @see php.java.bridge.http.ContextFactory#recycle()
   * @see php.java.bridge.Request#setBridge(php.java.bridge.JavaBridge)
   * @see php.java.bridge.Request#recycle()
   */
    public void recycle(String id);


  /**
   * Releases the context factory. This method should be called when the
   * factory is not needed anymore.
   * Implementations could then remove any unused context factory from the 
   * classloader's list of context factories.
   */
  public void release();

  /**
   * Wait until this context is finished and release/destroy it. This method returns immediately if this context
   * is not in use yet or it is no longer in use. Call this method only if Java has initiated the communication and
   * Java have full control over the connection, for example via a ScriptEngine's URLReader or CGIRunner. 
   * For Apache/PHP initiated requests use a combination of #waitFor(long) and #release() instead.
   * @throws InterruptedException
   * @see php.java.bridge.http.ContextRunner
   */
  public void releaseManaged() throws InterruptedException;
 
  /**
   * Wait until this context is finished.
   * @param timeout The timeout
   * @throws InterruptedException
   * @see php.java.bridge.http.ContextRunner
   */
  public void waitFor(long timeout) throws InterruptedException;

  /**
   * Return the serializable ID of the context factory
   * @return The ID
   */
  public String getId();

  /**
   * Return a JSR223 context
   * @return The context
   * @see php.java.servlet.ServletContextFactory#getContext()
   * @see php.java.bridge.http.Context
   */
  public IContext getContext();

  /**
   * Set the Context into this factory.
   * Should be called by Context.addNew() only.
   * @param context
   * @see php.java.bridge.http.ContextFactory#addNew()
   */
  public void setContext(IContext context);
  
  /**
   * @param name The session name. If name is null, the name PHPSESSION will be used.
   * @param clientIsNew true if the client wants a new session
   * @param timeout timeout in seconds. If 0 the session does not expire.
   * @return The session
   * @see php.java.bridge.ISession
   */
   public ISession getSession(String name, short clientIsNew, int timeout);

   /**
    * @param name The session name. If name is null, the name PHPSESSION will be used.
    * @param clientIsNew true if the client wants a new session
    * @param timeout timeout in seconds. If 0 the session does not expire.
    * @return The session
    * @see php.java.bridge.ISession
    */
    public ISession getSimpleSession(String name, short clientIsNew, int timeout);

   /**
    * Called when the context runner starts
    * @see IContextFactory#releaseManaged()
    * @see IContextFactory#destroy()
    */
   public void initialize ();
}
