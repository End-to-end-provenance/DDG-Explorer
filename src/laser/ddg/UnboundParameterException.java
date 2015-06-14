package laser.ddg;

/**
 * Unbound Parameter Exception
 * 
 * @author B. Lerner & S. Taskova
 */
public class UnboundParameterException extends IllegalArgumentException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public UnboundParameterException(String s) {
		super(s);
	}
}
