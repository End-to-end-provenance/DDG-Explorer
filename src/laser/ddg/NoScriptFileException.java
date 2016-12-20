package laser.ddg;

/**
 * Thrown if we try to access a script file but either there is no script file
 * information associated with the node or the file is unavailable.
 * 
 * @author Barbara Lerner
 * @version Aug 16, 2016
 *
 */
public class NoScriptFileException extends IllegalArgumentException {

	public NoScriptFileException(String message) {
		super(message);
	}

}
