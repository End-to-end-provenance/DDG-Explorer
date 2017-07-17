package laser.ddg.visualizer;

import java.awt.event.MouseEvent;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Defines the expand / collapse behavior when a node is clicked on.
 * @author Barbara Lerner
 * @version Jun 20, 2013
 *
 */
public class ExpandCollapseWorkflowControl extends ControlAdapter {
	// The objec that is building the Prefuse graph
	private WorkflowGraphBuilder builder;

	/**
	 * Creates the controller
	 * @param builder the object that is building the graph.
	 */
	public ExpandCollapseWorkflowControl(WorkflowGraphBuilder builder) {
		this.builder = builder;
	}

	/**
	 * When the user left-clicks on a start node, the node is 
	 * collapsed.  When the user left-clicks on a step node, the
	 * node is expanded.
         * @param item
         * @param e
	 */
	@Override
	public void itemClicked(VisualItem item, MouseEvent e) {
		if (e.isControlDown() || e.isPopupTrigger()) {
			// Right-click.  Don't expand/contract.
			return;
		}
		
		Visualization vis = item.getVisualization();
		synchronized(vis) {
			// Sometimes the item passed in is an item not currently visible!
			// This seems to get around that.
			Display d = vis.getDisplay(0);
			item = d.findItem(e.getPoint());
			
			if (item.isVisible()) {
				if (item instanceof NodeItem) {
					builder.handleNodeClick((NodeItem) item);
				}
			}
			else {
				assert false : "Clicked on INVISIBLE " + item;
			}
		}

	}

}
