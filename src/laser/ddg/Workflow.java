package laser.ddg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Workflow {
	
	ArrayList<ProvenanceData> scripts;
	Map<String, ArrayList<WorkflowFileData>> sharedfileinfo;
	// The process name of the script that the workflow originated from
	String processName;
	// The timestamp of the script that the workflow originated from
	String timestamp;
	
	Map<String, ArrayList<String>> filedata;
	
	public Workflow(String scr, String timestamp) {
		this.scripts = new ArrayList<ProvenanceData>();
		// The keys are going to be the names of files.
		// The values will be some sort of list or object containing relevant file information.
		// Making it an array list of some sort of object that stores these values is probably
		// better, because of the situation where the same file appears multiple times with
		// different hashes.
		this.filedata = new HashMap<String, ArrayList<String>>();
		this.processName = scr;
		this.timestamp = timestamp;
	}
	
	public void add(DataInstanceNode din) {

	}
	
	
}
