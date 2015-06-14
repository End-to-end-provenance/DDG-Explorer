package laser.ddg.visualizer;

import prefuse.data.Edge;

/**
 * Filter used when creating sets of edges.
 * 
 * @author Barbara Lerner
 * @version Jun 5, 2013
 *
 */
public interface EdgeFilter {

	/**
	 * An edge will only be included in the set if it passes this filter.
	 * @param nodeNeighborEdge the edge being tested
	 * @return true if it passes the filter test.
	 */
	public boolean passes(Edge nodeNeighborEdge);

}
