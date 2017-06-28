package laser.ddg;

import java.util.ArrayList;

import laser.ddg.r.RDataInstanceNode;

public class Workflow {

	private ArrayList<ScriptNode> scriptNodeList;
	private ArrayList<RDataInstanceNode> fileNodeList;


	public Workflow(String scr, String timestamp) {
		this.setScriptNodeList(new ArrayList<ScriptNode>());
		this.setFileNodeList(new ArrayList<RDataInstanceNode>());
	}

	public ArrayList<RDataInstanceNode> getFileNodeList() {
		return fileNodeList;
	}

	public void setFileNodeList(ArrayList<RDataInstanceNode> fileNodeList) {
		this.fileNodeList = fileNodeList;
	}

	public ArrayList<ScriptNode> getScriptNodeList() {
		return scriptNodeList;
	}

	public void setScriptNodeList(ArrayList<ScriptNode> scriptNodeList) {
		this.scriptNodeList = scriptNodeList;
	}

}
