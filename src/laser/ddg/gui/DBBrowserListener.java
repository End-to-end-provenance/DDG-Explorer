package laser.ddg.gui;

/**
 * A listener for the browser that allows the user to select scripts and DDG timestamps to
 * work with.
 * 
 * @author Barbara Lerner
 * @version Oct 21, 2013
 *
 */
public interface DBBrowserListener {
	/**
	 * Called when the user selects a script in the script list.
	 * @param script the selected script
	 */
	public void scriptSelected(String script);
	
	/**
	 * Called when the user selects a timestamp in the timestamp list.
	 * @param timestamp the timestamp selected
	 */
	public void timestampSelected(String timestamp);
}
