import java.util.Arrays;

public class Array6 {
    public static Object[][][][][][] test() {
	Object[][][][][][] testobj = new Object[2][2][2][2][2][2];
        testobj[0][0][0][0][0][1]="1";
        testobj[0][0][0][0][1][0]="2";
        testobj[0][0][0][1][0][0]="3";
        testobj[0][0][1][0][0][0]="4";
        testobj[0][1][0][0][0][0]="5";
        testobj[1][0][0][0][0][0]="6";
	return testobj;
    }

    public static boolean deepEquals(Object[] a1, Object[] a2) {
        if (a1 == a2)
            return true;
        if (a1 == null || a2==null)
            return false;
        int length = a1.length;
        if (a2.length != length)
            return false;
 
        for (int i = 0; i < length; i++) {
            Object e1 = a1[i];
            Object e2 = a2[i];
 
            if (e1 == e2)
                continue;
            if (e1 == null)
                return false;
 
            // Figure out whether the two elements are equal
            boolean eq;
            if (e1 instanceof Object[] && e2 instanceof Object[])
                eq = deepEquals ((Object[]) e1, (Object[]) e2);
            else if (e1 instanceof byte[] && e2 instanceof byte[])
                eq = Arrays.equals((byte[]) e1, (byte[]) e2);
            else if (e1 instanceof short[] && e2 instanceof short[])
                eq = Arrays.equals((short[]) e1, (short[]) e2);
            else if (e1 instanceof int[] && e2 instanceof int[])
                eq = Arrays.equals((int[]) e1, (int[]) e2);
            else if (e1 instanceof long[] && e2 instanceof long[])
                eq = Arrays.equals((long[]) e1, (long[]) e2);
            else if (e1 instanceof char[] && e2 instanceof char[])
                eq = Arrays.equals((char[]) e1, (char[]) e2);
            else if (e1 instanceof float[] && e2 instanceof float[])
                eq = Arrays.equals((float[]) e1, (float[]) e2);
            else if (e1 instanceof double[] && e2 instanceof double[])
                eq = Arrays.equals((double[]) e1, (double[]) e2);
            else if (e1 instanceof boolean[] && e2 instanceof boolean[])
                eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
            else
                eq = e1.equals(e2);
 
            if (!eq)
                return false;
        }
        return true;
    }

    public boolean check(Object[][][][][][] o) {
	return deepEquals((Object[])o, (Object[])test());
    }
}
    
