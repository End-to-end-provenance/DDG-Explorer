package laser.ddg.query;

import java.util.Iterator;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;

/**
 * Asks the user which variable and which value of the variable to
 * show the uses of.  Extracts the partial DDG from the database
 * and displays it.  This will include all of the nodes that use this
 * value as input transitively until output is reached.
 * 
 * @author Barbara Lerner
 * @version December 12, 2018
 *
 */
public class ResultsQuery extends DataQuery {

	/**
	 * Loads nodes that are reachable by following dataflow paths
	 * down from the given resource.
	 */
	@Override
	protected void loadNodes(DataInstanceNode qResource) {
		// Add the data node to the query result
		showDin(qResource);

		for (int i = 0; i < numDinsToShow(); i++) {
			DataInstanceNode nextDataResource = getDin(i);
			
			// Add all procedure nodes that use the data node and all
			// outputs of those procedure nodes.
			Iterator<ProcedureInstanceNode> procResources = nextDataResource.users();
			while (procResources.hasNext()) {
				ProcedureInstanceNode nextProcResource = procResources.next();
				showPin(nextProcResource);
				addAllOutputs(nextProcResource);
			}
		}
	}
	
	/**
	 * Add all the outputs of a procedure node to the query result
	 * @param procRes the procedure node whose outputs are added
	 */
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
