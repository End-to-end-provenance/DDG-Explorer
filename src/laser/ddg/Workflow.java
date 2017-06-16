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
	
	
	public Workflow(String scr, String timestamp) {
		this.processName = scr;
		this.timestamp = timestamp;
	}

	public void setWfnodes(ArrayList<WorkflowNode> wfns) {
		this.wfnodes = wfns;
	}
	
	private void createSourceTargetPairs(ArrayList<WorkflowNode> wfnodes) {
		for (WorkflowNode node : wfnodes) {
			for (WorkflowNode searchnode : wfnodes) {
				if (node.getMd5hash().equals(searchnode.getMd5hash())) {
					if (node.getRw().equals("write") && searchnode.getRw().equals("read")) {
						// This doesn't really work since there can be multiple of each.
						node.setTarget(searchnode);
						searchnode.setSource(node);
					} else if (node.getRw().equals("read") && searchnode.getRw().equals("write")) {
						
					}
				}
			}
		}
	}
	
}
