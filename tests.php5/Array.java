import java.util.*;

public class Array {
    private List entries;
    private Map conversion;

    public Array() {
	entries = new ArrayList(7);
	entries.add("Jakob der Lügner, Jurek Becker 1937--1997");
	entries.add("Mutmassungen über Jakob, Uwe Johnson, 1934--1984");
	entries.add("Die Blechtrommel, Günter Grass, 1927--");
	entries.add("Die Verfolgung und Ermordung Jean Paul Marats dargestellt durch die Schauspielgruppe des Hospizes zu Charenton unter Anleitung des Herrn de Sade, Peter Weiss, 1916--1982");
	entries.add("Der Mann mit den Messern, Heinrich Böll, 1917--1985");
	entries.add("Biedermann und die Brandstifter, Max Frisch, 1911--1991");
	entries.add("Seelandschaft mit Pocahontas, Arno Schmidt, 1914--1979");
	
	conversion = new HashMap();
	conversion.put("long", "java.lang.Byte java.lang.Short java.lang.Integer");
	conversion.put("boolean", "java.lang.Boolean");
	conversion.put("double", "java.lang.Double");
	conversion.put("null", "null");
	conversion.put("object", "depends");
	conversion.put("array of longs", "int[]");
	conversion.put("array of doubles", "double[]");
	conversion.put("array of boolean", "boolean[]");
	conversion.put("mixed array", "");
    }

    public Map getConversion() {
	return conversion;
    }
    public List getEntries() {
	return entries;
    }
    public String getEntry(int index) throws IndexOutOfBoundsException {
	return (String)entries.get(index);
    }
    public int getIndex(String title) {
	return entries.indexOf(title);
    }
}

    
