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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache [Entry(object, method, parameters) -> Method].  No
 * synchronization, so use this class per thread or per request
 * only.
 */
final class MethodCache {
    Map map;
    static final Entry noCache = new NoCache();
    
    private void init() {
        map = new HashMap();
    }
    /**
     * Create a new method cache.
     *
     */
    public MethodCache() {
        init();
    }
    private static class CachedMethod {
        private Method method;
        private Class[] typeCache;
        public CachedMethod(Method method) {
            this.method = method;
        }
        public Method get() {
            return method;
        }
        public Class[] getParameterTypes() {
            if(typeCache!=null) return typeCache;
            return typeCache = method.getParameterTypes();
        }
    }
    
    /**
     * A cache entry.
     */
    public static class Entry {
	boolean isStatic;
	String name;
	Class clazz;
	Class params[];
		
	protected Entry () {}
	protected Entry (String name, Object obj, Class params[]) {
	    this.name = name; // intern() is ~10% slower than lazy string comparison
	    boolean isStatic = obj instanceof Class;
	    this.clazz = isStatic?(Class)obj:obj.getClass();
	    this.isStatic = isStatic;
	    this.params = params;
	}
	private boolean hasResult = false;
	private int result = 1;
	public int hashCode() {
	    if(hasResult) return result;
	    for(int i=0; i<params.length; i++) {
		result = result * 31 + (params[i] == null ? 0 : params[i].hashCode());
	    }
	    result = result * 31 + clazz.hashCode();
	    result = result * 31 + name.hashCode();
	    result = result * 31 + (isStatic? 1231 : 1237);
	    hasResult = true;
	    return result;
	}
	public boolean equals(Object o) {
	    Entry that = (Entry) o;
	    if(clazz != that.clazz) return false;
	    if(isStatic != that.isStatic) return false;
	    if(params.length != that.params.length) return false;
	    if(!name.equals(that.name)) return false;
	    for(int i=0; i<params.length; i++) {
		if(params[i] != that.params[i]) return false;
	    }
	    return true;
	}
	private CachedMethod cache;
	public void setMethod(CachedMethod cache) {
	    this.cache = cache;
	}
	public Class[] getParameterTypes(Method method) {
	    return cache.getParameterTypes();
	}
    }
    private static final class NoCache extends Entry {
	public Class[] getParameterTypes(Method method) {
	    return method.getParameterTypes();
	}        
    }
    
    /**
     * Get the method for the entry
     * @param entry The entry
     * @return The method
     */
    public Method get(Entry entry) {
    	if(entry==noCache) return null;
	CachedMethod cache = (CachedMethod)map.get(entry);
	if(cache==null) return null;
	entry.setMethod(cache);
	return cache.get();
    }

    /**
     * Store a constructor with an entry
     * @param entry The cache entry
     * @param method The method
     */
    public void put(Entry entry, Method method) {
    	if(entry!=noCache) {
    	    CachedMethod cache = new CachedMethod(method);
    	    entry.setMethod(cache);
    	    map.put(entry, cache);
    	}
    }

    /**
     * Get a cache entry from a name, class and arguments.
     * @param name The method name
     * @param obj The object or class
     * @param args The arguments
     * @return A cache entry.
     */
    public Entry getEntry (String name, Object obj, Object args[]){
    	Class params[] = new Class[args.length];
    	for (int i=0; i<args.length; i++) {
	    Class c = args[i] == null ? null : args[i].getClass();
	    if(c == PhpArray.class) return noCache;
	    params[i] = c;
    	}
	return new Entry(name, obj, params);
    }
    
    /**
     * Removes all mappings from this cache.
     */
    public void clear() {
       init();
    }
}
