/**
 * 
 */
package php.java.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A simple HTTP servlet request which is not connected to any input stream.
 * 
 * @author jostb
 */
public class VoidInputHttpServletRequest extends HttpServletRequestWrapper {

    public VoidInputHttpServletRequest(HttpServletRequest req) {
	super(req);
    }

    private ServletInputStream in = null;
    public ServletInputStream getInputStream() {
	if (in != null) return in;
	return in = new ServletInputStream() {
            public int read() throws IOException {
        	return -1;
            }
	};
    }
    private BufferedReader reader = null;
    public BufferedReader getReaader () {
	if (reader != null) return reader;
	return reader = new BufferedReader(new InputStreamReader(getInputStream()));
    }
}