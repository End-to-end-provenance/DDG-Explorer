package laser.ddg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Workflow {
	
	private ArrayList<ProvenanceData> scripts;
	private Map<String, ArrayList<WorkflowNode>> sharedfileinfo;
	// The process name of the script that the workflow originated from
	private String processName;
	// The timestamp of the script that the workflow originated from
	private String timestamp;
	
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
	
	
}
