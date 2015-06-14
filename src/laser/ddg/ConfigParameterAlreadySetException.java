package laser.ddg;

/**
 * Configuration Parameter Already Set Exception
 */
public class ConfigParameterAlreadySetException extends IllegalStateException {
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
	public ConfigParameterAlreadySetException(String s) {
		super(s);
	}
}
