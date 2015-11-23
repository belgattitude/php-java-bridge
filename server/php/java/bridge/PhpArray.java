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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

final class PhpArray extends AbstractMap { // for PHP's array()
    private static final long serialVersionUID = 3905804162838115892L;
    private TreeMap t = new TreeMap(Request.PHP_ARRAY_KEY_COMPARATOR);
    private HashMap m = null;
    public Object put(Object key, Object value) {
        if(m!=null) return m.put(key, value);
        try {
            return t.put((Integer)key, value);
        } catch (ClassCastException e) {
            m = new HashMap(t);
            t = null;
            return m.put(key, value);
        }
    }
    public Set entrySet() {
        if(t!=null) return t.entrySet();
        return m.entrySet();
    }

    public int arraySize() {
        if(t!=null) {
    	if(t.size()==0) return 0;
    	return 1+((Integer)t.lastKey()).intValue();
        }
        throw new IllegalArgumentException("The passed PHP \"array\" is not a sequence but a dictionary");
    }
}
