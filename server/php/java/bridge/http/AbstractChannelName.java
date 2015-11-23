/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

import php.java.bridge.ILogger;

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
     * Represents the pipe or socket channel name.
     * @author jostb
     */
    public abstract class AbstractChannelName {
        protected String name;
        protected IContextFactory currentCtx;
        
        /**
         * Create a new ChannelName. 
         * @param name The name of the channel, see X_JAVABRIDGE_CHANNEL
         * @param currentCtx The ContextFactory associated with the current request.
         */
        public AbstractChannelName(String name, IContextFactory currentCtx) {
            this.name = name;
            this.currentCtx = currentCtx;
        }
        /**
         * Returns the name of the channel, for example the socket # or the pipe name.
         * @return the name of the channel
         */
        public String getName() {
            return name;
        }
        
        /**
         * Return the 
         * @return the value for X_JAVABRIDGE_CONTEXT.
         */
        public IContextFactory getCtx() {
            return currentCtx;
        }
        /**
         * Start the channel. This method calls IContextServer.start()
         */
        protected abstract boolean startChannel(ILogger logger);
	/**
	 * Start a new ContextRunner for a given ContextServer. The current ContextFactory becomes the 
	 * default for this runner.
	 * @return true, if the channel is available, false otherwise.
	 */
	public boolean start(ILogger logger) {
	    return startChannel(logger);
	}
    }
