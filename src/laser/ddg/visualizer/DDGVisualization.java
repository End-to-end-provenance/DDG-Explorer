package laser.ddg.visualizer;

import java.util.Iterator;
import java.util.Set;

import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.visual.NodeItem;

/**
 * This class extends Prefuse's Visualization class by keeping
 * track of the relationships between collapsed nodes and the
 * members of those collapsed nodes.
 * 
 * Collapsed nodes are what allow for the hierarchical drawing of DDGs.
 * A collapsed node has a single entry point (a start node), a 
 * single exit point (a finish node), and multiple other member
 * nodes.  Members, other than the start and finish nodes, can 
 * themselves be collapsed nodes.
 * 
 * Such a node either gets displayed as the single collapsed node,
 * or all of its direct members are displayed.
 * 
 * @author Barbara Lerner
 * @version Jun 20, 2013
 *
 */
public class DDGVisualization extends Visualization {
	// The table that keeps track of the relationships between 
	// collapsed nodes and their members.  All the methods
	// in this class simply delegate to the Step Table.
	private StepTable stepTable = new StepTable();
	
	// Keeps track of the relationship between checkpoint and restore
	// nodes and their collapsed node.
	private CheckpointTable checkpointTable = new CheckpointTable();
	
	/**
	 * Add an entry in the step table
	 * @param collapsedNode the collapsed node
	 * @param startNode the start node within the collapsed node
	 * @param finishNode the finish node within the collapsed node
	 * @param memberNodes all the members of the collapsed node
	 */
	public void add(NodeItem collapsedNode, NodeItem startNode, NodeItem finishNode,
			Set<NodeItem> memberNodes) {
		stepTable.add(collapsedNode, startNode, finishNode, memberNodes);
		if (PrefuseUtils.isRestoreNode(finishNode)) {
			checkpointTable.setCollapsed(finishNode, collapsedNode);
		}
	}

	/**
	 * Returns the members contained directly within the collapsed node.
	 * @param collapsedNode the collapsed node
	 * @return its members
	 */
	public Iterator<NodeItem> getMembers(NodeItem collapsedNode) {
		assert PrefuseUtils.isCollapsed(collapsedNode) : PrefuseUtils.getName(collapsedNode);
		return stepTable.getMembers(collapsedNode);
	}

	/**
	 * Returns true if the given node is contained anywhere within 
	 * the collapsed node.  This does a recursive test of membership.
	 * @param collapsedNode the collapsed node
	 * @param node the node we are looking for
	 * @return true if the node is recursively within the collapsed node.
	 */
	public boolean nestedContains(Node collapsedNode, Node node) {
		return stepTable.nestedContains(collapsedNode, node);
	}

	/**
	 * Returns the entry point for when the collapsed node is expanded
	 * @param collapsedNode the collapsed node
	 * @return the entry point
	 */
	public NodeItem getStart(Node collapsedNode) {
		NodeItem startNode = stepTable.getStart(collapsedNode);
		return startNode;
	}
	
	/**
	 * Returns the exit point for when the collapsed node is expanded
	 * @param collapsedNode the collapsed node 
	 * @return the exit point
	 */
	public NodeItem getFinish(Node collapsedNode) {
		return stepTable.getFinish(collapsedNode);
	}

	/**
	 * Returns the collapsed node that contains the given node.
	 * @param node the node we are searching for
	 * @return the collapsed node the node is inside of.  Returns
	 *    null if the node is not inside any collapsed node.
	 */
	public NodeItem getCollapsedStartFinish(NodeItem node) {
		return stepTable.getCollapsed(node);
	}

	/**
	 * Returns the collapsed node that corresponds to a restore node 
	 * @param restoreNode
	 * @return the collapsed node that corresponds to a restore node 
	 */
	public NodeItem getCollapsedCheckpoint(Node restoreNode) {
		return checkpointTable.getCollapsed(restoreNode);
	}

	/**
	 * Gets the collapsed node that corresponds to a start-finish pair.
	 * This will be a collapsed step node if the start node is a "Start" node.
	 * It will be a collapsed checkpoint node if the finish node is a Restore node.
	 * @param startNode
	 * @param finishNode
	 * @return the collapsed node
	 */
	public NodeItem getCollapsed(NodeItem startNode, Node finishNode) {
		NodeItem collapsedNode = null;
		if (PrefuseUtils.isStartNode(startNode)) {
			collapsedNode = getCollapsedStartFinish(startNode);	
		}
		else if (PrefuseUtils.isCheckpointNode(startNode)){
			collapsedNode = getCollapsedCheckpoint(finishNode);	
		}
		return collapsedNode;
	}

	/**
	 * Changes the renderer for edges
	 * @param arrowDirection the direction the arrows should point.  Possible values are
	 * 		prefuse.Constants.EDGE_ARROW_FORWARD and prefuse.Constants.EDGE_ARROW_REVERSE.
	 *		FORWARD draws edges from outputs to inputs.  REVERSE draws from inputs to outputs.  
	 */
	void setRenderer(int arrowDirection) {
		// draw the "name" label for NodeItems
		LabelRenderer r = new LabelRenderer(PrefuseUtils.NAME);
		r.setRoundedCorner(8, 8); // round the corners
		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		DefaultRendererFactory rendererFactory = new DefaultRendererFactory(
				r);
		// Add arrowheads to the edges
		EdgeRenderer edgeRenderer = new EdgeRenderer(
				prefuse.Constants.EDGE_TYPE_LINE,
				arrowDirection);
		rendererFactory.setDefaultEdgeRenderer(edgeRenderer);
		setRendererFactory(rendererFactory);
	}

	/**
	 * @param restoreNode
	 * @return the nodes contained within a checkpoint-restore collapsed node.
	 */
	public Set<NodeItem> getRestoreMembers(Node restoreNode) {
		return checkpointTable.getRestoreMembers(restoreNode);
	}

	/**
	 * @return the last restore node in the ddg
	 */
	public NodeItem getLastRestore() {
		return checkpointTable.getLastRestore();
	}

	/**
	 * @param n
	 * @return the last restore node in the ddg that precedes n
	 */
	public NodeItem getLastRestoreBefore(Node n) {
		if (PrefuseUtils.isCheckpointNode(n) || PrefuseUtils.isRestoreNode(n)) {
			return checkpointTable.getLastRestoreBefore(n);
		}
		else if (checkpointTable.anyRestoreNodes()) {
			Iterator predecessors = n.outNeighbors();
			while (predecessors.hasNext()) {
				NodeItem pred = (NodeItem) predecessors.next();
				if (PrefuseUtils.isRestoreNode(pred)) {
					return pred;
				}
				else if (PrefuseUtils.isProcNode(pred)){
					NodeItem restoreNode = getLastRestoreBefore(pred);
					if (restoreNode != null) {
						return restoreNode;
					}
				}
			}
			return null;
		}
		else {
			return null;
		}
	}

	/**
	 * @param restoreNode
	 * @return the checkpoint node that this restore pairs with
	 */
	public NodeItem getCheckpoint(Node restoreNode) {
		return checkpointTable.getCheckpoint(restoreNode);
	}

	/**
	 * Adds this node to the list of nodes that will be collapsed by a subsequent restore
	 * @param n
	 */
	public void add(NodeItem n) {
		checkpointTable.add(n);
	}

	/**
	 * @param collapsedNode
	 * @param node
	 * @return true if the node is immediately contained within the collapsed node.
	 */
	public boolean contains(NodeItem collapsedNode, NodeItem node) {
		return stepTable.contains(collapsedNode, node);
	}

	
}
