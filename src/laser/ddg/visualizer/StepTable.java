package laser.ddg.visualizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import prefuse.data.Node;
import prefuse.visual.NodeItem;

/**
 * Keeps track of information about collapsed steps nodes.
 * @author Barbara Lerner
 * @version Jun 14, 2013
 *
 */
public class StepTable {
	// Maps from a step node to its start node
	private Map<Node, NodeItem> startTable = new HashMap<Node, NodeItem>();

	// Maps from a step node to its finish node
	private Map<Node, NodeItem> finishTable = new HashMap<Node, NodeItem>();

	// Maps from a node to the collapsed node that it belongs to
	private Map<Node, NodeItem> startFinishTable = new HashMap<Node, NodeItem>();

	// Maps from collapsed node to the member steps.
	private Map<Node, Set<NodeItem>> memberTable = new HashMap<Node, Set<NodeItem>> ();
	
	/**
	 * Adds information about a new collapsed node
	 * @param collapsedNode the collapsed node
	 * @param startNode the start node for the expanded version
	 * @param finishNode the finish node for the expanded version
	 * @param memberNodes all the members of the expanded version
	 */
	public void add(NodeItem collapsedNode, NodeItem startNode, NodeItem finishNode,
			Set<NodeItem> memberNodes) {
		//System.out.println("StepTable.add:  Adding " + collapsedNode);
		memberTable.put(collapsedNode, memberNodes);

		startTable.put(collapsedNode, startNode);
		finishTable.put(collapsedNode, finishNode);

		// Make sure it is not a checkpoint / restore pair
		if (PrefuseUtils.isStartNode(startNode)) {
		
			for (Node member : memberNodes) {
				//System.out.println("StepTable.add: Putting " + PrefuseUtils.getName(member) + " as member of " + PrefuseUtils.getName(collapsedNode));
				startFinishTable.put(member, collapsedNode);
			}
		}
	}

	/**
	 * Return an iterator over the members of a collapsed node
	 * @param collapsedNode the collapsed node
	 * @return the member iterator
	 */
	public Iterator<NodeItem> getMembers(NodeItem collapsedNode) {
		assert PrefuseUtils.isCollapsedNode(collapsedNode);
		return memberTable.get(collapsedNode).iterator();
	}

	/**
	 * Returns true if the node is a member (recursively) of the collapsed node.
	 * @param collapsedNode the collapsed node
	 * @param node the node to look for
	 * @return true if the node is a member of collapsedNode, or, recursively,
	 *    the member of any collapsed node that is a member of collapsedNode.
	 */
	public boolean nestedContains(Node collapsedNode, Node node) {
		Set<NodeItem> members = memberTable.get(collapsedNode);
		
		if (members == null) {
			return false;
		}

		if (members.contains(node)) {
			return true;
		}
		
		Iterator<NodeItem> memberIter = members.iterator();
		while (memberIter.hasNext()) {
			Node nextMember = memberIter.next();
			if (PrefuseUtils.isCollapsedNode(nextMember)) {
				if (nestedContains(nextMember, node)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Returns the start node associated with a collapsed node
	 * @param collapsedNode the collapsed node
	 * @return the start node
	 */
	public NodeItem getStart(Node collapsedNode) {
		return startTable.get(collapsedNode);
	}

	/**
	 * Returns the finish node associated with a collapsed node
	 * @param collapsedNode the collapsed node
	 * @return the finish node
	 */
	public NodeItem getFinish(Node collapsedNode) {
		return finishTable.get(collapsedNode);
	}

	/**
	 * Returns the collasped node that this node is part of.
	 * @param item the node being searched for
	 * @return the collapsed node, or null if it is not 
	 *   part of a collapsed node.
	 */
	public NodeItem getCollapsed(Node item) {
		return startFinishTable.get(item);
	}

	/**
	 * @param collapsedNode
	 * @param node
	 * @return true if the node is contained directly within the collapsed node.
	 *   Returns false if the node is not within the collapsed node, or is beyond the 
	 *   first level of nodes contained.
	 */
	public boolean contains(NodeItem collapsedNode, NodeItem node) {
		Set<NodeItem> members = memberTable.get(collapsedNode);
		
		if (members == null) {
			return false;
		}
		
//		System.out.println("Immediate members of " + collapsedNode + ":");
//		for (Node member : members) {
//			System.out.println("   " + member);
//		}

		return members.contains(node);
	}

}
