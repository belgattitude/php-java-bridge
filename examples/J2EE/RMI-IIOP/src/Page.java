// A "value class".  Instances of this class are transferred between
// client and server
public class Page implements java.io.Serializable {
    int format;
    String content;

    public Page(int format, String content) {
	this.format = format;
	this.content = content;
    }

}
