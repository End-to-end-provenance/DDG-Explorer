package laser.ddg.persist;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.DataInstanceNode;
import laser.ddg.FileInfo;
import laser.ddg.LanguageConfigurator;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;

/**
 * This class reads provenance data from a Jena database.
 * 
 * @author Sophia. Created Jan 10, 2012.
 */

public class JenaLoader {
	// The singleton instance
	private static JenaLoader instance;
	
	/** URL that defines rdf syntax */
	public static final String RDF_PREFIX 
		= "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
	
	/* Constants to simplify query construction */
	private static final String PROCESS_PREFIX = "PROCESS:";
	private static final String DIN_PREFIX = "DIN:";
	private static final String SIN_PREFIX = "SIN:";
	private static final String SIN_DDG_ID = SIN_PREFIX + Properties.DDG_ID;

	// The provenance graph being constructed
	private ProvenanceData pd;
	
	// The database object
	private Dataset dataset;
	
	// Helper object that encapsulates the database properties.
	private Properties prop;
	
	// Basic information about the DDG being loaded
	private String processName;
	private String timestamp;
	private String language;
	private String ddgURI;
	
	//language specific loader
	private DDGBuilder load;	
	
	/**
	 * Create an object that can read data from a Jena database
	 */
	private JenaLoader() {
		dataset = RdfModelFactory.getDataset();

		//printDBContents();
	}
	
	/**
	 * Returns the singleton object that can read from the Jena database
	 * @return the loader
	 */
	public static JenaLoader getInstance() {
		if (instance == null) {
			instance = new JenaLoader();
		}
		return instance;
	}
	
	/**
	 * @return a list of the names of processes that have DDGs stored in this database
	 */
	public List<String> getAllProcessNames() {
		String queryVar = "name";
		
		String selectProcessNamesQueryString = JenaLoader.RDF_PREFIX
				+ "SELECT ?" + queryVar + " WHERE  { ?res <" + Properties.PROCESS_NAME_URI + "> ?" + queryVar 
				+ " . ?res <" + Properties.NUM_EXECUTIONS_URI + "> ?numExec }" ;

		return getStringListResult(queryVar,
				selectProcessNamesQueryString);
		
	}

	/**
	 * @return the number of different processes that have DDGs stored in the database.
	 *   This is done by the process name.  Thus, if process A is executed once and 
	 *   process B is executed 3 times, this method would return 2 (counting A and B 
	 *   once each).
	 */
	public int getNumProcesses() {
		String queryVar = "num";
		String selectNumProcessesQueryString = JenaLoader.RDF_PREFIX
				+ "SELECT ?" + queryVar + " WHERE  { ?res <" + Properties.NUM_PROCESSES_URI + "> ?" + queryVar + "}";

		return getNumFromResult(queryVar, selectNumProcessesQueryString);
	}
	
	/**
	 * Returns the URI to use for this process or null if there are no executions
	 * of this process in the database yet.
	 * @param processName the name of the process to look for
	 * @return the URI to use for this process
	 */
	public String getProcessURI(String processName) {
		String queryVar = "res";
		String selectProcessQueryString = JenaLoader.RDF_PREFIX
					+ "\nSELECT ?" + queryVar + " "
					+ "\nWHERE  { ?" + queryVar + " <" + Properties.PROCESS_NAME_URI + "> \"" + processName + "\"}";

		ResultSet processResultSet = performQuery(selectProcessQueryString);
		if (processResultSet.hasNext()) {
			QuerySolution nextProcessResult = processResultSet.next();
			Resource currentRes = nextProcessResult.getResource(queryVar);
			return currentRes.getURI();
		}
		return null;
	}

	/**
	 * Returns the number of times a process has run in the database
	 * 
	 * @param processURI the URI for the process to look up
	 * @return the number of executions of a specific process in the DB
	 */
	public int getNumExecutions(String processURI) {
		String queryVar = "num";
		String selectNumExecutionsQueryString = JenaLoader.RDF_PREFIX
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE  { <" + processURI + "> <" + Properties.NUM_EXECUTIONS_URI + "> ?" + queryVar + "}";

		return getNumFromResult(queryVar, selectNumExecutionsQueryString);
	}

	/**
	 * Will find all the timestamps in the database for a process
	 * 
	 * @param selectedProcessName the name of the process to get timestamps for
	 * @return a list of the timestamps for all executions of processes in the DB
	 */
	public List<String> getTimestamps(String selectedProcessName) {
		String queryVar = "timestamp";
		
		String selectTimestampsQueryString = JenaLoader.RDF_PREFIX
				+ "\nSELECT ?" + queryVar
				+ "\n WHERE  { ?res <" + Properties.PROCESS_NAME_URI + "> \"" + selectedProcessName + "\""
				+ "\n . ?res <" + Properties.TIMESTAMP_URI + "> ?" + queryVar + " }" ;

		return getStringListResult(queryVar, selectTimestampsQueryString);
		
	}

	/**
	 * Gets the integer value returned by a query
	 * @param queryVar the SPARQL variable containing the result
	 * @param queryString the query to execute
	 * @return the integer value returned by the query.  Returns 0 if there is no match.
	 */
	private int getNumFromResult(String queryVar,
			String queryString) {
		ResultSet resultSet = performQuery(queryString);
		if (resultSet.hasNext()) {
			QuerySolution nextResult = resultSet.next();
			int value = nextResult.getLiteral(queryVar)
					.getInt();
			return value;
		}
		return 0;
	}

	/**
	 * Gets a result that is a list of strings from a query
	 * @param queryVar the SPARQL variable that will hold the results
	 * @param queryString the query to ask
	 * @return the list of strings returned by the query.  Returns an empty list if there is no match.
	 */
	private List<String> getStringListResult(String queryVar, String queryString) {
		ArrayList<String> resultList = new ArrayList<String>();
		ResultSet resultSet = performQuery(queryString);
		while (resultSet.hasNext()) {
			QuerySolution nextResult = resultSet.next();
			String value = nextResult.getLiteral(queryVar).getString();
			resultList.add(value);
		}
		
		return resultList;
	}

	/**
	 * Load a DDG for one execution of a process
	 * @param processName the name of the process
	 * @param timestamp the timestamp for the execution
	 * @param provData 
	 * @return the ddg
	 */
	public ProvenanceData loadDDG(String processName,
			String timestamp, ProvenanceData provData) {
		assert processName != null;
		assert timestamp != null;
		this.processName = processName;
		this.timestamp = timestamp;

		// this.printDBContents();
		
		ddgURI = getDdgUri(processName, timestamp);
		String queryPrefix = getQueryPrefix(processName, timestamp);
		loadAttributes(processName, timestamp, provData);
	
		// Process the pins in the order in which they were originally added to the 
		// DDG so that the events can be sent to the visualization tool to do the 
		// incremental visualizations.
		SortedSet<Resource> sortedResources = getAllStepInstanceNodes(queryPrefix);
		for (Resource res : sortedResources) {
			// Add the next pin
			ProcedureInstanceNode pin = addProcResourceToProvenance(res, pd);
			
			// Connect the pin to its predecessors, inputs and outputs.
			setPredecessors(queryPrefix, pin);
			getAllInputs(pin);
			getAllOutputs(pin);
		}
		//System.out.println("All nodes loaded from DB.");
		pd.notifyProcessFinished();
		//System.out.println("Done with notifyProcessFinished");
		return pd;
	}
	
	/**
	 * Load the DDG attributes for a DDG from the database
	 * @param processName the name of the process
	 * @param timestamp the timestamp of the DDG execution
	 * @param provData the provenance data being loaded into
	 */
	public void loadAttributes(String processName, String timestamp,
			ProvenanceData provData) {
		//determine what language the process is written in
		this.language = getStringValue(processName, timestamp, Properties.LANGUAGE_URI, "Little-JIL");
		if (language == null) {
			language = "Little-JIL";
		}

		pd = provData;
		pd.setLanguage(language);
		pd.setTimestamp(timestamp);
		
		// Look up the attributes stored in the DB
		Resource ddgResource = getHeader(processName, timestamp);
		StmtIterator propertyIterator = ddgResource.listProperties();
		Attributes attributes = new Attributes();
		while (propertyIterator.hasNext()) {
			Statement nextStatement = propertyIterator.next();
			Property nextProp = nextStatement.getPredicate();
			String propURI = nextProp.getURI();
			String name = propURI.substring(propURI.lastIndexOf("/") + 1);
			String value = nextStatement.getObject().toString();
			attributes.set(name, value);
			//System.out.println("Name is " + name);
			//System.out.println("Object is " + value);
		}
		pd.setAttributes(attributes);
		
		// Find the file that contains the script used to create the DDG being loaded
		String processFileTimestamp = getStringValue(processName, timestamp, Properties.PROCESS_FILE_TIMESTAMP_URI, null);
		String savedFileName = FileUtil.getSavedFileName(processName, processFileTimestamp);
		File savedFile = new File (savedFileName);
		if (savedFile.exists()) {
			pd.createFunctionTable(savedFileName);	
		}
		
		load = LanguageConfigurator.createDDGBuilder(language, processName, provData, null);
	}

	/**
	 * Add a procedure node that was read from the database to the ddg
	 * @param res the resource describing the node
	 * @param provData the ddg
	 * @return the node added
	 */
	public ProcedureInstanceNode addProcResourceToProvenance(Resource res, ProvenanceData provData) {
		int id = retrieveSinId(res);
		String type = retrieveSinType(res);
		String name = retrieveSinName(res);
		String value = retrieveSinValue(res);
		Double elapsedTime = retrieveSinElapsedTime(res);
		int lineNumber = retrieveSinLineNumber(res);
		int scriptNumber = retrieveSinScriptNumber(res);
		ProcedureInstanceNode pin = addSinToProvData(name,
				type, value, elapsedTime, lineNumber, scriptNumber, res, id, provData);
		//System.out.println("Adding sin" + id + ": "
		//		+ pin.toString());
		return pin;
	}

	/** Returns the URI associated with a particular process and timestamp. */
	private String getDdgUri(String processName, String timestamp) {
		Resource ddgResource = getHeader(processName, timestamp);
		return ddgResource.getURI();

	}
	
	private String getDDGClause(String queryVar, String processName, String timestamp) {
		ddgURI = getDdgUri(processName, timestamp);
		return getDDGClause(queryVar);
	}

	private String getDDGClause(String queryVar) {
		return "?" + queryVar + " " + PROCESS_PREFIX + Properties.DDG + " \"" + ddgURI + "\"";
	}

	/**
	 * Read all the pins for this DDG from the database
	 * @param queryPrefix the prefix string to include in the query
	 * @param ddgClause 
	 * @return the set of pins sorted by their ids, which represents the order
	 * 	in which they were created
	 */
	private SortedSet<Resource> getAllStepInstanceNodes(String queryPrefix) {
		String queryVar = "s";

		String selectStepsQueryString = queryPrefix
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE  { ?" + queryVar + " " + SIN_PREFIX + Properties.NAME + " ?sinname ."
				+ "\n " + getDDGClause(queryVar) + "}";
		

		ResultSet stepResultSet = performQuery(selectStepsQueryString);
		
		SortedSet<Resource> sortedResources = new TreeSet<Resource>(new Comparator<Resource>() {

			@Override
			// Allows sorting of pin resources by id.
			public int compare(Resource r0, Resource r1) {
				return retrieveSinId(r0) - retrieveSinId(r1);
			}
			
		});
		
		// Go through the result set putting them into a sorted set.
		while (stepResultSet.hasNext()) {
			QuerySolution nextStepResult = stepResultSet.next();
			Resource currentRes = nextStepResult.getResource(queryVar);
			sortedResources.add(currentRes);
		}
		return sortedResources;
	}

	/**
	 * Get all the resources for all the data nodes of a ddg
	 * @param queryPrefix the prefix to use to query the ddg
	 */
	private Set<Resource> getAllDataInstanceNodes(String queryPrefix) {
		String queryVar = "s";

		String selectStepsQueryString = queryPrefix
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE  { ?" + queryVar + " " + DIN_PREFIX + Properties.NAME + " ?dinname ."
				+ "\n " + getDDGClause(queryVar) + "}";

		ResultSet dataResultSet = performQuery(selectStepsQueryString);
		
		Set<Resource> resources = new HashSet<Resource>();
		
		// Go through the result set putting them into a sorted set.
		while (dataResultSet.hasNext()) {
			QuerySolution nextStepResult = dataResultSet.next();
			Resource currentRes = nextStepResult.getResource(queryVar);
			resources.add(currentRes);
		}
		return resources;
	}

	/**
	 * Get the names of all the data objects for this execution in the database
	 * @param processName the name of the process that was executed
	 * @param timestamp the timestamp of the execution
	 * @return the set of data node names found
	 */
	public SortedSet<String> getAllDinNames(String processName, String timestamp) {
		String queryPrefix = getQueryPrefix(processName, timestamp);
		String queryVar = "dinname";

		String selectDinNamesQueryString = queryPrefix
				+ "\nSELECT ?" + queryVar + "\n WHERE  { ?d " + DIN_PREFIX + Properties.NAME + " ?" + queryVar + " ."
				+ "\n " + getDDGClause("d", processName, timestamp) + "}";

		ResultSet nameResultSet = performQuery(selectDinNamesQueryString);
		
		SortedSet<String> sortedNames= new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String s1, String s2) {
				try {
					int number1 = Integer.parseInt(s1.substring(0, s1.indexOf("-")));
					int number2 = Integer.parseInt(s2.substring(0, s2.indexOf("-")));
					return number1 - number2;
				} catch (NumberFormatException e) {
					// In case the names do not follow the syntax of "number-string"
					return s1.compareTo(s2);
				}
			}
			
		});
		
		// Go through the result set putting them into a sorted set.
		while (nameResultSet.hasNext()) {
			QuerySolution nextNameResult = nameResultSet.next();
			String currentName = nextNameResult.getLiteral(queryVar).getString();
			sortedNames.add(currentName);
		}
		return sortedNames;
	}
	
	/**
	 * Load all the data nodes that are inputs to a pin and connect them to the pin
	 * @param pin the node to get inputs of
	 */
	private void getAllInputs(ProcedureInstanceNode pin) {
		String queryVarName = "in";
		ResultSet inputsResultsSet = getAllInputs(processName, timestamp, pin.getId(), queryVarName);

		while (inputsResultsSet.hasNext()) {
			QuerySolution inputSolution = inputsResultsSet.next();
			Resource inputResource = inputSolution.getResource(queryVarName);
			DataInstanceNode inputDin;
			
			// Check if the data node has already been created.  It probably has
			// since it must be an output for a step that already has been loaded.
			if (pd.containsResource(inputResource.getURI())) {
				inputDin = (DataInstanceNode) pd
						.getNodeForResource(inputResource.getURI());
			}
			
			// Create the DIN
			else {
				inputDin = addDataResourceToProvenance(inputResource, pd);
			}
			
			// Connect the data node to the pin it is an input for
			pin.addInput(inputDin.getName(), inputDin);
			inputDin.addUserPIN(pin);
		}
	}

	/**
	 * Get all the inputs to a procedure node
	 * @param processName the process whose ddg is being used
	 * @param timestamp the execution time of the ddg
	 * @param pinId the id of the procedure node whose inputs are looked up
	 * @param queryVarName the variable name to use in the query
	 * @return the set of query results
	 */
	public ResultSet getAllInputs(String processName, String timestamp, int pinId, String queryVarName) {
		String queryPrefix = getQueryPrefix(processName, timestamp);		

		String sinVarName = "sin";
		String selectInputQuery = queryPrefix
				+ "\nSELECT ?" + queryVarName 
				+ "\n WHERE  { ?" + sinVarName + " " + SIN_PREFIX + Properties.INPUTS + " ?" + queryVarName 
				+ "\n . ?" + sinVarName + " " + SIN_DDG_ID + " " + pinId + " ."
				+ "\n " + getDDGClause(sinVarName) + "}";
		return performQuery(selectInputQuery);
	}

	/**
	 * Read all the output data nodes of a pin and add it to the provenance graph
	 * @param pin the pin to get the outputs of
	 */
	private void getAllOutputs(ProcedureInstanceNode pin) {
		String queryVarName = "out";
		ResultSet outputsResultsSet = getAllOutputs(processName, timestamp, pin.getId(), queryVarName);

		while (outputsResultsSet.hasNext()) {
			QuerySolution outputSolution = outputsResultsSet.next();
			Resource outputResource = outputSolution.getResource(queryVarName);
			DataInstanceNode outputDin;
			
			// If the node has already been loaded (probably not since each node is 
			// only output by one node), just look it up
			if (pd.containsResource(outputResource.getURI())) {
				assert false;
				outputDin = (DataInstanceNode) pd
						.getNodeForResource(outputResource.getURI());
				// No way to set the producer with the current API!
			}
			
			// Load the node from the database
			else {
				outputDin = addDataResourceToProvenance(outputResource, pd);
			}
			//System.out.println("Adding output " + outputDin.getName()
			//			+ " to " + pin.getName());
			pin.addOutput(outputDin.getName(), outputDin);
			// Producere is set when the data node is created.
		}
			
	}
	
	/**
	 * Get all of the outputs from a particular procedure node
	 * @param processName the process the node belongs to
	 * @param timestamp the timestamp of the process execution containing the node
	 * @param pinId the id of the node
	 * @param queryVarName the variable to use in the query
	 * @return the set of query results
	 */
	public ResultSet getAllOutputs(String processName, String timestamp, int pinId, String queryVarName) {
		String queryPrefix = getQueryPrefix(processName, timestamp);		

		String sinVarName = "sin";
		String selectOutputsQuery = queryPrefix
				+ "\nSELECT ?" + queryVarName 
				+ "\n WHERE  { ?" + sinVarName + " " + SIN_PREFIX + Properties.OUTPUTS + " ?" + queryVarName 
				+ "\n . ?" + sinVarName + " " + SIN_DDG_ID + " " + pinId + " ."
				+ "\n " + getDDGClause(sinVarName) + "}";

		return performQuery(selectOutputsQuery);
	}
	
	/**
	 * Returns information about all the files found in the database.
	 * @return information about all the files found in the database.
	 */
	public SortedSet<FileInfo> getAllFileNames() {
		String queryVar = "filename";

		String queryPrefix = getQueryPrefix(processName, timestamp);		
		String selectFileNamesQueryString = queryPrefix + "\n" + 
				"SELECT ?" + queryVar + "\n WHERE  { ?din " + DIN_PREFIX + Properties.TYPE + " \"File\" \n" +
				" . ?din" + " " + DIN_PREFIX + Properties.VALUE + " ?" + queryVar + " }";

		return getFilenames(queryVar, selectFileNamesQueryString);
	}

	/**
	 * Returns information about the files that match the query passed in
	 * @param queryVar the variable being set by the query
	 * @param selectFileNamesQueryString the query to execute
	 * @return the file information
	 */
	private SortedSet<FileInfo> getFilenames(String queryVar,
			String selectFileNamesQueryString) {
		//ErrorLog.showErrMsg(selectFileNamesQueryString + "\n");
		//printDBContents();
		ResultSet nameResultSet = performQuery(selectFileNamesQueryString);
		
		SortedSet<FileInfo> sortedFiles= new TreeSet<FileInfo>();
		
		//System.out.println("Files found:");
		
		// Go through the result set, createing FileInfo objects out of the results
		// and putting them into a sorted set.
		while (nameResultSet.hasNext()) {
			QuerySolution nextNameResult = nameResultSet.next();
			String longFilename = nextNameResult.getLiteral(queryVar).getString();
			//ErrorLog.showErrMsg(longFilename + "\n");
			FileInfo fileInfo = new FileInfo(longFilename);
			sortedFiles.add(fileInfo);
			//System.out.println("Number of files: " + sortedFiles.size());
			//System.out.println(fileInfo);
		}
		return sortedFiles;
	}
	
	/**
	 * Returns information about all the input files found in the database.
	 * @return information about all the input files found in the database.
	 */
	public SortedSet<FileInfo> getInputFileNames() {
		String queryVar = "filename";

		String queryPrefix = getQueryPrefix(processName, timestamp);		
		String selectFileNamesQueryString = queryPrefix + "\n" + 
				"SELECT ?" + queryVar + "\n WHERE  { \n" +
				"?din " + DIN_PREFIX + Properties.TYPE + " \"File\" \n" +
				" . ?din " + DIN_PREFIX + Properties.VALUE + " ?" + queryVar + "\n" +
				" . ?sin " + SIN_PREFIX + Properties.INPUTS + " ?din" + 
				" }";
		return getFilenames(queryVar, selectFileNamesQueryString);
	}
	
	/**
	 * Returns information about all the output files found in the database.
	 * @return information about all the output files found in the database.
	 */
	public SortedSet<FileInfo> getOutputFileNames() {
		String queryVar = "filename";

		String queryPrefix = getQueryPrefix(processName, timestamp);		
		String selectFileNamesQueryString = queryPrefix + "\n" + 
				"SELECT ?" + queryVar + "\n WHERE  { \n" +
				"?din " + DIN_PREFIX + Properties.TYPE + " \"File\" \n" +
				" . ?din " + DIN_PREFIX + Properties.VALUE + " ?" + queryVar + "\n" +
				" . ?sin " + SIN_PREFIX + Properties.OUTPUTS + " ?din" + 
				" }";
		return getFilenames(queryVar, selectFileNamesQueryString);
	}
	

	/**
	 * Adds a node to the ddg based on information read from the database
	 * @param dataResource the resource describing the data node
	 * @param provData the ddg to add the node to
	 * @return the data node constructed
	 */
	public DataInstanceNode addDataResourceToProvenance(Resource dataResource, ProvenanceData provData) {
		DataInstanceNode din;
		String name = retrieveDinName(dataResource);
		int dinId= retrieveDinId(dataResource);
		String type = retrieveDinType(dataResource);
		String currentVal = retrieveDinValue(dataResource);
		String timestamp = retrieveTimestamp(dataResource);
		String location = retrieveLocation(dataResource);
		din = addDinToProvData(
			name, type, dataResource,
			currentVal, dinId, timestamp, provData, location);
		return din;
	}

	/**
	 * Sets the predecessor nodes of the pin.  Assumes the predecessors have already been
	 * loaded from the DB
	 * @param queryPrefix the query to start the prefix
	 * @param pin the node to set the predecessors for
	 */
	private void setPredecessors(String queryPrefix, ProcedureInstanceNode pin) {
		String queryVarName = "pred";
		String sinVarName = "sin";
		String selectSuccessorsQuery = queryPrefix
				+ "\nSELECT ?" + queryVarName 
				+ "\n WHERE  { ?" + sinVarName + " " + SIN_PREFIX + Properties.PREDECESSORS + " ?" + queryVarName 
				+ "\n . ?" + sinVarName + " " + SIN_DDG_ID + " " + pin.getId() + " ."
				+ "\n " + getDDGClause(sinVarName) + "}";

		// Find out which nodes are the predecessors in the DB
		ResultSet predecessorsResultsSet = performQuery(selectSuccessorsQuery);
		while (predecessorsResultsSet.hasNext()) {
			QuerySolution predecessorSolution = predecessorsResultsSet.next();
			Resource predecessorResource = predecessorSolution.getResource(queryVarName);

			// Find the nodes in the provenance graph that correspond to the predecessors
			ProcedureInstanceNode pred = (ProcedureInstanceNode) pd.getNodeForResource(predecessorResource.getURI());
			
			// Connect the predecessor and successor
			pred.addSuccessor(pin);
			pin.addPredecessor(pred);
		}
	}
	
	/**
	 * Send the query to the DB 
	 * @param selectQueryString the query to ask
	 * @return the result set
	 */
	private ResultSetRewindable performQuery(String selectQueryString) {
		//System.out.println(selectQueryString);
		Query selectQuery = QueryFactory.create(selectQueryString);

		// Execute the query and obtain results
		QueryExecution selectQueryExecution = QueryExecutionFactory.create(
				selectQuery, dataset);

		ResultSet resultSet;
		
		// Do the query in a read transaction
		// System.out.println("JenaLoader.performQuery requesting read lock.");
		dataset.begin(ReadWrite.READ);
		// System.out.println("JenaLoader.performQuery got read lock.");
		try {
			resultSet = selectQueryExecution.execSelect();
		} finally {
			// System.out.println("JenaLoader.performQuery releasing read lock.");
			dataset.end();
		}

		// Allows the result set to be reused
		ResultSetRewindable rewindableResultSet = ResultSetFactory
				.makeRewindable(resultSet);

		// Output the result set as text
		//ResultSetFormatter.out(System.out, rewindableResultSet, selectQuery);
		rewindableResultSet.reset();
		return rewindableResultSet;
	}

	/**
	 * Create the appropriate type of data instance node for this resource
	 * and add it to the provenance data
	 * 
	 * @param currentName The parameter name 
	 * @param currentType The type of node
	 * @param currentRes The RDF resource describing the node
	 * @param currentVal The data value
	 * @param id The node id
	 * @return the node in the provenance data
	 */
	private DataInstanceNode addDinToProvData(String currentName,
			String currentType, Resource currentRes, String currentVal, int id, String dataTimestamp, ProvenanceData provData, String location) {
		DataInstanceNode din = createDataInstanceNode(currentName, currentType, id, currentVal, dataTimestamp, location);
		//System.out.println("Adding Din " + id);

		if (!nodesToResContains(currentRes, provData)) {
			provData.addDIN(din, currentRes.getURI());
		}
		return din;
	}

	/**
	 * Create a data instance node for this type stored in the database.  Implementations
	 * must define this method to support their specific language use
	 * @param name the name of the node
	 * @param type the type of the node
	 * @param id the id of the node
	 * @param currentVal the node's value
	 * @param dataTimestamp the timestamp of the data
	 * @param location the original location if the node is a file
	 * @return the node created
	 */
	protected DataInstanceNode createDataInstanceNode(String name, String type,
			int id, String currentVal, String dataTimestamp, String location) {
		return load.addDataNode(type, id, name, currentVal, dataTimestamp, location);
	}
	
	/**
	 * Create the appropriate procedure instance node for this type of data
	 * and add it to the provenance data.
	 * 
	 * @param name The node's name
	 * @param currentType The type of node
	 * @param currentRes The rdf resource describing the node
	 * @param id The node's id
	 * @return the procedure instance node that has been added to the provenance data
	 */
	private ProcedureInstanceNode addSinToProvData(String name, String type, String value, double elapsedTime, int lineNumber, int scriptNumber,
			Resource res, int id, ProvenanceData provData) {
		if (!nodesToResContains(res, provData)) {
			ProcedureInstanceNode pin = createProcedureInstanceNode (name, type, id, value, elapsedTime, lineNumber, scriptNumber);
			provData.addPIN(pin, res.getURI());
			return pin;
		}

		return null;
	}

	/**
	 * Gets the resources that descrbe data nodes with a specific name in a specific ddg
	 * @param processName the name of the process for the ddg
	 * @param timestamp the timestamp for the ddg
	 * @param name the data name we are looking for
	 * @return the set of matching data resources
	 */
	public SortedSet<Resource> getDinsNamed (String processName,
			String timestamp, String name) {
		String queryPrefix = getQueryPrefix(processName, timestamp);		
		String queryVar = "din";

		String selectDinQueryString = queryPrefix
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE  { ?" + queryVar + " " + DIN_PREFIX + Properties.NAME + " \"" + name + "\"  ."
				+ "\n " + getDDGClause(queryVar) + "}";

		ResultSet dinResultSet = performQuery(selectDinQueryString);
		
		SortedSet<Resource> sortedResources = new TreeSet<Resource>(new Comparator<Resource>() {

			@Override
			// Allows sorting of din resources by id.
			public int compare(Resource r0, Resource r1) {
				return retrieveDinId(r0) - retrieveDinId(r1);
			}
			
		});
		
		// Go through the result set putting them into a sorted set.
		while (dinResultSet.hasNext()) {
			QuerySolution nextStepResult = dinResultSet.next();
			Resource currentRes = nextStepResult.getResource(queryVar);
			sortedResources.add(currentRes);
		}
		return sortedResources;
		
	}

	/**
	 * Gets the resource that describes the producer of a data resource
	 * @param processName the name of the process containing the data
	 * @param timestamp the timestamp of the ddg containing the data
	 * @param din the data resource we are looking for 
	 * @return the resource that created the data resource
	 */
	public Resource getProducer (String processName, String timestamp, Resource din) {
		String queryPrefix = getQueryPrefix(processName, timestamp);		
		String queryVar = "producer";
		

		String selectStepsQueryString = queryPrefix
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE  { <" + din.getURI() + "> " + DIN_PREFIX + Properties.PRODUCER + " ?" + queryVar + "  ."
				+ "\n " + getDDGClause(queryVar) + "}";

		ResultSet stepResultSet = performQuery(selectStepsQueryString);
		
		/* There should be exactly one producer ! */
		while (stepResultSet.hasNext()) {
			QuerySolution nextStepResult = stepResultSet.next();
			Resource currentRes = nextStepResult.getResource(queryVar);
			return currentRes;
		}
		
		return null;
	}
	
	/**
	 * Performs a query on the Jena database to find all the nodes that
	 * input a particular data node
	 * @param processName the process being queried
	 * @param timestamp the execution timestamp of the ddg
	 * @param din the data
	 * @return an iterator over the resources in the Jena database that correspond
	 *    to procedure nodes that read the data
	 */
	public Iterator<Resource> getConsumers(String processName,
			String timestamp, Resource din) {
		String queryPrefix = getQueryPrefix(processName, timestamp);		
		String queryVar = "consumer";
		

		String selectStepsQueryString = queryPrefix
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE  { <" + din.getURI() + "> " + DIN_PREFIX + Properties.USERS + " ?" + queryVar + "  ."
				+ "\n " + getDDGClause(queryVar) + "}";

		ResultSet stepResultSet = performQuery(selectStepsQueryString);
		ArrayList<Resource> consumers = new ArrayList<Resource>();
		
		while (stepResultSet.hasNext()) {
			QuerySolution nextStepResult = stepResultSet.next();
			Resource currentRes = nextStepResult.getResource(queryVar);
			consumers.add(currentRes);
		}
		
		return consumers.iterator();
	}

	/**
	 * Will query the database and get the corresponding language from the attribute
	 * @param processName name of the process to be queried
	 * @param timestamp asscociated timestamp on the process
	 * @return the language of the process
	 */
	public String getLanguage(String processName, String timestamp){
		String queryPrefix = getQueryPrefix(processName, timestamp);	
		
		String selectLanguageQueryString = queryPrefix
				+ "\nSELECT ?language"
				+ "\n WHERE { ?res <" + Properties.PROCESS_NAME_URI + "> \"" + processName + "\""
				+ "\n . ?res <" + Properties.LANGUAGE_URI + "> ?language" + " }" ;
		
		ResultSet stepResultSet = performQuery(selectLanguageQueryString);
		
		/* There should be exactly one language ! */
		while (stepResultSet.hasNext()) {
			QuerySolution nextStepResult = stepResultSet.next();
			String value = nextStepResult.getLiteral("language").getString();
			return value;
		}
		
		// Default is Little-JIL
		return "Little-JIL";
	}

	/**
	 * Will query the database and get the corresponding value from the attribute.  It
	 * assumes there will be at most one value matching the query
	 * @param processName name of the process to be queried
	 * @param timestamp asscociated timestamp on the process
	 * @param uri the URI of the property whose value is being retrieved
	 * @param defaultValue the value to return if the property is not in the database
	 * @return the language of the process
	 */
	public String getStringValue(String processName, String timestamp, String uri, String defaultValue){
		String queryPrefix = getQueryPrefix(processName, timestamp);	
		
		String selectQueryString = queryPrefix
				+ "\nSELECT ?var"
				+ "\n WHERE { ?res <" + Properties.PROCESS_NAME_URI + "> \"" + processName + "\""
				+ "\n . ?res <" + uri + "> ?var" + " }" ;
		
		ResultSet stepResultSet = performQuery(selectQueryString);
		
		/* There should be exactly one language ! */
		while (stepResultSet.hasNext()) {
			QuerySolution nextStepResult = stepResultSet.next();
			String value = nextStepResult.getLiteral("var").getString();
			return value;
		}
		
		// Default is Little-JIL
		return defaultValue;
	}

	/**
	 * Create a procedure instance node for the data read from the database.  Subclasses must
	 * override this
	 * @param name the name of the node
	 * @param type the type of node
	 * @param id the id of the procedure node
	 * @param procDef the definition of the procedure that was executed
	 * @param lineNumber the line in the script that generated the node
	 * @param scriptNumber the script number for this node
	 * @return the node created
	 */
	protected ProcedureInstanceNode createProcedureInstanceNode (String name, String type, int id, String procDef, double elapsedTime, int lineNumber,
			int scriptNumber) {
		return load.addProceduralNode(type, id, name, procDef, elapsedTime, lineNumber, scriptNumber);
	}
	
	private static boolean nodesToResContains(Resource r, ProvenanceData provData) {
		return provData.containsResource(r.getURI());
	}
	
	/**
	 * Returns the node type associated with this resource
	 * @param res this resource must describe a procedural node
	 * @return the node type
	 */
	public String retrieveSinType(Resource res) {
		return retrieveStringProperty(res, prop.getSinType(res.getModel()));
	}

	/**
	 * Returns the id associated with this resource
	 * @param res this resource must describe a procedural node
	 * @return the node id
	 */
	public int retrieveSinId(Resource res) {
		return retrieveIntProperty(res, prop.getSinDDGId(res.getModel()));
	}

	/**
	 * Returns the node name associated with this resource
	 * @param res this resource must describe a procedural node
	 * @return the node name
	 */
	public String retrieveSinName(Resource res) {
		return retrieveStringProperty(res, prop.getSinName(res.getModel()));
	}

	private String retrieveSinValue(Resource res) {
		// TODO Auto-generated method stub
		return retrieveStringProperty(res, prop.getStepProperty(res.getModel()));
	}

	private double retrieveSinElapsedTime(Resource res) {
		Property sinElapsedTimeProperty = prop.getSinElapsedTime(res.getModel());
		try {
			double sinElapsedTime = retrieveDoubleProperty(res, sinElapsedTimeProperty);
			return sinElapsedTime;
		} catch (NullPointerException e) {
			// No elapsed time in the database.  Happens for ddgs saved before
			// we started recording elapsed time.
			return 0.0;
		}
	}

	private int retrieveSinLineNumber (Resource res) {
		Property sinLineNumberProperty = prop.getSinLineNumber(res.getModel());
		try {
			int sinLineNumber = retrieveIntProperty(res, sinLineNumberProperty);
			return sinLineNumber;
		} catch (NullPointerException e) {
			// No line number in the database.  Happens for ddgs saved before
			// we started recording line numbers.
			return -1;
		}
	}

	private int retrieveSinScriptNumber (Resource res) {
		Property sinScriptNumberProperty = prop.getSinScriptNumber(res.getModel());
		try {
			int sinScriptNumber = retrieveIntProperty(res, sinScriptNumberProperty);
			return sinScriptNumber;
		} catch (NullPointerException e) {
			// No script number in the database.  Happens for ddgs saved before
			// we started recording script numbers.
			return -1;
		}
	}

	/**
	 * Returns the node name associated with this resource
	 * @param res this resource must describe a data node
	 * @return the node name
	 */
	public String retrieveDinName(Resource res) {
		return retrieveStringProperty (res, prop.getDinName(res.getModel()));
	}

	/**
	 * Returns the node type associated with this resource
	 * @param res this resource must describe a data node
	 * @return the node type
	 */
	public String retrieveDinType(Resource res) {
		return retrieveStringProperty(res, prop.getDinType(res.getModel()));
	}

	/**
	 * Returns the value associated with this resource
	 * @param res this resource must describe a data node
	 * @return the value
	 */
	public String retrieveDinValue(Resource res) {
		return retrieveStringProperty(res, prop.getDinValue(res.getModel()));
	}

	/**
	 * Returns the timestamp associated with this resource
	 * @param res this resource must describe a data node
	 * @return the timestamp
	 */
	private String retrieveTimestamp(Resource res) {
		return retrieveStringProperty(res, prop.getTimestamp(res.getModel()));
	}

	private String retrieveLocation(Resource res) {
		return retrieveStringProperty(res, prop.getLocation(res.getModel()));
	}

	/**
	 * Returns the id associated with this resource
	 * @param res this resource must describe a data node
	 * @return the id
	 */
	public int retrieveDinId(Resource res) {
		return retrieveIntProperty (res, prop.getDinDDGId(res.getModel()));
	}

	/**
	 * Returns an integer value of a property 
	 * @param res this resource the property belongs to
	 * @param propertyName the property to return.  This must be an integer property
	 * @return the property value
	 */
	private static int retrieveIntProperty(Resource res, Property propertyName) {
		Statement propertyValue = res.getProperty(propertyName);
		return propertyValue.getInt();
	}

	/**
	 * Returns a double value of a property 
	 * @param res this resource the property belongs to
	 * @param propertyName the property to return.  This must be a double property
	 * @return the property value
	 */
	private static double retrieveDoubleProperty(Resource res, Property propertyName) {
		Statement propertyValue = res.getProperty(propertyName);
		return propertyValue.getDouble();
	}

	/**
	 * Returns a string value of a property 
	 * @param res this resource the property belongs to
	 * @param propertyName the property to return.  This must be a string property
	 * @return the property value
	 */
	private static String retrieveStringProperty(Resource res, Property propertyName) {
		Statement propertyValue = res.getProperty(propertyName);
		if (propertyValue == null) {
			return null;
		}
		return propertyValue.getString();
	}

	/**
	 * Returns the prefix to be included in queries.  This defines a prefix for
	 * querying data nodes and a second prefix for querying procedural nodes
	 * @param processName the process being queried
	 * @param timestamp the execution time of the ddg being queried
	 * @return the query prefix
	 */
	private String getQueryPrefix(String processName, String timestamp) {
		if (processName != null && timestamp != null) {
			prop = new Properties(processName, timestamp);
		}
	
		String queryPrefix = RDF_PREFIX
				+ "\nPREFIX  " + PROCESS_PREFIX + "  <" + Properties.ALL_PROCESSES_URI + ">"
				+ "\nPREFIX  " + SIN_PREFIX + "  <" + Properties.ALL_SINS_URI + ">"
				+ "\nPREFIX  " + DIN_PREFIX + "  <" + Properties.ALL_DINS_URI + ">";
		return queryPrefix;
	}

	/**
	 * Deletes a DDG from the database
	 * @param selectedProcessName the name of the ddg
	 * @param selectedTimestamp the execution timestamp of the ddg to delete
	 */
	public void deleteDDG(String selectedProcessName, String selectedTimestamp) {
		// printDBContents();
		String queryPrefix = getQueryPrefix(selectedProcessName, selectedTimestamp);
		
		// Extract all the information that we need before doing the write transaction
		// used for the deletion.  TDB does not support nested transactions.
		Set<Resource> pinResources = getAllStepInstanceNodes(queryPrefix);
		Set<Resource> dataResources = getAllDataInstanceNodes(queryPrefix);
		Resource headerResource = getHeader(selectedProcessName, selectedTimestamp);
		Resource counterResource = null;
		if (getTimestamps(selectedProcessName).size() == 1) {
			counterResource = getCounter(selectedProcessName);
		}
		
		// Do the deletion
		// System.out.println("JenaLoader.deleteDDG requesting write lock.");
		dataset.begin(ReadWrite.WRITE);
		// System.out.println("JenaLoader.deleteDDG got write lock.");
		try {
			Model model = dataset.getDefaultModel();
		
			for (Resource res : pinResources) {
				deleteResource(model, res);
			}
			
			for (Resource res : dataResources) {
				deleteResource(model, res);
			}
			
			deleteResource(model, headerResource);
			
			if (counterResource != null) {
				deleteResource (model, counterResource);
			}
			dataset.commit();
		} finally {
			// System.out.println("JenaLoader.deleteDDG releasing write lock.");
			dataset.end();
		}
		// printDBContents();
	}

	/**
	 * Get the resource that holds the execution counter for this process. 
	 */
	private Resource getCounter (String selectedProcessName) {
		String queryVar = "res";
		
		String selectNumExecutionsQueryString = JenaLoader.RDF_PREFIX
				+ "\nSELECT ?" + queryVar 
				+ "\n WHERE { ?" + queryVar + " <" + Properties.PROCESS_NAME_URI + "> \"" + selectedProcessName
				+ "\" . ?" + queryVar + " <" + Properties.NUM_EXECUTIONS_URI + "> ?counter }";
		
		ResultSet resultSet = performQuery(selectNumExecutionsQueryString);
		
		// I expect there to be exactly one result.
		QuerySolution nextResult = resultSet.next();
		assert !resultSet.hasNext();
		return nextResult.getResource(queryVar);
	}
	
	/**
	 * Get the resource that holds the header information for this ddg
	 */
	private Resource getHeader(String selectedProcessName, String selectedTimestamp) {
		String queryVar = "res";
		
		String selectProcessNamesQueryString = JenaLoader.RDF_PREFIX
					+ "SELECT ?" + queryVar + " WHERE  { ?res <" + Properties.PROCESS_NAME_URI + "> \"" + selectedProcessName
					+ "\" . ?res <" + Properties.TIMESTAMP_URI + "> \"" + selectedTimestamp + "\" }" ;
		
		ResultSet resultSet = performQuery(selectProcessNamesQueryString);
		
		// I expect there to be exactly one result.
		QuerySolution nextResult = resultSet.next();
		assert !resultSet.hasNext();
		return nextResult.getResource(queryVar);
	}

	/**
	 * Delete the given resource
	 * @param model the Jena model to delete from 
	 * @param resource the resource to delete
	 */
	private static void deleteResource(Model model, Resource resource) {
	    // remove statements where resource is subject
	    model.removeAll(resource, null, (RDFNode) null);
	    // remove statements where resource is object
	    //model.remove(null, null, resource);
	}
	
	/**
	 * Print the DB contents to standard output as XML.
	 */
	public void printDBContents() {
		// System.out.println("JenaLoader.printDBContents requesting read lock.");
		dataset.begin(ReadWrite.READ);
		// System.out.println("JenaLoader.printDBContents got read lock.");
		try {
			Model model = dataset.getDefaultModel();
			model.write(System.out);
		} finally {
			// System.out.println("JenaLoader.printDBContents releasing read lock.");
			dataset.end();
		}
	}

}
