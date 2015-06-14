package laser.ddg;

/**
 * Root Already Set exception thrown in ProvenanceData class
 * 
 * @author Barbara Lerner & S.Taskova
 * @version 6/3/2010
 */

public class RootAlreadySetException extends IllegalStateException {

	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public RootAlreadySetException(String s) {
		super(s);
	}
}
