package laser.ddg.r;

import laser.ddg.ProvenanceData;

/**
 * Represents the point between executions of a loop
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RIntermNode extends RFunctionInstanceNode {

	/**
	 * Creates the node
	 * @param name node name
	 * @param provData the ddg
	 * @param elapsedTime 
	 */
	public RIntermNode(String name, ProvenanceData provData, double elapsedTime) {
		super(name, null, provData, elapsedTime);
	}
	
	/**
	 * States whether or not the node could be a root
	 * 
	 * @return false-Interim node cannot be root
	 */
	public boolean canBeRoot() {
		return false;
	}
	
	/**
	 * Returns the type 
	 * 
	 * @return "Interm"
	 */
	public String getType() {
		return "Interm";
	}
}
