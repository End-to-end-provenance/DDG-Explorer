package laser.ddg.visualizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import prefuse.data.Node;
import prefuse.visual.NodeItem;

/**
 * Keeps track of information about checkpoint and restore nodes.
 * @author Barbara Lerner
 * @version December 24, 2013
 *
 */
public class CheckpointTable {
	// Maps from a checkpoint node to every node that follows it.
	private Map<Node, Set<NodeItem>> checkpointFollowers = new HashMap<>();
	
	// Maps from a restore node to every node between its checkpoint and the restore
	private Map<Node, Set<NodeItem>> restoreMembers = new HashMap<>();
	
	// Maps from a restore node to the checkpoint it pairs with
	private Map<Node, NodeItem> restoreCheckpoint = new HashMap<>();
	
	// Maps from a restore node to the collapsed node
	private Map<Node, NodeItem> collapsedTable = new HashMap<>();
	
	// Restore nodes in order 
	private LinkedList<NodeItem> checkpointRestoreNodes = new LinkedList<>();
	
	/**
	 * Add a node as a follower to every checkpoint encountered so far.
	 * @param n the node to add
	 */
	public void add(NodeItem n) {
		if (PrefuseUtils.isCheckpointNode(n)) {
			checkpointFollowers.put(n, new HashSet<>());
			checkpointRestoreNodes.add(n);
		}
		
                checkpointFollowers.keySet().stream().map((checkpoint) -> checkpointFollowers.get(checkpoint)).forEach((followers) -> {
                    followers.add(n);
                    // System.out.println("Added " + n + " to checkpoint follower");
                });

		if (PrefuseUtils.isRestoreNode(n)) {
			checkpointRestoreNodes.add(n);
			Iterator neighbors = n.outNeighbors();
			while (neighbors.hasNext()) {
				Node neighbor = (Node) neighbors.next();
				
				// Look for the node representing the checkpoint file
				if (PrefuseUtils.isAnyDataNode(neighbor)) {
					// System.out.println("Found checkpoint file node");
					
					// Get the checkpoint node
					NodeItem checkpointNode = (NodeItem) neighbor.outNeighbors().next();
					restoreCheckpoint.put(n,  checkpointNode);
					
					// Get the nodes that follow the checkpoint
					Set<NodeItem> followers = checkpointFollowers.get(checkpointNode);
					
					// Record these as the nodes belonging to this restore node.
					Set<NodeItem> nodesForRestore = new HashSet<>();
                                        followers.stream().forEach((f) -> {
                                            nodesForRestore.add(f);
                                            // System.out.println("Added " + f + " to nodes for restore");
                                        });
					restoreMembers.put(n, nodesForRestore);
				}
			}
		}
		
	}
	
	/**
	 * @param restoreNode
	 * @return the nodes that will be made visible if the collapsed node corresponding
	 *    to this restore is expanded
	 */
	public Set<NodeItem> getRestoreMembers(Node restoreNode) {
		return restoreMembers.get(restoreNode);
	}

	/**
	 * @return the last restore node in the ddg
	 */
	public NodeItem getLastRestore() {
		if (checkpointRestoreNodes.isEmpty()) {
			return null;
		}
		
		ListIterator<NodeItem> iter = checkpointRestoreNodes.listIterator(checkpointRestoreNodes.size());
		while (iter.hasPrevious()) {
			NodeItem prev = iter.previous();
			if (PrefuseUtils.isRestoreNode(prev)) {
				return prev;
			}
		}
		return null;
	}
	
	/**
	 * @param n Must be a checkpoint or restore node
	 * @return the last restore node before n
	 */
	public NodeItem getLastRestoreBefore(Node n) {
		assert PrefuseUtils.isRestoreNode(n) || PrefuseUtils.isCheckpointNode(n);
		
		if (checkpointRestoreNodes.isEmpty()) {
			return null;
		}
		
		boolean found = false;
		ListIterator<NodeItem> iter = checkpointRestoreNodes.listIterator(checkpointRestoreNodes.size());
		while (iter.hasPrevious()) {
			NodeItem prev = iter.previous();
			if (found) {
				if (PrefuseUtils.isRestoreNode(prev)) {
					return prev;
				}
			}
			else if (prev == n) {
				found = true;
			}
			
		}
		return null;
	}
	
	/**
	 * @param n a restore node
	 * @return the checkpoint that corresponds to this restore node
	 */
	public NodeItem getCheckpoint(Node n) {
		assert PrefuseUtils.isRestoreNode(n);
		return restoreCheckpoint.get(n);
	}

	/**
	 * @param restoreNode Must be a restore node
	 * @return the collapsed checkpoint/restore node that corresponds to this restore node
	 */
	public NodeItem getCollapsed(Node restoreNode) {
		assert PrefuseUtils.isRestoreNode(restoreNode);
		return collapsedTable.get(restoreNode);
	}

	/**
	 * Records that this collapsed node corresponds to this restore node.
	 * @param restoreNode 
	 * @param collapsedNode
	 */
	public void setCollapsed(Node restoreNode, NodeItem collapsedNode) {
		assert PrefuseUtils.isRestoreNode(restoreNode);
		assert PrefuseUtils.isCollapsedNode(collapsedNode);
		collapsedTable.put(restoreNode, collapsedNode);
	}

	/**
	 * @return true if there are any restore nodes in the DDG
	 */
	public boolean anyRestoreNodes() {
		return !restoreMembers.isEmpty();
	}

}
