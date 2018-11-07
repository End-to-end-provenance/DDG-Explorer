package laser.ddg.persist;

import java.text.DateFormat;
import java.util.Calendar;

import laser.ddg.DataBindingEvent;
import laser.ddg.DataBindingEvent.BindingEvent;
import laser.ddg.workflow.ScriptNode;
import laser.ddg.DataInstanceNode;
import laser.ddg.Node;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.ProvenanceListener;
import edu.harvard.pass.cpl.CPL;
import edu.harvard.pass.cpl.CPLObject;

/**  To run the with CPLWriter, DDGBuilder must use this instead of JenaWriter.
 *   The command line is:
 *   
 *   java -Djava.library.path=".:/usr/lib" -jar ~/Documents/LJilJarFiles/jrunner.jar <jul-file>
 *   
 * @author Barbara Lerner
 * @version Aug 21, 2012
 *
 */
public class CPLWriter implements ProvenanceListener {

	private ProvenanceData provData;

	@Override
	public void processStarted(String processName, ProvenanceData provData) {
		//get the language and timestamp and pass it on to the other processStarted constructor 
		String timestamp = addDDG(provData.getProcessName());
		String language = provData.getLanguage();
		processStarted(processName, provData, timestamp, language);
	}
	
	@Override
	public void processStarted(String processName, ProvenanceData provData,String timestamp, String language) {
		// This creates a model that persists in a CPL database.
		//System.out.println("Attaching to CPL database");
		// System.getProperties().list(System.out);
		CPL.attachODBC("DSN=CPL");
		this.provData = provData;
		
		addProcessNameToDB(processName, timestamp);
	}

	private String addDDG(String processName) {
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.FULL,
				DateFormat.FULL).format(Calendar.getInstance());
		return timestamp;
	}

	private void addProcessNameToDB(String processName, String timestamp) {
        CPLObject processObj = new CPLObject("Little-JIL", processName, "Process");

		//System.out.println("Adding process " + processName);
		processObj.addProperty("numExecutions", "1");
		processObj.addProperty ("timestamp", timestamp);
	}

	@Override
	public void processFinished() {
		// TODO Auto-generated method stub

	}

	@Override
	/* Problem:  Currently procedure and data nodes do not know which ddg they are a part of
	 * Using Orbiter, it will just use the last node with a particular name, but the names
	 * I am using are unique for the ddg, not unique for the database.  So each execution
	 * of the same process will create new procedure and data nodes with the same name.
	 * Could incorporate the procees name & timestamp into the node names, but that will make
	 * the graphs produced by Orbiter harder to read.
	 */
	public void procedureNodeCreated(ProcedureInstanceNode pin) {
		CPLObject procNode = new CPLObject("Little-JIL", pin.getName() + " " + pin.getId(), "Process");
		provData.bindNodeToResource(pin, procNode.toString());
		procNode.addProperty("Node type", procNode.getType());
		//System.out.println("Adding step " + pin.getName() + " " + pin.getId());
	}

	@Override
	public void dataNodeCreated(DataInstanceNode din) {
		CPLObject dataNode = new CPLObject("Little-JIL", din.getName() + " " + din.getId(), "Artifact");
		//System.out.println("Adding data " + din.getName() + " " + din.getId());
		provData.bindNodeToResource(din, dataNode.toString());
		dataNode.addProperty("Node type", din.getType());
		dataNode.addProperty("value", din.getValue().toString());
	}

	@Override
	/*
	 * Problem:  these lookups will get the last nodes with these names.  That is probably what we want
	 * if we can assume that we are not running the same process multiple times concurrently.
	 * 
	 * Same for creating the control flow edges
	 */
	public void bindingCreated(DataBindingEvent e) {
		CPLObject procObj = CPLObject.lookup("Little-JIL", e.getProcNode().getName() + " " + e.getProcNode().getId(), "Process");
		CPLObject dataObj = CPLObject.lookup("Little-JIL", e.getDataNode().getName() + " " + e.getDataNode().getId(), "Artifact");

		if (e.getEvent() == BindingEvent.INPUT) {
			procObj.dataFlowFrom(dataObj);
		}
		
		else {
			dataObj.dataFlowFrom(procObj);
		}
	}

	@Override
	public void successorEdgeCreated(ProcedureInstanceNode predecessor,
			ProcedureInstanceNode successor) {
		CPLObject predObj = CPLObject.lookup("Little-JIL", predecessor.getName() + " " + predecessor.getId(), "Process");
		CPLObject succObj = CPLObject.lookup("Little-JIL", successor.getName() + " " + successor.getId(), "Process");
		succObj.controlledBy(predObj);
	}

	@Override
	public void rootSet(Node root) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scriptNodeCreated(ScriptNode sn) {
		// TODO Auto-generated method stub
		
	}

}
