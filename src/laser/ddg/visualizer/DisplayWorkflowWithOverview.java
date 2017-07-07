package laser.ddg.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import laser.ddg.visualizer.WorkflowDisplay.AutoPanAction;
import laser.ddg.visualizer.WorkflowDisplay.PopupMenu;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;

public class DisplayWorkflowWithOverview {
	private WorkflowGraphBuilder builder;

	private WorkflowDisplay display;
	private AutoPanAction autoPan;

	// display overview
	private WorkflowDisplay displayOverview;

	public DisplayWorkflowWithOverview(WorkflowGraphBuilder builder) {
		this.builder = builder;
		display = new WorkflowDisplay(builder);
		autoPan = display.new AutoPanAction();
		displayOverview = new WorkflowDisplay(builder);
	}

	public WorkflowDisplay getDisplay() {
		return display;
	}

	public WorkflowDisplay getOverview() {
		// TODO Auto-generated method stub
		return displayOverview;
	}

	public void initialize(Visualization vis, boolean compareWorkflow) {
		display.setVisualization(vis);

		display.addControlListener(new DragControl());
		display.addControlListener(new ZoomControl());
		// zoom with mouse wheel
		display.addControlListener(new WheelZoomControl(true, true));
		display.addControlListener(new ExpandCollapseWorkflowControl(builder));
		display.addPaintListener(new PaintListener() {
			@Override
			public void prePaint(Display d, Graphics2D g) {
			}

			@Override
			public void postPaint(Display d, Graphics2D g) {
				//System.out.println("display postPaint");
				displayOverview.repaint();
			}
		});

		if (!compareWorkflow) {
			display.addControlListener(new PanControl());
			
			// Pan control is set after both sides of the compare
			// panel are ready

		}

		// set up the display's overview
		// (no drag, pan, or zoom control needed)
		displayOverview.setVisualization(vis);

		// keep track of the display's view and draw Overview's square
		// accordingly
		displayOverview.addPaintListener(new ViewWorkflowFinderBorders(this));
		
		//To force overview's shape and zoom when bounds change
		displayOverview.addItemBoundsListener(new FitOverviewListener());

		displayOverview.setPreferredSize(new Dimension(175, 500));
			
		if (!compareWorkflow) {
			// keep track of mouse clicks to move the grey rectangle.
			// When doing workflow comparisons, we do this later so that 
			// both workflows have the same view finder listener.
			ViewWorkflowFinderListener vwfL = new ViewWorkflowFinderListener(this);
			displayOverview.addMouseMotionListener(vwfL);
			displayOverview.addMouseListener(vwfL);
		}
	}

	public JPanel createPanel(Component description) {
		JPanel wfMain = new JPanel();
		wfMain.setLayout(new BoxLayout(wfMain, BoxLayout.X_AXIS));
		wfMain.setBackground(Color.WHITE);
		if (description != null) {
			// Main display shows the workflow name here.
			JPanel bigDisp = new JPanel();
			bigDisp.setLayout(new BorderLayout());
			bigDisp.add(description, BorderLayout.NORTH);
			bigDisp.add(display, BorderLayout.CENTER);
			wfMain.add(bigDisp);
		}
		else {
			wfMain.add(display);
		}

		WorkflowDisplay wfOverview = displayOverview;
		wfOverview.setBorder(BorderFactory.createTitledBorder("Overview"));
		wfMain.add(wfOverview);
		
		// legend added to WEST through preferences

		
		wfOverview.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				//System.out.println("overview resized");
				wfOverview.zoomToExactFit();
			}
		});


		return wfMain;
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
	 *            corner overview of workflow
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
	
	/**
	 * Keeps track of bounds of the workflow so that Overview will accommodate changes
	 * @author Nicole
	 */
	 public static class FitOverviewListener implements ItemBoundsListener {
			private Rectangle2D m_bounds = new Rectangle2D.Double();
			private Rectangle2D m_temp = new Rectangle2D.Double();
			private double m_d = 15;

			public FitOverviewListener() {
				super();
			}

			@Override
			public void itemBoundsChanged(Display displayGiven) {
			    displayGiven.getItemBounds(m_temp);
			    //expand a rectangle by the given amount
			    GraphicsLib.expand(m_temp, 25/displayGiven.getScale());

			    double dd = m_d/displayGiven.getScale();
			    //difference between past and present bounds in x, y, width, or height
			    double xd = Math.abs(m_temp.getMinX()-m_bounds.getMinX());
			    double yd = Math.abs(m_temp.getMinY()-m_bounds.getMinY());
			    double wd = Math.abs(m_temp.getWidth()-m_bounds.getWidth());
			    double hd = Math.abs(m_temp.getHeight()-m_bounds.getHeight());
			    if ( xd>dd || yd>dd || wd>dd || hd>dd ) {
			    	m_bounds.setFrame(m_temp);
			    	DisplayLib.fitViewToBounds(displayGiven, m_bounds, 0);
			    }
			}
	 }


}
