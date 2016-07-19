package laser.ddg.diff.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import laser.ddg.visualizer.DDGDisplay;
import laser.ddg.visualizer.ExpandCollapseControl;
import laser.ddg.visualizer.FitOverviewListener;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.ViewFinderBorders;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.ZoomControl;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * The panel to display two ddgs that are being compared side-by-side.
 * Scrolling one of the ddgs also scrolls the other one.
 * 
 * @author Barbara Lerner
 * @version Jul 19, 2016
 *
 */
public class DDGDiffPanel extends JPanel {
	private JPanel leftPanel;
	private JPanel rightPanel;
	private ToolbarCompare toolbar;

	public DDGDiffPanel () {
		setLayout (new BorderLayout());
		
		toolbar = new ToolbarCompare();
		add(toolbar, BorderLayout.NORTH);
	
		JPanel comparePanel = new JPanel();
		comparePanel.setLayout (new GridLayout (1, 2, 8, 0));
		leftPanel = new JPanel();
		comparePanel.add (leftPanel);
		rightPanel = new JPanel();
		comparePanel.add (rightPanel);
		leftPanel.setBorder(BorderFactory.createEtchedBorder());
		rightPanel.setBorder(BorderFactory.createEtchedBorder());
		add (comparePanel, BorderLayout.CENTER);
	}

	public void displayDiffResults (PrefuseGraphBuilder builderLeft, PrefuseGraphBuilder builderRight) {
		builderLeft.processFinished();
		builderRight.processFinished();
	
		DDGDisplay displayLeft = builderLeft.getDisplay();
		DDGDisplay displayOverviewLeft = builderLeft.getOverview();
	
		DDGDisplay displayRight = builderRight.getDisplay();
		DDGDisplay displayOverviewRight = builderRight.getOverview();
		PanMyControl panning = new PanMyControl(displayLeft, displayRight);
		VfListener vfL = new VfListener(displayLeft, displayOverviewLeft, displayRight, displayOverviewRight);
	
		initializeDisplay(panning, displayLeft, displayOverviewLeft, vfL, builderLeft);
		initializeDisplay(panning, displayRight, displayOverviewRight, vfL, builderRight);
	
		populateDisplay(leftPanel, displayLeft, displayOverviewLeft);
		populateDisplay(rightPanel, displayRight, displayOverviewRight);
		
		toolbar.setDisplays(displayLeft, displayRight);
	}

	/**
	 * Populate the panel with the graph display and its corresponding overview
	 * display
	 * 
	 * @param display
	 * @param displayOverview
	 * @return the panel constructed
	 */
	private static void populateDisplay(JPanel outerPanel, DDGDisplay display, DDGDisplay displayOverview) {
		JPanel newPanel = new JPanel(new BorderLayout());
		newPanel.setBackground(Color.WHITE);
		newPanel.add(display, BorderLayout.CENTER);
	
		displayOverview.setBorder(BorderFactory.createTitledBorder("Overview"));

		newPanel.add(displayOverview, BorderLayout.EAST);
		displayOverview.setMaximumSize(new Dimension (90, 400));
		
		outerPanel.add(newPanel);
	}

	/**
	 * Add action listeners to the display and the corresponding overview using
	 * its own graph builder.
	 * 
	 * @param panning
	 * @param display
	 * @param displayOverview
	 * @param vfL
	 *            listener object
	 * @param builder
	 */
	private static void initializeDisplay(PanMyControl panning, DDGDisplay display, DDGDisplay displayOverview,
			VfListener vfL, PrefuseGraphBuilder builder) {
		display.addControlListener(new DragControl());
		display.addControlListener(panning);
		display.addControlListener(new ZoomControl());
		display.addControlListener(new ExpandCollapseControl(builder));
		display.addPaintListener(new PaintListener() {
			@Override
			public void prePaint(Display d, Graphics2D g) {
			}

			@Override
			public void postPaint(Display d, Graphics2D g) {
				displayOverview.repaint();
			}
		});
		displayOverview.addItemBoundsListener(new FitOverviewListener());
		displayOverview.addPaintListener(new ViewFinderBorders(display));
		displayOverview.addMouseMotionListener(vfL);
		displayOverview.addMouseListener(vfL);
		display.repaint();
	}
}

/**
 * Listen for clicks or drags in the overview, move the viewFinder accordingly
 */
class VfListener implements MouseInputListener {
	private DDGDisplay userDisplayLeft;
	private DDGDisplay overviewLeft;
	private DDGDisplay userDisplayRight;
	private DDGDisplay overviewRight;
	private boolean draggingRect;
	private Point prev;

	public VfListener(DDGDisplay userDisplayLeft, DDGDisplay overviewLeft, DDGDisplay userDisplayRight,
			DDGDisplay overviewRight) {
		super();
		this.userDisplayLeft = userDisplayLeft;
		this.overviewLeft = overviewLeft;
		this.userDisplayRight = userDisplayRight;
		this.overviewRight = overviewRight;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Rectangle viewFinderLeft = calcViewFinder(userDisplayLeft, overviewLeft);
		Rectangle viewFinderRight = calcViewFinder(userDisplayRight, overviewRight);
		if (viewFinderLeft.contains(e.getPoint()) || viewFinderRight.contains(e.getPoint())) {
			prev = e.getPoint();
			draggingRect = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// find where mouse was clicked on the Overview, transform it
		// out of the overview and onto the userDisplay. Then pan to that
		// location
		if (!draggingRect) {
			Point p = transPoint(e.getPoint());
			userDisplayLeft.animatePanTo(p, 1000);
			userDisplayRight.animatePanTo(p, 1000);
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
		AffineTransform overTransI = overviewLeft.getInverseTransform();
		overTransI.transform(p, p);
		AffineTransform userTrans = userDisplayLeft.getTransform();
		userTrans.transform(p, p);
		return p;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (draggingRect) {
			Point p = transPoint(e.getPoint());
			prev = transPoint(prev);

			int xMovement = prev.x - p.x;
			int yMovement = prev.y - p.y;
			userDisplayLeft.animatePan(xMovement, yMovement, 1);
			userDisplayRight.animatePan(xMovement, yMovement, 1);
			prev = e.getPoint();
		}
	}

	public static Rectangle calcViewFinder(Display userDisplay, Display overview) {
		// retrieve width and height of the userDisplay's window on the screen
		Rectangle compBounds = userDisplay.getBounds();
		Point topLeft = new Point(0, (int) compBounds.getMinY());
		Point bottomRight = new Point((int) (compBounds.getMaxX() - compBounds.getMinX()), (int) compBounds.getMaxY());
		AffineTransform userTransI = userDisplay.getInverseTransform();
		userTransI.transform(topLeft, topLeft);
		userTransI.transform(bottomRight, bottomRight);
		AffineTransform overTrans = overview.getTransform();
		overTrans.transform(topLeft, topLeft);
		overTrans.transform(bottomRight, bottomRight);

		int x = topLeft.x;
		int y = topLeft.y;
		int width = bottomRight.x - x;
		int height = bottomRight.y - y;

		return new Rectangle(x, y, width, height);
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

/**
 * To animate the display when the Graph display is dragged with the mouse.
 */
class PanMyControl extends ControlAdapter {

	private boolean mPanOverItem;
	private int mXDown, mYDown;
	private int mButton;
	private Display displayLeft;
	private Display displayRight;

	/**
	 * Create a new PanControl.
	 */
	public PanMyControl(Display displayLeft, Display displayRight) {
		this(LEFT_MOUSE_BUTTON, false);
		this.displayLeft = displayLeft;
		this.displayRight = displayRight;
	}

	/**
	 * Create a new PanControl.
	 * 
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is
	 *            over a visual item.
	 */
	public PanMyControl(boolean panOverItem) {
		this(LEFT_MOUSE_BUTTON, panOverItem);
	}

	/**
	 * Create a new PanControl.
	 * 
	 * @param mouseButton
	 *            the mouse button that should initiate a pan. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 */
	public PanMyControl(int mouseButton) {
		this(mouseButton, false);
	}

	/**
	 * Create a new PanControl
	 * 
	 * @param mouseButton
	 *            the mouse button that should initiate a pan. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is
	 *            over a visual item.
	 */
	public PanMyControl(int mouseButton, boolean panOverItem) {
		mButton = mouseButton;
		mPanOverItem = panOverItem;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (UILib.isButtonPressed(e, mButton)) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			mXDown = e.getX();
			mYDown = e.getY();
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (UILib.isButtonPressed(e, mButton)) {
			// Display display_left = (Display)e.getComponent();
			int x = e.getX(), y = e.getY();
			int dx = x - mXDown, dy = y - mYDown;
			displayLeft.pan(dx, dy);
			displayRight.pan(dx, dy);
			mXDown = x;
			mYDown = y;
			displayLeft.repaint();
			displayRight.repaint();
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (UILib.isButtonPressed(e, mButton)) {
			e.getComponent().setCursor(Cursor.getDefaultCursor());
			mXDown = -1;
			mYDown = -1;
		}
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	@Override
	public void itemPressed(VisualItem item, MouseEvent e) {
		if (mPanOverItem) {
			mousePressed(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	@Override
	public void itemDragged(VisualItem item, MouseEvent e) {
		if (mPanOverItem) {
			mouseDragged(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	@Override
	public void itemReleased(VisualItem item, MouseEvent e) {
		if (mPanOverItem) {
			mouseReleased(e);
		}
	}
} // end of class PanMyControl


