public class Exception {

    public class Ex extends java.lang.Exception {
	int id;
	public Ex(int id) {
	    this.id = id;
	}
	public int getID() {
	    return id;
	}
    }

    public class Inner {
	public Integer o = new Integer(33);
	public Integer meth(int val) throws Ex {
	    if(val==o.intValue()) throw new Ex(o.intValue());
	    return o;
	}
    };

    public Inner inner = new Inner();
}
