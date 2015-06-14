package laser.ddg;

/**
 * Info Already Set Exception
 */
public class InfoAlreadySetException extends IllegalStateException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public InfoAlreadySetException(String s) {
		super(s);
	}
}
