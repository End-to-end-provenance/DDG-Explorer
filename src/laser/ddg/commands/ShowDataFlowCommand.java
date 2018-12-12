package laser.ddg.commands;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.query.DataQuery;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.PrefuseUtils;
import prefuse.data.Node;

/**
 * Executes a data flow query and displays the result.
 * 
 * @author Barbara Lerner
 * @version Jan 5, 2017
 *
 */
public class ShowDataFlowCommand {
	/**
	 * Executes the query to display a dataflow path to a particular node
	 * @param builder the Prefuse graph builder that holds the data for the ddg
	 * @param node the node that the user wants to see the derivation of
	 * @param query the query that Jena uses
	 */
//	public static void execute(PrefuseGraphBuilder builder, Node node, DataQuery query) {
//        DDGExplorer ddgExplorer = DDGExplorer.getInstance();
//        
//        // Attach a listener so that the resulting graph is displayed when
//        // the query is complete.
//        DDGExplorer.loadingDDG();
//		query.addQueryListener(ddgExplorer);
//
//		// Execute the query
//		query.doQuery(selectedResource, nodeName, nodeValue);
//		DDGExplorer.doneLoadingDDG();
//	}

	public static void execute(PrefuseGraphBuilder builder, Node rootNode, DataQuery query) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ProvenanceData origProvData = ddgExplorer.getCurrentDDG();
		query.setLanguage(origProvData.getLanguage());

		// Attach a listener so that the resulting graph is displayed when
		// the query is complete.
		DDGExplorer.loadingDDG();
		query.addQueryListener(ddgExplorer);

		// Find the DIN clicked on
		String nodeName = PrefuseUtils.getName(rootNode);
		// String nodeId = nodeName.substring(0, nodeName.indexOf('-'));
		System.out.println("nodeName = " + nodeName);
		DataInstanceNode din = origProvData.findDin(nodeName);

		// Execute the query
		query.doQuery(din, nodeName, "value");
		DDGExplorer.doneLoadingDDG();

	}

}
