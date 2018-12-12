package laser.ddg.commands;

import laser.ddg.AbstractDataInstanceNode;
import laser.ddg.DataInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.query.DerivationQuery;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.PrefuseUtils;
import prefuse.data.Node;

public class CopyCommand  {
	public static void execute(PrefuseGraphBuilder builder, Node rootNode, DerivationQuery query) {
//        DDGExplorer ddgExplorer = DDGExplorer.getInstance();
//        ProvenanceData origProvData = ddgExplorer.getCurrentDDG();
//        
//		final ProvenanceData provData = new ProvenanceData("Query result"); 
//		PrefuseGraphBuilder graphBuilder = new PrefuseGraphBuilder(false, true);
//		graphBuilder.setTitle("Query result", "");
//		
//		provData.addProvenanceListener(graphBuilder);
//		new Thread() {
//			@Override
//			public void run() {
//				// Load the database in a separate thread so that it does not tie
//				// up the Swing thread.  This allows us to see the DDG being 
//				// built incrementally as it is read from the DB.
//				graphBuilder.createLegend(origProvData.getLanguage());
//				DDGExplorer.loadingDDG();
//				graphBuilder.processStarted("Query result", provData); 
//				String nodeName = PrefuseUtils.getName(rootNode);
//				//String nodeId = nodeName.substring(0, nodeName.indexOf('-'));
//				System.out.println("nodeName = " + nodeName);
//				DataInstanceNode din = origProvData.findDin (nodeName);
//				System.out.println("Found " + din.getName());
//				DataInstanceNode rootCopy = (DataInstanceNode) din.clone();
//				provData.addDIN(rootCopy, rootCopy.getId());
//				provData.setRoot(rootCopy);
//				graphBuilder.processFinished();
//				DDGExplorer.doneLoadingDDG();
//				//also used to set Save to DB as disabled. This should be handled somewhere else
//			}
//		}.start();
        DDGExplorer ddgExplorer = DDGExplorer.getInstance();
        ProvenanceData origProvData = ddgExplorer.getCurrentDDG();
        query.setLanguage (origProvData.getLanguage());
        
        // Attach a listener so that the resulting graph is displayed when
        // the query is complete.
        DDGExplorer.loadingDDG();
		query.addQueryListener(ddgExplorer);
		
		// Find the DIN clicked on
		String nodeName = PrefuseUtils.getName(rootNode);
		//String nodeId = nodeName.substring(0, nodeName.indexOf('-'));
		System.out.println("nodeName = " + nodeName);
		DataInstanceNode din = origProvData.findDin (nodeName);

		// Execute the query
		query.doQuery(din, nodeName, "value");
		DDGExplorer.doneLoadingDDG();


	}


}
