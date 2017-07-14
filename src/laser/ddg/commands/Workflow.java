package laser.ddg.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
	private Queue<Integer> startNodes;
	public Queue<Integer> orderedNodes;

	public Workflow(WorkflowGraphBuilder builder) {
		edges = new ArrayList<WorkflowEdge>();
		fileNodes = new HashMap<Integer, RDataInstanceNode>();
		scriptNodes = new HashMap<Integer, ScriptNode>();
		addedFiles = new ArrayList<RDataInstanceNode>();
		addedScripts = new ArrayList<ScriptNode>();
		startNodes = new LinkedList<Integer>();
		orderedNodes = new LinkedList<Integer>();
	}

	public void addFile(RDataInstanceNode rdin) {
		fileNodes.put(rdin.getId(), rdin);
	}

	public void addScript(ScriptNode sn) {
		scriptNodes.put(sn.getId(), sn);
	}

	public void addEdge(String type, int source, int target) {
		WorkflowEdge we = new WorkflowEdge(type, source, target);
		edges.add(we);
	}

	public void findRoots() {
		for (ScriptNode node : addedScripts) {
			node.setIndegree(node.getInputs().size());
			if (node.getInputs().size() == 0) {
				startNodes.add(node.getId());
			}
		}
		for (RDataInstanceNode node : addedFiles) {
			node.setIndegree(node.getInputs().size());
			if (node.getInputs().size() == 0) {
				startNodes.add(node.getId());
			}
		}
		topoSortHelper();
	}
	
	private void topoSortHelper() {
		while (startNodes.size() > 0) {
			int index = startNodes.poll();
			orderedNodes.add(index);
			ScriptNode sn = scriptNodes.get(index);
			RDataInstanceNode rdin = fileNodes.get(index);
			if (sn != null) {
				for (Integer out : sn.getOutput()) {
					RDataInstanceNode outfile = fileNodes.get(out);
					outfile.setIndegree(outfile.getIndegree() - 1);
					if (outfile.getIndegree() == 0) {
						startNodes.add(outfile.getId());
					}
				}
			} else if (rdin != null) {
				for (Integer out : rdin.getOutput()) {
					ScriptNode outscript = scriptNodes.get(out);
					outscript.getIndegree();
					outscript.setIndegree(outscript.getIndegree() - 1);
					if (outscript.getIndegree() == 0) {
						startNodes.add(outscript.getId());
					}
				}
			}
		}
	}
	
	public void topobuilder(WorkflowGraphBuilder builder) {
		while (!orderedNodes.isEmpty()) {
			int index = orderedNodes.poll();
			RDataInstanceNode rdin = fileNodes.get(index);
			ScriptNode sn = scriptNodes.get(index);
			if (sn != null) {
				builder.addNode(sn, index);
				for (int inputin : sn.getInputs()) {
					builder.addEdge("SFR", inputin, index);
					System.out.println("Adding SFR edge between " + inputin + " and " + index);
				}
			} else if (rdin != null) {
				builder.addNode(rdin.getType(), index, rdin.getName(), rdin.getValue(),
						rdin.getCreatedTime(), rdin.getLocation(), null);
				for (int inputin : rdin.getInputs()) {
					builder.addEdge("SFW", inputin, index);
					System.out.println("Adding SFW edge between " + inputin + " and " + index);
				}
			}
		}
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
