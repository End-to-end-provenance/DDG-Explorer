package laser.ddg;

import java.util.HashMap;
import java.util.List;
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
	public static final String PROV_DIRECTORY = "ProvDirectory";

	// Names of attributes describing the entire DDG as used in JSON
	public static final String JSON_LANGUAGE = "rdt:language";
	public static final String JSON_EXECUTION_TIME = "rdt:provTimestamp";
	public static final String JSON_MAIN_SCRIPT_NAME = "rdt:script";
	public static final String JSON_MAIN_SCRIPT_TIMESTAMP = "rdt:scriptTimeStamp";
	public static final String JSON_SOURCED_SCRIPTS = "rdt:sourcedScripts";
	public static final String JSON_SOURCED_SCRIPT_TIMESTAMPS = "rdt:sourcedScriptTimeStamps";
	public static final String JSON_PROV_DIRECTORY = "rdt:provDirectory";
	
	// Information about the sourced scripts as recorded in Json attributes
	private List<ScriptInfo> sourcedScriptInfo;

	// Information about installed packages as recorded in Json attributes
	private List<String> packages;

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

	public void setSourcedScriptInfo(List<ScriptInfo> sourcedScriptInfo) {
		this.sourcedScriptInfo = sourcedScriptInfo;
	}
	
	public List<ScriptInfo> getSourcedScriptInfo() {
		return sourcedScriptInfo;
	}

	public void setPackages(List<String> packages) {
		this.packages = packages;		
	}
	
	public List<String> getPackages() {
		return packages;
	}
	
}
