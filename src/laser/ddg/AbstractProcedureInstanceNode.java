package laser.ddg;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import laser.ddg.gui.DDGExplorer;

/**
 * A Procedure Instance Node represents a transformation of the data whose
 * provenance is being recorded. It allows for a bidirectional traversal of the
 * DDG it is part of either with the use of its Procedure Instance Node
 * predecessors and successors or with the use of the Data Instance Node inputs
 * and outputs. The procedure instance node is bound to the agent responsible
 * for the procedure the node stands for.
 * 
 * @author Sophia
 * 
 */
public abstract class AbstractProcedureInstanceNode implements
		ProcedureInstanceNode {

	// The ID of the PIN (zero if no ID has been assigned)
	private int pinId = 0;

	// A mapping from the names of the inputs to this
	// procedure to their DataInstanceNode-type values
	private Map<String, DataInstanceNode> inputs = new TreeMap<String, DataInstanceNode>();

	// A map from the names of the outputs produced by this procedure
	// to their DataInstanceNode-type values, complete with their
	// derivation graphs. If a procedure execution throws exceptions,
	// each exception object will be an output.
	private Map<String, DataInstanceNode> outputs = new TreeMap<String, DataInstanceNode>();

	// Used in case the procedure that precedes the current
	// ProcedureInstanceNode
	// is another ProcedureInstanceNode (rather than a DataInstanceNode)
	private List<ProcedureInstanceNode> predecessors;
	
	// The line number where the code is that corresponds to this node. */
	private int lineNumber;
	
	/**
	 * @return inputs map of names of input parameters to DIN values of those
	 *         parameters
	 */
	public Map<String, DataInstanceNode> getInputs() {
		return inputs;
	}

	/**
	 * @return outputs map of names of output parameters to DIN values of those
	 *         parameters
	 */
	public Map<String, DataInstanceNode> getOutputs() {
		return outputs;
	}

	/**
	 * @return predecessors PINs that immediately precede this PIN
	 */
	public LinkedList<ProcedureInstanceNode> getPredecessors() {
		return (LinkedList<ProcedureInstanceNode>) predecessors;
	}

	/**
	 * @param predecessors
	 */
	public void setPredecessors(List<ProcedureInstanceNode> predecessors) {
		this.predecessors = predecessors;
	}

	/**
	 * @return successors PINs that immediately succeed this PIN
	 */
	public LinkedList<ProcedureInstanceNode> getSuccessors() {
		return (LinkedList<ProcedureInstanceNode>) successors;
	}

	/**
	 * @param successors
	 */
	public void setSuccessors(LinkedList<ProcedureInstanceNode> successors) {
		this.successors = successors;
	}

	// Used in case the procedure that follows the current ProcedureInstanceNode
	// is another ProcedureInstanceNode (rather than a DataInstanceNode)
	private List<ProcedureInstanceNode> successors;

	// PIN name
	private final String nameOfPIN;

	// Time when a Procedure Instance Node is created
	private final String timeCreated;
	
	// CPU time that this opeartion took
	private double elapsedTime;

	// Information about the agent that carried out this procedure
	private AgentConfiguration agent;
	
	// The provenance data object this node is part of
	private ProvenanceData provData;
	
	// Attribute-value pairs to allow arbitrary extensions
	private Map<String, Object> attributeValues = new TreeMap<String, Object>();
	
	// A definition of the procedure that this node derives from.
	private Object procedureDefinition;

	/**
	 * Create a procedure instance node.
	 * 
	 * @param name
	 *            the name of the node (must be unique)
	 * @param procDefinition the procedure definition that this represents an 
	 * 		execution of
	 * @param ac
	 *            the agent that executed the procedure
	 * @param provData the provenance data that this node belongs to
	 * @param elapsedTime 
	 */
	public AbstractProcedureInstanceNode(String name, Object procDefinition, 
			AgentConfiguration ac, ProvenanceData provData, double elapsedTime, int lineNum) {

		nameOfPIN = name;
		procedureDefinition = procDefinition;
		agent = ac;
		timeCreated = Calendar.getInstance().toString();
		successors = new LinkedList<ProcedureInstanceNode>();
		predecessors = new LinkedList<ProcedureInstanceNode>();
		this.provData = provData;
		this.elapsedTime = elapsedTime;
		this.lineNumber = lineNum;
	}
	
	/**
	 * Get time when execution of PIN started
	 * 
	 * @return time of creation of the Procedure Instance Node
	 */
	@Override
	public String getCreatedTime() {
		return timeCreated;
	}

	public double getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * return the name of the PIN
	 * 
	 */

	/**
	 * Returns the name of the node.
	 * @return the name of the node.
	 */
	@Override
	public String getName() {
		return nameOfPIN;

	}

	/**
	 * @return node name followed by node type
	 */
	@Override
	public String getNameAndType(){
		return nameOfPIN + " " + getType();
	}

	/**
	 * Record that a call is preceded by another PIN
	 * 
	 * @param pred
	 *            the predecessor of the call
	 */
	@Override
	public void addPredecessor(ProcedureInstanceNode pred) {
		//assert getId() > pred.getId() : getName() + " id = " + getId() + "  " + pred.getName() + " id = " + pred.getId();
		predecessors.add(pred);
	}

	/**
	 * Record that a call is succeeded by another PIN
	 * 
	 * @param suc
	 *            the successor of the call
	 */
	@Override
	public void addSuccessor(ProcedureInstanceNode suc) {
		successors.add(suc);
		provData.notifySuccessorEdgeCreated(this, suc);
	}

	/**
	 * Add an input parameter
	 * 
	 * @param paramName
	 *            the name of the parameter being bound
	 * @param value
	 *            the value being bound
	 * @throws ParameterAlreadyBoundException
	 *             thrown if the parameter already is bound to a value
	 */
	@Override
	public void addInput(String paramName, DataInstanceNode value)
		throws ParameterAlreadyBoundException {
		if (inputs.containsKey(paramName)) {
			// It is possible that the same argument is an input more than
			// once to a node.  This can happen if it is passed in to 2 or more
			// parameters, or if it is both passed in as a parameter and
			// accessed as a global.  This second condition can happen in DDGs
			// generated from R but not from Little-JIL.
			return;
			//throw new ParameterAlreadyBoundException(
					//"Parameter already bound:  " + paramName);
		} else {
			inputs.put(paramName, value);
			
			DataBindingEvent e 
				= new DataBindingEvent (DataBindingEvent.BindingEvent.INPUT, value, this, paramName);
			provData.notifyDataBindingListeners(e);
		}

	}

	/**
	 * @param paramName
	 *            the name of the input parameter whose corresponding DIN is
	 *            returned
	 * @return the value bound to the name
	 * @throws UnboundParameterException
	 *             thrown if there is no parameter with that name
	 */
	@Override
	public DataInstanceNode getInput(String paramName)
		throws UnboundParameterException {
		if (!inputs.containsKey(paramName)) {
			throw new UnboundParameterException("No such input parameter:  "
					+ paramName);
		} else {
			return inputs.get(paramName);
		}
	}

	/**
	 * Add an output parameter
	 * 
	 * @param paramName
	 *            the name of the parameter being bound
	 * @param value
	 *            the value being bound
	 * @throws ParameterAlreadyBoundException
	 *             thrown if the parameter is already bound to a value
	 */
	@Override
	public void addOutput(String paramName, DataInstanceNode value)
		throws ParameterAlreadyBoundException {
		if (outputs.containsKey(paramName)) {
			throw new ParameterAlreadyBoundException(
					"Parameter already bound:  " + paramName);
		} else {
			outputs.put(paramName, value);
			
			DataBindingEvent e 
				= new DataBindingEvent (DataBindingEvent.BindingEvent.OUTPUT, value, this, paramName);
			provData.notifyDataBindingListeners(e);
		}
	}

	/**
	 * @param paramName
	 *            the name of the output parameter whose corresponding DIN is
	 *            returned
	 * @return the value bound to the name of the output
	 * @throws UnboundParameterException
	 *             thrown if there is no parameter with that name
	 */
	@Override
	public DataInstanceNode getOutput(String paramName)
		throws UnboundParameterException {
		if (!outputs.containsKey(paramName)) {
			throw new UnboundParameterException("No such output parameter:  "
					+ paramName);
		} else {
			return outputs.get(paramName);
		}
	}

	/**
	 * @return the ID assigned to the DIN
	 */
	@Override
	public int getId() {
		return pinId;
	}

	/**
	 * Set the ID that will be assigned to the DIN Cannot assign an ID that has
	 * been assigned to any other PIN so far. Cannot assign a zero ID because
	 * all PINs are assigned zero IDs as they are constructed
	 * 
	 * @param newId
	 *            the new ID
	 * @throws IdAlreadySetException
	 */
	@Override
	public void setId(int newId) throws IdAlreadySetException {
		assert newId != 0;
		if (pinId == 0) {
			pinId = newId;
		} else {
			throw new IdAlreadySetException(
				"Cannot reset ID of a node that has already been assigned an ID");
		}
	}

	/**
	 * Return an iterator over the successors
	 * 
	 * @return an iterator over the PINs that are successors of the current PIN
	 */
	@Override
	public Iterator<ProcedureInstanceNode> successorIter() {
		return successors.iterator();

	}

	/**
	 * Return an iterator over the predecessors
	 * 
	 * @return an iterator over the PINs that are predecessors of the current
	 *         PIN
	 */
	@Override
	public Iterator<ProcedureInstanceNode> predecessorIter() {
		return predecessors.iterator();

	}

	/**
	 * Return an iterator over the input parameter names
	 * 
	 * @return an iterator over the names of the input parameters of the current
	 *         PIN
	 */
	@Override
	public Iterator<String> inputParamNames() {

		return inputs.keySet().iterator();
	}

	/**
	 * Return an iterator over the input parameter values
	 * 
	 * @return an iterator over the input parameter values
	 */
	@Override
	public Iterator<DataInstanceNode> inputParamValues() {
		return inputs.values().iterator();
	}

	/**
	 * Return an iterator over the output parameter names
	 * 
	 * @return an iterator over the output parameter names
	 */
	@Override
	public Iterator<String> outputParamNames() {
		return outputs.keySet().iterator();
	}

	/**
	 * Return an iterator over the output parameter values
	 * 
	 * @return an iterator over the output parameter values
	 */
	@Override
	public Iterator<DataInstanceNode> outputParamValues() {
		return outputs.values().iterator();
	}

	/**
	 * @return All DINs produced by a procedure instance node
	 */
	@Override
	public Set<DataInstanceNode> getProcessOutputsDerived() {
		HashSet<DataInstanceNode> processOutputs 
			= new HashSet<DataInstanceNode>();

		Iterator<DataInstanceNode> it = this.outputParamValues();
		while (it.hasNext()) {
			processOutputs.addAll(it.next().getProcessOutputsDerived());
		}

		Iterator<ProcedureInstanceNode> it1 = this.successorIter();
		while (it1.hasNext()) {
			processOutputs.addAll(it1.next().getProcessOutputsDerived());
		}

		return processOutputs;
	}

	/**
	 * @return the "raw data" input for a given PIN
	 */
	@Override
	public Set<DataInstanceNode> getProcessInputsDerived() {
		HashSet<DataInstanceNode> processInputs 
			= new HashSet<DataInstanceNode>();

		Iterator<DataInstanceNode> it = this.inputParamValues();
		while (it.hasNext()) {
			processInputs.addAll((it.next()).getProcessInputsDerived());
		}

		Iterator<ProcedureInstanceNode> it1 = this.predecessorIter();
		while (it1.hasNext()) {
			processInputs.addAll(it1.next().getProcessInputsDerived());
		}

		return processInputs;
	}

	/**
	 * @return the agent that carried out the procedure
	 */
	@Override
	public AgentConfiguration getAgent() {
		return agent;
	}
	
	/**
	 * @return a string description of this node
	 */
	@Override
	public String toString() {
		String s = getType() + " p" + pinId + " \"" + nameOfPIN + "\"\n" +
			inputEdgesToString() + outputEdgesToString() 
			+ predecessorEdgesToString();

		if (agent == null) {
			return s;
		}
		
		return s + agentToString();
	}
	
	private String agentToString() {
		return "A p" + pinId + " a" + agent.getId() + "\n";
	}
	
	private String inputEdgesToString() {
		StringBuilder sb = new StringBuilder();
		
		Iterator<DataInstanceNode> inputIter = inputParamValues();
		while (inputIter.hasNext()) {
			DataInstanceNode input = inputIter.next();
			sb.append("DF p" + pinId + " d" + input.getId() + "\n");
		}
		return sb.toString();
	}
	
	private String outputEdgesToString() {
		StringBuilder sb = new StringBuilder();
		
		Iterator<DataInstanceNode> outputIter = outputParamValues();
		while (outputIter.hasNext()) {
			DataInstanceNode input = outputIter.next();
			sb.append("DF d" + input.getId() + " p" + pinId + "\n");
		}
		return sb.toString();
	}
	
	private String predecessorEdgesToString() {
		if (predecessors == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for (ProcedureInstanceNode pred : predecessors) {
			sb.append("CF p" + pinId + " p" + pred.getId() + "\n");
		}
		return sb.toString();
	}
	
	/**
	 * @return a description of the successor edges for this node
	 */
	private String successorEdgesToString() {
		if (successors == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (ProcedureInstanceNode succ : successors) {
			sb.append("CF p" + succ.getId() + " p" + pinId + "\n");
		}
		return sb.toString();
		
	}

	/**
	 * @return an iterator over all the attribute names attached to this node. 
	 */
	@Override
	public Iterator<String> attributes() {
		return attributeValues.keySet().iterator();
	}

	/**
	 * @param name the name of the attribute
	 * @return the value associated with an attribute.  Returns null if the 
	 * 		attribute name is not known.
	 */
	@Override
	public Object getAttributeValue(String name) {
		return attributeValues.get(name);
	}

	/**
	 * Changes the value associated with an attribute.  Creates the attribute 
	 * if it is previously unknonwn.
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	@Override
	public void setAttribute(String name, Object value) {
		attributeValues.put(name, value);
	}

	/**
	 * @return the procedure definition that this node represents an execution 
	 * 	of
	 */
	@Override
	public Object getProcedureDefinition() {
		return procedureDefinition;
	}

	@Override
	public void setProcedureDefinition(Object procDef) {
		procedureDefinition = procDef;
	}

	@Override
	public int compareTo(ProcedureInstanceNode other) {
		return pinId - other.getId();
	}
	
	/**
	 * @return the line number in the script that corresponds to this node.
	 * @return
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Constructs the name to use for the node from the tokens
	 * @param tokens the tokens from the declaration
	 * @return the name to use
	 */
	public static String constructName(String nodeType, String nodeName) {
		if(nodeName == null){
			DDGExplorer.showErrMsg("Invalid node construct. No name given.");
			return null;
		}
		StringBuilder str = new StringBuilder();
		str.append(nodeName);

		// Concatenate node name and type for non-leaf nodes to distinguish
		// start, finish, etc. nodes
		if (isMultipleNodePIN(nodeType)){
			str.append(" " + nodeType);
		}
		return str.toString();
	}
	
	/**
	 * Returns true if this type of node corresponds to part of the execution of 
	 * a step.
	 * @param type the type of the node
	 * @return true if this type of node corresponds to part of the execution of 
	 * a step.
	 */
	private static boolean isMultipleNodePIN(String type) {
		// Parts of a non-leaf step
		if (type.equals("Start") || type.equals("Interm") || type.equals("Finish")) {
			return true;
		}

		// Parts of a virtual step
		if (type.equals("VStart") || type.equals("VInterm") || type.equals("VFinish")) {
			return true;
		}

		return false;
	}
}
