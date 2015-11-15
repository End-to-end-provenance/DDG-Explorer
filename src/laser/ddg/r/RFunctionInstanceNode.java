package laser.ddg.r;

import laser.ddg.AbstractProcedureInstanceNode;
import laser.ddg.AgentConfiguration;
import laser.ddg.ProvenanceData;

/**
 * Represents the execution of an R function or R block
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public abstract class RFunctionInstanceNode extends AbstractProcedureInstanceNode {

	/**
	 * Constructor with call to super class
	 * 
	 * @param name Name of node 
	 * @param procDefinition Procedure definition(R function itself)
	 * @param provData Provenance data object it will be passed to
	 */
	public RFunctionInstanceNode(String name, Object procDefinition, ProvenanceData provData) {
		super(name, procDefinition, null, provData, provData.getTimestamp());
		
	}
}