package laser.ddg.workflow.visualizer;


import prefuse.action.layout.graph.NodeLinkTreeLayout;

/**
 * Creates a layout for workflow graphs
 * 
 * @author Connor Gregorich-Trevor
 * 
 */
public class WorkflowLayout extends NodeLinkTreeLayout {

	/**
	 * Constructor used to call NodeLinkTreeLayout
	 * 
	 * @param group the string representation of the graph.
	 * @param orientation the orientation of the flow.
	 * @param dspace the space between depth levels of the trees
	 * @param bspace the space between sibling nodes
	 * @param tspace the space between neighboring subtrees
	 */
	public WorkflowLayout(String group, int orientation, double dspace, double bspace, double tspace) {
		super(group, orientation, dspace, bspace, tspace);
	}

}

