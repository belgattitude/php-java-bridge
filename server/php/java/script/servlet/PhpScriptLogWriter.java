/*-*- mode: Java; tab-width:8 -*-*/

package php.java.script.servlet;

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
import java.io.OutputStream;

import php.java.bridge.ILogger;
import php.java.bridge.NotImplementedException;
import php.java.script.PhpScriptWriter;

/**
 * A PrintWriter which uses the JavaBridge logger.
 *
 */
public class PhpScriptLogWriter extends PhpScriptWriter {

    private PhpScriptLogWriter(OutputStream out) {
	super(out);
    }
    /**
     * Get a new log writer
     * @param logger The logger
     * @return The log writer
     */
    public static final PhpScriptLogWriter getWriter (ILogger logger) {
	    return new PhpScriptLogWriter (new LogOutputStream(logger));
    }
    static class LogOutputStream extends OutputStream {
	private ILogger logger;
	public LogOutputStream(ILogger logger) {
	    this.logger = logger;
	}
	    public void write(int b) throws IOException {
		throw new NotImplementedException();
	    }
	    public void write(byte b[], int off, int len) throws IOException {
		logger.log(ILogger.INFO, new String(b, off, len));
	    }
    }
}
