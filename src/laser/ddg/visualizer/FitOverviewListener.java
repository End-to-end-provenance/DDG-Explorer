package laser.ddg.visualizer;

import java.awt.geom.Rectangle2D;

import prefuse.Display;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;

/**
 * Keeps track of bounds of DDG so that Overview will accommodate changes
 * 
 * @author Nicole
 */
public class FitOverviewListener implements ItemBoundsListener {
	private Rectangle2D m_bounds = new Rectangle2D.Double();
	private Rectangle2D m_temp = new Rectangle2D.Double();
	private double m_d = 15;

	public FitOverviewListener() {
		super();
	}

	@Override
	public void itemBoundsChanged(Display displayGiven) {
		displayGiven.getItemBounds(m_temp);
		// expand a rectangle by the given amount
		GraphicsLib.expand(m_temp, 25 / displayGiven.getScale());

		double dd = m_d / displayGiven.getScale();
		// difference between past and present bounds in x, y, width, or
		// height
		double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
		double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
		double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
		double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
		if (xd > dd || yd > dd || wd > dd || hd > dd) {
			m_bounds.setFrame(m_temp);
			DisplayLib.fitViewToBounds(displayGiven, m_bounds, 0);
		}
	}
}