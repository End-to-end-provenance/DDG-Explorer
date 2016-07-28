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
         * @param lineNum 
         * @param scriptNum
	 */
	public RIntermNode(String name, ProvenanceData provData, double elapsedTime, int lineNum, int scriptNum) {
		super(name, null, provData, elapsedTime, lineNum, scriptNum);
	}
	
	/**
	 * States whether or not the node could be a root
	 * 
	 * @return false-Interim node cannot be root
	 */
        @Override
	public boolean canBeRoot() {
		return false;
	}
	
	/**
	 * Returns the type 
	 * 
	 * @return "Interm"
	 */
        @Override
	public String getType() {
		return "Interm";
	}
}
