package laser.ddg.commands;

import java.util.SortedSet;

import com.hp.hpl.jena.rdf.model.Resource;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.JenaLoader;
import laser.ddg.persist.JenaWriter;
import laser.ddg.query.DataQuery;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.PrefuseUtils;
import laser.ddg.visualizer.WorkflowGraphBuilder;
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
	public static void execute(PrefuseGraphBuilder builder, Node node, DataQuery query) {
		String processPath = builder.getProcessName();
		String timestamp = builder.getTimestamp();
		String language = builder.getLanguage();
		
		// For now, we are using the existing code from Jena to perform the query.
		// We need to save the ddg in the database if it is not already there.
		DBWriter dbWriter = builder.getDBWriter();
		if (!((JenaWriter) dbWriter).alreadyInDB(processPath,
			timestamp, language)) {
			//System.out.println("Saving to DB");
			SaveToDBCommand.execute();
		}

        DDGExplorer ddgExplorer = DDGExplorer.getInstance();
        
        // Attach a listener so that the resulting graph is displayed when
        // the query is complete.
        DDGExplorer.loadingDDG();
		query.addQueryListener(ddgExplorer);

        JenaLoader dbLoader = JenaLoader.getInstance();
        String processName = processPath.substring(processPath.lastIndexOf('/') + 1);
		String nodeName = PrefuseUtils.getName(node);
		String nodeValue = PrefuseUtils.getValue(node);
		//System.out.println("Search for node " + nodeName);
		//System.out.println("All node names: " + dbLoader.getAllDinNames(processName, timestamp));
		SortedSet<Resource> dins = dbLoader.getDinsNamed(processName, timestamp, nodeName);
		assert dins.size() == 1;
		Resource selectedResource = dins.first();
		query.initQuery(dbLoader, processName, timestamp);
		
		// Execute the query
		query.doQuery(selectedResource, nodeName, nodeValue);
		DDGExplorer.doneLoadingDDG();
	}

}
