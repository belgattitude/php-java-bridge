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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class Session implements ISession {
  
    protected Map map;
    protected String name;
    private static int sessionCount=0;
    boolean isNew=true;
    protected long creationTime, lastAccessedTime, timeout;
	
    public Object get(Object ob) {
	this.lastAccessedTime=System.currentTimeMillis();
	return map.get(ob);
    }
	
    public void put(Object ob1, Object ob2) {
	this.lastAccessedTime=System.currentTimeMillis();
	map.put(ob1, ob2);
    }
	
    public Object remove(Object ob) {
	this.lastAccessedTime=System.currentTimeMillis();
	return map.remove(ob);
    }
	
    Session(String name) {
	this.name=name;
	Session.sessionCount++;
	this.map=Collections.synchronizedMap(new HashMap());
	this.creationTime = this.lastAccessedTime=System.currentTimeMillis();
	this.timeout=1440000;
    }

    public void setTimeout(int timeout) {
	this.timeout=timeout*1000;
	this.lastAccessedTime=System.currentTimeMillis();
    }
	
    public int getTimeout() {
	return (int)(timeout/1000);
    }
	
    public int getSessionCount() {
	return sessionCount;
    }
	
    public boolean isNew() {
	return isNew;
    }
	
    public void destroy() {
	sessionCount--;
	synchronized(JavaBridge.sessionHash) {
	    if(JavaBridge.sessionHash!=null)
		JavaBridge.sessionHash.remove(name);
	}
    }
	
    public void invalidate() {
	destroy();
    }

    public void putAll(Map vars) {
	this.lastAccessedTime=System.currentTimeMillis();
	map.putAll(vars);
    }

    /* (non-Javadoc)
     * @see php.java.bridge.ISession#getAll()
     */
	public Map getAll() {
	this.lastAccessedTime=System.currentTimeMillis();
	return new HashMap(map); // unshare the map 
    }

    /** Check for expired sessions every 10 minutes 
     * see #CHECK_SESSION_TIMEOUT
     */
    static synchronized void expire() {
	if(JavaBridge.sessionHash==null) return;
    	synchronized(JavaBridge.sessionHash) {
	    for(Iterator e = JavaBridge.sessionHash.values().iterator(); e.hasNext(); ) {
		Session ref = (Session)e.next();
		if((ref.timeout >0) && (ref.lastAccessedTime+ref.timeout<=System.currentTimeMillis())) {
		    sessionCount--;
		    e.remove();
		    if(Util.logLevel>3) Util.logDebug("Session " + ref.name + " expired.");
		}
	    }
	}
    }
    
    /**
     * Expires all sessions immediately.
     *
     */
    public static void reset() {
	if(JavaBridge.sessionHash==null) return;
    	synchronized(JavaBridge.sessionHash) {
	    for(Iterator e = JavaBridge.sessionHash.values().iterator(); e.hasNext(); ) {
		Session ref = (Session)e.next();
		sessionCount--;
		e.remove();
		if(Util.logLevel>3) Util.logDebug("Session " + ref.name + " destroyed.");
	    }
	}
  	
    }

    public long getCreationTime() {
      return creationTime;
    }

    public long getLastAccessedTime() {
      return lastAccessedTime;
    }
}
