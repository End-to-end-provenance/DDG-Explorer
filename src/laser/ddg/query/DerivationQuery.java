package laser.ddg.query;

import java.util.Iterator;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;

/**
 * Asks the user which variable and which value of the variable to
 * show the derivation for.  Extracts the partial DDG from the database
 * and displays it.
 * 
 * @author Barbara Lerner
 * @version Aug 2, 2012
 *
 */
public class DerivationQuery extends DataQuery {
	
	/**
	 * The value to display in the query menu
     * @return 
	 */
	@Override
	public String getMenuItem() {
		return "Show Value Derivation";
	}

	@Override
	protected String getFrameTitle() {
		return "Derivation query";
	}
	
	/**
	 * Loads the nodes that are reachable by following data flow paths
	 * up from the given resource.
	 */
	@Override
	protected void loadNodes(DataInstanceNode qResource) {
		System.out.println("DerivationQuery.loadNodes called");
		System.out.println("Adding Data " + qResource.getName());
		showDin(qResource);

		for (int i = 0; i < numDinsToShow(); i++) {
			DataInstanceNode nextDataResource = getDin(i);
			ProcedureInstanceNode nextProcResource = nextDataResource.getProducer();
			if (nextProcResource != null) {
				showPin(nextProcResource);
				System.out.println("Adding Proc " + nextProcResource.getName());
				addAllInputs(nextProcResource);
			}
		}
	}

	
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
