package laser.ddg.query;

import javax.swing.JComponent;

/**
 * Listener for when a query has finished producing results so they
 * can be displayed.
 */
public interface QueryListener {
	/**
	 * Called when a query has completed loading
	 * @param name to display as the title of the query result
	 * @param panel component containing the visualization of the query result
	 */
	public void queryFinished(String name, JComponent panel);
}
