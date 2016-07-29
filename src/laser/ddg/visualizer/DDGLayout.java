package laser.ddg.visualizer;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import prefuse.action.layout.graph.TreeLayout;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ArrayLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 * Creates a layout for DDG graphs
 * 
 * @author Antonia Miruna Oprescu
 * 
 */
public class DDGLayout extends TreeLayout {

	private static final double SIBLING_SPACING = 50; //5; // the spacing between sibling nodes
	private static final double SUBTREE_SPACING = 25; // the spacing between subtrees
	private static final double DEPTH_LEVEL_SPACING = 25; // the spacing between depth levels

	private double[] mDepths = new double[10];
	private int mMaxDepth = 0;

	private double mAx;
	private double mAy; // for holding anchor co-ordinates

	// keep track of the nodes already added to the layout
	private Set<NodeItem> laidOutNodes;
	private Set<NodeItem> secondWalkDone;
	
	// The rightmost node at each depth
	private Map<Integer, NodeItem> rightMostAtDepth = new HashMap<>();

	// If true, means that we are drawing a data derivation, not a full DDG.
	// The graph might not contain any control flow edges, which affects the way
	// layout is done.
	private boolean dataDerivation = false;
	
	// The node to use as the root when the real root of the graph is collapsed.
	private NodeItem collapsedRoot;
	
	/**
	 * Create a new DDGLayout. A top-to-bottom orientation is assumed.
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param dataDerivation 
	 */
	public DDGLayout(String group, boolean dataDerivation) {
		super(group);
		this.dataDerivation = dataDerivation;
	}

	/**
	 * Set the node that should be used as the layout root in cases where the real root is
	 * replaced with a collapsed node.
	 * @param collapsedRoot the collapsed node to use as the root
	 */
	public void setLayoutCollapsedRoot(NodeItem collapsedRoot) {
		this.collapsedRoot = collapsedRoot;
	}

	private double spacing(NodeItem l, NodeItem r, boolean siblings) {
		double totalDistance = l.getBounds().getWidth() + r.getBounds().getWidth();

		double space;
		if (siblings) {
			space = SIBLING_SPACING;
		}
		else {
			space = SUBTREE_SPACING;
		}
		return space + 0.5 * totalDistance;
	}

	private void updateDepths(int depth, NodeItem item) {
		double d = item.getBounds().getHeight();
		if (mDepths.length <= depth) {
			mDepths = ArrayLib.resize(mDepths, 3 * depth / 2);
		}
		mDepths[depth] = Math.max(mDepths[depth], d);
		mMaxDepth = Math.max(mMaxDepth, depth);
	}

	private void determineDepths() {
		for (int i = 1; i < mMaxDepth; ++i) {
			mDepths[i] += mDepths[i - 1] + DEPTH_LEVEL_SPACING;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		NodeItem root = getLayoutRoot();
		synchronized(root.getVisualization()) {
			// initialize the two sets every time the layout is done
			laidOutNodes = new HashSet<>();
			secondWalkDone = new HashSet<>();

			Graph g = (Graph) m_vis.getGroup(m_group);
			TupleSet nodes = g.getNodes();
			initSchema(nodes);

			Arrays.fill(mDepths, 0);
			mMaxDepth = 0;

			Point2D a = getLayoutAnchor();
			mAx = a.getX();
			mAy = a.getY();

			Params rp = getParams(root);
	
			// do first pass - compute breadth information, collect depth info
			resetRightMost();
			firstWalk(root, 0, 1);
			setEdgeVisibility(root);
			//System.out.println("Entire firstwalk complete");
	
			// sum up the depth info
			determineDepths();
	
			// do second pass - assign layout positions
			secondWalk(root, null, -rp.prelim, 0);
		}
	}
	
	@Override
	public NodeItem getLayoutRoot() {
		// Get the default root
		NodeItem root = super.getLayoutRoot();
		if (!root.isVisible()) {
			// Change root to be the collapsed node.
			root = collapsedRoot;
		}
		
		// It might have a parent that is a data node.  If so, use that as the root instead.
		if (root.getOutDegree() > 0) {
			Iterator<NodeItem> outNeighbors = root.outNeighbors();
			return outNeighbors.next();
		}
		return root;
	}

	private void setEdgeVisibility(NodeItem node) {
		Iterator<EdgeItem> edgeIter = node.getGraph().edges();
		while (edgeIter.hasNext()) {
			EdgeItem nextEdge = edgeIter.next();
			NodeItem source = nextEdge.getSourceItem();
			NodeItem target = nextEdge.getTargetItem();
			if (source.isVisible() && target.isVisible()) {
				nextEdge.setVisible(true);
			}
			else {
				nextEdge.setVisible(false);
			}
		}
	}

	/**
	 * Walks the graph from the root down in a depth-first fashion.  Nodes get laid
	 * out from the leaves up
	 * @param n the node being laid out
	 * @param num
	 * @param depth the distance from the root
	 */
	private void firstWalk(NodeItem n, int num, int depth) {

		// returns if the node has already been laid out. Avoids infinite while
		// loops corresponding to closed loops in the graph.

		if (laidOutNodes.contains(n)) {
			//System.out.println("Already laid out: " + n);
			return;
		}
		
		// System.out.println("Calling firstWalk on " + n + " with depth = " + depth);
		laidOutNodes.add(n);

		Params np = getParams(n);
		np.number = num;
		//System.out.println("   number = " + num);

		updateDepths(depth, n);

		boolean expanded = n.isExpanded();

		// If there are no successors to this node and it is visible.
		if ((PrefuseUtils.getChildCount(n) == 0 || !expanded) && n.isVisible())
		{
			// System.out.println("    laying out leaf" + n.toString());
			NodeItem prevSibling = PrefuseUtils.getPreviousSibling(n);

			if (prevSibling == null) {
				// Left most node is center-aligned initially.
				np.prelim = 0;
			} else {
				// Each subsequent sibling is moved over an appropriate distance based on 
				// the widths of the nodes and the desired spacing between siblings.
				np.prelim = getParams(prevSibling).prelim + spacing(prevSibling, n, true);
			}
			//System.out.println("    prelim = " + np.prelim);
		} 
		
		// This branch deals with nodes with descendants
		else if (expanded && n.isVisible()) {
			NodeItem leftMost = PrefuseUtils.getFirstVisibleProcChild(n);
			NodeItem rightMost = PrefuseUtils.getLastVisibleProcChild(n);
			//NodeItem defaultAncestor = leftMost;
			
			// When displaying a full graph, we prioritize the layout of procedural
			// nodes.  When doing a data derivation, we need to do a recursive 
			// walk on the data nodes, too, since there are no control flow edges,
			// so we would not reach all the nodes if we only walked the procedural
			// nodes.
			if (PrefuseUtils.isProcNode(n) || dataDerivation || depth == 1) {
				Iterator<Node> childrenIter = PrefuseUtils.visibleChildren(n);
	
				// Recursively walk the children of procedural nodes
				int i = 0;
				while (childrenIter.hasNext()) {
					NodeItem c = (NodeItem) childrenIter.next();
					// Increase the depth by 2.  We use a depth offset of 1 for data inputs
					// so that they will come at staggered heights from normal nodes.  That
					// way we do not need to be concerned about horizontal overlap.
					//System.out.println("About to walk " + c);
					firstWalk(c, i, depth + 2);
					
					//defaultAncestor = apportion(c, defaultAncestor);
					addAtDepth(c, depth+2);
					i++;
				}
			}
			
			// System.out.println("Finished firstwalk for all children of " + n);

			// I think this is responsible for the graphs that shoot off wildly to the left and right
			//executeShifts(n);

			// Find the halfway point between the leftmost and rightmost procedural children
			double midpoint = 0.5 * (getParams(leftMost).prelim + getParams(rightMost).prelim);
			
			//NodeItem leftNeighbor = PrefuseUtils.getPreviousSibling(n);
			NodeItem leftNeighbor = getRightmostAtDepth(depth);
			//System.out.println("Left neighbor of " + n + " is " + leftNeighbor + " at depth " + depth);

			if (leftNeighbor == null) {
				// Leftmost node.  Center-align with the midpoints of its children. 
				np.prelim = midpoint;
			}
			else {
				// Subsequent nodes should be spaced over an appropriate distance based
				// on the widths of the nodes and the desired spacing between siblings.
				np.prelim = getParams(leftNeighbor).prelim + spacing(leftNeighbor, n, true);
				
				// mod affects how far over the children start.  Since we just moved the
				// parent right by prelim, we should do the same for the children.  Subtract
				// off midpoint for where the first child should start.
				np.mod = np.prelim - midpoint;
			}
			
			//System.out.println("Laying out " + n);
			//System.out.println("    prelim = " + np.prelim);
			//System.out.println("    mod = " + np.mod);
		}
		
		// The node has external data input.  It should appear up 1/2 level and offset.
		//Iterator<NodeItem> externalDataInputs = PrefuseUtils.getExternalDataInputs(n);
		Iterator<NodeItem> unattachedParentIter = unattachedParents(n);
		while (unattachedParentIter.hasNext()) {
			NodeItem nextUnattached = unattachedParentIter.next();
			laidOutNodes.add(nextUnattached);
			Params params = getParams(nextUnattached);
			params.number = num;
			params.prelim = np.prelim;
			if (nextUnattached.getChildCount() > 1) {
				params.mod = np.prelim - spacing(n, nextUnattached, false);
			}
			else {
				params.mod = 0;
			}
			//System.out.println("***Laying out unattached " + nextUnattached);
			//System.out.println("    prelim = " + params.prelim + "   mod = " + params.mod + "   number = " + num);
		}

		// System.out.println("Ending firstwalk of " + n);
		// System.out.println("    prelim = " + np.prelim);
		// System.out.println("    mod = " + np.mod);

	}

	/**
	 * Return an iterator over nodes that are higher in the graph than this node
	 * but do not have a path to the root.
	 * @param n the node whose parents we are examining
	 * @return an iterator over nodes that are parents but not on the path to the root.
	 *   It could be an empty iterator.
	 */
	private Iterator<NodeItem> unattachedParents(NodeItem n) {
		//System.out.println("Getting unattached parents of " + n);
		ArrayList<NodeItem> unattachedParents = new ArrayList<>();
		Iterator<Node> neighbors = n.outNeighbors();
		Node root = getLayoutRoot();

		while (neighbors.hasNext()) {
			Node neighbor = neighbors.next();
			if (!PrefuseUtils.pathExists(neighbor, root)) {
				//System.out.println("    Adding " + neighbor);
				unattachedParents.add((NodeItem) neighbor);
			}
		}

		return unattachedParents.iterator();
	}

	private void resetRightMost() {
		rightMostAtDepth = new HashMap<>();
	}

	/**
	 * Remembers the rightmost node at the given depth during the first walk
	 * @param n the new node
	 * @param depth the depth the node is at
	 */
	private void addAtDepth(NodeItem n, int depth) {
		NodeItem rightMost = getRightmostAtDepth(depth);
		
		// If this node is further right than the current rightmost node, remember it
		if (rightMost == null || getParams(rightMost).prelim < getParams(n).prelim) {
			//System.out.println("Setting rightmost at depth " + depth + " to " + n);
			rightMostAtDepth.put(depth, n);
		}
	}

	/**
	 * Remembers the rightmost node at the given depth during the second walk
	 * @param n the new node
	 * @param depth the depth of the node
	 */
	private void addAtDepthSecondWalk(NodeItem n, int depth) {
		NodeItem rightMost = getRightmostAtDepth(depth);

		// If this node is further right than the current rightmost node, remember it
		if (rightMost == null || rightMost.getEndX() < n.getEndX()) {
			//System.out.println("Setting rightmost at depth " + depth + " to " + n);
			rightMostAtDepth.put(depth, n);
		}
	}

	/**
	 * Returns the rightmost node at this depth
	 * @param depth the depth from the root
	 * @return the rightmost node
	 */
	private NodeItem getRightmostAtDepth(int depth) {
		return rightMostAtDepth.get(depth);
	}

//	private NodeItem apportion(NodeItem node, NodeItem ancestor) {
//
//		NodeItem prevSibling = PrefuseUtils.getPreviousSibling(node);
//		
//		if (prevSibling == null) {
//			return ancestor;
//		}
//		
//		//System.out.println("Calling apportion on " + node);
//		//System.out.println(getParams(node));
//
//		NodeItem vip = node;
//		NodeItem vim = prevSibling;
//		NodeItem vop = node;
//		NodeItem vom = PrefuseUtils.getFirstSibling(vip);
//		//System.out.println("    vim = " + vim);
//		//System.out.println("    vip = " + vip);
//		//System.out.println("    vom = " + vom);
//		//System.out.println("    vop = " + vop + "\n");
//
//		double sip = getParams(vip).mod;
//		double sop = getParams(vop).mod;
//		double sim = getParams(vim).mod;
//		double som = getParams(vom).mod;
//
//		NodeItem nextRight = nextRight(vim);
//		NodeItem nextLeft = nextLeft(vip);
//
//		// keep track of the laid out nodes
//		Set<NodeItem> iteratedOverNr = new HashSet<NodeItem>();
//		Set<NodeItem> iteratedOverNl = new HashSet<NodeItem>();
//
//		while (nextRight != null && nextLeft != null && !iteratedOverNr.contains(nextRight)
//				&& !iteratedOverNl.contains(nextLeft) && nextLeft(vom) != null && nextRight(vop) != null) {
//
//			iteratedOverNr.add(nextRight);
//			iteratedOverNl.add(nextLeft);
//
//			vim = nextRight;
//			vip = nextLeft;
//			vom = nextLeft(vom);
//			vop = nextRight(vop);
//			//System.out.println("    vim = " + vim);
//			//System.out.println("    vip = " + vip);
//			//System.out.println("    vom = " + vom);
//			//System.out.println("    vop = " + vop + "\n");
//
//			getParams(vop).ancestor = node;
//			//System.out.println("apportion " + vop + " ancestor = " + node);
//			double shift = (getParams(vim).prelim + sim)
//					- (getParams(vip).prelim + sip)
//					+ spacing(vim, vip, false);
//			if (shift > 0) {
//				//moveSubtree(ancestor(vim, node, ancestor), node, shift);
//				sip += shift;
//				sop += shift;
//			}
//			sim += getParams(vim).mod;
//			sip += getParams(vip).mod;
//			som += getParams(vom).mod;
//			sop += getParams(vop).mod;
//
//			nextRight = nextRight(vim);
//			nextLeft = nextLeft(vip);
//		}
//
//		if (nextRight != null && nextRight(vop) == null) {
//			Params vopp = getParams(vop);
//			vopp.thread = nextRight;
//			vopp.mod += sim - sop;
//			//System.out.println("apportion: vop = " + vop);
//			//System.out.println("    mod = " + vopp.mod);
//		}
//		if (nextLeft != null && nextLeft(vom) == null) {
//			Params vomp = getParams(vom);
//			vomp.thread = nextLeft;
//			vomp.mod += sip - som;
//			ancestor = node;
//			//System.out.println("apportion: vom = " + vom);
//			//System.out.println("    mod = " + vomp.mod);
//		}
//		
//		
//		//System.out.println(getParams(node));
//
//		return ancestor;
//	}

//	private NodeItem nextLeft(NodeItem n) {
//		NodeItem c = null;
//
//		if (n.isExpanded()) {
//			c = PrefuseUtils.getFirstVisibleChild(n);
//		}
//		if (c == null) {
//			return getParams(n).thread;
//		}
//		else {
//			return c;
//		}
//	}
//
//	private NodeItem nextRight(NodeItem n) {
//		if (n == null) {
//			assert false;
//		}
//		NodeItem c = null;
//		if (n.isExpanded()) {
//			c = PrefuseUtils.getLastVisibleChild(n);
//		}
//		if (c == null) {
//			return getParams(n).thread;
//		}
//		else {
//			return c;
//		}
//	}

//	private void moveSubtree(NodeItem wm, NodeItem wp, double shift) {
//		Params wmp = getParams(wm);
//		Params wpp = getParams(wp);
//
//		double subtrees = wpp.number - wmp.number;
//
//		if (subtrees == 0) {
//			return;
//		}
//
//		System.out.println("moveSubtree start: wp = " + PrefuseUtils.getName(wp));
//		System.out.println("    change = " + wpp.change + "   shift = " + wpp.shift + "   prelim = " + wpp.prelim + "   mod = " + wpp.mod);
//
//		wpp.change -= shift / subtrees;
//		wpp.shift += shift;
//		wmp.change += shift / subtrees;
//		wpp.prelim += shift;
//		wpp.mod += shift;
//		//System.out.println("moveSubtree: wm = " + wm);
//		//System.out.println("    change = " + wmp.change);
//		System.out.println("moveSubtree end: wp = " + PrefuseUtils.getName(wp));
//		System.out.println("    change = " + wpp.change + "   shift = " + wpp.shift + "   prelim = " + wpp.prelim + "   mod = " + wpp.mod);
//
//	}

//	private void executeShifts(NodeItem n) {
//		double shift = 0;
//		double change = 0;
//		Iterator<NodeItem> reverseIterator = PrefuseUtils.reverseChildren(n);
//		while (reverseIterator.hasNext()) {
//			NodeItem c = reverseIterator.next();
//			//System.out.println("executeShifts: c = " + c + "   shifting by " + shift);
//			Params cp = getParams(c);
//			cp.prelim += shift;
//			cp.mod += shift;
//			change += cp.change;
//			shift += cp.shift + change;
//			//System.out.println("    prelim = " + cp.prelim + "   mod = " + cp.mod);
//		}
//	}

//	private NodeItem ancestor(NodeItem vim, NodeItem v, NodeItem a) {
//
//		NodeItem p = PrefuseUtils.getVisibleParent(v);
//		Params vimp = getParams(vim);
//
//		if (PrefuseUtils.getVisibleParent(vimp.ancestor) == p) {
//			return vimp.ancestor;
//		} else {
//			return a;
//		}
//	}

	/**
	 * Assigns x, y coordinates to the nodes
	 * @param node the node being placed
	 * @param parent the parent of the node being placed
	 * @param centerOffset offset from the centerline
	 * @param depth levels from the root
	 */
	private void secondWalk(NodeItem node, NodeItem parent, double centerOffset, int depth) {

		if (secondWalkDone.contains(node)) {
			return;
		}
		
		secondWalkDone.add(node);

		Params nodeParams = getParams(node);
//		System.out.println("Calling secondWalk on " + node);
//		System.out.println("    depth = " + depth);
//		System.out.println("    mDepths[depth] = " + mDepths[depth]);
//		System.out.println("    with centerOffset = " + centerOffset);
//		System.out.println("    mod = " + nodeParams.mod);
//		System.out.println("    prelim = " + nodeParams.prelim);
		setBreadth(node, parent, getRightmostAtDepth(depth), nodeParams.prelim + centerOffset);
		setDepth(node, parent, mDepths[depth]);
		addAtDepthSecondWalk(node, depth);

		// Recursively walk the children of visible procedural nodes
		// If doing a data derivation, we also recursively walk the data nodes since
		// that is the only way that we will reach all the nodes we want to layout
		if (node.isExpanded() && node.isVisible() && (PrefuseUtils.isProcNode(node) || dataDerivation || depth == 0)) { 
			Iterator<Node> childrenIterator = PrefuseUtils.visibleChildren(node);
			while (childrenIterator.hasNext()) {
				NodeItem child = (NodeItem) childrenIterator.next();

				// Add in the parent's mod when placing a child.  A normal
				// child is placed down 1 level.
				secondWalk(child, node, centerOffset + nodeParams.mod, depth+2);
			}
			
			// The node has a data node that is an external input.
			//Iterator<NodeItem> externalInputs = PrefuseUtils.getExternalDataInputs(node);
			Iterator<NodeItem> unattachedParentIter = unattachedParents(node);
			
			// How far did the parent move?  Try to move the unattached nodes the same amount.
			double parentShift = node.getX() - mAx;
			while (unattachedParentIter.hasNext()) {
				NodeItem nextExternalInput = unattachedParentIter.next();
				Params params = getParams(nextExternalInput);
				secondWalk(nextExternalInput, node, params.mod + parentShift, depth-2);
			}

		}

		nodeParams.clear();
	}

	/**
	 * Sets the x-coordinate of a node
	 * @param n the node being set
	 * @param p the node's parent
	 * @param b 
	 */
	private void setBreadth(NodeItem n, NodeItem p, NodeItem leftNeighbor, double b) {
		double leftEndX;
		if (leftNeighbor == null){
			leftEndX = 0;
		}
		else {
			leftEndX = leftNeighbor.getEndX();
			//System.out.println("Left neighbor is " + leftNeighbor + " at x " + leftEndX);
		}
		setX(n, p, mAx + b);

		// If the node did not get placed to the right of its left neighbor, we slide it over.
		// Perhaps not ideal, but we are doing this instead of trying to get apportion and
		// moveSubtree to work right for these graphs.  Those algorithms were designed for
		// trees, not DAGs.  Barbara Lerner  June 24, 2013
		if (leftNeighbor != null && n.getEndX() - n.getBounds().getWidth() < leftEndX + SIBLING_SPACING) {
			//System.out.print("Moving end of " + n + " from " + n.getEndX() + " to ");
			setX(n, p, leftEndX + SIBLING_SPACING + n.getBounds().getWidth());
			//System.out.println(n.getEndX());
		}
		
		//System.out.println("  Setting coordinates of " + n);
		//System.out.println("  x = " + n.getX());
	}

	/**
	 * Sets the y-coordinate of a node
	 * @param n the node being placed
	 * @param p the parent of the node
	 * @param d 
	 */
	private void setDepth(NodeItem n, NodeItem p, double d) {
		setY(n, p, mAy + d);
		//System.out.println("  y = " + (mAy + d));
	}

	// ------------------------------------------------------------------------
	// Params Schema

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String PARAMS = "_reingoldTilfordParams";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema PARAMS_SCHEMA = new Schema();
	static {
		PARAMS_SCHEMA.addColumn(PARAMS, Params.class);
	}

	protected void initSchema(TupleSet ts) {
		ts.addColumns(PARAMS_SCHEMA);
	}

	private Params getParams(NodeItem item) {
		//System.out.println("Getting params of " + PrefuseUtils.getName(item));
		Params rp = null;
		if (item != null) {
			rp = (Params) item.get(PARAMS);
		}

		if (rp == null) {
			//System.out.println("    Params were null!  Creating...");
			rp = new Params();
			if (item != null) {
				item.set(PARAMS, rp);
			}
		}
		if (rp.number == -2 && item != null) {
			//System.out.println("    Calling init on params");
			rp.init(item);
		}
		//System.out.println("    prelim = " + rp.prelim);
		return rp;
	}

	/**
	 * Wrapper class holding parameters used for each node in this layout.
	 */
	public static class Params implements Cloneable {
		// Seems to be the amount offset from the center axis
		private double prelim;
		
		// Seems to be the amount the child should be offset in addition to 
		// the child's on mod value.
		private double mod;
		
		private double shift;
		private double change;
		
		private int number = -2;
		private NodeItem ancestor = null;
		private NodeItem thread = null;

		public void init(NodeItem item) {
			ancestor = item;
			number = -1;
		}

		public void clear() {
			number = -2;
			prelim = 0;
			mod = 0;
			shift = 0;
			change = 0;
			ancestor = null; 
			thread = null;
		}
		
		@Override
		public String toString() {
			return "   prelim = " + prelim + "   mod = " + mod + "   shift = " + shift + 
					"\n   change = " + change + "   number = " + number;
		}
		
	}


} // end of class DDGLayout

