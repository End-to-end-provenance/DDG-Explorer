package laser.ddg;

/**
 * * No Such Configuration Parameter Exception
 * 
 * @author B. Lerner & S. Taskova
 */
public class NoSuchConfigParameterException extends IllegalArgumentException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public NoSuchConfigParameterException(String s) {
		super(s);
	}
}
