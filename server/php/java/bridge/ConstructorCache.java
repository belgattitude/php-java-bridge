/*-*- mode: Java; tab-width:8 -*-*/

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

package php.java.bridge;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache [Entry(method, parameters) -> Method].  No
 * synchronization, so use this class per thread or per request
 * only.
 */
final class ConstructorCache {
    Map map;
    static final Entry noCache = new NoCache();
    
    private void init() {
        map = new HashMap();
    }
    /**
     * Create a new ConstructorCache
     */
    public ConstructorCache() {
        init();
    }
    private static class CachedConstructor {
      private Constructor method;
      private Class[] typeCache;
      public CachedConstructor(Constructor method) {
          this.method = method;
      }
      public Constructor get() {
          return method;
      }
      public Class[] getParameterTypes() {
          if(typeCache!=null) return typeCache;
          return typeCache = method.getParameterTypes();
      }
    }
     /**
     * A cache entry
     */
    public static class Entry {
	String name;
	Class params[];
		
	protected Entry () {}

	protected Entry (String name, Class params[]) {
	    this.name = name; // intern() is ~10% slower than lazy string comparison
	    this.params = params;
	}
	private boolean hasResult = false;
	private int result = 1;
	public int hashCode() {
	    if(hasResult) return result;
	    for(int i=0; i<params.length; i++) {
		result = result * 31 + (params[i] == null ? 0 : params[i].hashCode());
	    }
	    result = result * 31 + name.hashCode();
	    hasResult = true;
	    return result;
	}
	public boolean equals(Object o) {
	    Entry that = (Entry) o;
	    if(params.length != that.params.length) return false;
	    if(!name.equals(that.name)) return false;
	    for(int i=0; i<params.length; i++) {
		if(params[i] != that.params[i]) return false;
	    }
	    return true;
	}
	private CachedConstructor cache;
	public void setMethod(CachedConstructor cache) {
	    this.cache = cache;
	}
	public Class[] getParameterTypes(Constructor method) {
	    return cache.getParameterTypes();
	}
    }
    private static final class NoCache extends Entry {
	public Class[] getParameterTypes(Constructor method) {
	    return method.getParameterTypes();
	}        
    }

    /**
     * Get the constructor for the entry
     * @param entry The entry
     * @return The constructor
     */
    public Constructor get(Entry entry) {
  	if(entry==noCache) return null;
	CachedConstructor cache = (CachedConstructor)map.get(entry);
	if(cache==null) return null;
	entry.setMethod(cache);
	return cache.get();
    }
    
    /**
     * Store a constructor with an entry
     * @param entry The cache entry
     * @param method The constructor
     */
    public void put(Entry entry, Constructor method) {
  	if(entry!=noCache) {
  	    CachedConstructor cache = new CachedConstructor(method);
  	    entry.setMethod(cache);
  	    map.put(entry, cache);
  	}
    }
    
    /**
     * Get a cache entry from a name args pair
     * @param name The constructor name
     * @param args The arguments
     * @return A cache entry.
     */
    public Entry getEntry (String name, Object args[]){
    	Class params[] = new Class[args.length];
    	for (int i=0; i<args.length; i++) {
	    Class c = args[i] == null ? null : args[i].getClass();
	    if(c == PhpArray.class) return noCache;
	    params[i] = c;
    	}
	return new Entry(name, params);
    }

    /**
     * Removes all mappings from this cache.
     */
    public void clear() {
       init();
    }
}
