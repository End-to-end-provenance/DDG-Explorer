package laser.ddg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaWriter;
import laser.ddg.r.RDataInstanceNode;
import laser.ddg.visualizer.WorkflowGraphBuilder;

public class Workflow {
	
	// The process name of the script that the workflow originated from
	private String processName;
	// The timestamp of the script that the workflow originated from
	private String timestamp;
	
	private ArrayList<ScriptNode> scriptNodeList;
	private ArrayList<RDataInstanceNode> fileNodeList;
	
	
	public Workflow(String scr, String timestamp) {
		this.processName = scr;
		this.timestamp = timestamp;
		this.setScriptNodeList(new ArrayList<ScriptNode>());
		this.setFileNodeList(new ArrayList<RDataInstanceNode>());
	}
	
	public void myDisplay(ProvenanceData provData) {
		JenaWriter jenaWriter = JenaWriter.getInstance();
		WorkflowGraphBuilder builder = new WorkflowGraphBuilder(false, jenaWriter);
		
		builder.drawGraph(provData);
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
		//DDGExplorer.doneLoadingDDG();
	}
	
	public void addNodes(WorkflowGraphBuilder builder) {
		int index = 1;
		for (ScriptNode node : scriptNodeList) {
			builder.addNode("Script", index++, node.getName(), "Value", 
					node.getCreatedTime(), "Location", null);
		}
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
