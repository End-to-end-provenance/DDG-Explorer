package laser.ddg.visualizer;

import java.io.IOException;

/**
 * An exception used to indicate an attempt to display the contents of a binary file.
 * 
 * @author Barbara Lerner
 * @version Jun 12, 2018
 *
 */
public class BinaryFileException extends IOException {
	/**
	 * Creates an instance of the exception.
	 */
	public BinaryFileException () {
		super();
	}

	/**
	 * Creates an instance of the exception
	 * @param msg the message to attach to the exception
	 */
	public BinaryFileException (String msg) {
		super(msg);
	}
}
