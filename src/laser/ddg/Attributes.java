package laser.ddg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A hash table mapping attributes names to values.
 * 
 * @author Barbara Lerner
 * @version Nov 21, 2013
 *
 */
public class Attributes {
	private Map<String, String> attributes = new HashMap<String, String>();

	public String get(String attrName) {
		return attributes.get(attrName);
	}

	public String set(String attrName, String attrValue) {
		return attributes.put(attrName, attrValue);
	}

	public Set<String> names() {
		return attributes.keySet();
	}

	public boolean contains(String name) {
		return attributes.containsKey(name);
	}
	
	
}
