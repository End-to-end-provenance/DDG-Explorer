package laser.ddg.diff;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import laser.ddg.diff.ToolbarCompare.ZoomListener;
import laser.ddg.visualizer.DDGDisplay;
import prefuse.Display;
import prefuse.util.display.PaintListener;

/**
 * Toolbar to allow for zooming a graph. Zooming is unified for both the left
 * and right ddgs.
 */
class ToolbarCompare extends JToolBar implements ActionListener {
	private DDGDisplay ddgDisplayLeft;
	private DDGDisplay ddgDisplayRight;
	private JComponent[] tools;
	private JSlider zoomSetting;
	private static final int SLIDER_SETTING = 10;
	
	public ToolbarCompare () {
		super("DDG Tools", SwingConstants.HORIZONTAL);
		populateTools();
		addTools();
		this.addPropertyChangeListener(createListener());
	}

	public ToolbarCompare(DDGDisplay ddgDisplay) {
		super("DDG Tools", SwingConstants.HORIZONTAL);
		this.ddgDisplayLeft = ddgDisplay;
		populateTools();
		addTools();

		ddgDisplay.addPaintListener(new ZoomListener(zoomSetting));
		this.addPropertyChangeListener(createListener());
	}

	public ToolbarCompare(DDGDisplay ddgDisplayLeft, DDGDisplay ddgDisplayRight) {
		super("DDG Tools", SwingConstants.HORIZONTAL);
		this.ddgDisplayLeft = ddgDisplayLeft;
		this.ddgDisplayRight = ddgDisplayRight;
		populateTools();
		addTools();
		ZoomListener listener = new ZoomListener(zoomSetting);
		ddgDisplayLeft.addPaintListener(listener);
		ddgDisplayRight.addPaintListener(listener);
		this.addPropertyChangeListener(createListener());
	}

	private void populateTools() {

		zoomSetting = new JSlider(JSlider.HORIZONTAL, 1, 50, SLIDER_SETTING);
		zoomSetting.setPreferredSize(new Dimension(10, 20));
		zoomSetting.setMinorTickSpacing(10);
		zoomSetting.setPaintTicks(true);
		zoomSetting.setEnabled(false);
		zoomSetting.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double zoomedScaleLeft = zoomSetting.getValue();

				zoomedScaleLeft = zoomedScaleLeft / 10;
				double currentScaleLeft = ddgDisplayLeft.getScale();

				if (currentScaleLeft != zoomedScaleLeft) {
					double scaleFactorLeft = (zoomedScaleLeft / currentScaleLeft);
					Rectangle frameBoundsLeft = ddgDisplayLeft.getBounds();
					int xMiddleLeft = (int) (frameBoundsLeft.getWidth() - frameBoundsLeft.getX()) / 2;
					int yMiddleLeft = (int) (frameBoundsLeft.getHeight() - frameBoundsLeft.getY()) / 2;
					Point2D centerScreenLeft = new Point(xMiddleLeft, yMiddleLeft);
					// call zoom!
					ddgDisplayLeft.zoom(centerScreenLeft, scaleFactorLeft);
					ddgDisplayLeft.repaint();
				}

				double zoomedScaleRight = zoomSetting.getValue();
				zoomedScaleRight = zoomedScaleRight / 10;
				double currentScaleRight = currentScaleLeft;

				if (currentScaleRight != zoomedScaleRight) {
					double scaleFactorRight = (zoomedScaleRight / currentScaleRight);
					Rectangle frameBoundsRight = ddgDisplayRight.getBounds();
					int xMiddleRight = (int) (frameBoundsRight.getWidth() - frameBoundsRight.getX()) / 2;
					int yMiddleRight = (int) (frameBoundsRight.getHeight() - frameBoundsRight.getY()) / 2;
					Point2D centerScreenRight = new Point(xMiddleRight, yMiddleRight);
					ddgDisplayRight.zoom(centerScreenRight, scaleFactorRight);
					ddgDisplayRight.repaint();
				}
			}
		});

		JButton refocuser = new JButton("Zoom");
		refocuser.setOpaque(false);
		refocuser.setEnabled(false);
		tools = new JComponent[2];
		tools[0] = zoomSetting;
		tools[1] = refocuser;
	}

	/**
	 * adds components from array to the JToolBar
	 */
	private void addTools() {
		for (int i = 0; i < tools.length; i++) {
			JComponent current = tools[i];
			if (current != null) {
				this.add(current);
			}
		}
	}

	/**
	 * creates a PropertyChangeListener that will listen for a change in the
	 * toolbar's orientation and set the slider to that direction
	 * 
	 * @return propertyChangeListener
	 */
	private PropertyChangeListener createListener() {
		PropertyChangeListener propListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String propName = e.getPropertyName();
				if ("orientation".equals(propName)) {
					if ((Integer) e.getNewValue() == JToolBar.VERTICAL) {
						zoomSetting.setOrientation(JSlider.VERTICAL);
					} else {
						zoomSetting.setOrientation(JSlider.HORIZONTAL);
					}
				}
			}
		};
		return propListener;
	}

	public static class ZoomListener implements PaintListener {
		private JSlider slider;

		public ZoomListener(JSlider slider) {
			super();
			this.slider = slider;
		}

		@Override
		public void prePaint(Display displayGiven, Graphics2D g) {
			// TODO Auto-generated method stub
		}

		@Override
		public void postPaint(Display displayGiven, Graphics2D g) {
			int zoom = (int) Math.round((displayGiven.getScale() * 10));
			// System.out.println("zoomListener resetting slider to value " +
			// zoom);
			if (slider.getValue() != zoom) {
				slider.setValue(zoom);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}

	public void setDisplays(DDGDisplay leftDisplay, DDGDisplay rightDisplay) {
		ddgDisplayLeft = leftDisplay;
		ddgDisplayRight = rightDisplay;
		zoomSetting.setEnabled(true);
	}
}