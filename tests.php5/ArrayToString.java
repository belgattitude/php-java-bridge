public class ArrayToString {
    public static String arrayToString(String[] arr) {
	StringBuffer b = new StringBuffer(arr.length);
	for(int i=0; i<arr.length; i++) {
	    b.append(String.valueOf(arr[i]));
	    if(i+1<arr.length) b.append(" ");
	}
	return b.toString();
    }
    public static String arrayToString(int[] arr) {
	StringBuffer b = new StringBuffer(arr.length);
	for(int i=0; i<arr.length; i++) {
	    b.append(String.valueOf(arr[i]));
	    if(i+1<arr.length) b.append(" ");
	}
	return b.toString();
    }
    public static String arrayToString(double[] arr) {
	StringBuffer b = new StringBuffer(arr.length);
	for(int i=0; i<arr.length; i++) {
	    b.append(String.valueOf(arr[i]));
	    if(i+1<arr.length) b.append(" ");
	}
	return b.toString();
    }
    public static String arrayToString(boolean[] arr) {
	StringBuffer b = new StringBuffer(arr.length);
	for(int i=0; i<arr.length; i++) {
	    b.append(String.valueOf(arr[i]));
	    if(i+1<arr.length) b.append(" ");
	}
	return b.toString();
    }
}
