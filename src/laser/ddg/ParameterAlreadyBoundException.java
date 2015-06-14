package laser.ddg;

/**
 * Parameter Already Bound exception
 * 
 * @author B. Lerner & S. Taskova
 * @version 6/3/2010
 */

public class ParameterAlreadyBoundException extends IllegalStateException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public ParameterAlreadyBoundException(String s) {
		super(s);
	}
}
