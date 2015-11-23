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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import php.java.bridge.ISession;
import php.java.bridge.JavaBridge;
import php.java.bridge.Request;
import php.java.bridge.SessionFactory;
import php.java.bridge.Util;


/**
 * Create session, jsr223 contexts.<p>
 * The ContextFactory may keep a promise (a "proxy") which one may evaluate to a
 * session reference (for PHP/JSP session sharing), and/or it may
 * reference a "half-executed" bridge for local channel re-directs (for
 * "high speed" communication links). 
 *
 *<p>
 * A unique context
 * instance should be created for each request and destroyed when the request
 * is done.
 * </p>
 * <p>
 * Clients of the PHP clients may attach additional data and run with
 * a customized ContextFactory by using the visitor pattern, 
 * see {@link #accept(IContextFactoryVisitor)}.
 * </p>
 * <p>
 * The string ID of the instance should be passed to the client, which may
 * pass it back together with the getSession request or the "local
 * channel re-direct". If the former happens, we invoke the promise
 * and return the session object to the client. Different promises can
 * evaluate to the same session object.  For local channel re-directs
 * the ContextFactory is given to a ContextRunner which handles the
 * local channel communication.
 * </p>
 * <p>
 * When a php client is not interested in a context for 5 seconds (checked every 10 minutes), the
 * context is destroyed: a) switching from the HTTP tunnel to the local channel of the
 * ContextRunner or b) switching from the fresh context created by the client of the PHP client to the
 * recycled, persistent context, costs only one round-trip. The time for such a context switch 
 * is usually much less than 10ms unless either the php client or the client that waits for the php client 
 * is traced. If 5 seconds is not enough during debugging, change the ORPHANED_TIMEOUT.
 * </p>
 * <p>
 * In a shared environment with k web contexts there can be up to n*k active JavaBridge/ContextFactory instances 
 * (where n is the number of active php clients). All ContextFactories are kept in a shared, per-loader
 * map. But the map can only be accessed via {@link #get(String)}, which checks if the ContextFactory
 * belongs to the same ContextServer.
 * </p>
 * @see php.java.servlet.ServletContextFactory
 * @see php.java.bridge.http.ContextServer
 * @see php.java.bridge.SessionFactory#TIMER_DURATION
 */
public final class ContextFactory extends SessionFactory implements IContextFactory {

    /** This context name can be used when a ContextFactory is used 
     * outside of a servlet environment */
    public static final String EMPTY_CONTEXT_NAME = "";
    

    static {
	try {
	    getTimer().addJob(new Runnable() {public void run() {destroyOrphaned();}});
	} catch (Throwable t) {
	    Util.printStackTrace(t);
	}
    }

    private static final HashMap contexts = new HashMap();
    private static final HashMap liveContexts = new HashMap();
    
    private String id;
    private long timestamp;

    private IContextFactoryVisitor visitor;
    private boolean initialized; 

    private static long counter = 0;
    private static synchronized String addNext(String webContext, ContextFactory thiz, boolean isManaged) {
        String id;
        counter++;
        try {webContext=URLEncoder.encode(webContext, Util.DEFAULT_ENCODING);} catch (UnsupportedEncodingException e) {Util.printStackTrace(e);}
        id = Long.toHexString(counter)+"@"+webContext;
        if (isManaged)
            liveContexts.put(id, thiz);
        else
            contexts.put    (id, thiz);
        return id;
    }
    private static synchronized void remove(String id) {
	ContextFactory ob = (ContextFactory) liveContexts.remove(id);
        if(Util.logLevel>4) Util.logDebug("contextfactory: removed context: " + (ob==null?"already handled":String.valueOf(ob.visitor)) + ", # of contexts: " + contexts.size());
    }
    private static synchronized ContextFactory moveContext(String id) {
        Object o;
        if((o = liveContexts.get(id))!=null) return (ContextFactory)o;
        if((o = contexts.remove(id))!=null) { liveContexts.put(id, o); return (ContextFactory)o; }
        return null;
    }

    /**
     * Create a new ContextFactory.
     * @param webContext The current web context or "@" 
     * @param isManaged true if the factory should NOT be gc'ed after MAX_TIMEOUT
     */
    public ContextFactory(String webContext, boolean isManaged) {
      super();
      timestamp = System.currentTimeMillis();
      id=addNext(webContext, this, isManaged);
      if(Util.logLevel>4) Util.logDebug("contextfactory: new context: " + id + " for web context" + webContext + ", # of contexts: " + contexts.size());
    }

    /**
     * Create a new simple ContextFactory (a factory which creates an emulated JSR223 context) and add it 
     * to the list of context factories kept by this classloader.
     * @return The created ContextFactory.
     * @see php.java.bridge.http.ContextFactory#get(String)
     */
    public static IContextFactory addNew() {
    	return new SimpleContextFactory(EMPTY_CONTEXT_NAME, false);
    }
    
   /**
     * Only for internal use.
     *  
     * Returns the context factory associated with the given <code>id</code>
     * @param id The ID
     * @return The ContextFactory or null.
     * @see php.java.bridge.http.ContextFactory#addNew()
     * @throws SecurityException if id belongs to a different ContextServer.
     */
    /* See PhpJavaServlet#contextServer, http.ContextRunner#contextServer and 
     * JavaBridgeRunner#ctxServer. */
    public static IContextFactory get(String id) {
        ContextFactory factory = moveContext(id); 
        if(factory==null) return null;
        
        if (factory.initialized) throw new SecurityException("illegal access");
        factory.initialized = true;
        
        return factory.visitor;
    }
    
    /*
     * Attach the new factory to the persistent one
     * @param id The fresh factory id from the servlet
     * @param thiz The persistent factory
     */ 
    private static synchronized void switchContext(final String id, final ContextFactory thiz) {
	boolean contextIsManaged = true;
	ContextFactory factory = ((ContextFactory)liveContexts.get(id));
	if (factory == null) {
	    factory=((ContextFactory)contexts.get(id));
	    contextIsManaged = false;
	}
	// May only happen if this is a simple ContextFactory outside of a J2EE environment
	if(factory == null || factory == thiz) return;

	JavaBridge bridge = thiz.getBridge();
	JavaBridge newBridge = factory.checkBridge();
	if(newBridge == null) throw new IllegalStateException("recycle empty context");

	if(Util.logLevel>4) Util.logDebug("contextfactory: setting new bridge. visited: " + bridge.getFactory() + " <= visitor: " + newBridge.getFactory()); 

	// For historical and performance reasons the bridge keeps direct references to the threadContextClassLoader
	// and the i/o channels, so we have to keep it.
	// But we want to run with the new values from the bridge created from the servlet. So we keep the old
	// bridge with its request, response and class-loader and set the new bridge temporarily into this request. 
	// It will be cleaned up in recycle(). It is ugly, but this will stay that way.
	bridge.request.setBridge(newBridge);
	bridge = newBridge;
	    
	/* release the fresh context factory and attach the visitor */
	if (contextIsManaged) { // actually an empty factory, so move it back before releasing it
	    liveContexts.remove(id);
	    contexts.put(id, factory);
	}
	factory.visitor.release();
	thiz.accept(factory.visitor);
	thiz.visitor.initialize();
	
	if(Util.logLevel>4) Util.logDebug("contextfactory: " +thiz + " is swiching thread context" );

	if(Util.logLevel>4) Util.logDebug("contextfactory: accepted visitor: " + factory.visitor);
    }

    /**{@inheritDoc}*/  
    public void recycle(String id) {
	switchContext(id, this);
    }
    /**
     * Called before destroy()
     */    
    public void recycle() {
	if(Util.logLevel>=4) Util.logDebug("contextfactory: finish context called (recycle context factory) " + this.visitor);
	super.recycle();
    }
    
    /**{@inheritDoc}*/  
    public void destroy() {
	if(Util.logLevel>4) Util.logDebug("contextfactory: context destroyed (remove context factory): " +visitor);
	remove(getId());
	super.destroy();
	visitor.invalidate();
   }
    
    /**
     * Orphaned contexts may appear when the PHP client has no interest in the new context
     * that the client of the PHP client has allocated, this may only happen if a remote client
     * crashed directly after sending the initial PUT request. 
     * 
     * Orphaned contexts will be automatically removed after 15 seconds. -- Even 10ms would 
     * be sufficient because contexts only bridge the gap between a) the first statement executed
     * via the HTTP tunnel and the second statement executed via the ContextRunner or 
     * b) they pass initial information from a client of a PHP client to the PHP script. If one round-trip
     * costs more than 10ms, then there's something wrong with the connection. 
     */
    private static synchronized void destroyOrphaned() {
	long timestamp = System.currentTimeMillis();
	
        for(Iterator ii=contexts.values().iterator(); ii.hasNext();) {
	    ContextFactory ctx = ((ContextFactory)ii.next());
	    if(ctx.timestamp+Util.MAX_WAIT<timestamp) {
	        ctx.visitor.invalidate();
	        Util.warn("contextfactory: Orphaned context: " + ctx.visitor + " removed.");
	        ii.remove();
	    }
	}        
    }
    /**
     * Remove all context factories from the classloader.
     * May only be called by the ContextServer.
     * @see php.java.bridge.http.ContextServer
     */
    public static synchronized void destroyAll() {
        for(Iterator ii=contexts.values().iterator(); ii.hasNext();) {
	    ContextFactory ctx = ((ContextFactory)ii.next());
	    ctx.visitor.invalidate();
	    if(Util.logLevel>4) Util.logDebug("contextfactory: Orphaned context: " + ctx.visitor + " removed.");
	    ii.remove();
	}
	for(Iterator ii=contexts.values().iterator(); ii.hasNext();) {
	    ContextFactory ctx = ((ContextFactory)ii.next());
	    ctx.visitor.invalidate();
	    ii.remove();
	}
    }
    /**{@inheritDoc}*/  
    public void releaseManaged() throws InterruptedException {
	    visitor.releaseManaged();
    }
    /**{@inheritDoc}*/  
    public void waitFor(long timeout) throws InterruptedException {
	    visitor.waitFor(timeout);
    }
    /**{@inheritDoc}*/  
    public String getId() { 
	return id; 
    }
    /**{@inheritDoc}*/  
    public String toString() {
	return "Context# " +id;
    }
    /**
     * Returns the context.
     * @return The context or null.
     */
    public IContext getContext() {
	return visitor.getContext();
    }
	
    /**
     * Set the Context into this factory.
     * Should be called by Context.addNew() only.
     * @param context The context.
     * @see php.java.bridge.http.ContextFactory#addNew()
     */
    public void setContext(IContext context) {
        visitor.setContext(context);
    }
    private void setVisitor(IContextFactoryVisitor newVisitor) {
        visitor = newVisitor;
    }
    /**
     * Use this method to attach a visitor to the ContextFactory.
     * @param visitor The custom ContextFactory
     */
    public void accept(IContextFactoryVisitor visitor) {
	visitor.visit(this);
        setVisitor(visitor);
    }
    
    /**
     * Return a simple session which cannot be shared with JSP
     * @param name The session name
     * @param clientIsNew true, if the client wants a new session
     * @param timeout expires in n seconds
     * @return The session
     */
    public ISession getSimpleSession(String name, short clientIsNew, int timeout) {
        return super.getSession(name, clientIsNew, timeout);
    }
    /**
     * Return a standard session, shared with JSP
     * @param name The session name
     * @param clientIsNew true, if the client wants a new session
     * @param timeout expires in n seconds
     * @return The session
     */
    public ISession getSession(String name, short clientIsNew, int timeout) {
	return visitor.getSession(name, clientIsNew, timeout);
    }
    /**{@inheritDoc}*/  
    public synchronized void release() {
	ContextFactory ob = (ContextFactory) contexts.remove(id);
        if(Util.logLevel>4) Util.logDebug("contextfactory: released empty context: " + (ob!=null?String.valueOf(ob.visitor):"<already handled>") + ", # of contexts: " + contexts.size()+", # of live contexts: "+ liveContexts.size());
    }
    /**{@inheritDoc}*/  
    public void initialize() {
	visitor.initialize();
    }
    /**{@inheritDoc}*/  
    public void invalidate() {
	visitor.invalidate();
    }
    /**{@inheritDoc}
     */  
    public void parseHeader(Request req,InputStream in) throws IOException {
	JavaBridge bridge = getBridge();

	in.read();
	
	// the header used to be binary encoded
	byte shortPathHeader = (byte) (0xFF&in.read());

	bridge.out.write(0); bridge.out.flush(); // dummy write: avoid ack delay
	int len =(0xFF&in.read()) | (0xFF&in.read()<<8);
	byte[] buf = new byte[len];
	in.read(buf);
	String newContext = new String(buf, 0, len,  Util.ASCII);
	IContextFactory factory = (IContextFactory)bridge.getFactory();
	factory.recycle(newContext);

	if(shortPathHeader != (byte) 0xFF)  // short path: no previous PUT request
	    req.init(shortPathHeader);
    }
}
