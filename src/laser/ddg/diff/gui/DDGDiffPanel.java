package laser.ddg.diff.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import laser.ddg.gui.Toolbar;
import laser.ddg.visualizer.DDGDisplay;
import laser.ddg.visualizer.DisplayWithOverview;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.ViewFinderListener;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
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
	private Toolbar toolbar;

	public DDGDiffPanel () {
		setLayout (new BorderLayout());
		
		toolbar = new Toolbar ();
		add(toolbar, BorderLayout.NORTH);
	
		JPanel comparePanel = new JPanel();
		comparePanel.setLayout (new GridLayout (1, 2, 8, 0));
		leftPanel = new JPanel();
		leftPanel.setBorder(BorderFactory.createEtchedBorder());
		comparePanel.add (leftPanel);
		
		rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEtchedBorder());
		comparePanel.add (rightPanel);
		
		add (comparePanel, BorderLayout.CENTER);
	}

	public void displayDiffResults (PrefuseGraphBuilder builderLeft, PrefuseGraphBuilder builderRight) {
		builderLeft.processFinished();
		builderRight.processFinished();
		
		DisplayWithOverview leftDispPlusOver = builderLeft.getDispPlusOver();
		DisplayWithOverview rightDispPlusOver = builderRight.getDispPlusOver();

		DDGDisplay leftDisplay = leftDispPlusOver.getDisplay();
		DDGDisplay rightDisplay = rightDispPlusOver.getDisplay();
		DualPanControl panning = new DualPanControl(leftDisplay, rightDisplay);
		leftDisplay.addControlListener(panning);
		rightDisplay.addControlListener(panning);
		toolbar.addDisplays(leftDisplay, rightDisplay);
		
		ViewFinderListener vfL = new ViewFinderListener (leftDispPlusOver, rightDispPlusOver);
		DDGDisplay leftOverview = leftDispPlusOver.getOverview();
		DDGDisplay rightOverview = rightDispPlusOver.getOverview();
		leftOverview.addMouseMotionListener(vfL);
		leftOverview.addMouseListener(vfL);
		rightOverview.addMouseMotionListener(vfL);
		rightOverview.addMouseListener(vfL);
		
		leftPanel.add(leftDispPlusOver.createPanel(null));
		rightPanel.add(rightDispPlusOver.createPanel(null));
	}

	/**
	 * This is a modification of Prefuse's PanControl that will
	 * simultaneously pan two displays.  Unfortunately, PanControl
	 * is not written with subclassing in mind so it could not
	 * just be extended.
	 */
	private static class DualPanControl extends ControlAdapter {

		private boolean mPanOverItem;
		private int mXDown, mYDown;
		private int mButton;
		private Display displayLeft;
		private Display displayRight;

		/**
		 * Create a new PanControl.
		 */
		public DualPanControl(Display displayLeft, Display displayRight) {
			this(LEFT_MOUSE_BUTTON, false);
			this.displayLeft = displayLeft;
			this.displayRight = displayRight;
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
		 *            if true, the panning control will work even while the
		 *            mouse is over a visual item.
		 */
		public DualPanControl(int mouseButton, boolean panOverItem) {
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
	} // end of class DualPanControl
}




