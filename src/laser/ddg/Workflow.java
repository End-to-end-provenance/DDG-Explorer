package laser.ddg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Workflow {
	
	private ArrayList<WorkflowNode> wfnodes;
	// The process name of the script that the workflow originated from
	private String processName;
	// The timestamp of the script that the workflow originated from
	private String timestamp;
	
	Map<String, ArrayList<String>> filedata;
	
	public Workflow(String scr, String timestamp) {
		this.filedata = new HashMap<String, ArrayList<String>>();
		this.processName = scr;
		this.timestamp = timestamp;
	}

	public void setWfnodes(ArrayList<WorkflowNode> wfns) {
		this.wfnodes = wfns;
	}
	
}
