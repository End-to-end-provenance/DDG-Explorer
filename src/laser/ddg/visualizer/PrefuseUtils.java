package laser.ddg.visualizer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import laser.ddg.SourcePos;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Utility functions to help us add ddg-specific information to 
 * Prefuse nodes and edges.
 * 
 * @author Barbara Lerner
 * @version Jun 20, 2013
 *
 */
public class PrefuseUtils {
	/*   Field names  */
	
	/** The field name to identify the name of an item */
	public static final String NAME = "name";

	/** The field name that holds an item's type */
	public static final String TYPE = "Type";

	/** The field name to identify the id of an item */
	public static final String ID = "id";

	/** The field name to identify the value of an item */
	public static final String VALUE = "Value";
	
	/** The field name to identify the timestamp of an item */
	public static final String TIMESTAMP = "Time";
	
	/** The field names to identify the line and column numbers in the script */
	public static final String STARTLINE = "StartLine";
	public static final String STARTCOL = "StartCol";
	public static final String ENDLINE = "EndLine";
	public static final String ENDCOL = "EndCol";

	/** The field name to identify the script number  */
	public static final String SCRIPT = "Script";

	/** The field name to identify the source of an edge */
	public static final String SOURCE = "source";

	/** The field name to identify the target of an edge */
	public static final String TARGET = "target";
	
    /** Type of a control flow edge */
	public static final String CONTROL_FLOW = "CF";

	/** Type of a data flow edge */
	public static final String DATA_FLOW = "DF";

	/** Type of a collapsed step node  or  an edge that leads to a step node */
	public static final String STEP = "Step";
	public static final String STEPDF = "StepDF";
	public static final String STEPCF = "StepCF";
	
	/** Type of a procedural node that represents the start of a non-leaf operation */
	private static final String START = "Start";
	
	/** Type of a procedural node that represents the finish of a non-leaf operation */
	private static final String FINISH = "Finish";
	
	/** Type of a procedural node that represents a leaf*/
	static final String LEAF = "Leaf";
	
	/** Type of a procedural node when we have not collected full detail, like inside a loop. */
	static final String INCOMPLETE = "Incomplete";
	
	static final String RESTORE = "Restore";
	static final String CHECKPOINT = "Checkpoint";
	public static final String CHECKPOINT_FILE = "CheckpointFile";
	
	/** A synonym for LEAF that makes more sense to non computer scientists. */
	static final String OPERATION = "Operation";
	
	/** Type of a data node */
	public static final String DATA_NODE = "Data";
	
	public static final String EXCEPTION = "Exception";
	public static final String FILE = "File";
	public static final String URL = "URL";
	public static final String DEVICE = "Device";
	public static final String SNAPSHOT = "Snapshot";
	public static final String STANDARD_OUTPUT = "StandardOutput";
	public static final String STANDARD_OUTPUT_SNAPSHOT = "StandardOutputSnapshot";

	public static final String LOCATION = "Location";
	
	public static final DecimalFormat elapsedTimeFormat = new DecimalFormat("##.###");


	/**
	 * Filter used to identify visible edges
	 */
	private static EdgeFilter visibleFilter = new EdgeFilter() {

		@Override
		public boolean passes(Edge edge) {
			return isEdgeVisible((EdgeItem)edge);
		}
		
	};

	/**
	 * Filter used to identify procedural nodes
	 */
	private static NodeFilter procNodeFilter = new NodeFilter() {

		@Override
		public boolean passes(Node node) {
			return isProcNode(node);
		}

	};

	/**
	 * replaces method neighbors() from class Node
	 * 
	 * @param n
	 * @return an iterator over the neighbors of a node, ordered by their Ids
	 */
	private static Iterator<Node> orderedNeighbors(Node n) {

		Iterator<Node> neighbors = n.neighbors();
		Node neighbor;
		List<Node> orderedNeighborList = new ArrayList<>();

		while (neighbors.hasNext()) {
			neighbor = neighbors.next();
			orderedNeighborList.add(neighbor);
		}

		Collections.sort(orderedNeighborList, new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				return node1.getInt("id") - node2.getInt("id");
			}

		});

		return orderedNeighborList.iterator();
	}

	/**
	 * method getChildCount() from class Node
	 * 
	 * @param n
	 * @return the number of visible neighbors which are connected to the node
	 *         by outgoing edges
	 */
	static int getChildCount(Node n) {

		int childrenNum = 0;
		Iterator<Node> neighbors = orderedNeighbors(n);
		Node neighbor;
		EdgeItem neighborEdge;

		while (neighbors.hasNext()) {
			neighbor = neighbors.next();
			neighborEdge = (EdgeItem) (n.getGraph()).getEdge(neighbor, n);

			if (neighborEdge != null && isEdgeVisible(neighborEdge)) {
				childrenNum++;
			}
		}

		return childrenNum;
	}

	/**
	 * replaces method getFirstChild() from class Node
	 * 
	 * @param n
	 * @return the first visible neighbor connected to the node by an outgoing
	 *         edge or null if none
	 */
	static NodeItem getFirstVisibleChild(NodeItem n) {
		return (NodeItem) getFirstChild(n, visibleFilter);
	}
	
	/**
	 * Get the first child of a node that passes the filter
	 * @param n
	 * @param filter
	 * @return
	 */
	private static Node getFirstChild(Node n, EdgeFilter filter) {

		return getFirstChild(n, filter, null);
	}

	/**
	 * Return the first child of this node that is visible and is a 
	 * procedural node
	 * @param n the node whose child is returned
	 * @return the child
	 */
	static NodeItem getFirstVisibleProcChild(NodeItem n) {
		return getFirstChild(n, visibleFilter, procNodeFilter);
	}

	/**
	 * Get the first child of a node that passes the filter
	 * @param n
	 * @param eFilter
	 * @return
	 */
	private static NodeItem getFirstChild(Node n, EdgeFilter eFilter, NodeFilter nFilter) {

		NodeItem firstChild = null;
		Iterator<Node> neighbors = orderedNeighbors(n);
		NodeItem neighbor;
		EdgeItem neighborEdge;

		while (neighbors.hasNext()) {
			neighbor = (NodeItem) neighbors.next();
			if (nFilter == null || nFilter.passes(neighbor)) {
				neighborEdge = (EdgeItem) (n.getGraph()).getEdge(neighbor, n);

				if (neighborEdge != null && eFilter.passes(neighborEdge)) {
					firstChild = neighbor;
					break;
				}
			}
		}

		return firstChild;
	}

	/**
	 * replaces method getLastChild() from class Node
	 * 
	 * @param n
	 * @return the first neighbor connected to the node by an outgoing edge or
	 *         null if none
	 */
	static NodeItem getLastVisibleChild(NodeItem n) {
		return getLastChild(n, visibleFilter, null);
	}

	/**
	 * Return the last child of the node that is visible and is a procedural
	 * node.
	 * @param n the node whose child is returned
	 * @return the child
	 */
	static NodeItem getLastVisibleProcChild(NodeItem n) {
		return getLastChild(n, visibleFilter, procNodeFilter);
	}

	private static NodeItem getLastChild(Node n, EdgeFilter eFilter, NodeFilter nFilter) {
		NodeItem lastChild = null;
		int childrenNum = getChildCount(n);
		Iterator<Node> neighbors = orderedNeighbors(n);
		NodeItem neighbor;
		EdgeItem neighborEdge;
		while (neighbors.hasNext()) {
			neighbor = (NodeItem) neighbors.next();
			if (nFilter == null || nFilter.passes(neighbor)) {
				neighborEdge = (EdgeItem) (n.getGraph()).getEdge(neighbor, n);

				if (neighborEdge != null && eFilter.passes(neighborEdge)) {
					lastChild = neighbor;
					childrenNum--;
					if (childrenNum == 0) {
						return lastChild;
					}
				}
			}
		}

		return lastChild;
	}

	/**
	 * Gets the visible parent of a node
	 * @param n
	 * @return the visible parent of this node
	 */
	static NodeItem getVisibleParent (Node n) {
		return (NodeItem) getParent (n, visibleFilter);
	}
	
	/**
	 * replaces method getParent() from class Node
	 * 
	 * @param n
	 * @return the first neighbor connected to the node by an incoming edge
	 */
	private static Node getParent(Node n, EdgeFilter filter) {

		Node parent = null;
		Iterator<Node> neighbors = orderedNeighbors(n);
		Node neighbor;
		Edge nodeNeighborEdge;

		while (neighbors.hasNext()) {
			neighbor = neighbors.next();
			nodeNeighborEdge = n.getGraph().getEdge(n, neighbor);

			if (nodeNeighborEdge != null && filter.passes(nodeNeighborEdge)) {
				parent = neighbor;
				break;
			}
		}

		return parent;
	}

	/**
	 * @param n
	 * @return an iterator over the children of a node
	 */
	static Iterator<Node> visibleChildren(NodeItem n) {
		return children(n, visibleFilter);
	}

	/**
	 * @param n
	 * @return an iterator over the children of a node
	 */
	private static Iterator<Node> children(Node n, EdgeFilter filter) {

		Iterator<Node> neighbors = orderedNeighbors(n);
		Node neighbor;
		EdgeItem nodeNeighborEdge;
		List<Node> childrenList = new ArrayList<>();

		while (neighbors.hasNext()) {
			neighbor = neighbors.next();
			nodeNeighborEdge = (EdgeItem) (n.getGraph()).getEdge(neighbor, n);

			if (nodeNeighborEdge != null && filter.passes(nodeNeighborEdge)) {
				childrenList.add(neighbor);
			}
		}

		return childrenList.iterator();
	}

	/**
	 * 
	 * @param parent
	 * @param child
	 * @return the previous child of a node, current child being c
	 */
	private static NodeItem getPreviousChild(NodeItem parent, NodeItem child) {
		if (parent == null) {
			return null;
		}
		NodeItem lastChild = null;
		Iterator<Node> children = visibleChildren(parent);
				
		while (children.hasNext()) {
			NodeItem nextChild = (NodeItem) children.next();
			if (nextChild == child) {
				return lastChild;
			}
			lastChild = nextChild;
		}

		return null;
	}

	/**
	 * Get the sibling that precedes this node
	 * @param n the node whose sibling is returned
	 * @return the sibling.
	 */
	static NodeItem getPreviousSibling(NodeItem n) {
		return getPreviousChild(getVisibleParent(n), n);
	}

	/**
	 * Returns the first sibling of this node
	 * @param n the node whose sibling is returned.
	 * @return the first sibling.
	 */
	static NodeItem getFirstSibling(Node n) {
		return getFirstVisibleChild(getVisibleParent(n));
	}

	/**
	 * Returns true if the node is a start node
	 * @param n the node being tested
	 * @return true if a start node
	 */
	public static boolean isStartNode(Node n) {
		return getNodeType(n).equals(START);
	}

	/**
	 * Returns true if the node is a finish node
	 * @param n the node being tested
	 * @return true if a finish node
	 */
	static boolean isFinishNode(Node n) {
		return getNodeType(n).equals(FINISH);
	}

	/**
	 * Returns true if the node is a collapsed node
	 * @param n the node being tested
	 * @return true if a collapsed node
	 */
	public static boolean isCollapsedNode(Node n) {
		return getNodeType(n).equals(STEP);
	}
	
	/**
	 * Returns true if the node is a script node
	 * @param n the node to test
	 * @return true if a script node
	 */
	public static boolean isScriptNode(Node n) {
		return getNodeType(n).equals(SCRIPT);
	}

	/**
	 * Returns true if the node is a leaf node
	 * @param n the node to test
	 * @return true if a leaf node
	 */
	public static boolean isLeafNode(Node n) {
		String nodeType = n.getString(PrefuseUtils.TYPE);
		return nodeType.equals(LEAF) || nodeType.equals(OPERATION);
	}
	
	public static boolean isIncompleteNode (Node n) {
		String nodeType = n.getString(PrefuseUtils.TYPE);
		return nodeType.equals(INCOMPLETE);
	}

	/**
	 * Returns true if the node is any kind of procedural node
	 * @param n the node being tested
	 * @return true if a procedural node
	 */
	public static boolean isProcNode(Node n) {
		return !isAnyDataNode(n);
	}

	/**
	 * @param n
	 * @return true if the node represents a restore operation
	 */
	public static boolean isRestoreNode(Node n) {
		String nodeType = n.getString(PrefuseUtils.TYPE);
		return nodeType.equals(RESTORE);
	}

	/**
	 * @param n
	 * @return true if the node represents a checkpoint operation
	 */
	public static boolean isCheckpointNode(Node n) {
		String nodeType = n.getString(PrefuseUtils.TYPE);
		return nodeType.equals(CHECKPOINT);
	}

	/**
	 * Returns true if the node type indicates that the node
	 * represents a local file
	 * @param node the node to test
	 * @return true if it represents a file
	 */
	public static boolean isFile(VisualItem node) {
		String nodeType = node.getString(TYPE);
		return nodeType.equals(FILE) || nodeType.equals(SNAPSHOT) || nodeType.equals(STANDARD_OUTPUT_SNAPSHOT);
	}

	public static boolean isSnapshot(VisualItem node) {
		String nodeType = node.getString(TYPE);
		return nodeType.equals(SNAPSHOT);
	}

	private static String getNodeType(Node n) {
		return n.getString(PrefuseUtils.TYPE);
	}

	private static String getEdgeType(Edge e) {
		return e.getString(PrefuseUtils.TYPE);
	}

	/**
	 * Returns the name associated with a node
	 * @param node
	 * @return the name
	 */
	public static String getName(Node node) {
		return node.getString(NAME);
	}

	/**
	 * Returns the id associated with a node
	 * @param n
	 * @return the id
	 */
	public static int getId(Node n) {
		return n.getInt(ID);
	}
	
	/**
	 * Returns the value associated with a node
	 * @param n
	 * @return the value
	 */
	public static String getValue(Node n) {
		return n.getString(VALUE);
	}

	/**
	 * Returns the timestamp associated with a node
	 * @param n
	 * @return the timestamp
	 */
	public static String getTimestamp(Node n) {
		return n.getString(TIMESTAMP);
	}

	public static void setTimestamp(Node n, String time) {
		n.setString(TIMESTAMP, time);
	}

	public static void setTimestamp(Node n, double time) {
		String formattedTime = elapsedTimeFormat.format(time);
		n.setString(TIMESTAMP, formattedTime);
	}

	/**
	 * @param n
	 * @return the location associated with a node
	 */
	public static String getLocation(Node n) {
		return n.getString(LOCATION);
	}

	/**
	 * @param n
	 * @return the source code position associated with a node
	 */
	public static SourcePos getSourcePos(Node n) {
		int startLine = n.getInt(STARTLINE);
		int startCol = n.getInt(STARTCOL);
		int endLine = n.getInt(ENDLINE);
		int endCol = n.getInt(ENDCOL);
		int script = n.getInt(SCRIPT);
		return new SourcePos (script, startLine, startCol, endLine, endCol);
	}

	/**
	 * Returns true if the node is a simple data node
	 * @param n
	 * @return true if a simple data node
	 */
	static boolean isSimpleDataNode(Node n) {
		return n.getString(TYPE).equals(DATA_NODE);
	}
	
	public static boolean isException(Node n) {
		return n.getString(TYPE).equals(EXCEPTION);
	}
	
	public static boolean isStandardOutput(Node n) {
		return (n.getString(TYPE).equals(STANDARD_OUTPUT) || n.getString(TYPE).equals(STANDARD_OUTPUT_SNAPSHOT));
	}


	/**
	 * Returns true if the node is any kind of data node 
	 * (data, exception, file, url)
	 * 
	 * @param n
	 * @return true if a data node
	 */
	public static boolean isAnyDataNode(Node n) {
		String nodeType = getNodeType(n);
		if (nodeType.equals(DATA_NODE)) {
			return true;
		}
		
		if (nodeType.equals(EXCEPTION)) {
			return true;
		}
		
		if (nodeType.equals(FILE)) {
			return true;
		}
		
		if (nodeType.equals(URL)) {
			return true;
		}
		
		if (nodeType.equals(DEVICE)) {
			return true;
		}
		
		if (nodeType.equals(SNAPSHOT)) {
			return true;
		}
		
		if (nodeType.equals(STANDARD_OUTPUT)) {
			return true;
		}
		
		if (nodeType.equals(CHECKPOINT_FILE)) {
			return true;
		}
		
		return false;
	}

	/**
	 * Returns true if the item is a collapsed node
	 * @param item the item being tested
	 * @return true if a collapsed node
	 */
	public static boolean isCollapsed(VisualItem item) {
		if (item instanceof NodeItem) {
			return isCollapsedNode((NodeItem) item);
		}
		return false;
	}

	/**
	 * Returns true if the item is a start node
	 * @param item the item being tested
	 * @return true if a start node
	 */
	public static boolean isStart(VisualItem item) {
		if (item instanceof NodeItem) {
			return isStartNode((NodeItem) item);
		}
		return false;
	}
	
	/**
	 * Returns true if the item is a finish node
	 * @param item the item being tested
	 * @return true if a finish node
	 */
	public static boolean isFinish(VisualItem item) {
		if (item instanceof NodeItem) {
			return isFinishNode((NodeItem) item);
		}
		return false;
	}

	private static boolean isEdgeVisible(EdgeItem edge) {
		boolean endPointsVisible = edge.getSourceItem().isVisible() && edge.getTargetItem().isVisible();
//		if (edge.isVisible() != endPointsVisible) {
//			System.out.println("isEdgeVisible:  " + edge);
//			System.out.println("    source: " + edge.getSourceItem());
//			System.out.println("        visible? " + edge.getSourceItem().isVisible());
//			System.out.println("    target: " + edge.getTargetItem());
//			System.out.println("        visible? " + edge.getTargetItem().isVisible());
//			System.out.println("    visible on call?  " + edge.isVisible());
//			System.out.println("    visible on return?  " + endPointsVisible);
//		}
		edge.setVisible(endPointsVisible);
		return endPointsVisible;
	}

	/**
	 * Return true if the item is a data flow edge where one of
	 * the endpoints is a collapsed node.
	 * @param item the item being checked
	 * @return true if the item is a data flow edge where one of
	 *    the endpoints is a collapsed node.
	 */
	public static boolean isStepDFEdge (VisualItem item) {
		if (item instanceof Edge) {
			return getEdgeType((Edge)item).equals(STEPDF);
		}
		return false;
	}

	/**
	 * Returns true if there is a directed path from the first node to the second node.
	 * @param startNode the start of the path
	 * @param endNode the end of the path
	 * @return true if the path exists
	 */
	public static boolean pathExists(Node startNode, Node endNode) {
		if (startNode == endNode) {
			return true;
		}
		
		Iterator<Node> neighbors = startNode.outNeighbors();
	
		while (neighbors.hasNext()) {
			Node neighbor = neighbors.next();
			if (pathExists(neighbor, endNode)) {
				return true;
			}
		}
		return false;
		
	}

}
