package laser.ddg;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ScriptNode implements Node {
	
	// The language the script was written in
	private String language;
	
	// The Java data produced by the running process
	private Serializable value;
	
	// The name of the script in question
	private String nameOfScript;
	
	// The date & time the script node was created.
	private String timeCreated;
	
	// The procedure that created this data
	private ScriptNode producedBy;
	
	//Flag to ensure producer is only set once.
	private int hasProducer;
	
	// The id assigned to a script node
	private int id;
	
	// The procedures that use this data
	private Set<ScriptNode> usedByScript = new LinkedHashSet<>();
	
	// Attribute-value pairs to allow arbitrary extensions
	private Map<String, Object> attributeValues = new TreeMap<>();
	
	// The script provenance data that this node belongs to
	private ScriptProvenanceData scriptProvData;

	public ScriptNode(Serializable val, String name, ScriptNode producer, 
			ScriptProvenanceData scriptProvData) {
		value = val;
		nameOfScript = name;
		producedBy = producer;
		this.hasProducer = 1;
		timeCreated = Calendar.getInstance().toString();
		id = 0;
		this.scriptProvData = scriptProvData;
	}
	
	public ScriptNode(Serializable val, String name, ScriptProvenanceData scriptProvData) {
		value = val;
		nameOfScript = name;
		timeCreated = Calendar.getInstance().toString();
		id = 0;
		this.scriptProvData = scriptProvData;
	}
	
	/**
	 * Nullary constructor required for objects to be java beans
	 */
	public ScriptNode() {
		// no producer node set yet
		this.hasProducer = -1;
	}
	
	/**
	 * Produces a short display representation of the node
	 * 
	 * @return script name
	 * 
	 */
	@Override
	public String toString() {
		return getType() + " d" + id + " \"" + nameOfScript + "\"";
	}
	
	/**
	 * Returns a simplified string representation of the type of node
	 * 
	 * @return a simplified string representation of the type of node
	 */

	public String getType() {
		return "Script";
	}
	
	public String getLanguage() {
		return language;
	}
	
	public Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}
	
	/**
	 * Adds a script node to a data node
	 * @param p the procedure/function node that serves as the producer for the data
	 * @param s the relevant script node to be connected with
	 */
	public void setProducer(ScriptNode p, ScriptNode s){
		//make sure producer is not already set
		if(this.hasProducer >= -1){
			this.producedBy = p;
			this.hasProducer = 1;
		}else{
			System.err.println("Cannot reset the producer for this data node.");
		}
	}
	
	public ScriptNode getProducer() {
		return producedBy;
	}
	
	public Iterator<ScriptNode> users() {
		return usedByScript.iterator();
	}
	
	public void addUserScript(ScriptNode user) {
		usedByScript.add(user);

	}
	
	public void setId(int newId) throws IdAlreadySetException {
		assert newId != 0;
		if (id == 0) {
			id = newId;
		} else {
			throw new IdAlreadySetException("Cannot reset the ID of a node that has already been assigned an ID.");
		}
	}
	
	
	@Override
	public String getCreatedTime() {
		return timeCreated;
	}

	@Override
	public double getElapsedTime() {
		return 0.0;
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public Set<DataInstanceNode> getProcessOutputsDerived() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DataInstanceNode> getProcessInputsDerived() {
		// TODO Auto-generated method stub
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

}
