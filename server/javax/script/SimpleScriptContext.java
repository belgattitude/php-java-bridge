package javax.script;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * The GenericScriptContext is a simple implementation of ScriptContext.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk> 
 */
public class SimpleScriptContext implements ScriptContext {
	
	/** namespace of the scope of level GLOBAL_SCOPE */
    protected Bindings globalScope;
    
	/** namespace of the scope of level ENGINE_SCOPE */
	protected Bindings engineScope;

	/**
	 * Create a simple script context
	 */
	public SimpleScriptContext() {
	        engineScope = new SimpleBindings();
	}
    
    /**
     * Retrieves the value for getAttribute(String, int) for the 
     * lowest scope in which it returns a non-null value.
     * 
     * @param name the name of the attribute 
     * @return the value of the attribute
     * @throws IllegalArgumentException 
     */
    public Object getAttribute(String name) throws IllegalArgumentException{
      
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        if (engineScope.get(name) != null) {
            return engineScope.get(name);
        } else if (globalScope.get(name) != null) {
            return globalScope.get(name);
        } else {
            return null;            
        }
    }
    
    /**
     * Retrieves the value associated with specified name in the 
     * specified level of scope. Returns null if no value is 
     * associated with specified key in specified level of scope.
     *  
     * @param name   the name of the attribute
     * @param scope the level of scope
     * @return the value value associated with the specified name in
     *         specified level of scope
     * @throws IllegalArgumentException 
     */
    public Object getAttribute(String name, int scope) 
            throws IllegalArgumentException{
        
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope.get(name);
        	case GLOBAL_SCOPE:
        		return globalScope.get(name);
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }
    }

    /**
     * Retrieves the lowest value of scopes for which the attribute 
     * is defined. If there is no associate scope with the given 
     * attribute (-1) is returned.
     * 
     * @param  name the name of attribute
     * @return the value of level of scope  
     */
    public int getAttributesScope(String name) {
        if (engineScope.containsKey(name)) {
            return ENGINE_SCOPE;
        } else if(globalScope.containsKey(name)) {
            return GLOBAL_SCOPE;
        }
        
        return -1;
    }
    
    /**
     * Retrieves the Namespace instance associated with the specified
     * level of scope.
     * 
     * @param scope the level of the scope
     * @return the namespace associated with the specified level of 
     *         scope
     */
    public Bindings getBindings(int scope) {
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope;
        	case GLOBAL_SCOPE:
        		return globalScope;
        	default:
        		return null;
        }
    }
        
    /**
     * Removes the specified attribute form the specified level of 
     * scope.
     * 
     * @param name the name of the attribute
     * @param scope the level of scope 
     * @return value which is removed
     * @throws IllegalArgumentException
     */
    public Object removeAttribute(String name, int scope) 
            throws IllegalArgumentException{ 
        
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope.remove(name);
            case GLOBAL_SCOPE:
            	return globalScope.remove(name);
            default:
            	throw new IllegalArgumentException("invalid scope");
        }    
    }
    
    /**
     * Sets an attribute specified by the name in specified level of 
     * scope.
     *  
     * @param name   the name of the attribute
     * @param value the value of the attribute
     * @param scope the level of the scope
     * @throws IllegalArgumentException if the name is null scope is
     *         invalid
     */
    public void setAttribute(String name, Object value, int scope) 
            throws IllegalArgumentException{
        
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		engineScope.put(name, value);
        		break;
        	case GLOBAL_SCOPE:
        		globalScope.put(name, value);
        		break;
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }
    }
	
	/**
	 * Associates the specified namespace with specified level of 
     * scope.
	 * 
	 * @param namespace the namespace to be associated with specified
     *                  level of scope
     * @param scope     the level of scope 
	 * @throws IllegalArgumentException 
	 */	
	public void setBindings(Bindings namespace, int scope) 
            throws IllegalArgumentException {
        
		switch (scope) {
			case ENGINE_SCOPE:
				engineScope = namespace;
				break;
			case GLOBAL_SCOPE:
				globalScope = namespace;
				break;
			default:
				throw new IllegalArgumentException("invalid scope");
			
		}
    }

        private static final List scopes = Arrays.asList(new Integer[] {new Integer(ENGINE_SCOPE), new Integer(GLOBAL_SCOPE)});
        /**{@inheritDoc}*/
	public List getScopes() {
	    return scopes;
        }

	protected Writer errorWriter;
	    /**{@inheritDoc}*/
	public Writer getErrorWriter() {
	    if(this.errorWriter==null) return this.errorWriter = new PrintWriter(System.err, true);
	    return errorWriter;
	}

	    /**{@inheritDoc}*/
	public void setErrorWriter(Writer writer) {
	    this.errorWriter = writer;
	}

	protected Reader reader;
	    /**{@inheritDoc}*/
	public Reader getReader() {
	    if(this.reader == null) return new InputStreamReader(System.in);
	    return reader;
	}

	    /**{@inheritDoc}*/
	public void setReader(Reader reader) {
	    this.reader = reader;
	}

	protected Writer writer;
	    /**{@inheritDoc}*/
        public Writer getWriter() {
            // autoflush is true so that I can see the output immediately
            if(writer==null) {
        	writer = new PrintWriter(System.out, true);
               }
            return writer;
        }
        /**{@inheritDoc}*/
	public void setWriter(Writer writer) {
	    this.writer = writer;
	}
}
