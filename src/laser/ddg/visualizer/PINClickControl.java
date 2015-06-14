/**
 * 
 */
package laser.ddg.visualizer;

import java.awt.event.MouseEvent;

import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * @author xiang
 * 
 */
public class PINClickControl extends ControlAdapter {

	private PrefuseGraphBuilder p;

	public PINClickControl(PrefuseGraphBuilder p) {
		this.p = p;

	}

	@Override
	public void itemClicked(VisualItem item, MouseEvent e) {
		System.out.println(item.getString(PrefuseUtils.TYPE));
		if (!item.getString(PrefuseUtils.TYPE).equals(
				PrefuseUtils.DATA_NODE)) {
			if (item.getFillColor() == PrefuseGraphBuilder.NONLEAF_COLOR) {
				item.setFillColor(PrefuseGraphBuilder.NONLEAF_COLOR_SELECTED_COLOR);
				p.setSelectedProcedureNodeID(item
						.getInt(PrefuseUtils.ID));
			} else if (item.getFillColor() == PrefuseGraphBuilder.NONLEAF_COLOR_SELECTED_COLOR) {
				item.setFillColor(PrefuseGraphBuilder.NONLEAF_COLOR);
				p.setSelectedProcedureNodeID(-1);
			} else if (item.getFillColor() == PrefuseGraphBuilder.LEAF_COLOR) {
				item.setFillColor(PrefuseGraphBuilder.LEAF_COLOR_SELECTED_COLOR);
				p.setSelectedProcedureNodeID(item
						.getInt(PrefuseUtils.ID));
			} else {
				item.setFillColor(PrefuseGraphBuilder.LEAF_COLOR);
				p.setSelectedProcedureNodeID(-1);
			}
			System.out.println("Clicked: "
					+ item.getInt(PrefuseUtils.ID) + " "
					+ item.getString(PrefuseUtils.TYPE) + " "
					+ item.getString(PrefuseUtils.NAME));
			item.getVisualization().repaint();
		}
		super.itemClicked(item, e);
	}
}
