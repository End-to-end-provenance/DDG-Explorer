package laser.ddg.r;

import laser.ddg.ProvenanceData;

/**
 * Represents the finish node for a function or block in an R script
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RFinishNode extends RFunctionInstanceNode {

	/**
	 * Creates a finish node
	 * @param name name of the node
	 * @param provData the ddg
	 */
	public RFinishNode(String name, ProvenanceData provData,String time) {
		super(name, null, provData, time);
	}

	/**
	 * States whether or not the node could be the root
	 * @return false - a finish node cannot be the root 
	 */
	public boolean canBeRoot() {
		return false;
	}

	/**
	 * Gets the type of the node
	 * 
	 * @return "Finish" as the type
	 */
	public String getType() {
		return "Finish";
	}



}
