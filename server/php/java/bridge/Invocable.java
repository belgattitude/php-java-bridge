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

/**
 * Classes which implement this interface are able to call php code.
 * Invocable PHP scripts must end with the line:<br>
 * <code>
 * java_context()-&gt;call(java_closure());
 * </code>
 * <br>
 * @see php.java.bridge.PhpProcedure#invoke(Object, String, Object[])
 * @see php.java.bridge.PhpProcedure#invoke(Object, java.lang.reflect.Method, Object[])
 * @author jostb
 *
 */
public interface Invocable {
	
    /**
     * Call the java continuation with the current continuation <code>kont</code> as its argument.
     * @param kont The continuation.
     * @return True on success, false otherwise.
     * @throws Exception
     */
    public boolean call(Object kont) throws Exception;
}
