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

/**
 * A global array of object references that the client keeps during
 * the connection (int -> Object mappings).  After connection shutdown
 * the request-handling bridge instance and its global ref array are
 * destroyed.
 * 
 * We guarantee that the first ref ID is 1 and that each new ref ID is
 * n+1. This can be used to avoid round-trips by "guessing" the next
 * object ID, see java_begin_document()/java_end_document().
 */
class GlobalRef {
    
    /**
     * The default size (prime).
     */
    public static final int DEFAULT_SIZE=1021;
    
    private int threshold;
    
    private Entry[] globalRef;
    private int id, count;

    class Entry {
	int id;
	Object value;
	Entry next;
	
	public Entry(int id, Object value, Entry entry) {
	    this.id = id;
	    this.value = value;
	    this.next = entry;
	}
    }
    
    public GlobalRef(int initialCapacity) {
	id = 1;
	count = 0;
	globalRef = new Entry[initialCapacity];
	threshold = (initialCapacity>>>2)*3;
    }
    
    /**
     * Create a new global ref table. Must be called for each new or
     * recycled JavaBridge instance.
     *
     */
    public GlobalRef() {
	this(DEFAULT_SIZE);
    }
    
    /**
     * Get the object associated with the ref ID
     * @param id The ref ID
     * @return The associated object.
     * @throws NullPointerException if ref ID does not exist.
     */
    public Object get(int id) {
	int index = (id & 0x7FFFFFFF) % globalRef.length;
	for (Entry e = globalRef[index]; e != null; e = e.next) {
	    if(e.id == id) return e.value;
	}
	throw new NullPointerException("cannot manipulate the object #"+id+" which has already been destroyed by PHP");
    }
    /**
     * Remove an element from the table.
     * @param id The ref ID.
     */
    public void remove(int id) {
	int index = (id & 0x7FFFFFFF) % globalRef.length;
	for (Entry e=globalRef[index], prev=null; e!=null; prev=e, e=e.next) {
	    if (e.id == id) {
		if (prev!=null) prev.next=e.next; else globalRef[index]=e.next;
		--count;
	    }
	}
    }
    /**
     * Return a string representation of the global ref table.
     * @return The string representation.
     */
    public String dump() {
	StringBuffer result = new StringBuffer();
	for (int i=0;i<count;i++) {
	    if (globalRef[i]!=null) {
	      for(Entry e=globalRef[i]; e!= null; e=e.next)
		result.append("globalRef["+i+"]="+JavaBridge.objectDebugDescription(e.value)+"\n");
	    }
	}
	return result.toString();
    }
    
    public String toString() {
        return "GlobalRef: " + dump();
    }
    
    /**
     * Append an object to the global ref table.
     * @param value The value, may be null or PhpNull
     * @return The ref ID.
     */
    public int append(Object value) {
	return put(id++, value);
    }
    
    private int put(int id, Object value) {
	int index = (id & 0x7FFFFFFF ) % globalRef.length;
	for (Entry e = globalRef[index]; e != null; e = e.next) {
	    if (e.id==id) {
		e.value = value;
		return id;
	    }
	}
	
	if (count >= threshold) {
	    rehash();
	    return put(id, value);
	} 
	
	globalRef[index] = new Entry(id, value, globalRef[index]);
	++count;
	return id;
    }
    private void rehash() {
	int oldCapacity = globalRef.length;
	Entry oldTable[] = globalRef;
	
	int newCapacity = (oldCapacity << 1) + 1;
	Entry newTable[] = new Entry[newCapacity];
	
	threshold = (newCapacity>>>2)*3;
	globalRef = newTable;
	
	for (int i=oldCapacity; i-->0;) {
	    for (Entry old=oldTable[i]; old != null;) {
		Entry e = old;
		old = old.next;
		
		int index = (e.id & 0x7FFFFFFF) % newCapacity;
		e.next = newTable[index];
		newTable[index] = e;
	    }
	}
    }
}
