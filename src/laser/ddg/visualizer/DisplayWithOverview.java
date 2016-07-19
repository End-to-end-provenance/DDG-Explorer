package laser.ddg.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import laser.ddg.visualizer.DDGDisplay.AutoPanAction;
import laser.ddg.visualizer.DDGDisplay.PopupMenu;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.util.display.PaintListener;

public class DisplayWithOverview {
	private PrefuseGraphBuilder builder;

	private DDGDisplay display;
	private AutoPanAction autoPan;

	// display overview
	private DDGDisplay displayOverview;

	public DisplayWithOverview(PrefuseGraphBuilder builder) {
		this.builder = builder;
		display = new DDGDisplay(builder);
		autoPan = display.new AutoPanAction();
		displayOverview = new DDGDisplay(builder);
	}

	public DDGDisplay getDisplay() {
		return display;
	}

	public DDGDisplay getOverview() {
		// TODO Auto-generated method stub
		return displayOverview;
	}

	public void initialize(Visualization vis, boolean compareDDG) {
		display.setVisualization(vis);
		// display size
		display.setSize(720, 500);

		display.addControlListener(new DragControl());
		display.addControlListener(new ZoomControl());
		// zoom with mouse wheel
		display.addControlListener(new WheelZoomControl(true, true));
		// make node and incident edges invisible
		// d.addControlListener(mControl);
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

		if (!compareDDG) {
			display.addControlListener(new PanControl());
		}

		// set up the display's overview
		// (no drag, pan, or zoom control needed)
		displayOverview.setVisualization(vis);
		// display size
		displayOverview.setSize(175, 500);
		// To force overview's shape and zoom when bounds change
		displayOverview.addItemBoundsListener(new FitOverviewListener());

		// keep track of the display's view and draw Overview's square
		// accordingly
		displayOverview.addPaintListener(new ViewFinderBorders(this));

		if (!compareDDG) {
			// keep track of mouse clicks to move the grey rectangle
			ViewFinderListener vfL = new ViewFinderListener(this);
			displayOverview.addMouseMotionListener(vfL);
			displayOverview.addMouseListener(vfL);
		}
	}

	public JPanel createPanel(Component description) {
		JPanel ddgMain = new JPanel(new BorderLayout());
		ddgMain.setBackground(Color.WHITE);
		if (description != null) {
			ddgMain.add(description, BorderLayout.NORTH);
		}
		ddgMain.add(display, BorderLayout.CENTER);

		DDGDisplay ddgOverview = displayOverview;
		ddgOverview.setBorder(BorderFactory.createTitledBorder("Overview"));
		ddgMain.add(ddgOverview, BorderLayout.EAST);
		// legend added to WEST through preferences
		// TODO searchbar added to SOUTH! (through button press?)

		// resize components within layers
		ddgMain.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int panelHeight = ddgMain.getHeight();
				Rectangle prevBounds = ddgOverview.getBounds();
				ddgOverview.setBounds(prevBounds.x, prevBounds.y, prevBounds.width, panelHeight - 16);
			}
		});

		return ddgMain;
	}

	public Action getPanner() {
		return autoPan;
	}

	public void createPopupMenu() {
		PopupMenu options = display.new PopupMenu();
		options.createPopupMenu();
	}

	public void stopRefocusing() {
		display.stopRefocusing();
		displayOverview.stopRefocusing();
	}

	/**
	 * calculate viewFinder's bounds
	 * 
	 * @param userDisplay
	 *            larger, maleable user display
	 * @param overview
	 *            corner overview of DDG
	 * @return rectangle transformed to place directly onto overview
	 */
	public Rectangle calcViewFinder() {
		// retrieve width and height of the userDisplay's window on the screen
		Rectangle compBounds = display.getBounds();
		Point topLeft = new Point(0, (int) compBounds.getMinY()); // (int)compBounds.getMinX(),
																	// (int)compBounds.getMinY());
		Point bottomRight = new Point((int) (compBounds.getMaxX() - compBounds.getMinX()), (int) compBounds.getMaxY());
	
		// transform point off of the user's display and onto the overview's
		// transformation
		AffineTransform userTransI = display.getInverseTransform();
		userTransI.transform(topLeft, topLeft);
		userTransI.transform(bottomRight, bottomRight);
		AffineTransform overTrans = displayOverview.getTransform();
		overTrans.transform(topLeft, topLeft);
		overTrans.transform(bottomRight, bottomRight);
	
		int x = topLeft.x;
		int y = topLeft.y;
		int width = bottomRight.x - x;
		int height = bottomRight.y - y;
	
		return new Rectangle(x, y, width, height);
	}

}
