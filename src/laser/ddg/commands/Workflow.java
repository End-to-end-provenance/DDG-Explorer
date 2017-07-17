package laser.ddg.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import laser.ddg.ScriptNode;
import laser.ddg.r.RDataInstanceNode;
import laser.ddg.visualizer.WorkflowGraphBuilder;

/**
 * This is a data structure that contains the file and script nodes to
 * be used in a workflow. Using this, a workflow can be organized and
 * assembled so that it encompasses only a section of the nodes in the
 * hashtable.
 * 
 * @author Connor Gregorich-Trevor
 * @version July 11, 2017
 *
 */

public class Workflow {

	private ArrayList<WorkflowEdge> edges;
	private Map<Integer, RDataInstanceNode> fileNodes;
	private Map<Integer, ScriptNode> scriptNodes;
	private ArrayList<RDataInstanceNode> addedFiles;
	private ArrayList<ScriptNode> addedScripts;

	public Workflow(WorkflowGraphBuilder builder) {
		edges = new ArrayList<WorkflowEdge>();
		fileNodes = new HashMap<Integer, RDataInstanceNode>();
		scriptNodes = new HashMap<Integer, ScriptNode>();
		addedFiles = new ArrayList<RDataInstanceNode>();
		addedScripts = new ArrayList<ScriptNode>();
	}

	/**
	 * adds a file node to the list of file nodes. 
	 * 
	 * @param rdin the RDataInstanceNode to be added.
	 */
	public void addFile(RDataInstanceNode rdin) {
		fileNodes.put(rdin.getId(), rdin);
	}

	/**
	 * adds a script node to the list of script nodes
	 * 
	 * @param sn the ScriptNode to be added.
	 */
	public void addScript(ScriptNode sn) {
		scriptNodes.put(sn.getId(), sn);
	}

	/**
	 * adds an edge to the list of edges.
	 * 
	 * @param type a string, either "SFW" or "SFR" indicating if this is a
	 * 		script write or script read edge.
	 * @param source the index of the source node of the edge
	 * @param target the index of the target node of the edge
	 */
	public void addEdge(String type, int source, int target) {
		WorkflowEdge we = new WorkflowEdge(type, source, target);
		edges.add(we);
	}

	/**
	 * This function walks back to one of the root nodes before drawing the
	 * workflow. This assists in the layout of the workflow.
	 * 
	 * @param builder the workflow graph builder being used.
	 * @param index the id of the node currently being acted upon.
	 */
	public void walkBeginning(WorkflowGraphBuilder builder, int index) {
		for (int j = 0; j <  edges.size(); j++) {
			if (edges.get(j).getTarget() == index) {
				walkBeginning(builder, edges.get(j).getSource());
				return;
			}
		}
		assembleRecursively(builder, index);
	}

	/**
	 * Using recursive calls, constructs the DDG.
	 * 
	 * @param builder the workflow graph builder being used.
	 * @param index the id of the node currently being acted upon.
	 */
	public void assembleRecursively(WorkflowGraphBuilder builder, int index) {
		ScriptNode sn = scriptNodes.get(index);
		RDataInstanceNode rdin = fileNodes.get(index);
		if (sn != null) {
			if (!addedScripts.contains(sn)) {
				builder.addNode(sn, index);
				addedScripts.add(sn);
			} else {
				return;
			}
		} else if (rdin != null) {
			if (!addedFiles.contains(rdin)) {
				builder.addNode(rdin.getType(), index, rdin.getName(), rdin.getValue(),
						rdin.getCreatedTime(), rdin.getLocation(), null);
				addedFiles.add(rdin);
			} else {
				return;
			}
		}

		for (int j = 0; j <  edges.size(); j++) {
			int sourceIndex = edges.get(j).getSource();
			int targetIndex = edges.get(j).getTarget();
			if (targetIndex == index) {
				assembleRecursively(builder, sourceIndex);
			}
			if (sourceIndex == index) {
				assembleRecursively(builder, targetIndex);
				builder.addEdge(edges.get(j).getType(), index, targetIndex);
			}
		}
	}
}
