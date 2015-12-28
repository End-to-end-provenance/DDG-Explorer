package laser.ddg.r;

import laser.ddg.ProvenanceData;

/**
 * Represents the beginning of an R function or block
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RStartNode extends RFunctionInstanceNode {

	/**
	 * Creates the node
	 * @param name the node's name
	 * @param procDefinition the code the node represents
	 * @param provData the ddg
	 * @param elapsedTime 
	 */
	public RStartNode(String name, Object procDefinition, ProvenanceData provData, double elapsedTime) {
		super(name, procDefinition, provData, elapsedTime);
		assert provData != null;
	}

	/**
	 * States if node is potentially a root
	 * @return true - A start node can be a root
	 */
	public boolean canBeRoot() {
		return true;
	}

	/**
	 * Returns the type of the node
	 * @return "Start"
	 */
	public String getType() {
		return "Start";
	}

}
