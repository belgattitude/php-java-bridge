public class Cache {// Compile this class, create cache.jar and copy it to /usr/share/java
    static Cache instance=null;
    public static Cache getInstance() {
	if(instance==null) instance=makeInstance();
	return instance;
    }
    static Cache makeInstance() {
	try {
	    System.out.println("create new instance");
	    java.lang.Thread.sleep(3000);
	    System.out.println("done creating new instance");
	} catch (Exception e) {}
        return new Cache();
    }
} 
