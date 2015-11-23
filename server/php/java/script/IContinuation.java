package php.java.script;

/**
 * Classes implementing this interface represent the script continuation;
 * they can be used to allocate php scripts on a HTTP- or FastCGI server.
 * @author jostb
 *
 */
public interface IContinuation {

    /**
     * The PHP script must call this function with the current
     * continuation as an argument.<p>
     * 
     * Example:<p>
     * <code>
     * java_context()-&gt;call(java_closure());<br>
     * </code>
     * @param script - The php continuation
     * @throws InterruptedException
     */
    public void call(Object script) throws InterruptedException;

    /**
     * One must call this function if one is interested in the php continuation.
     * @return The php continuation.
     * @throws Exception 
     */
    public Object getPhpScript() throws Exception;

    /**
     * This function must be called to release the allocated php continuation.
     * Note that simply calling this method does not guarantee that
     * the script is finished, as the ContextRunner may still produce output.
     * Use contextFactory.waitFor() to wait for the script to terminate.
     * @throws InterruptedException 
     *
     */
    public void release() throws InterruptedException;

}