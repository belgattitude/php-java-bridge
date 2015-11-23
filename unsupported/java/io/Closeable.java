package java.io;

/** This class is needed for compatibility with JDK 1.4 */
public interface Closeable {
    public void close() throws IOException;
}
