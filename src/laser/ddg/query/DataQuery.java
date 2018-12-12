package laser.ddg.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import laser.ddg.DDGBuilder;
import laser.ddg.DataInstanceNode;
import laser.ddg.LanguageConfigurator;
import laser.ddg.Node;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.SourcePos;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Abstract class that provides the framework to do queries on the lineage of
 * data nodes.  The query results will include both procedure and data nodes, 
 * but only data edges.  There will be no control flow edges.
 * 
 * @author Barbara Lerner
 * @version December 12, 2018
 *
 */
public abstract class DataQuery extends AbstractQuery {
	// Process being queried
	private String processName;
	
	// List of procedure resources that should be part of the query result
	private List<ProcedureInstanceNode> allPinsToShow = new ArrayList<>();
	
	// List of data resources that should be part of the query result
	//private List<Resource> allDinsToShow = new ArrayList<Resource>();
	private List<DataInstanceNode> allDinsToShow = new ArrayList<>();
	
	// Object to build the visible graph
	private PrefuseGraphBuilder graphBuilder;
	
	// Object to build the internal ddg
	private DDGBuilder builder;

	/**
	 * Load the nodes that are returned by the query and display the resulting DDG.
	 * @param qResource The data node whose derivation information
	 * 		is being loaded
	 */
	public void doQuery(DataInstanceNode qResource) {
		loadNodes(qResource);
		displayDDG(qResource);
	}

	/**
	 * Loads the nodes that correspond to the given query beginning at 
	 * the resource passed in.
	 * @param dNode the node at which the query should start
	 */
	protected abstract void loadNodes(DataInstanceNode dNode);

	/**
	 * Display the result of the query.
	 * @param rootNode the node at which the query started
	 */
	private void displayDDG(final DataInstanceNode rootNode) {
		// Create the new DDG
		final ProvenanceData provData = new ProvenanceData(processName);
		
		// Create the new graph
		graphBuilder = new PrefuseGraphBuilder(false, true);
		String queryString = getQuery(rootNode.getName(), "");
		graphBuilder.setTitle(queryString, "");
		
		provData.addProvenanceListener(graphBuilder);
		provData.setQuery(queryString);
		
		new Thread() {
			@Override
			public void run() {
				// Load the query result in a separate thread so that it does not tie
				// up the Swing thread.  
				DataQuery.this.builder = LanguageConfigurator.createDDGBuilder(language, "Query result", provData);
				graphBuilder.createLegend(language);
				DDGExplorer.loadingDDG();
				loadQueryResult(provData, rootNode);
				DDGExplorer.doneLoadingDDG();
			}
		}.start();
	}

	/**
	 * Load the query result the new ddg
	 * @param pd the new ddg
	 * @param rootNode the root of the ddg
	 */
	protected void loadQueryResult(ProvenanceData pd, DataInstanceNode rootNode) {
		// Sort the procedure nodes by id
    	Collections.sort(allPinsToShow, (ProcedureInstanceNode res0, ProcedureInstanceNode res1) -> res0.getId() - res1.getId());
    	
    	// Load each procedure node, its inputs and its outputs
		for (ProcedureInstanceNode res : allPinsToShow) {
			ProcedureInstanceNode pin = addProcResourceToProvenance(res, pd);
			loadInputs(pd, rootNode, res, pin);
			loadOutputs(pd, res, pin);
		}
		
		// If graph contains no procedure nodes, display a message.  It might contain
		// one or more data nodes.
		if (allPinsToShow.isEmpty()) {
			DDGExplorer.showErrMsg(getSingletonMessage("name", "value"));
		}
		
		// Cause the drawing to occur
		pd.notifyProcessFinished();
		notifyQueryFinished(graphBuilder.getPanel().getName(), graphBuilder.getPanel());
	}
	
	/**
	 * Make a copy of a procedure node and add it to the provenance
	 * @param res the node to copy
	 * @param provData the ddg to add it to
	 * @return the new node, or null if no node was created
	 */
	private ProcedureInstanceNode addProcResourceToProvenance(ProcedureInstanceNode res, ProvenanceData provData) {
		if (nodesToResContains(res, provData)) {
			return null;
		}
		
		ProcedureInstanceNode pin = createProcedureInstanceNode (res);
		provData.addPIN(res, pin);
		return pin;
	}

	/**
	 * Returns true if the procedure instance node has already been copied
	 * @param r the node to check
	 * @param provData the ddg to check
	 * @return true if the procedure instance node has already been copied
	 */
	private static boolean nodesToResContains(ProcedureInstanceNode r, ProvenanceData provData) {
		return provData.containsResource(r);
	}
	
	/**
	 * Returns true if the data instance node has already been copied
	 * @param r the node to check
	 * @param provData the ddg to check
	 * @return true if the data instance node has already been copied
	 */
	private static boolean nodesToResContains(DataInstanceNode r, ProvenanceData provData) {
		return provData.containsResource(r);
	}
	
	/**
	 * Copy a procedure node and add it to the ddg being built
	 * @param res the node to copy
	 * @return the new node
	 */
	private ProcedureInstanceNode createProcedureInstanceNode (ProcedureInstanceNode res) {
		// Get the fields to copy
		int id = res.getId();
		String type = res.getType();
		String name = res.getName();
		String procDef;
		Object value = res.getProcedureDefinition();
		if (value == null) {
			procDef = null;
		}
		else {
			procDef = value.toString();
		}
		double elapsedTime = res.getElapsedTime();
		SourcePos sourcePos = res.getSourcePos();

		// Make the copy and add it to the ddg.
		return builder.addProceduralNode(type, id, name, procDef, elapsedTime, sourcePos);
	}
	
	/**
	 * Load all of the outputs of a procedure node that we want to include from the database
	 * @param pd the ddg
	 * @param oldNode the procedure node whose outputs are examined
	 * @param newNode the copy of oldNode to update
	 */
	private void loadOutputs(ProvenanceData pd, ProcedureInstanceNode oldNode, ProcedureInstanceNode newNode) {
		Iterator<DataInstanceNode> outputs = oldNode.outputParamValues();
		while (outputs.hasNext()) {
			DataInstanceNode nextData = outputs.next();
			
			// Check if the node is part of the query result
			if (allDinsToShow.contains(nextData)) {
				
				// Check if the node has already been copied
				if (!nodesToResContains(nextData, pd)) {
					
					// Add the new output node and link it to the new procedure node
					DataInstanceNode din = addDataResourceToProvenance(nextData, pd);
					newNode.addOutput(din.getName(), din);
				}
			}
		}
	}
	
	/**
	 * Make a copy of a data node and add it to the provenance
	 * @param dataResource the node to copy
	 * @param provData the ddg to add it to
	 * @return the new node
	 */
	private DataInstanceNode addDataResourceToProvenance(DataInstanceNode dataResource, ProvenanceData provData) {
		DataInstanceNode din = createDataInstanceNode (dataResource);
		provData.addDIN(dataResource, din);
		return din;
	}

	/**
	 * Copy a data node and add it to the ddg being built
	 * @param dataResource the node to copy
	 * @return the new node
	 */
	private DataInstanceNode createDataInstanceNode(DataInstanceNode dataResource) {
		// Get the fields to copy
		String name = dataResource.getName();
		int dinId= dataResource.getId();
		String type = dataResource.getType();
		String currentVal = dataResource.getValue().toString();
		String timestamp = dataResource.getCreatedTime();
		String location = dataResource.getLocation();
		
		// Make the copy and add it to the ddg
		return builder.addDataNode(type, dinId, name, currentVal, timestamp, location);
	}

	/**
	 * Load all of the inputs of a procedure node that are part of the query result
	 * @param pd the ddg
	 * @param rootNode the root of the ddg, if the root is a data node.  This will be
	 * 	null if the root is a procedure node
	 * @param origNode the procedure node whose outputs are examined
	 * @param newNode the copy of origNode in the query result
	 */
	private void loadInputs(ProvenanceData pd, DataInstanceNode rootNode,
			ProcedureInstanceNode origNode, ProcedureInstanceNode newNode) {
		Iterator<DataInstanceNode> inputs = origNode.inputParamValues();
		while (inputs.hasNext()) {
			DataInstanceNode nextInput = inputs.next();
			
			// Check if the input is part of the query result
			if (allDinsToShow.contains(nextInput)) {
				
				// Get the new version of the input and hookup the edges
				// to the new procedure node.
				DataInstanceNode din = loadDin(pd, rootNode, nextInput);
				newNode.addInput(din.getName(), din);
				din.addUserPIN(newNode);
			}
		}
	}

	/**
	 * Gets a new node that corresponds to an existing data node but loaded
	 * into a new ddg.  
	 * @param pd the ddg to add the new node to.
	 * @param rootNode the node at which the query started
	 * @param inputNode the data node to be copied
	 * @return If the node already exists in the new ddg, that node is returned.
	 *   If it does not exist, a new node is created and returned.
	 */
	private DataInstanceNode loadDin(ProvenanceData pd, Node rootNode,
			DataInstanceNode inputNode) {
		// We may have already loaded it as it might be an output previously loaded, or
		// it might be an input to more than one procedure node.
		DataInstanceNode din = pd.getResource(inputNode);
		if (din != null) {
			return din;
		}
		
		// If it is not yet loaded, load it.
		din = addDataResourceToProvenance(inputNode, pd);
			
		// If this is a copy of the starting node of the query, make it
		// the root of the new ddg.
		if (inputNode.equals(rootNode)) {
			pd.setRoot(din);
		}
		return din;
	}

	/**
	 * Get a description of the query performed
	 * @param name the name of the data item
	 * @param value the value of the data item
	 * @return a description of the query
	 */
	protected abstract String getQuery(String name, String value);
	
	/**
	 * Get a message to display if there is just one node in the query result
	 * @param name the name of the data item
	 * @param value the value of the data item
	 * @return the message to display
	 */
	protected abstract String getSingletonMessage(String name, String value);
	
	/**
	 * Adds the resource to the list of data nodes included in the query result if
	 * it is not already there.  Does nothing if it is in the list already.
	 * @param res the resource to add
	 */
	protected void showDin(DataInstanceNode res) {
		if (!allDinsToShow.contains(res)) {
			allDinsToShow.add(res);			
		}
	}
	
	/**
	 * @return the number of dins that are in the result
	 */
	protected int numDinsToShow() {
		return allDinsToShow.size();
	}
	
	/**
	 * Get the data resource at a position 
	 * @param index the position
	 * @return the data resource
	 */
	protected DataInstanceNode getDin(int index) {
		return allDinsToShow.get(index);
	}
	
	/**
	 * Adds the resource to the list of procedure nodes in the query result if
	 * it is not there already.  Does nothing if it is already in the list.
	 * @param nextProcResource the resource
	 */
	protected void showPin(ProcedureInstanceNode nextProcResource) {
		if (!allPinsToShow.contains(nextProcResource)) {
			allPinsToShow.add(nextProcResource);
		}
	}

}
