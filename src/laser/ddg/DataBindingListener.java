package laser.ddg;

/**
 * This interface provides methods that announce how data is passed around
 * during execution. To receive these notifications an object should attach
 * itself as a listener to ProvenanceData object.
 * 
 * @author Barbara Lerner
 * @version Jan 10, 2011
 * 
 */
public interface DataBindingListener {
	/**
	 * Announces that a binding has been created.
	 * 
	 * @param e
	 *            details of the binding that was create.
	 */
	public void bindingCreated(DataBindingEvent e);
}
