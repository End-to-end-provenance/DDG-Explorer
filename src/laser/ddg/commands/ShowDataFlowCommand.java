package laser.ddg.commands;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.query.DataQuery;
import laser.ddg.visualizer.PrefuseUtils;
import prefuse.data.Node;

/**
 * Executes a data flow query and displays the result.
 * 
 * @author Barbara Lerner
 * @version December 12, 2018
 *
 */
public class ShowDataFlowCommand {
	/**
	 * Executes the query to display a dataflow path to a particular node
	 * @param rootNode the node that the user wants to see the derivation of
	 * @param query the query to execute
	 */
	public static void execute(Node rootNode, DataQuery query) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ProvenanceData origProvData = ddgExplorer.getCurrentDDG();
		query.setLanguage(origProvData.getLanguage());

		// Attach a listener so that the resulting graph is displayed when
		// the query is complete.
		DDGExplorer.loadingDDG();
		query.addQueryListener(ddgExplorer);

		// Find the DIN clicked on
		String nodeName = PrefuseUtils.getName(rootNode);
		//System.out.println("nodeName = " + nodeName);
		DataInstanceNode din = origProvData.findDin(nodeName);

		// Execute the query
		query.doQuery(din);
		DDGExplorer.doneLoadingDDG();

	}

}
