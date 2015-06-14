package laser.ddg;

/**
 * Thrown in AgentConfiguration class
 * 
 * @author B. Lerner & S. Taskova
 */
public class NoSuchInformationException extends IllegalStateException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public NoSuchInformationException(String s) {
		super(s);
	}
}
