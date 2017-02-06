public class ArrayArray implements java.io.Serializable {
    public String[] array;

    public static ArrayArray[] create(int n) {
	ArrayArray[] a = new ArrayArray[n];

	for(int i=0; i<a.length; i++) {
	    a[i]=new ArrayArray();
	    a[i].array=new String[] {String.valueOf(i), null};
	}
	return a;
    }
}
	
	    
