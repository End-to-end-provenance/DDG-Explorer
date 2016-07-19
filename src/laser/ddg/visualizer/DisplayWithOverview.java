package laser.ddg.visualizer;

import java.awt.Graphics2D;

import laser.ddg.visualizer.DDGDisplay.AutoPanAction;
import laser.ddg.visualizer.DDGDisplay.PopupMenu;
import laser.ddg.visualizer.PrefuseGraphBuilder.vfListener;
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

	public DisplayWithOverview (PrefuseGraphBuilder builder) {
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
		if (!compareDDG) {
			display.addControlListener(new DragControl());
			display.addControlListener(new PanControl());
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

		}

		// set up the display's overview
		// (no drag, pan, or zoom control needed)
		displayOverview.setVisualization(vis);
		if (!compareDDG) {
			// display size
			displayOverview.setSize(175, 500);
			// To force overview's shape and zoom when bounds change
			displayOverview.addItemBoundsListener(new FitOverviewListener());

			// keep track of the display's view and draw Overview's square
			// accordingly
			displayOverview.addPaintListener(new ViewFinderBorders(display));

			// keep track of mouse clicks to move the grey rectangle
			vfListener vfL = new vfListener(display, displayOverview);
			displayOverview.addMouseMotionListener(vfL);
			displayOverview.addMouseListener(vfL);
		}
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
}
