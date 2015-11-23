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

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines the parser callbacks.
 * @author jostb
 *
 */
public interface IDocHandler {
    
    /**
     * Called for each &lt;tag arg1 ... argn&gt;
     * @param tag The tag and the args.
     * @return true, if the parser should stop after reading the top-level end tag, false otherwise. 
     * Implements a short path: Set this to true, if you already know that the current top-level request doesn't need a reply. 
     */
    public boolean begin(ParserTag[] tag);
    
    /**
     * Called for each &lt;/tag&gt;
     * @param strings The tag and the args.
     * @see IDocHandler#begin(ParserTag[])
     */
    public void end(ParserString[] strings);

    /**
     * Called for the header
     * 
     * @param in the input stream
     * @throws IOException
     */
    public void parseHeader(InputStream in) throws IOException;
    
}
 
