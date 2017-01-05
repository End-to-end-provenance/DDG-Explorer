package laser.ddg.query;

import java.util.Iterator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Asks the user which variable and which value of the variable to
 * show the uses of.  Extracts the partial DDG from the database
 * and displays it.  This will include all of the nodes that use this
 * value as input transitively until output is reached.
 * 
 * @author Barbara Lerner
 * @version August 1, 2013
 *
 */
public class ResultsQuery extends DataQuery {

	/**
	 * The value to display in the query menu
         * @return 
	 */
	@Override
	public String getMenuItem() {
		return "Show Values Computed From";
	}

	@Override
	protected String getFrameTitle() {
		return "Values computed from query";
	}

	/**
	 * Loads nodes that are reachable by following dataflow paths
	 * down from the given resource.
	 */
	@Override
	protected void loadNodes(Resource qResource) {
		showDin(qResource);

		for (int i = 0; i < numDinsToShow(); i++) {
			Resource nextDataResource = getDin(i);
			Iterator<Resource> procResources = getConsumers(nextDataResource);
			while (procResources.hasNext()) {
				Resource nextProcResource = procResources.next();
				showPin(nextProcResource);
				addAllOutputs(nextProcResource);
			}
		}
	}
	
	private void addAllOutputs(Resource procRes) {
		String queryVarName = "out";
		ResultSet outputs = getAllOutputs(procRes, queryVarName);
		while (outputs.hasNext()) {
			QuerySolution outputSolution = outputs.next();
			Resource outputResource = outputSolution.getResource(queryVarName);
			showDin(outputResource);
		}
	}
	
	@Override
	protected String getSingletonMessage(String name, String value) {
		return name + " is an external output.";
	}
	
	@Override
	protected String getQuery(String name, String value) {
		return "Show values computed from " + name;
	}
}
