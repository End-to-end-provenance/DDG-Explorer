package laser.ddg.r;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import laser.ddg.AbstractDataInstanceNode;

/**
 * A data node representing data produced by an R script.
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RDataInstanceNode extends AbstractDataInstanceNode{
	// The node type. 
	private String type;
	private String value;
	private ArrayList<Integer> inputs = new ArrayList<Integer>();
	private ArrayList<Integer> outputs = new ArrayList<Integer>();
	private int indegree;
	
	/**
	 * Creates a new data node
	 * @param type the node type -- one of "Data", "File" or "URL" 
	 * @param name the data name
	 * @param value the data value
	 * @param time the time that the data was created or the timestamp associated
	 * 		with a file used as input
	 * @param location the original location for a file node, null if not a file node
	 */
	public RDataInstanceNode(String type, String name, String value, String time, String location){		
		super(value, name, time, location);
		this.value = value;
		this.type = type;
		this.indegree = 0;
	}
	
	public RDataInstanceNode(String type, String name, String value, String time, String location, String hash, String scrloc) {
		super(value, name, time, location, hash, scrloc);
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	public ArrayList<Integer> getInputs() {
		return inputs;
	}
	
	public ArrayList<Integer> getOutput() {
		return outputs;
	}
	
	public void addNode(int index, String type) {
		if (type == "input") {
			inputs.add(index);
		} else {
			outputs.add(index);
		}
	}

	public int getIndegree() {
		return indegree;
	}

	public void setIndegree(int indegree) {
		this.indegree = indegree;
	}
}
