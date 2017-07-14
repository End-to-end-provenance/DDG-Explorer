package laser.ddg.visualizer;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ArrayLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 * Creates a layout for workflow graphs
 * 
 * @author Antonia Miruna Oprescu
 * 
 */
public class WorkflowLayout extends NodeLinkTreeLayout {

	public WorkflowLayout(String group, int orientation, double dspace, double bspace, double tspace) {
		super(group, orientation, dspace, bspace, tspace);
		// TODO Auto-generated constructor stub
	}

} // end of class WorkflowLayout.

