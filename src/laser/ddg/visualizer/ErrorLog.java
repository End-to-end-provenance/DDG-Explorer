package laser.ddg.visualizer;

import javax.swing.JTextArea;

/**
 * Singleton error log object
 * 
 * @author Barbara Lerner
 * @version Jul 25, 2013
 *
 */
public class ErrorLog extends JTextArea {
	private static ErrorLog instance;

	private ErrorLog() {
		setEditable(false);
	}

	public static ErrorLog getInstance() {
		if (instance == null) {
			instance = new ErrorLog();
		}
		return instance;
	}
	
	/**
	 * Adds a message to the error log
	 * @param msg the message to add
	 */
	public static void showErrMsg(String msg){
		getInstance().append(msg);
	}
	
}
