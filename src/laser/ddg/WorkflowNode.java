package laser.ddg;

public class WorkflowNode {
	
	private DataInstanceNode din;
	private ProvenanceData provData;
	private WorkflowNode source;
	private WorkflowNode target;
	private String filepath;
	private String ddgpath;
	private String nodepath;
	private int nodenumber;
	private String md5hash;
	private String rw;
	private String timestamp;

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

	public ProvenanceData getProvData() {
		return provData;
	}

	public void setProvData(ProvenanceData provData) {
		this.provData = provData;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getDdgpath() {
		return ddgpath;
	}

	public void setDdgpath(String ddgpath) {
		this.ddgpath = ddgpath;
	}

	public int getNodenumber() {
		return nodenumber;
	}

	public void setNodenumber(int nodenumber) {
		this.nodenumber = nodenumber;
	}

	public String getMd5hash() {
		return md5hash;
	}

	public void setMd5hash(String md5hash) {
		this.md5hash = md5hash;
	}

	public String getNodepath() {
		return nodepath;
	}

	public void setNodepath(String nodepath) {
		this.nodepath = nodepath;
	}

	public WorkflowNode getSource() {
		return source;
	}

	public void setSource(WorkflowNode source) {
		this.source = source;
	}

	public WorkflowNode getTarget() {
		return target;
	}

	public void setTarget(WorkflowNode target) {
		this.target = target;
	}
}
