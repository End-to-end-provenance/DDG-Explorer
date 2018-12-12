package laser.ddg.query;

import java.util.Iterator;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;

/**
 * Extracts the partial DDG that corresponds to a data node and the
 * procedure and data nodes that led to its value.
 * 
 * @author Barbara Lerner
 * @version December 12, 2018
 *
 */
public class DerivationQuery extends DataQuery {
	
	/**
	 * Loads the nodes that are reachable by following data flow paths
	 * up from the given resource.
	 */
	@Override
	protected void loadNodes(DataInstanceNode qResource) {
		showDin(qResource);

		for (int i = 0; i < numDinsToShow(); i++) {
			DataInstanceNode nextDataResource = getDin(i);
			ProcedureInstanceNode nextProcResource = nextDataResource.getProducer();
			if (nextProcResource != null) {
				// Add the procedure node to the query result
				showPin(nextProcResource);
				
				// Add all the inputs of the procedure node to the query result
				addAllInputs(nextProcResource);
			}
		}
	}

	/**
	 * Add all it the inputs of a procedure node to the query result
	 * @param procRes the procedure node whose inputs are added
	 */
	private void addAllInputs(ProcedureInstanceNode procRes) {
		Iterator<DataInstanceNode> inputs = procRes.inputParamValues();
		while (inputs.hasNext()) {
			showDin (inputs.next());
		}
	}
	
	@Override
	protected String getSingletonMessage(String name, String value) {
		return name + " is an external input.";
	}

	
	@Override
	protected String getQuery(String name, String value) {
		return "Show derivation of " + name;
	}

}
