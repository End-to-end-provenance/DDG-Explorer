package laser.ddg.r;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import laser.ddg.AbstractDataInstanceNode;

/**
 * A data node representing data produced by an R script.
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RDataInstanceNode extends AbstractDataInstanceNode{ 
	private String type;
	private String value;
	private ArrayList<Integer> inputs = new ArrayList<Integer>();
	private ArrayList<Integer> outputs = new ArrayList<Integer>();
	
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
	}
	
	/**
	 * Creates a new RDataInstanceNode
	 * 
	 * @param type the node type -- one of "Data", "File" or "URL"
	 * @param name the data name
	 * @param value the data value
	 * @param time the time that the data was created or the timestamp associated
	 * 		with a file used as input
	 * @param location the original location for a file node, null if not a file node
	 * @param hash the SHA-1 hash of the associated node's data, null if not a file node
	 * @param scrloc the file path of the associated node, null if not a file node
	 */
	public RDataInstanceNode(String type, String name, String value, String time, String location, String hash, String scrloc) {
		super(value, name, time, location, hash, scrloc);
		this.type = type;
	}
	
	/**
	 * Getter for type
	 */
	@Override
	public String getType() {
		return type;
	}
	
	/**
	 * Getter for value
	 */
	@Override
	public String getValue() {
		return value;
	}
	
	/**
	 * Getter for inputs
	 * 
	 * @return the list of inputs
	 */
	public ArrayList<Integer> getInputs() {
		return inputs;
	}
	
	/**
	 * Getter for outputs
	 * 
	 * @return the list of outputs
	 */
	public ArrayList<Integer> getOutput() {
		return outputs;
	}
	
	/**
	 * Adds a node as an input or an output
	 * 
	 * @param index the index of the node to be added
	 * @param type a string, either "input" or "output" indicating whether this
	 * 		is an input or output node
	 */
	public void addNode(int index, String type) {
		if (type == "input") {
			inputs.add(index);
		} else {
			outputs.add(index);
		}
	}
}
