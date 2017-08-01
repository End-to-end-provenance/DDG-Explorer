package laser.ddg.workflow;

/**
 * This class contains information about an edge in a workflow: Its source, target, and type.
 * 
 * @author Connor Gregorich-Trevor
 *
 */

public class WorkflowEdge {

	private String type;
	private int source;
	private int target;
	
	public WorkflowEdge(String type, int source, int target) {
		this.type = type;
		this.source = source;
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public int getSource() {
		return source;
	}
	
	public int getTarget() {
		return target;
	}
	
}
