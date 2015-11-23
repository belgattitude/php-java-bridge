package javax.script;

import java.io.Reader;

/**
 * Compilable -- an optional interface contains methods which support
 * the re-execution of intermediate code retained from previous 
 * script compilations.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public interface Compilable {

    /**
     * Retrieves a CompileScript implementation for the given piece
     * of script which is a abstraction for the intermediate code 
     * produced by the compilation.
     * 
     * @param script the source of the script represented as String
     * @return an implementation of CompileScript which can be used 
     *         to re-execute intermediate code produced by the 
     *         compilation of script
     * @throws ScriptException if the compilation fials due to any 
     *         reason
     */
    public CompiledScript compile(String script) throws ScriptException;
    
    /**
     * Retrieves a CompileScript implementation for the script 
     * obtained using java.io.Reader as the script source.
     * 
     * @param reader the reader form which the script source is 
     *        obtained
     * @return an implementation of CompileScript which can be used 
     *         to re-execute intermediate code produced by the 
     *         compilation of script
     * @throws ScriptException if the compilation fials due to any 
     *         reason
     */
    public CompiledScript compile(Reader reader) throws ScriptException;
}