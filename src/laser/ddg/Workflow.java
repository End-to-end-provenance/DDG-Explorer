package laser.ddg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaWriter;
import laser.ddg.visualizer.WorkflowGraphBuilder;

public class Workflow {
	
	// The process name of the script that the workflow originated from
	private String processName;
	// The timestamp of the script that the workflow originated from
	private String timestamp;
	
	
	public Workflow(String scr, String timestamp) {
		this.processName = scr;
		this.timestamp = timestamp;
	}
	
	public void myDisplay(ProvenanceData provData) {
		JenaWriter jenaWriter = JenaWriter.getInstance();
		WorkflowGraphBuilder builder = new WorkflowGraphBuilder(false, jenaWriter);
		
		builder.drawGraph(provData);
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
		//DDGExplorer.doneLoadingDDG();
	}

	
}
