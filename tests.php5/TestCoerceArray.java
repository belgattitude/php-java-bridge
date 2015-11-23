public class TestCoerceArray {
    public static String f(TestCoerceArray[] ar) {
	StringBuffer buf = new StringBuffer();
	for(int i=0; i<ar.length; i++) {
	    buf.append(ar[i].toString());
	}
	return buf.toString();
    }
}