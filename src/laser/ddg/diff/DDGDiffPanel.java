package laser.ddg.diff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import laser.ddg.visualizer.DDGDisplay;
import laser.ddg.visualizer.ExpandCollapseControl;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import prefuse.controls.DragControl;
import prefuse.controls.ZoomControl;

class DDGDiffPanel extends JPanel {
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

	void displayDiffResults (PrefuseGraphBuilder builderLeft, PrefuseGraphBuilder builderRight) {
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
		display.addPaintListener(new UpdateOverview(displayOverview));
		displayOverview.addItemBoundsListener(new FitOverviewListener());
		displayOverview.addPaintListener(new VfBorders(display));
		displayOverview.addMouseMotionListener(vfL);
		displayOverview.addMouseListener(vfL);
		display.repaint();
	}
}
