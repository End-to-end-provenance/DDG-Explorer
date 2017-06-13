package laser.ddg;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import laser.ddg.persist.FileUtil;
import laser.ddg.visualizer.PrefuseGraphBuilder;

public class ScriptProvenanceData {

	// Name of the process the provenance data is for
	private String processName;

	// The list of script nodes to be used
	private List<ScriptNode> scriptNodes;

	// The root procedure
	private Node root;

	// The execution timestamp of the ddg
	private String timestamp;

	// The path to the source file from which the ddg was created
	private String sourceDDGFile;

	// Attributes describing this ddg
	private Attributes attributes = new Attributes();

	// The objects that want to be notified of data bindings.
	private List<DataBindingListener> bindingListeners = new LinkedList<>();

	// Listeners to changes to the DDG
	private List<ProvenanceListener> provListeners = new LinkedList<>();

	// All input scripts in the process
	private List<ScriptNode> processInputs;

	// All output scripts in the process
	private List<ScriptNode> processOutputs;

	// Map to/from resource URIs
	// private Hashtable<Node, Resource> nodesToResources;
	private Map<Node, String> nodesToResources;

	// private Hashtable<Resource, Node> resourcesToNodes;
	private Map<String, Node> resourcesToNodes;

	// Information about the agents used in the process.
	private Set<AgentConfiguration> agentConfigurations;

	// The ID of the script that is incremented when the script is added to the scripts
	// set
	private int nextScriptId = 1;

	// Information about all of the scripts that are executed
	// to create the ddg.  The main script is in position 0.
	// The other entries are for sourced scripts, in the order
	// that they are listed in the attributes at the top of the
	// ddg.  The order is important since procedure nodes refer
	// to source & line numbers which we use to display the
	// source code to the user.
	private List<ScriptInfo> scripts = new ArrayList<>();

	public ScriptProvenanceData(String processName) {
		this.processName = processName;
		agentConfigurations = new TreeSet<>();
		scriptNodes = new LinkedList<>();
		processInputs = new LinkedList<>();
		processOutputs = new LinkedList<>();

		nodesToResources = new ConcurrentHashMap<>();
		resourcesToNodes = new ConcurrentHashMap<>();
	}

	public Iterator<ScriptNode> scriptIter() {
		return scriptNodes.iterator();
	}

	// This can be used later to draw the graph possibly.
	public void drawGraph() {
		PrefuseGraphBuilder graphBuilder = new PrefuseGraphBuilder();
		//graphBuilder.setTitle(processName, timestamp);
		//graphBuilder.drawGraph(this);
	}

	public synchronized void addScriptNode(ScriptNode s) {

		scriptNodes.add(s);
		s.setId(nextScriptId);
		nextScriptId++;
		//notifyScriptCreated(s);
	}
	
	public Iterator<ScriptNode> outputScriptIter() {
		return processOutputs.iterator();
	}

	public synchronized void addInputScript(ScriptNode iscr) {
		processInputs.add(iscr);
	}

	public synchronized void addOutputScript(ScriptNode oscr) {
		processInputs.add(oscr);
	}

	public String getResource(ScriptNode scr) {
		return nodesToResources.get(scr);
	}

	public void bindNodeToResource(Node node, String resURI) {
		nodesToResources.put(node, resURI);
	}

	public boolean containsResource(String resURI) {
		return nodesToResources.containsValue(resURI);
	}

	public Node getNodeForResource(String resURI) {
		return resourcesToNodes.get(resURI);
	}

	public Iterator<ScriptNode> inputScriptIter() {
		return processInputs.iterator();
	}

	public Iterator<AgentConfiguration> agentIter() {
		return agentConfigurations.iterator();
	}

	public synchronized void setRoot(Node node)
			throws RootAlreadySetException {
		if (root == null) {
			root = node;
			notifyRootSet(root);

		} else {
			throw new RootAlreadySetException("Root already set");
		}
	}

	public synchronized Node getRoot() {
		return root;
	}

	public boolean isProcessOutput(ScriptNode scr) {
		return processOutputs.contains(scr);
	}

	public boolean isProcessInput(ScriptNode scr) {
		return processInputs.contains(scr);
	}

	public void addDataBindingListener(DataBindingListener l) {
		bindingListeners.add(l);
	}

	public void removeDataBindingListner(DataBindingListener l) {
		bindingListeners.remove(l);
	}

	void notifyDataBindingListeners(DataBindingEvent e) {
		for (DataBindingListener l : bindingListeners) {
			l.bindingCreated(e);
		}

		for (ProvenanceListener l : provListeners) {
			l.bindingCreated(e);
		}
	}

	public void addProvenanceListener(ProvenanceListener l) {
		provListeners.add(l);
	}

	public void removeProvenanceListener(ProvenanceListener l) {
		provListeners.remove(l);
	}
	
	private void notifyRootSet(Node root) {
		Iterator<ProvenanceListener> listeners = provListeners.iterator();
		while(listeners.hasNext()) {
			ProvenanceListener l = listeners.next();
			l.rootSet(root);
		}
	}

	public void notifyProcessFinished() {
		for (ProvenanceListener l : provListeners) {
			l.processFinished();
		}
	}

	synchronized void notifySuccessorEdgeCreated(
			ProcedureInstanceNode predecessor, ProcedureInstanceNode successor) {
		for (ProvenanceListener l : provListeners) {
			l.successorEdgeCreated(predecessor, successor);
		}
	}

	public String getProcessName() {
		return processName;
	}
	
	public ScriptNode findScript(int producer) {
		Iterator<ScriptNode> scrIt = scriptIter();
		while(scrIt.hasNext()){
			ScriptNode sCheck = scrIt.next();
			if(producer == sCheck.getId()){
				return sCheck;
			}
		}
		return null;
	}
	
	public String getScriptTimestamp() {
		if (scripts != null && scripts.size() >= 1) {
			return scripts.get(0).getTimestamp();
		}
		
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
	
	public void setSourceDDGFile(String sourceDDGFile) {
		this.sourceDDGFile = sourceDDGFile;
		attributes.set("souceDDGFile", sourceDDGFile);

	}

	public File getSourceDDGDirectory(){
		File thefile = new File(sourceDDGFile);
		return thefile.getParentFile();
	}
	
	public void addAttribute(String name, String value) {
		attributes.set(name, value);
	}
	
	public String getScriptPath(int which) {
		return scripts.get(which).getFilepath();
	}

	public String getSourcePath() {
		return sourceDDGFile;
	}
	
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
		
		// Use the attribute information about the main and the sourced scripts
		// to build the list of scripts referenced so that we will be able to
		// find the source code later.
		
		
		// Json files will have this set already
		scripts = attributes.getSourcedScriptInfo();
		if (scripts == null) {
			// for ddg.txt files, we need to build the list here
			scripts = new ArrayList<>();

			// Include the main script that was executed
			String mainScriptName = attributes.get(Attributes.MAIN_SCRIPT_NAME);
			String mainScriptTimestamp = attributes.get(Attributes.MAIN_SCRIPT_TIMESTAMP);
			scripts.add(new ScriptInfo(mainScriptName, mainScriptTimestamp));
			
			File mainScript = new File(mainScriptName);
			File scriptDir = mainScript.getParentFile();

			// Include all the scripts included via a call to R's source function
			String sourcedScriptList = attributes.get(Attributes.SOURCED_SCRIPT_NAMES);
			if (sourcedScriptList == null) {
				return;
			}
			String[] sourcedScriptNames = sourcedScriptList.split(",");
	
			String scriptTimestampList = attributes.get(Attributes.SCRIPT_TIMESTAMPS);
			if (scriptTimestampList == null) {
				return;
			}
			String[] sourcedScriptTimestamps = scriptTimestampList.split(",");
			assert sourcedScriptNames.length == sourcedScriptTimestamps.length;
			
			for (int i = 0; i < sourcedScriptNames.length; i++) {
				scripts.add(new ScriptInfo(scriptDir + File.separator + sourcedScriptNames[i], sourcedScriptTimestamps[i]));
			}
		}
		
		// System.out.println(attributes.toString());
		
	}

	public List<ScriptInfo> scripts() {
		return scripts;
	}
	
}
