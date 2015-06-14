package laser.ddg;

import java.util.Iterator;

/**
 * See comments for AbstractProcedureInstanceNode The requirements for a
 * Procedure Instance Node are that it is bound to the agent that executes the
 * procedure, that it holds collections of its predecessors(of type
 * ProcedureInstanceNode), its successors(of type ProcedureInstanceNode), its
 * inputs(of type DataInstanceNode), and its outputs (of type DataInstanceNode).
 * The ProcedureInstanceNode object has to provide iterators over the names and
 * over the values of its inputs and of its outputs.
 * 
 * @author Sophia
 * 
 */
public interface ProcedureInstanceNode extends Node, Comparable<ProcedureInstanceNode> {

	/**
	 * @return the definition of the procedure that this node represents an
	 *         instance of
	 */
	public Object getProcedureDefinition();

	/**
	 * Save the procedure definition.  The exact form of the definition will depend on 
	 * the language being executed
	 * @param def
	 */
	public void setProcedureDefinition(Object def);

	/**
	 * Record that a procedure is preceded by another ProcedureInstanceNode
	 * 
	 * @param pred
	 *            the predecessor of the procedure
	 */
	public void addPredecessor(ProcedureInstanceNode pred);

	/**
	 * Record that a procedure is followed by another PIN
	 * 
	 * @param suc
	 *            the successor of the procedure
	 */
	public void addSuccessor(ProcedureInstanceNode suc);

	/**
	 * Add an input parameter. Implementations should call ProvenanceData's
	 * notifyListeners to announce the data binding.
	 * 
	 * @param paramName
	 *            the name of the parameter being bound
	 * @param value
	 *            the value being bound
	 * @throws ParameterAlreadyBoundException
	 * 
	 */
	public void addInput(String paramName, DataInstanceNode value)
		throws ParameterAlreadyBoundException;

	/**
	 * @param paramName
	 *            the name of the input parameter whose corresponding DIN is
	 *            returned
	 * @return the value bound to the name
	 * @throws UnboundParameterException
	 *             thrown if there is no parameter with that name
	 */
	public DataInstanceNode getInput(String paramName)
		throws UnboundParameterException;

	/**
	 * Add an output parameter. Implementations should call ProvenanceData's
	 * notifyListeners to announce the data binding.
	 * 
	 * @param paramName
	 *            the name of the parameter being bound
	 * @param value
	 *            the value being bound
	 * @throws ParameterAlreadyBoundException
	 *             thrown if there is already a value bound to this name
	 */
	public void addOutput(String paramName, DataInstanceNode value)
		throws ParameterAlreadyBoundException;

	/**
	 * @param paramName
	 *            the name of the output parameter whose corresponding DIN is
	 *            returned
	 * @return the value bound to the name of the output
	 * @throws UnboundParameterException
	 *             thrown if there is no parameter with that name
	 */
	public DataInstanceNode getOutput(String paramName)
		throws UnboundParameterException;

	/**
	 * Return an iterator over the successors
	 * 
	 * @return an iterator over the PINs that are successors of the current PIN
	 */
	public Iterator<ProcedureInstanceNode> successorIter();

	/**
	 * Return an iterator over the predecessors
	 * 
	 * @return an iterator over the PINs that are predecessors of the current
	 *         PIN
	 */
	public Iterator<ProcedureInstanceNode> predecessorIter();

	/**
	 * Return an iterator over the input parameter names
	 * 
	 * @return an iterator over the names of the input parameters of the current
	 *         PIN
	 */
	public Iterator<String> inputParamNames();

	/**
	 * Return an iterator over the input parameter values
	 * 
	 * @return an iterator over the input parameter values
	 */
	public Iterator<DataInstanceNode> inputParamValues();

	/**
	 * Return an iterator over the output parameter names
	 * 
	 * @return an iterator over the output parameter names
	 */
	public Iterator<String> outputParamNames();

	/**
	 * Return an iterator over the output parameter values
	 * 
	 * @return an iterator over the output parameter values
	 */
	public Iterator<DataInstanceNode> outputParamValues();

	/**
	 * @return the agent that carried out the procedure
	 */
	public AgentConfiguration getAgent();

	/**
	 * Set the ID that will be assigned to the DIN Cannot assign an ID that has
	 * been assigned to any other PIN so far. Cannot assign a zero ID because
	 * all PINs are assigned zero IDs as they are constructed
	 * 
	 * @param newId
	 *            the new ID
	 * @throws IdAlreadySetException
	 */
	public void setId(int newId) throws IdAlreadySetException;

	/**
	 * @return the ID assigned to the DIN
	 */
	@Override
	public int getId();
	
	/**
	 * Returns the name of the node.
	 * 
	 * @return the name of the node.
	 */
	public String getName();

	/**
	 * @return a short String representation for the type name
	 */
	public abstract String getType();

	/**
	 * @return node name followed by node type
	 */
	public String getNameAndType();

	/**
	 * @return true if this type of procedure instance node can be the root of a ddg
	 */
	public boolean canBeRoot();

}
