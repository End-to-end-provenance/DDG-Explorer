package laser.ddg;

public class WorkflowNode {
	
	private String timestamp;
	private DataInstanceNode din;
	private String rw;
	
	public WorkflowNode(String timestamp, DataInstanceNode din, String rw) {
		this.timestamp = timestamp;
		this.din = din;
		this.rw = rw;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public DataInstanceNode getDin() {
		return din;
	}

	public void setDin(DataInstanceNode din) {
		this.din = din;
	}

	public String getRw() {
		return rw;
	}

	public void setRw(String rw) {
		this.rw = rw;
	}
}
