package laser.ddg.visualizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import prefuse.Display;
import prefuse.util.display.PaintListener;

/**
 * Draws viewFinder's borders onto the overview after paint is called
 * 
 * @author Nicole
 */
public class ViewFinderBorders implements PaintListener {
	private DisplayWithOverview dispPlusOver;

	public ViewFinderBorders(DisplayWithOverview dispPlusOver) {
		super();
		this.dispPlusOver = dispPlusOver;
	}

	@Override
	public void prePaint(Display overview, Graphics2D g) {
		//System.out.println("overview prepaint");
		((DDGDisplay)overview).zoomToExactFit();
	}

	@Override
	/**
	 * after both ddg displays have been drawn, create a rectangle in the
	 * overview that represents the regular display's view.
	 */
	public void postPaint(Display overview, Graphics2D g) {
		//System.out.println("Drawing viewfinder");
		
		// retrieve rectangle for viewFinder
		Rectangle rect = dispPlusOver.calcViewFinder();

		// draw the rectangle
		int x = rect.x;
		int y = rect.y;
		int width = rect.width;
		int height = rect.height;
		g.setColor(Color.LIGHT_GRAY);
		g.drawRoundRect(x, y, width, height, 10, 10);
		g.setColor(new Color(150, 150, 200, 50));
		g.fillRoundRect(x, y, width, height, 10, 10);
	}

}