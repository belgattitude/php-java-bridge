package io.soluble.pjb.bridge;

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

/**
 * Exposes the request options. There is one Options instance for each request, but certain options may change for each packet.
 * For example if a user calls java_set_file_encoding(enc), the new file encoding becomes available in the next packet.
 *
 * @author jostb
 */
public class Options {

    protected byte options = 0;

    /**
     * Default encoding: UTF-8
     */
    private String encoding = null;

    private boolean valuesCache, valuesCacheSet = false;
    private boolean base64Cache, base64CacheSet = false;


    /**
     * Returns the file encoding, see java_set_file_encoding(). This option may change for each packet.
     *
     * @return The file encoding
     */
    public String getEncoding() {
        if (encoding != null) {
            encoding = Util.DEFAULT_ENCODING;
        }
        return encoding;
    }

    /**
     * Return a byte array using the current file encoding (see java_set_file_encoding()).
     *
     * @param s The string
     * @return The encoded bytes.
     */
    public byte[] getBytes(String s) {
        try {
            return s.getBytes(getEncoding());
        } catch (java.io.UnsupportedEncodingException e) {
            Util.printStackTrace(e);
            return s.getBytes();
        }
    }

    /**
     * Returns true when the bridge must destroy object identity (see PROTOCOL.TXT) due
     * to limitations in the client (for PHP4 for example).
     * This option stays the same for all packets.
     *
     * @return the appropriate value from the request header.
     */
    public boolean preferValues() {
        if (!valuesCacheSet) {
            int opts = 3 & this.options;
            valuesCacheSet = true;
            return valuesCache = opts == 2 || opts == 1;
        }
        return valuesCache;
    }

    /**
     * re-initialize for keep alive
     */
    protected void recycle() {
        encoding = null;
    }

    /**
     * Set the new file encoding.
     *
     * @param symbol The new file encoding, for example "UTF-8".
     */
    public void setEncoding(String symbol) {
        this.encoding = symbol;
    }

    /**
     * Update the current request options
     *
     * @param b The options from the request header.
     */
    public void updateOptions(byte b) {
        encoding = null;
        this.options = b;
    }

    /**
     * Return true if we must return a base 64 encoded string
     * due to limitations in the client's XML parser.
     * This option stays the same for all packets.
     *
     * @return true if the bridge must return strings as cdata, false otherwise.
     */
    public boolean base64Data() {
        if (!base64CacheSet) {
            int opts = 2 & this.options;
            base64CacheSet = true;
            return base64Cache = opts == 2;
        }
        return base64Cache;
    }

    /**
     * Return true, if the client cannot keep a back-pointer to its own data structures.
     * This option stays the same for all packets.
     *
     * @return true if the bridge must accept and pass a context ID, false otherwise.
     */
    public boolean passContext() {
        return false;
    }
}
