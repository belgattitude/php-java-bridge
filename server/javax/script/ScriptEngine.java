
package javax.script;

import java.io.Reader;

/**
 * The ScriptEngine interfaces contains only the methods which are expected to
 * be fully functional in every Java ScriptEngine.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public interface ScriptEngine {
    
    /** 
     * Reserved key associated with an object array which is used to 
     * pass set of positional parameters to the ScriptEngines.
     */
    public static final String ARGV="javax.script.argv";
	
    /** 
     * Reserved key associated with name of the file which contains 
     * the source of the script.
     */
	public static final String FILENAME = "javax.script.filename";
    
	/** 
     * Reserved key associated with the name of the Java 
     * ScriptEngine 
     */	
	public static final String ENGINE = "javax.script.engine";
    
	/** 
     * Reserved key associated with the version of the Java 
     * ScriptEngine 
     */
	public static final String ENGINE_VERSION = "javax.script.engine.version";
    
	/**
     * Reserved key associated with the name of the supported 
     * scripting language
     */
	public static final String LANGUAGE = "javax.script.language";
    
	/** 
     * Reserved key associated with the version of the supported 
     * scripting language 
     */
	public static final String LANGUAGE_VERSION = "javax,script.language_version";
    
    /** 
     * Reserved key associated with the named value which identifies 
     * the short name of the supported language
     */
    public static final String NAME = "javax.script.name";
	
    /**
     * Retrieves an uninitailized namespace which can be used as the scope of 
     * the ScriptEngine.
     *   
     * @return an initialzed namespace which can be used to repalce the state 
     *         of the ScriptEngine
     */
    public Bindings createBindings();
    
    /**
     * Evaluates a piece of script obtained using the specified 
     * reader as the script source. Returns null for non-returning 
     * scripts.
     * 
     * @param reader the source of the script
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs 
     */
    public Object eval(Reader reader) throws ScriptException;
    
    /**
     * Evaluates a piece of scripts obtained using a reader as the 
     * script source and using the specified namespace as the 
     * SCRIPT_SCOPE. Returns null for non-returning scripts.
     * 
     * @param reader    the script source used to obtained the script 
     * @param namespace the namespace to be used as SCRIPT_SCOPE
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs 
     */
    public Object eval(Reader reader,Bindings namespace) 
            throws ScriptException;
    
    /**
     * Evaluates a script obtained using the specified reader as the
     * script source and using the namespaces in the specifed 
     * ScriptContext. Returns null for non-returning scripts 
     * 
     * @param reader  the script source
     * @param context the context contianing different namespace for
     *                script evaluation
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs
     */
    public Object eval(Reader reader,ScriptContext context) 
            throws ScriptException;
    
    /**
     * Evaluates a piece of script and returns the resultant object. 
     * Returns null for non-returning scripts.
     * 
	 * @param script the String representation of the script
	 * @return the value of the evaluated script
	 * @throws ScriptException if an error occurs 
	 */
    public Object eval(String script) throws ScriptException;
		
    /**
     * Evaluates a piece of script using the specified namespace as 
     * the SCRIPT_SCOPE. Retruns null for non-returning scripts.
     *  
     * @param script    the String representation of the script
     * @param namespace the namespace to be used as the SCRIPT_SCOPE
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs 
     */
	public Object eval(String script ,Bindings namespace) 
            throws ScriptException;
	
    /**
     * Evaluates a script using the namespaces in the specifed 
     * ScriptContext. Return null for non-returning scripts.
     *  
     * @param script  the String representation of the script
     * @param context tbe ScriptContext containing namespaces for the
     *                script evaluation 
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs
     */
	public Object eval(String script,ScriptContext context) 
            throws ScriptException;
    
    /**
     * Retrieves the value which is associated with the specified key
     * in the state of the ScriptEngine.
     * 
     * @param key the key associated with value.
     * @return an object value which is associated with the key
     */
    public Object get(String key);
    
    /**
     * Retrieves a ScriptEngineFactory for the class to which 
     * describes the underlying ScriptEngine.
     * 
     * @return an instance of ScriptEngineFactory which describes the
     *         underlying ScriptEngine
     */
    public ScriptEngineFactory getFactory();
    
    /**
     * Retrieves a reference to the associated namespace for the 
     * specified level of scope.
     * 
     * Possible scopes are:
     * 
     * GLOBAL_SCOPE :
     * if the ScriptEngine was created by ScriptingEngineManager 
     * then GLOBAL_SCOPE of it is returned (or null if there is no
     * GLOBAL_SCOPE stored in the ScriptEngine).
     * 
     * ENGINE_SCOPE : 
     * the set of key-value pairs stored in the ScriptEngine is 
     * returned.
     * 
     * @param scope the specified level of scope
     * @return associated namespace for the specified level of scope
     * @throws IllegalArgumentException if the scope is invalid
     */
    public Bindings getBindings(int scope) throws IllegalArgumentException;
		
    /**
     * Associates a key and a value in the ScriptEngine namespace.
     * 
     * @param key   the specified key associated with the value
     * @param value value which is to be associated with the 
     *              specified key
     * @throws IllegalArgumentException if the key is null
	 */
	public void put(String key, Object value) throws IllegalArgumentException;
    	
    /**
     * Associates the specified namespace with the specified level of
     * scope.
     * 
     * @param namespace namespace to be associated with the specified
     *                  level of scope 
     * @param scope     level of scope for which the namespace should
     *                  be associated with
     * @throws IllegalArgumentException if the scope is invalid
     */
	public void setBindings(Bindings namespace,int scope) throws 
            IllegalArgumentException;
	
    
    /**
     * @return the default ScriptContext.
     */
    public ScriptContext getContext();

    /**
     * Set the default ScriptContext
     * @param ctx The context
     */
    public void setContext(ScriptContext ctx);
}
