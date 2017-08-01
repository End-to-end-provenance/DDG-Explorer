package laser.ddg;

import laser.ddg.workflow.ScriptNode;

/**
 * This interfaces plays the role of the Visitor in the Visitor pattern.  It allows a class to be able
 * to opearte on all the elements of a DDG.
 * 
 * @author Barbara Lerner
 * @version Aug 23, 2013
 *
 */
public interface ProvenanceDataVisitor {
	/**
	 * Visit an individual script node
	 * @param sn the node
	 */
	public void visitSn(ScriptNode sn);
	/**
	 * Visit an individual procedure node.
	 * @param pin the node
	 */
	public void visitPin(ProcedureInstanceNode pin);
	
	/**
	 * Visit an individual data node
	 * @param din the node
	 */
	public void visitDin(DataInstanceNode din);
	
	/**
	 * Visit an individual control flow edge
	 * @param predecessor the node that executes first
	 * @param successor the node that executes second
	 */
	public void visitControlFlowEdge(ProcedureInstanceNode predecessor, ProcedureInstanceNode successor);
	
	/**
	 * Visit an individual input edge
	 * @param input the data being input
	 * @param consumer the procedure node reading the data
	 */
	public void visitInputEdge(DataInstanceNode input, ProcedureInstanceNode consumer);
	
	/**
	 * Visit an individual output edge
	 * @param producer the node writing the data
	 * @param output the data
	 */
	public void visitOutputEdge(ProcedureInstanceNode producer, DataInstanceNode output);
}
