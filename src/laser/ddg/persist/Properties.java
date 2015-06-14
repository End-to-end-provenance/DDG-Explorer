package laser.ddg.persist;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Description of the properties of SINs and DINs that are made persistent, and
 * methods that persist the nodes. To persist a DDG, call persistDin for all
 * DINs, persistSin for all SINs, completePersistSin, and completePersistDin in
 * that order (swapping completePersistDin with completePersistSin and
 * persistDin with persistSin is also allowed)
 * 
 * 
 * @author Sophia. Created Jan 4, 2012.
 */
public class Properties {

	
	// Property names
	private static final String PROCESS_NAME = "processName";
	private static final String NUM_EXECUTIONS = "numExecutions";
	private static final String NUM_PROCESSES = "numProcesses";
	static final String TIMESTAMP = "DateTime";
	private static final String LOCATION = "location";
	private static final String STEP = "step";
	private static final String SINS = "sins";
	private static final String DINS = "dins";
	public static final String USERS = "users";
	public static final String PRODUCER = "producer";
	public static final String OUTPUTS = "outputs";
	public static final String INPUTS = "inputs";
	public static final String TYPE = "type";
	public static final String DDG_ID = "DDGId";
	public static final String PREDECESSORS = "predecessors";
	public static final String SUCCESSORS = "successors";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	static final String LANGUAGE = "Language";
	private static final String PROCESS_FILE_TIMESTAMP = "ProcessFileTimestamp";
	static final String DDG = "ddg";

	// URIs
	public static final String ALL_PROCESSES_URI = "http://allprocesses/";
	public static final String PROCESS_NAME_URI = ALL_PROCESSES_URI + PROCESS_NAME;
	public static final String NUM_EXECUTIONS_URI = ALL_PROCESSES_URI + NUM_EXECUTIONS;
	public static final String NUM_PROCESSES_URI = ALL_PROCESSES_URI + NUM_PROCESSES;
	public static final String TIMESTAMP_URI = ALL_PROCESSES_URI + TIMESTAMP;
	public static final String LANGUAGE_URI = ALL_PROCESSES_URI + LANGUAGE;
	public static final String PROCESS_FILE_TIMESTAMP_URI = ALL_PROCESSES_URI + PROCESS_FILE_TIMESTAMP;
	public static final String ALL_SINS_URI = ALL_PROCESSES_URI + SINS + "/";
	public static final String ALL_DINS_URI = ALL_PROCESSES_URI + DINS + "/";
	
	private String sinURI;
	private String dinURI;
	private String curProcessURI;
	

	public Properties(String processName, String timestamp) {
		curProcessURI = "http://process/" + processName.replaceAll(" ", "") + "/" + timestamp.replaceAll(" ", "") + "/";
		sinURI = curProcessURI + "sins/";
		dinURI = curProcessURI + "dins/";
	}


	public String getDinResourceId(DataInstanceNode din) {
		return dinURI + din.getId();
	}

	public String getSinResourceId(ProcedureInstanceNode sin){
		return sinURI + sin.getId();
	}

	public Property getSinName(Model m) {
		return m.createProperty(ALL_SINS_URI, NAME);
	}

	public Property getSinPredecessors(Model m) {
		return m.createProperty(ALL_SINS_URI, PREDECESSORS);
	}

	public Property getSinSuccessors(Model m) {
		return m.createProperty(ALL_SINS_URI, SUCCESSORS);
	}

	public Property getSinDDGId(Model m) {
		return m.createProperty(ALL_SINS_URI, DDG_ID);
	}

	public Property getDinDDGId(Model m) {
		return m.createProperty(ALL_DINS_URI, DDG_ID);
	}

	public Property getDinType(Model m) {
		return m.createProperty(ALL_DINS_URI, TYPE);
	}

	public Property getSinType(Model m) {
		return m.createProperty(ALL_SINS_URI, TYPE);
	}

	public Property getSinInputs(Model m) {
		return m.createProperty(ALL_SINS_URI, INPUTS);
	}

	public Property getSinOutputs(Model m) {
		return m.createProperty(ALL_SINS_URI, OUTPUTS);
	}

	public Property getDinName(Model m) {
		return m.createProperty(ALL_DINS_URI, NAME);
	}

	public Property getDinValue(Model m) {
		return m.createProperty(ALL_DINS_URI, VALUE);
	}

	public Property getDinProducer(Model m) {
		return m.createProperty(ALL_DINS_URI, PRODUCER);
	}

	public Property getDinUsers(Model m) {
		return m.createProperty(ALL_DINS_URI, USERS);
	}

	public Property getStepProperty(Model m) {
		return m.createProperty(ALL_DINS_URI, STEP);
	}


	public String getCurProcessURI() {
		return curProcessURI;
	}


	public Property getProcessNameProperty(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, PROCESS_NAME);
	}


	public Property getNumProcessesProperty(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, NUM_PROCESSES);
	}

	public Property getNumExecutions(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, NUM_EXECUTIONS);
	}
	
	public Property getTimestamp(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, TIMESTAMP);
	}
	
	public Property getLocation(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, LOCATION);
	}

	public Property getLanguage(Model m){
		return m.createProperty(ALL_PROCESSES_URI, LANGUAGE);
	}

	public Property getProcessFileTimestamp(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, PROCESS_FILE_TIMESTAMP);
	}

	public String getSinURI() {
		return sinURI;
	}


	public String getDinURI() {
		return dinURI;
	}


	public Property getDDG(Model m) {
		return m.createProperty(ALL_PROCESSES_URI, DDG);
	}


	public Property getProperty(Model model, String name) {
		return model.createProperty(ALL_PROCESSES_URI, name);
	}




}
