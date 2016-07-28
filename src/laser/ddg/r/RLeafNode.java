package laser.ddg.r;

import laser.ddg.ProvenanceData;

/**
 * Creates a node that represents an atomic unit of computation in an R script, like
 * a function that calls no other locally-defined functions
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RLeafNode extends RFunctionInstanceNode {

	/**
	 * Creates the node
	 * @param name node name
	 * @param procName the name of the function
	 * @param provData the ddg
	 * @param elapsedTime 
         * @param lineNum 
         * @param scriptNum
	 */
	public RLeafNode(String name, String procName, ProvenanceData provData, double elapsedTime, int lineNum, int scriptNum) {
		super(name, procName, provData, elapsedTime, lineNum, scriptNum);
	}

	/**
	 * States whether or not the node can be a root
	 * @return false - LeafNode so cannot be root
	 */
	@Override
	public boolean canBeRoot() {
		return false;
	}

	/**
	 * Returns the type of the node
	 * 
	 * @return "Leaf"
	 */
	@Override
	public String getType() {
		return "Operation";
	}

}
