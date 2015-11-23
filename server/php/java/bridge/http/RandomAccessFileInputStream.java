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
import java.io.RandomAccessFile;

class RandomAccessFileInputStream extends InputStream {
    private final NPChannel channel;
    private RandomAccessFile raFile;

    RandomAccessFileInputStream(NPChannel channel, RandomAccessFile file) {
        this.channel = channel;
        this.raFile = file;
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        return -1;
    }

    /**
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        if(this.channel.writeIsClosed) 
	    this.raFile.close();
        this.channel.readIsClosed=true;
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        return this.raFile.read();
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return this.raFile.read(b);
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return this.raFile.read(b, off, len);
    }
}