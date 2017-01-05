package laser.ddg.r;

import laser.ddg.AbstractProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.SourcePos;

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
	 * @param elapsedTime 
	 * @param sourcePos the location in the source file that this node corresponds to
	 */
	public RFunctionInstanceNode(String name, Object procDefinition, ProvenanceData provData, double elapsedTime, 
			SourcePos sourcePos) {
		super(name, procDefinition, null, provData, elapsedTime, sourcePos);
	}
}