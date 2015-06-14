package laser.ddg.visualizer;

import prefuse.data.Node;

/**
 * Filter used when creating sets of nodes.
 * 
 * @author Barbara Lerner
 * @version Jun 5, 2013
 *
 */
public interface NodeFilter {

	/**
	 * A node will only be included in the set if it passes this filter.
	 * @param n the node being tested
	 * @return true if it passes the filter test.
	 */
	public boolean passes(Node n);

}
