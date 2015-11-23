
package javax.script;

/**
 * ScriptException is a generic checked exception thrown by mehtods 
 * of scripting API. Checked exceptions thrown by the underlying 
 * interpreters must be caught and used to initialize 
 * ScriptExceptions.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk> 
 */
public class ScriptException extends Exception {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 3689065132168917297L;

	/** Stroes the file name of the script */
	protected String fileName = null;
    
    /** 
     * Stores the line number of the script in which the error has 
     * occured
     */ 
   	protected int lineNumber = -1;
    
    /** 
     * Stores the column number of the script in which the error has 
     * occured
     */
    protected int columnNumber = -1;
    
    /** Stores the message which describes the cause of error */
    protected String message;
     
    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param exception the cause of exception
     */
	public ScriptException(Exception exception) {
      		super(exception);
	}
    
    /**
     * Constructs a new exception with the specified detailed 
     * message.
     *  
     * @param message the datailed message which caused the 
     *        exception 
     */
    public ScriptException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * Constructs a new exception with the spcified detailed message 
     * of cause, the file name of the source of script and the line 
     * number of the script where the error has occured.
     * 
     * @param message    the detailed message of cause of exception
     * @param fileName   the file name which contains the script
     * @param lineNumber the line number of the script where the error has 
     *                   occured
     */
    public ScriptException(String message,String fileName,int lineNumber) {
		super(message);
        this.message = message;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
    
    /**
     * Constructs a new exception using the detailed message of 
     * cause, file name which contains the script, line number and
     * column number in which the error has occured.
     *  
     * @param message      the detailed message of the cause of 
     *                     exception
     * @param fileName     the name of the file which contains the
     *                     script
     * @param lineNumber   the line number of the script where the 
     *                     error has occured
     * @param columnNumber the column number of the script where the
     *                     error has occured
     */
	public ScriptException(String message,String fileName,int lineNumber,int columnNumber) {
		super(message);
        this.message = message;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}
	
	/**
     * Retrieves the file name in which the script is contained.
     * 
     * @return Returns the file name in which the script is contained
     */
    public String getFileName() {
        return fileName;
    }
    
	/**
     * Retrieves the column number of the script where the error has 
     * occured. If the information is not available, returns -1.
     * 
	 * @return Returns the column number of the script where the 
     *         error has occured
	 */
	public int getColumnNumber() {        
		return columnNumber;
	}
    
	/**
     * Retrieves the line number of the script where the error has 
     * occured. If the information is not available, returns -1.
     * 
	 * @return Returns the line number of the script where the error 
     *         has occured
	 */
	public int getLineNumber() {
		return lineNumber;
	}
    
    /**
     * Retrieves a String describing the cause of error.
     * 
     * @return a String describing the cause of error
     */
    public String getMessage(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(message);
        if (fileName != null) {
            buffer.append("in: " + fileName);
        }
        if (lineNumber != -1) {
            buffer.append("at line no: " + lineNumber);
        }
        if (columnNumber != -1) {
            buffer.append("at column number: " + columnNumber);
        }
        return buffer.toString();        
    }
}
