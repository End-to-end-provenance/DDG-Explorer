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
	private Map<String, String> attributes = new HashMap<>();

	// Names of attributes describing the entire DDG
	public static final String LANGUAGE = "Language";
	public static final String EXECUTION_TIME = "DateTime";
	public static final String MAIN_SCRIPT_NAME = "Script";
	public static final String MAIN_SCRIPT_TIMESTAMP = "ProcessFileTimestamp";
	public static final String SOURCED_SCRIPT_NAMES = "SourcedScripts";
	public static final String SCRIPT_TIMESTAMPS = "SourcedScriptTimestamps";

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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String key : names()) {
			sb.append(key + " = " + attributes.get(key) + "\n");
		}
		return sb.toString();
	}
	
}
