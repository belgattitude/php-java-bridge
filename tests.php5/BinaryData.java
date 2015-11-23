public class BinaryData {

    public byte[] b;
    public byte[] getData(int len) {
	b = new byte[len];
	for(int i=0; i<b.length; i++) {
	    b[i]=(byte)(i%255);
	}
	return b;
    }

    public byte[] compare(byte[] b2) throws java.lang.Exception {
	if(b.length!=b2.length) throw new RuntimeException();
	for(int i=0; i<b2.length; i++)
	    if(b2[i]!=b[i]) throw new RuntimeException();
	return b2;
    }
    public String toString() throws java.lang.Exception {
	return new String(b, "ASCII");
    }
}
