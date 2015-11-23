package php.java.bridge.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A default output stream factory for use with parseBody.
 */
public abstract class OutputStreamFactory {
    /**
     * Return an output stream
     * @return an output stream
     */
    public abstract OutputStream getOutputStream() throws IOException;
}