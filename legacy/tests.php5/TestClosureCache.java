public class TestClosureCache {
    public interface IFace {}
    public interface IChild extends IFace {}
    public static String proc1(IChild o) { return "ichild::"+o;}
    public static String proc1(Object o) { return "object::"+o;}
    public static String proc1(IFace o) { return "iface::"+o;}
}
