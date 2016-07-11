package laser.ddg.persist;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import laser.ddg.Attributes;
import laser.ddg.DataBindingEvent;
import laser.ddg.DataBindingEvent.BindingEvent;
import laser.ddg.DataInstanceNode;
import laser.ddg.Node;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.ProvenanceListener;
import laser.ddg.RemoveListenerException;
import laser.ddg.gui.DDGExplorer;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Writes an RDF model to a Jena database.  It does this incrementally as
 * the process executes.
 * 
 * @author Barbara Lerner
 * @version Jun 21, 2012
 *
 */
public class JenaWriter extends AbstractDBWriter implements ProvenanceListener {
	// The singleton
	private static JenaWriter instance;
	
	private Dataset dataset;
	private Properties props;
	private String processName;
	private String processURI;

	private String ddgURI;
	
	/**
	 * Open a connection to the Jena database for writing.
	 */
	private JenaWriter() {
		//System.out.println("Connecting to the Jena database");
		dataset = RdfModelFactory.getDataset();
	}
	
	/**
	 * Return the singleton object that writes DDGs to the database
	 * @return the writer
	 */
	public static JenaWriter getInstance() {
		if (instance == null) {
			instance = new JenaWriter();
		}
		return instance;
	}
	
	/**
	 * Saves the DDG to a Jena database.  Creates a transaction to do the saving within.
	 */
	@Override
	protected void saveDDG(ProvenanceData provData) {
		// System.out.println("JenaWriter.persistDDG requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.persistDDG got write lock.");
		try {
			addNodesAndEdges(provData);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.persistDDG releasing write lock.");
			dataset.end();
			//printDBContents();
		}
	}
		/**
	 * Connects to the Jena database and adds header information for this DDG
	 * @param processPathName the name of the process being executed.  This can be a full path or just a process or filename
	 * @param provData the object that will hold the in-memory ddg
	 */
	@Override
	public void processStarted(String processPathName, ProvenanceData provData) throws RemoveListenerException  {	
		String time = createTimestamp();
		String lang = provData.getLanguage();
		processStarted(processPathName, provData, time, lang);
	}

	/**
	 * Actually creates the database and loader for a given DDG process
	 * 
	 * @param processPathName name of the process
	 * @param provData the provenance data object associated with the process
	 * @param executionTimestamp timestamp of the process
	 * @param language the language the process was written in
	 * @throws RemoveListenerException thrown when a duplicate process and timestamp is encountered
	 */
	@Override
	public void processStarted(String processPathName, ProvenanceData provData,String executionTimestamp, String language) throws RemoveListenerException {
		if (alreadyInDB(processPathName, executionTimestamp, language)) {
			//Do not put anything into the database, remove the listener
			throw new RemoveListenerException();
		}

		initializeDDGinDB(provData, executionTimestamp, language);
	}

	/**
	 * Creates the header information in the database for this ddg
	 * @param provData the in-memory ddg
	 * @param executionTimestamp the time the ddg was created
	 * @param language the language that the program was written in that was executed to 
	 * 		create the ddg
	 */
	@Override
	protected void initializeDDGinDB(ProvenanceData provData,
			String executionTimestamp, String language) {
		try {
			props = new Properties(processName, executionTimestamp);
			//ErrorLog.showErrMsg("Created properties\n");
			String processFileTimestamp = provData.getScriptTimestamp();
			//ErrorLog.showErrMsg("processFileTimestamp = " + processFileTimestamp + "\n");
			Attributes attributes = provData.getAttributes();
			//ErrorLog.showErrMsg("Retrieved attributes\n");
				
			if (processURI == null) {
				//ErrorLog.showErrMsg("Adding new script to DB\n");
				processURI = addProcessNameToDB(executionTimestamp, language, attributes, processFileTimestamp);
			} else {
				//ErrorLog.showErrMsg("Updating counter for script in DB\n");
				updateProcessExecutionCounter(executionTimestamp, language, attributes, 
						processFileTimestamp);
			}
			//ErrorLog.showErrMsg("Initialization in DB done\n");
		} catch (Exception e) {
			DDGExplorer.showErrMsg("Unable to initialize the database for the DDG.\n");
			DDGExplorer.showErrMsg(e + "\n");
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Return true if a ddg for the given process and timestamp is already stored in the database
	 * @param processPathName the name of the process
	 * @param executionTimestamp the time at which the process was executed
	 * @return true if this ddg is already stored in the database
	 */
	@Override
	public boolean alreadyInDB(String processPathName, String executionTimestamp, String language) {
		try {
			//get processName from processPathName
			processName = FileUtil.getPathDest(processPathName, language);
			//ErrorLog.showErrMsg("script = " + processName + "\n");
			
			JenaLoader jenaLoader = JenaLoader.getInstance();
			processURI = jenaLoader.getProcessURI(processName);
			//ErrorLog.showErrMsg("script URI = " + processURI + "\n");
			
			if (processURI == null) {
				return false;
			}
			
			//If process is already in the database determine if it is the same one by checking the timestamp
			List<String> timestampList = jenaLoader.getTimestamps(processName);
			return timestampList.stream().anyMatch((temp) -> (temp.equals(executionTimestamp)));
		} catch (Exception e) {
			DDGExplorer.showErrMsg("Error when trying to determine if the DDG is already in the database.\n");
			DDGExplorer.showErrMsg(e.toString());
			e.printStackTrace(System.err);
			return false;
		}
	}

	/**
	 * Just returns the timestamp of a process if it was not provided
	 * 
	 * @return the timestamp
	 */
	private static String createTimestamp() {
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.FULL,
				DateFormat.FULL).format(Calendar.getInstance().getTime());
		return timestamp;

	}

	// Inside a write transaction
	private void updateProcessExecutionCounter(String timestamp, String language,
			Attributes attributes, String processFileTimestamp) {
		JenaLoader jenaLoader = JenaLoader.getInstance();
		int numExecutions = jenaLoader.getNumExecutions(processURI);
		numExecutions++;
		// System.out.println("Updating numExecutions of " + processURI + " in DB to " + numExecutions);
		// System.out.println("JenaWriter.updateProcessExecutionCounter requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.updateProcessExecutionCounter got write lock.");
		try {
			Model model = dataset.getDefaultModel();
			Resource processResource = model.getResource(processURI);
			Statement numExecutionssProperty = processResource.getProperty(props.getNumExecutions(model));
			numExecutionssProperty.changeLiteralObject(numExecutions);
			persistAttributes(timestamp, numExecutions-1, model, language, attributes,
					processFileTimestamp);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.updateProcessExecutionCounter releasing write lock.");
			dataset.end();
		}
	}

	// Inside a write transaction
	private String addProcessNameToDB(String timestamp, String language,
			Attributes attributes, String processFileTimestamp) {
		JenaLoader jenaLoader = JenaLoader.getInstance();
		int numProcesses = jenaLoader.getNumProcesses();
		//System.out.println("numProcesses = " + numProcesses + " (different process definitions executed");
		processURI = Properties.ALL_PROCESSES_URI + numProcesses;
		// System.out.println("JenaWriter.addProcessNameToDB requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.addProcessNameToDB got write lock.");
		try {
			Model model = dataset.getDefaultModel();
			Resource newProcess = model.createResource(processURI);
			// System.out.println("Adding process " + processName + " to model " + System.identityHashCode(model));
			Property processNameProperty = props.getProcessNameProperty(model);

			newProcess.addProperty(processNameProperty, processName);
			numProcesses++;
			
			if (numProcesses == 1) {
				// System.out.println("num processes in db is 0");
				Resource allProcesses = model.createResource(Properties.ALL_PROCESSES_URI);
				Property numProcessesProperty = props.getNumProcessesProperty(model);
				allProcesses.addLiteral(numProcessesProperty, numProcesses);
			}
			else {
				// System.out.println("Updating numProcesses in DB to " + numProcesses);
				Resource allProcesses = model.getResource(Properties.ALL_PROCESSES_URI);
				Statement numProcessesProperty = allProcesses.getProperty(props.getNumProcessesProperty(model));
				numProcessesProperty.changeLiteralObject(numProcesses);
			}
			
			// System.out.println("This is the first execution of " + processName + " in the db");
			Property processExecutionsProperty = props.getNumExecutions(model);
			newProcess.addLiteral(processExecutionsProperty, 1);
			
			persistAttributes(timestamp, 0, model, language, attributes, processFileTimestamp);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.addProcessNameToDB releasing write lock.");
			dataset.end();
		}

		return processURI;
	}

	// Inside a transaction
	private void persistAttributes(String timestamp, int executionId, Model model, String language,
			Attributes attributes, String processFileTimestamp) {
		ddgURI = processURI + "/" + executionId;
		Resource newExecution = model.createResource(ddgURI);
		Property processNameProperty = props.getProcessNameProperty(model);
		newExecution.addProperty(processNameProperty, processName);
		
		Property timeStampProperty = props.getTimestamp(model);
		newExecution.addProperty(timeStampProperty, timestamp);
		if(language != null){
			Property languageProperty = props.getLanguage(model);
			newExecution.addProperty(languageProperty, language);
		}
                attributes.names().stream().forEach((name) -> {
                    String value = attributes.get(name);
                    Property prop = props.getProperty(model, name);
                    newExecution.addProperty(prop, value);
                });
		if(processFileTimestamp != null){
			Property processFileTimestampProperty = props.getProcessFileTimestamp(model);
			newExecution.addProperty(processFileTimestampProperty, processFileTimestamp);
		}
	}

	/**
	 * Makes the din persistent
	 */
	@Override
	public void dataNodeCreated(DataInstanceNode din) {
		// System.out.println("JenaWriter.dataNodeCreated requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.dataNodeCreated got write lock.");
		try {
			persistDin(din);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.dataNodeCreated releasing write lock.");
			dataset.end();
		}
	}

	/**
	 * @param din
	 */
	@Override
	public void persistDin(DataInstanceNode din) {
		Model model = dataset.getDefaultModel();
		String resourceId = props.getDinResourceId(din);
		Resource newDin = model.createResource(resourceId);
		getProvData().bindNodeToResource(din, newDin.getURI());
		//	System.out.println("Adding name " + din.getName() + " to resource "
		//	+ resourceId + " in model " + System.identityHashCode(model) + " with value " + din.getValue().toString());
		//System.out.println("Adding name " + din.getName() + " to resource "
		//		+ resourceId + " in model " + System.identityHashCode(model));
		newDin.addProperty(props.getDDG(model), ddgURI);
		newDin.addProperty(props.getDinName(model), din.getName());
		newDin.addLiteral(props.getDinDDGId(model), din.getId());
		newDin.addProperty(props.getDinType(model), din.getType());
		if(din.getValue() != null){
			newDin.addProperty(props.getDinValue(model), din.getValue().toString());
		}
		if(din.getCreatedTime() != null){
			newDin.addProperty(props.getTimestamp(model), din.getCreatedTime());
		}
		if(din.getLocation() != null){
			newDin.addProperty(props.getLocation(model), din.getLocation());
		}
	}

	/**
	 * Makes the pin persistent
	 */
	@Override
	public void procedureNodeCreated(ProcedureInstanceNode pin) {
		// System.out.println("JenaWriter.procedureNodeCreated requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.procedureNodeCreated got write lock.");
		try {
			persistSin(pin);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.procedureNodeCreated releasing write lock.");
			dataset.end();
		}
	}

	/**
	 * @param sin
	 */
	@Override
	public void persistSin(ProcedureInstanceNode sin) {
		Model model = dataset.getDefaultModel();
		Resource newSin = model.createResource(props.getSinResourceId(sin));
			
		getProvData().bindNodeToResource(sin, newSin.getURI());
		//System.out.println("Adding name " + sin.getName()
		//			+ " to resource" + newSin);

		newSin.addProperty(props.getDDG(model), ddgURI);
		newSin.addProperty(props.getSinName(model), sin.getName());
		newSin.addProperty(props.getSinType(model), sin.getType());
		newSin.addLiteral (props.getSinDDGId(model), sin.getId());
		newSin.addLiteral (props.getSinElapsedTime(model), sin.getElapsedTime());
		newSin.addLiteral (props.getSinLineNumber(model), sin.getLineNumber());
			
		Object procDef = sin.getProcedureDefinition();
		if (procDef == null) {
			newSin.addProperty(props.getStepProperty(model), "Dummy Step Def");
		}
		else if (procDef instanceof String){
			newSin.addProperty(props.getStepProperty(model), (String) procDef);
		}
		else {
			newSin.addProperty(props.getStepProperty(model), "Dummy Step Def");
		}
	}

	/**
	 * Adds the successor/predecessor properties to the nodes that are connected.
	 */
	@Override
	public void successorEdgeCreated(ProcedureInstanceNode predecessor,
			ProcedureInstanceNode successor) {
		// System.out.println("JenaWriter.successorEdgeCreated requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.successorEdgeCreated got write lock.");
		try {
			persistSuccessorEdge(predecessor, successor);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.successorEdgeCreated releasing write lock.");
			dataset.end();
		}
	}
	
	/**
	 * Saves a control flow edge to a Jena database.  Assumes that it is called
	 * from within a transaction.
         * @param predecessor
         * @param successor
	 */
	@Override
	public void persistSuccessorEdge(ProcedureInstanceNode predecessor,
		ProcedureInstanceNode successor) {
		Model model = dataset.getDefaultModel();
		ProvenanceData provData = getProvData();
		Resource succResource = model.getResource(provData.getResource(successor));
		Resource predResource = model.getResource(provData.getResource(predecessor));
		succResource.addProperty(props.getSinPredecessors(model), predResource);
		predResource.addProperty(props.getSinSuccessors(model), succResource);
	}

	/**
	 * Adds the input and output properties to the pin and user/producer properties to the din
	 */
	@Override
	public void bindingCreated(DataBindingEvent e) {
		// System.out.println("JenaWriter.bindingCreated requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaWriter.bindingCreated got write lock.");
		try {
			persistBinding(e);
			dataset.commit();
		} finally {
			// System.out.println("JenaWriter.bindingCreated releasing write lock.");
			dataset.end();
		}
	}
	
	private void persistBinding(DataBindingEvent e) {
		if (e.getEvent() == BindingEvent.INPUT) {
			persistInputEdge(e.getProcNode(), e.getDataNode());
		}
			
		else {
			persistOutputEdge(e.getProcNode(), e.getDataNode());
		}
	}

	/**
	 * Saves an input edge to a Jena database.  Assumes that it is called
	 * from within a transaction.
         * @param pin
         * @param din
	 */
	@Override
	public void persistInputEdge(ProcedureInstanceNode pin, DataInstanceNode din) {
		Model model = dataset.getDefaultModel();
		ProvenanceData provData = getProvData();
		Resource proc = model.getResource(provData.getResource(pin));
		Resource data = model.getResource(provData.getResource(din));
		proc.addProperty(props.getSinInputs(model), data);
		data.addProperty(props.getDinUsers(model), proc);
	}

	/**
	 * Saves an output edge to a Jena database.  Assumes that it is called
	 * from within a transaction.
         * @param pin
         * @param din
	 */
	@Override
	public void persistOutputEdge(ProcedureInstanceNode pin, DataInstanceNode din) {
		Model model = dataset.getDefaultModel();
		ProvenanceData provData = getProvData();
		Resource proc = model.getResource(provData.getResource(pin));
		Resource data = model.getResource(provData.getResource(din));
		proc.addProperty(props.getSinOutputs(model), data);
		data.addProperty(props.getDinProducer(model), proc);
	}

	/**
	 * Closes the model, causing it to get flushed to disk.
	 */
	@Override
	public void processFinished() {
		printDBContents();
	}
	
	/**
	 * Dumps the entire contents of the database to standard output.
	 */
	@Override
	public void printDBContents() {
		// System.out.println("JenaWriter.printDBContents requesting read lock.");
		dataset.begin(ReadWrite.READ);
		// System.out.println("JenaWriter.printDBContents got read lock.");
		try {
			Model model = dataset.getDefaultModel();
			model.write(System.out);
		} finally {
			// System.out.println("JenaWriter.printDBContents releasing read lock.");
			dataset.end();
		}
	}
	
	/** No-op  */
	@Override
	public void rootSet(Node root) {
		// TODO Auto-generated method stub
		
	}

}
