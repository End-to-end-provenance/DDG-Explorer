package laser.ddg.query;

import java.util.Iterator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;

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
	protected void loadNodes(DataInstanceNode qResource) {
		showDin(qResource);

		for (int i = 0; i < numDinsToShow(); i++) {
			DataInstanceNode nextDataResource = getDin(i);
			Iterator<ProcedureInstanceNode> procResources = nextDataResource.users();
			while (procResources.hasNext()) {
				ProcedureInstanceNode nextProcResource = procResources.next();
				showPin(nextProcResource);
				addAllOutputs(nextProcResource);
			}
		}
	}
	
	private void addAllOutputs(ProcedureInstanceNode procRes) {
		Iterator<DataInstanceNode> outputs = procRes.outputParamValues();
		while (outputs.hasNext()) {
			DataInstanceNode nextOutput = outputs.next();
			showDin(nextOutput);
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
