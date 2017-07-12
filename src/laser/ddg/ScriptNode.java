package laser.ddg;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ScriptNode implements Node {

	// SIN name
	private final String nameOfSN;
	
	// The id of the node is set to zero if no id is assigned
	private int id = 0;
	
	// The value of the node
	private String value;
	
	// The location of the file
	private String location;
	
	// Attribute-value pairs to allow arbitrary extensions
	private Map<String, Object> attributeValues = new TreeMap<>();

	// Time when a Script Node is created
	private final String timeCreated;

	// CPU time that this operation took
	private double elapsedTime;

	// The full path to the script
	private String fullpath;
	
	private Set<Integer> inputs = new HashSet<Integer>();;
	private Set<Integer> outputs = new HashSet<Integer>();;

	/**
	 * Constructs a script node
	 * 
	 * @param elapsedTime the amount of time elapsed
	 * @param name the name of the file
	 * @param json the associated json for the file
	 * @param fullpath the full path to the file
	 */
	public ScriptNode(double elapsedTime, String name, String json, String fullpath) {
		this.timeCreated = Calendar.getInstance().toString();
		this.elapsedTime = elapsedTime;
		this.nameOfSN = name;
		this.value = json;
		this.setFullpath(fullpath);
	}
	
	public String getName() {
		return nameOfSN;
	}

	@Override
	public String getCreatedTime() {
		return timeCreated;
	}

	@Override
	public double getElapsedTime() {
		return elapsedTime;
	}

	@Override
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public Set<DataInstanceNode> getProcessOutputsDerived() {
		return null;
	}

	@Override
	public Set<DataInstanceNode> getProcessInputsDerived() {
		return null;
	}

	@Override
	public Object getAttributeValue(String name) {
		return attributeValues.get(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributeValues.put(name, value);
	}

	@Override
	public Iterator<String> attributes() {
		return attributeValues.keySet().iterator();
	}
	
	public String getType() {
		return "Script";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getFullpath() {
		return fullpath;
	}

	public void setFullpath(String fullpath) {
		this.fullpath = fullpath;
	}
	
	public Set<Integer> getInputs() {
		return inputs;
	}
	
	public Set<Integer> getOutput() {
		return outputs;
	}
	
	public void addNode(int index, String type) {
		if (type == "input") {
			inputs.add(index);
		} else {
			outputs.add(index);
		}
	}
}
