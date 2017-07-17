package laser.ddg.visualizer;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.event.MouseInputListener;

/**
 * Listen for clicks or drags in the overview, move the viewFinder
 * accordingly
 */
public class ViewWorkflowFinderListener implements MouseInputListener {
	private ArrayList<DisplayWorkflowWithOverview> displays = new ArrayList<>();
	private boolean draggingRect;
	private Point prev;

	public ViewWorkflowFinderListener(DisplayWorkflowWithOverview dispPlusOver) {
		super();
		displays.add(dispPlusOver);
	}

	public ViewWorkflowFinderListener(DisplayWorkflowWithOverview leftDispPlusOver, DisplayWorkflowWithOverview rightDispPlusOver) {
		super();
		displays.add(leftDispPlusOver);
		displays.add(rightDispPlusOver);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (DisplayWorkflowWithOverview display : displays) {

			Rectangle viewFinder = display.calcViewFinder();
			if (viewFinder.contains(e.getPoint())) { // inside rectangle
				prev = e.getPoint();
				draggingRect = true;
				return;
			}			
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// find where mouse was clicked on the Overview, transform it
		// out of the overview and onto the userDisplay. Then pan to that
		// location
		if (!draggingRect) {
			Point p = transPoint(e.getPoint());
			for (DisplayWorkflowWithOverview dispWithOver : displays) {
				dispWithOver.getDisplay().animatePanTo(p, 1000);
			}
		} else {
			draggingRect = false; // reset draggingRect for next time.
		}
	}

	/**
	 * translate point from overview coordinates to userDisplay coordinates
	 * 
	 * @param p
	 *            Point in question
	 * @return transformed point
	 */
	private Point transPoint(Point p) {
		// System.out.println(p.x + ", " + p.y + " absolute point");
		AffineTransform overTransI = displays.get(0).getOverview().getInverseTransform();
		overTransI.transform(p, p);
		AffineTransform userTrans = displays.get(0).getDisplay().getTransform();
		userTrans.transform(p, p);
		// System.out.println(p.getX() + ", " + p.getY() + " transformed
		// point");
		return p;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (draggingRect) {
			Point p = transPoint(e.getPoint());
			prev = transPoint(prev);

			int xMovement = prev.x - p.x;
			int yMovement = prev.y - p.y;
			
			for (DisplayWorkflowWithOverview dispPlusOver: displays) {
				dispPlusOver.getDisplay().animatePan(xMovement, yMovement, 1);
			}

			prev = e.getPoint();
			// System.out.println("x movement: " + xMovement + " and y
			// movement: " + yMovement);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

}