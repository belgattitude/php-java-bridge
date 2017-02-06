public class NoClassDefFound {

    Object o;
    public static String s = "test okay";

    public NoClassDefFound() {
	o = new DoesNotExist();
    }

    public static Object call(DoesNotExist e) {
	return e;
    }

    public String toString() {
	return String.valueOf(o);
    }
}

	
