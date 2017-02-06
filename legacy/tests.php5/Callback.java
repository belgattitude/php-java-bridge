public interface Callback {
    public void c1(String s1, Object o1) throws Exception;
    public boolean c2(boolean b);
    public long c3(Exception e);
    public long ni(Exception e);

    public class Test {
	public Callback cb;
	public Test(Callback closure) {
	    cb=closure;
	}
	public long test1() {
	    try {
		cb.c1(new String("TEST"), this);
		return 1;
	    } catch (Exception e) {
		return cb.c3(e);
	    }
	}
	public boolean test() {
	    return cb.c2(test1()==2);
	}
    }
}
    
