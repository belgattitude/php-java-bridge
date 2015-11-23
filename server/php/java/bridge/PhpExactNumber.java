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

final class PhpExactNumber extends Number {

    /**
     * 
     */
    private static final long serialVersionUID = 3257566187666749240L;
    private long l;

    /**
     * @param l
     */
    public PhpExactNumber(long l) {
        this.l = l;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#intValue()
     */
    public int intValue() {
    		
        return (int)l;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#longValue()
     */
    public long longValue() {
        return l;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#floatValue()
     */
    public float floatValue() {
        return l;
    }

    /* (non-Javadoc)
     * @see java.lang.Number#doubleValue()
     */
    public double doubleValue() {
        return l;
    }
    	
    public String toString() {
        return String.valueOf(l);
    }
	
}
