package laser.ddg;

/**
 * Indicates that an expected agenda item could not be found.
 * 
 * @author Barbara Lerner
 * @version Mar 14, 2011
 * 
 */
public class NoSuchAgendaItemException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public NoSuchAgendaItemException(String s) {
		super(s);
	}
}
