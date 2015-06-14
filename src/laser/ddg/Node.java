package laser.ddg;

import java.util.Iterator;
import java.util.Set;

/**
 * 
 * The common requirements for nodes that stand for procedures and that stand
 * for data are that they record the time when they are created and that they
 * are assigned an ID number
 * 
 * @author Sophia
 * 
 */
public interface Node {

	/**
	 * 
	 * @return time when the procedure instance node is created
	 */
	public String getCreatedTime();

	/**
	 * @return the ID assigned to the node
	 */
	int getId();

	/**
	 * @return all DIN outputs derived from the given node
	 */
	public Set<DataInstanceNode> getProcessOutputsDerived();

	/**
	 * @return raw data input for the given node
	 */
	public Set<DataInstanceNode> getProcessInputsDerived();

	/**
	 * Returns the value associated with an attribute
	 * 
	 * @param name
	 *            the name of the attribute
	 * @return the stored value. Returns null if there is no attribute with that
	 *         name.
	 */
	public Object getAttributeValue(String name);

	/**
	 * Sets the value of the attribute. If the node already has an attribute
	 * with the given name, its value is changed. If it does not have an
	 * attribute with the given name, a new attribute is added to the node.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            its value
	 */
	public void setAttribute(String name, Object value);

	/**
	 * @return an iterator over all the attribute names.
	 */
	public Iterator<String> attributes();
}
