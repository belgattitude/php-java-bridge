
package javax.script;

/**
 * The Invocable interface (optional) contains methods which allow 
 * the Java application to invoke intermediate codes in the 
 * underlying scripting interpreter.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public interface Invocable {
	
	/**
     * Invokes a scripting procedure with the given name using the 
     * array of objects as its arguments set.
     * 
	 * @param methodName name of the scripting procedure
	 * @param args       arguments set for the scripting procedure
	 * @return resultant object after the execution of the procedure
     * @throws ScriptException if the invocation of the scripting procedure
     *         fails
	 * @throws NoSuchMethodException 
	 */
    public Object invokeFunction(String methodName, Object[] args)
            throws ScriptException, NoSuchMethodException;
    
    /**
     * Invokes a procedure on an object which already defined in the
     * script using the array of objects as its arguments set.
     * 
	 * @param methodName name of the procedure to be invoked
	 * @param thiz       object on which the procedure is called
	 * @param args       arguments set for the procedure
	 * @return           resultant object after the execution of the 
     *                   procedure
	 * @throws ScriptException if the invocation of the procedure 
     *         fails
     * @throws NoSuchMethodException 
	 */
	public Object invokeMethod(Object thiz,String methodName,Object[] args) throws 
            ScriptException, NoSuchMethodException;
	
    /**
     * Retrieves an instance of java class whose methods are 
     * impelemented using procedures in script which are in the 
     * intermediate code repository in the underlying interpreter.
     * 
	 * @param thiz       object on which the procedure is called
     * @param clasz an interface which the returned class must 
     *              implement
     * @return an instance of the class which implement the specified
     *         interface
     */
	public Object getInterface(Object thiz, Class clasz);

    /**
     * Retrieves an instance of java class whose methods are 
     * impelemented using procedures in script which are in the 
     * intermediate code repository in the underlying interpreter.
     * 
     * @param clasz an interface which the returned class must 
     *              implement
     * @return an instance of the class which implement the specified
     *         interface
     */
	public Object getInterface(Class clasz);
    
}