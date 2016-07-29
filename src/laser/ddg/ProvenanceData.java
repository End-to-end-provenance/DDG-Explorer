package laser.ddg;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import laser.ddg.persist.FileUtil;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * This is the class that is the root of all information. This is probably
 * incomplete. It should hold metadata about the process as well as serve as the
 * starting point for walking the data derivation graph beginning either with
 * the inputs or the outputs. The ProvenanceData object holds a collection of
 * all Data Instance Nodes and a collection of all process instance nodes; the
 * user needs to add each DIN and each PIN to the respective collection
 * separately from constructing the node itself. ProvenanceData also stores
 * Information about the agents used in the process, and holds collections of
 * inputs and of outputs of the process
 * 
 * @author B. Lerner & S. Taskova
 * 
 */

public class ProvenanceData {
	// Name of the process the provenance data is for
	private String processName;
	
	// The timestamp of the process the provenance data is for
	private String timestamp;
	
	// The language the provenance data process was written in
	private String language;
	
	// The path to the source file from which the ddg was created
	private String sourceDDGFile;
	
	// Attributes describing this ddg
	private Attributes attributes = new Attributes();
	
	// Information about the agents used in the process.
	private Set<AgentConfiguration> agentConfigurations;

	// All input DINs in the process
	private List<DataInstanceNode> processInputs;

	// All output DINs in the process
	private List<DataInstanceNode> processOutputs;

	// All Data Instance Nodes in a DDG
	private List<DataInstanceNode> dins;

	// All Procedure Instance Nodes in a DDG
	private List<ProcedureInstanceNode> pins;

	// The root procedure
	private Node root;

	// Map to/from resource URIs
	// private Hashtable<Node, Resource> nodesToResources;
	private Hashtable<Node, String> nodesToResources;

	// private Hashtable<Resource, Node> resourcesToNodes;
	private Hashtable<String, Node> resourcesToNodes;

	// The ID of the DIN that is incremented when the DIN is added to the dins
	// set
	private int nextDinId = 1;

	// The ID of the PIN that is incremented when the PIN is added to the pins
	// set
	private int nextPinId = 1;

	// The objects that want to be notified of data bindings.
	private List<DataBindingListener> bindingListeners = new LinkedList<DataBindingListener>();

	// Listeners to changes to the DDG
	private List<ProvenanceListener> provListeners = new LinkedList<ProvenanceListener>();

	// Table mapping function names to function bodies
	private Map<String, String> functionTable;
	
	// Table mapping named blocks to the code within those blocks.
	private Map<String, String> blockTable;
	
	// The query that this provenance data represents.
	private String query;
	
	// The name of the file containing the script/program executed to create this DDG.
	private String scriptFileName;

	/**
	 * Construct a default object
	 * 
	 * @param processName
	 *            the name of the process or activity this is the provenance
	 *            data for
	 */
	public ProvenanceData(String processName) {
		this.processName = processName;
		agentConfigurations = new TreeSet<AgentConfiguration>();
		pins = new LinkedList<ProcedureInstanceNode>();
		dins = new LinkedList<DataInstanceNode>();
		processInputs = new LinkedList<DataInstanceNode>();
		processOutputs = new LinkedList<DataInstanceNode>();

		nodesToResources = new Hashtable<Node, String>();
		resourcesToNodes = new Hashtable<String, Node>();
	}
	
	/**
	 * Construct a default object
	 * 
	 * @param processName
	 *            the name of the process or activity this is the provenance
	 *            data for
	 * @param timestamp
	 * 			the timestamp associated with the script
	 * @param language the language that the process was written in
	 */
	public ProvenanceData(String processName, String timestamp, String language) {
		this(processName);
		this.timestamp = timestamp;
		this.language = language;
	}

	/**
	 * Add an agent to the configuration
	 * 
	 * @param newAgent
	 *            the agent to add
	 * 
	 * */
	public synchronized void addAgent(AgentConfiguration newAgent) {
		agentConfigurations.add(newAgent);
	}

	/**
	 * Add a PIN to the PINs set
	 * 
	 * @param p
	 *            PIN to add
	 */
	public synchronized void addPIN(ProcedureInstanceNode p) {
		pins.add(p);
		p.setId(nextPinId);
		nextPinId++;
		notifyPinCreated(p);
	}

	/**
	 * Add a PIN to the PINs set. This should be called when the node is being
	 * read from a database.
	 * 
	 * @param pin
	 *            PIN to add
	 * @param resURI
	 *            The URI of the resource for this pin
	 */
	public synchronized void addPIN(ProcedureInstanceNode pin, String resURI) {
		if (!nodesToResources.containsKey(pin)) {
			this.nodesToResources.put(pin, resURI);
			this.resourcesToNodes.put(resURI, pin);
		}

		if (!pins.contains(pin)) {
			pins.add(pin);
			notifyPinCreated(pin);
		}
		
	}
	
	/**
	 * Add a PIN to the PINs set and sets its id. This should be called when the node is being
	 * created with the RDDGBuilder.
	 * 
	 * @param p
	 *            PIN to add
	 * @param id
	 *            the PIN's id number
	 */
	public synchronized void addPIN(ProcedureInstanceNode p, int id) {
		pins.add(p);
		p.setId(id);  // Using the one passed in as a parameter
		notifyPinCreated(p);
	}

	/**
	 * Add DIN to the process inputs set
	 * 
	 * @param idin
	 *            the DIN to add
	 */
	public synchronized void addInputDIN(DataInstanceNode idin) {
		processInputs.add(idin);
	}

	/**
	 * Add DIN to the process outputs set
	 * 
	 * @param odin
	 *            the DIN to add
	 */
	public synchronized void addOutputDIN(DataInstanceNode odin) {
		processOutputs.add(odin);
	}

	/**
	 * Add a DIN to the DINs set
	 * 
	 * @param d
	 *            DIN to add
	 */
	public synchronized void addDIN(DataInstanceNode d) {
		dins.add(d);
		d.setId(nextDinId);
		nextDinId += 1;
		notifyDinCreated(d);
	}

	/**
	 * Add a DIN to the DINs set method specific to RDF
	 * 
	 * @param d
	 *            DIN to add
	 * @param resURI
	 *            the URI of the JENA resource
	 * 
	 */
	public synchronized void addDIN(DataInstanceNode d, String resURI) {
		if (!nodesToResources.containsKey(d)) {
			resourcesToNodes.put(resURI, d);
			this.nodesToResources.put(d, resURI);
		}
		
		if (!dins.contains(d)) {
			dins.add(d);
			notifyDinCreated(d);
		}
	}
	
	/**
	 * Add a DIN to the DINs set and sets its Id number
	 * 
	 * @param d
	 *            DIN to add
	 * @param id
	 *            DIN id number to add
	 */
	public synchronized void addDIN(DataInstanceNode d, int id) {
		d.setId(id);
		dins.add(d);
		notifyDinCreated(d);
	}

	/**
	 * @return an iterator through all output DINs in the process
	 */
	public Iterator<DataInstanceNode> outputDinIter() {
		return processOutputs.iterator();
	}

	/**
	 * @param din
	 * @return resource corresponding to this DIN
	 */
	public String getResource(DataInstanceNode din) {
		return nodesToResources.get(din);
	}

	/**
	 * Records that the ddg node is represented by the rdf resource
	 * 
	 * @param node
	 *            the ddg node
	 * @param resURI
	 *            the URI for the rdf resource
	 */
	public void bindNodeToResource(Node node, String resURI) {
		nodesToResources.put(node, resURI);
	}

	/**
	 * Returns true if the rdf resource exists in the provenance data
	 * 
	 * @param resURI
	 *            the URI for the rdf resource
	 * @return true if the rdf resource exists in the provenance data
	 */
	public boolean containsResource(String resURI) {
		return nodesToResources.containsValue(resURI);
	}

	/**
	 * Gets the DDG node associated with a particular RDF resource
	 * 
	 * @param resURI
	 *            the URI of the resource to look up
	 * @return the associated DDG node
	 */
	public Node getNodeForResource(String resURI) {
		return resourcesToNodes.get(resURI);
	}

	/**
	 * @param pin
	 * @return resource corresponding to this PIN
	 */
	public String getResource(ProcedureInstanceNode pin) {
		return nodesToResources.get(pin);
	}

	/**
	 * @return an iterator through all input DINs in the process
	 */
	public Iterator<DataInstanceNode> inputDinIter() {
		return processInputs.iterator();
	}

	/**
	 * Create an iterator through the DINs
	 * 
	 * @return an iterator through the DINs
	 * 
	 * */
	public Iterator<DataInstanceNode> dinIter() {
		return dins.iterator();
	}

	/**
	 * Create an iterator through the PINs
	 * 
	 * @return an iterator through the PINs
	 * 
	 * */
	public Iterator<ProcedureInstanceNode> pinIter() {
		return pins.iterator();
	}

	/**
	 * Create an iterator through the agents
	 * 
	 * @return an iterator through the agents
	 * 
	 * */
	public Iterator<AgentConfiguration> agentIter() {
		return agentConfigurations.iterator();
	}

	/**
	 * Set the root procedure.
	 * 
	 * @param node
	 *            the procedure which will be the root
	 * 
	 * @throws RootAlreadySetException
	 *             if the root is already set
	 */
	public synchronized void setRoot(Node node)
			throws RootAlreadySetException {
		if (root == null) {
			root = node;
			notifyRootSet(root);

			// put root on top of list b/c prefuse visualizer expects root to
			// be added to graph first
			//pins.remove(sin);
			//pins.add(0, sin);

		} else {
			throw new RootAlreadySetException("Root already set");
		}
	}

	/**
	 * @return the root procedure
	 */
	public synchronized Node getRoot() {
		return root;
	}

	/**
	 * @param din
	 *            DIN to check for in the process outputs
	 * @return true if the DataInstanceNode on which the method is called is
	 *         among the outputs of the process
	 */
	public boolean isProcessOutput(DataInstanceNode din) {
		return processOutputs.contains(din);
	}

	/**
	 * @param din
	 *            DIN to check for in the process inputs
	 * @return true if the DataInstanceNode on which the method is called is
	 *         among the outputs of the process
	 */
	public boolean isProcessInput(DataInstanceNode din) {
		return processInputs.contains(din);
	}

	/**
	 * Returns a String representation of all the nodes, edges and agents held
	 * in this ProvenanceData object.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(pins.size() + "\n");

		// Agents
		for (AgentConfiguration agent : agentConfigurations) {
			sb.append(agent + "\n");
		}

		// All Data Instance Nodes in a DDG
		for (DataInstanceNode dataNode : dins) {
			sb.append(dataNode + "\n");
		}

		// All Procedure Instance Nodes in a DDG
		for (ProcedureInstanceNode procNode : pins) {
			sb.append(procNode + "\n");
		}

		return sb.toString();

	}

	/**
	 * Draw visual representation of DDG
	 */
	public void drawGraph() {
		PrefuseGraphBuilder graphBuilder = new PrefuseGraphBuilder();
		graphBuilder.setTitle(processName, timestamp);
		graphBuilder.drawGraph(this);
	}

	/**
	 * Add an object as a listener to data bindings
	 * 
	 * @param l
	 *            the new listener
	 */
	public void addDataBindingListener(DataBindingListener l) {
		bindingListeners.add(l);
	}

	/**
	 * Remove a data binding listener
	 * 
	 * @param l
	 *            the listener to remove
	 */
	public void removeDataBindingListner(DataBindingListener l) {
		bindingListeners.remove(l);
	}

	/**
	 * Notify data binding listeners of a new data binding
	 * 
	 * @param e
	 *            the event that provides details about the new data binding.
	 */
	void notifyDataBindingListeners(DataBindingEvent e) {
		for (DataBindingListener l : bindingListeners) {
			l.bindingCreated(e);
		}

		for (ProvenanceListener l : provListeners) {
			l.bindingCreated(e);
		}
	}

	/**
	 * Add a listener that will be notified of new nodes and edges being added
	 * to the provenance.
	 * @param l the listener to add
	 */
	public void addProvenanceListener(ProvenanceListener l) {
		provListeners.add(l);
	}

	/**
	 * Remove an existing listener from the provenance data
	 * @param l the listener to remove
	 */
	public void removeProvenanceListener(ProvenanceListener l) {
		provListeners.remove(l);
	}

	/**
	 * Notifies provenance listeners when a process is started
	 * 
	 * @param processName
	 *            the name of the process
	 */
	public void notifyProcessStarted(String processName) {
		if(this.timestamp != null){
			Iterator<ProvenanceListener> listeners = provListeners.iterator();
			while(listeners.hasNext()) {
				try {
					ProvenanceListener l = listeners.next();
					l.processStarted(processName, this, timestamp, language);
				} catch (RemoveListenerException e) {
					// Remove the last listener iterated
					listeners.remove();
				}
			}
		}else{
			Iterator<ProvenanceListener> listeners = provListeners.iterator();
			while(listeners.hasNext()) {
				try {
					ProvenanceListener l = listeners.next();
					l.processStarted(processName, this);
				} catch (RemoveListenerException e) {
					// Remove the last listener from iterated
					listeners.remove();
				}
			}
		}
	}

	/**
	 * Notifies provenance listeners when the root node is set
	 * 
	 * @param root the root node
	 */
	private void notifyRootSet(Node root) {
		Iterator<ProvenanceListener> listeners = provListeners.iterator();
		while(listeners.hasNext()) {
			ProvenanceListener l = listeners.next();
			l.rootSet(root);
		}
	}

	/**
	 * Notifies provenance listeners when a process is finished
	 */
	public void notifyProcessFinished() {
		for (ProvenanceListener l : provListeners) {
			l.processFinished();
		}
	}

	/**
	 * Notifies provenance listeners when a procedure node is added to the DDG
	 * 
	 * @param pin
	 *            the node added
	 */
	private void notifyPinCreated(ProcedureInstanceNode pin) {
		for (ProvenanceListener l : provListeners) {
			l.procedureNodeCreated(pin);
		}
	}

	/**
	 * Notfies provenance listeners when a data node is added the DDG
	 * 
	 * @param din
	 *            the node added
	 */
	private void notifyDinCreated(DataInstanceNode din) {
		for (ProvenanceListener l : provListeners) {
			l.dataNodeCreated(din);
		}
	}

	/**
	 * Notifies provenance listeners when a predecessor/successor edge is added
	 * to a DDG
	 * 
	 * @param predecessor
	 *            the predecessor procedure node
	 * @param successor
	 *            the successor procedure node
	 */
	synchronized void notifySuccessorEdgeCreated(
			ProcedureInstanceNode predecessor, ProcedureInstanceNode successor) {
		for (ProvenanceListener l : provListeners) {
			l.successorEdgeCreated(predecessor, successor);
		}
	}

	/**
	 * 
	 * Return the name of the process that was executed to create this ddg
	 * 
	 * @return the name of the process that was executed to create this ddg
	 */
	public String getProcessName() {
		return processName;
	}
	
	/**
	 * 
	 * Return the language the process was written in to create this ddg
	 * 
	 * @return the name of the language the process was written in to create this ddg
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Revert provenance data to a previous step [EXPERIMENTAL]
	 * 
	 * @param pin
	 * @return
	 */
	public Map<String, Serializable> revertTo(ProcedureInstanceNode pin) {
		Map<String, Serializable> artifacts = new HashMap<String, Serializable>();
		List<DataInstanceNode> outputParamList = new ArrayList<DataInstanceNode>();

		ProcedureInstanceNode currentpin = pin;
		Iterator<DataInstanceNode> outputParamValues = currentpin
				.outputParamValues();
		while (!outputParamValues.hasNext()
				&& currentpin.successorIter().hasNext()) {
			currentpin = currentpin.successorIter().next();
			outputParamValues = currentpin.outputParamValues();
		}
		if (!outputParamValues.hasNext()) {
			// TODO No artifacts
			return null;
		} else {
			while (outputParamValues.hasNext()) {
				outputParamList.add(outputParamValues.next());
			}
			int loc = dins.size();
			for (DataInstanceNode dinCandiate : outputParamList) {
				for (DataInstanceNode din : dins) {
					if (din.getId() == dinCandiate.getId() && din.getId() < loc) {
						loc = din.getId();
						break;
					}
				}
			}
			ListIterator<DataInstanceNode> listIterator = dins
					.listIterator(loc - 1);
			while (listIterator.hasPrevious()) {
				DataInstanceNode previous = listIterator.previous();
				if (previous.getType().equals("Exception")) {
					continue;
				}
				if (artifacts.get(previous.getName()) == null) {
					artifacts.put(previous.getName(), previous.getValue());
				}
			}
			return artifacts;
		}
	}

	public ProcedureInstanceNode drawRevertibleGraph() {
		PrefuseGraphBuilder prefuseGraphBuilder = new PrefuseGraphBuilder();
		prefuseGraphBuilder.setTitle(processName, timestamp);
		prefuseGraphBuilder.drawGraph(this);
		for (ProcedureInstanceNode pin : pins) {
			if (pin.getId() == prefuseGraphBuilder.getPinID()) {

				return pin;
			}
		}
		return null;

	}

	/**
	 * Returns the procedure instance node with the given id
	 * @param producer the id of the node to look for
	 * @return the node with that id.  Returns null if the node is not found.
	 */
	public ProcedureInstanceNode findPin(int producer) {
		Iterator<ProcedureInstanceNode> pinIt = pinIter();
		while(pinIt.hasNext()){
			ProcedureInstanceNode pCheck = pinIt.next();
			if(producer == pCheck.getId()){
				return pCheck;
			}
		}
		return null;
	}

	/**
	 * Returns the data instance node with the given id
	 * @param data the id of the node to look for
	 * @return the node with that id.  Returns null if the node is not found.
	 */
	public DataInstanceNode findDin(int data) {
		Iterator<DataInstanceNode> dinIt = dinIter();
		//("Looking for data node " + data + "  Found:");
		
		while(dinIt.hasNext()){
			DataInstanceNode dCheck = dinIt.next();
			//System.out.println("   " + dCheck.getId());
			if(data== dCheck.getId()){
				return dCheck;
			}
		}
		return null;
	}

	/**
	 * @return the timestamp associated with the file that contains
	 * 	the program that was executed to produce the ddg.  This is 
	 *  distinct from the timestamp associated with the execution of the
	 *  program that created this specific ddg. 
	 */
	public String getScriptTimestamp() {
		if (processName == null) {
			return null;
		}
		File programFile = new File(processName);
		if (!programFile.exists()) {
			// We don't have a full path to the program, so we can't
			// know the timestamp
			return null;
		}
		
		return FileUtil.getTimestamp(programFile);
	}

	/**
	 * @return the list of attribute names and values associated with this ddg,
	 * 	  separated by newlines
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
		attributes.set("DateTime", timestamp);
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setLanguage(String language) {
		this.language = language;
		attributes.set("Language", language);
	}
	
	public void setSourceDDGFile(String sourceDDGFile) {
		this.sourceDDGFile = sourceDDGFile;
		attributes.set("souceDDGFile", sourceDDGFile);
	}
	
	public void addAttribute(String name, String value) {
		attributes.set(name, value);
	}

	/**
	 * Parses the file that contains the source code for the program executed.
	 * Creates a table that allows us to look up function definitions given a
	 * function name.
	 */
	public void createFunctionTable() {
		createFunctionTable(processName);
	}

	/**
	 * Parses the file that contains the source code for the program executed.
	 * Creates a table that allows us to look up function definitions given a
	 * function name.
	 * @param fileName the name of the file that contains the function definitions
	 */
	public void createFunctionTable(String fileName) {
		LanguageParser scriptParser = LanguageConfigurator.createParser(language); 
		scriptFileName = fileName;
		if (scriptParser != null) {
			functionTable = scriptParser.buildFunctionTable(fileName);
			blockTable = scriptParser.buildBlockTable(fileName);
		}
		else {
			functionTable = new HashMap<String, String>();
			blockTable = new HashMap<String, String>();
		}
	}

	/**
	 * Looks up the definition of a function.  Returns an error string if there is more
	 * than one function with that name. 
	 * @param functionName the name of the function to look up
	 * @return the function definition
	 */
	public String getFunctionBody(String functionName) {
		return functionTable.get(functionName);
	}

	public String getBlockBody(String blockName) {
		return blockTable.get(blockName);
	}

	public String getScript() {
		return scriptFileName;
	}
	
	public String getSourcePath() {
		return sourceDDGFile;
	}

	public String getQuery() {
		return query;
	} 

	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Allows a visitor to operate on each pin in a ddg
	 * @param visitor the object that will operate on the pin
	 */
	public void visitPins(ProvenanceDataVisitor visitor) {
		Iterator<ProcedureInstanceNode> pinIterator = pinIter();

		while (pinIterator.hasNext()) {
			ProcedureInstanceNode nextPin = pinIterator.next();
			visitor.visitPin(nextPin);
		}
	}

	/**
	 * Allows a visitor to operate on each din in a ddg
	 * @param visitor the object that will operate on the din
	 */
	public void visitDins(ProvenanceDataVisitor visitor) {
		Iterator<DataInstanceNode> dinIterator = dinIter();
		while (dinIterator.hasNext()) {
			DataInstanceNode nextDin = dinIterator.next();
			visitor.visitDin(nextDin);
		}

	}

	/**
	 * Allows a visitor to operate on each data flow edge in a ddg
	 * @param visitor the object that will operate on the edge
	 */
	public void visitDataflowEdges(ProvenanceDataVisitor visitor) {
		Iterator<ProcedureInstanceNode> pinIterator = pinIter();
		while (pinIterator.hasNext()) {
			ProcedureInstanceNode nextPin = pinIterator.next();
			visitInputEdges(nextPin, visitor);
			visitOutputEdges(nextPin, visitor);
		}
	}

	/**
	 * Allows a visitor to operate on each input edge in a ddg
	 * @param visitor the object that will operate on the edge
	 */
	private void visitInputEdges(ProcedureInstanceNode pin, ProvenanceDataVisitor visitor) {
		Iterator<DataInstanceNode> inputIter = pin.inputParamValues();
		while (inputIter.hasNext()) {
			DataInstanceNode input = inputIter.next();
			visitor.visitInputEdge(input, pin);
		}
	}

	/**
	 * Allows a visitor to operate on each output edge in a ddg
	 * @param visitor the object that will operate on the edge
	 */
	private void visitOutputEdges(ProcedureInstanceNode pin, ProvenanceDataVisitor visitor) {

		Iterator<DataInstanceNode> outputIter = pin.outputParamValues();
		while (outputIter.hasNext()) {
			DataInstanceNode output = outputIter.next();
			visitor.visitOutputEdge(pin, output);
		}
	}

	/**
	 * Allows a visitor to operate on each control flow edge in a ddg where the given node
	 * is the successor
	 * @param pin the node whose edges to predecessors are visited
	 * @param visitor the object that will operate on the edge
	 */
	public void visitControlFlowEdges(ProcedureInstanceNode pin, ProvenanceDataVisitor visitor) {
		Iterator<ProcedureInstanceNode> predecessors = pin.predecessorIter();
		while (predecessors.hasNext()) {
			ProcedureInstanceNode pred = predecessors.next();
			visitor.visitControlFlowEdge(pred, pin);
		}
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}


}
