package laser.ddg.query;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

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
	
	@Override
	protected void doQuery (Resource qResource) {
		showDin(qResource);

		for (int i = 0; i < numDinsToShow(); i++) {
			Resource nextDataResource = getDin(i);
			Resource nextProcResource = getProducer(nextDataResource);
			if (nextProcResource != null) {
				showPin(nextProcResource);
				addAllInputs(nextProcResource);
			}
		}
		displayDDG();
	}
	
	private void addAllInputs(Resource procRes) {
		String queryVarName = "in";
		ResultSet inputs = getAllInputs(procRes, queryVarName);
		while (inputs.hasNext()) {
			QuerySolution inputSolution = inputs.next();
			Resource inputResource = inputSolution.getResource(queryVarName);
			showDin(inputResource);
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
