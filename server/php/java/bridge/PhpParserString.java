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

import java.io.UnsupportedEncodingException;

final class PhpParserString extends PhpString {
    ParserString st;
    private JavaBridge bridge;
    /** Create a new php parser string
     * @param bridge The JavaBridge
     * @param st The ParserString
     */
    public PhpParserString(JavaBridge bridge, ParserString st) {
        this.bridge = bridge;
        getBytes(st);
    }
    private byte[] bytes;
    private void getBytes(ParserString st) {
         if(bytes==null) {
            bytes=new byte[st.length];
            System.arraycopy(st.string,st.off,bytes,0,bytes.length);
        }
    }
    public byte[] getBytes() {
        return bytes;
    }
    private String newString(byte[] b) {
        return bridge.getString(b, 0, b.length);
    }
    /**
     * Get the encoded string representation
     * @return The encoded string.
     */
    public String getString() {
        return newString(getBytes());
    }
    /**
     * Use UTF-8 encoding, for debugging only
     * @return The string UTF-8 encoded
     */
    public String toString() {
        try {
            return new String(getBytes(), Util.UTF8);
        } catch (UnsupportedEncodingException e) {
            return new String(getBytes());               
        }
     }
}
