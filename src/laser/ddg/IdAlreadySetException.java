package laser.ddg;

/**
 * Exception thrown when an attempt is made to reset the ID of a DIN or a PIN
 * that has already been assigned an ID
 * 
 * @author Sophia
 * 
 */
public class IdAlreadySetException extends IllegalStateException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 * 
	 */
	public IdAlreadySetException(String s) {
		super(s);
	}
}
