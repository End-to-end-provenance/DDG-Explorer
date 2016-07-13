package laser.ddg;

import java.util.ArrayList;

import laser.ddg.gui.LegendEntry;
import laser.ddg.persist.JenaWriter;
import laser.ddg.persist.ReportErrorException;

/**
 * The interface that language-specific DDG builders must satisfy
 * 
 * @author Barbara Lerner
 * @version Jul 3, 2013
 *
 */
public abstract class DDGBuilder {
	// The provenance object that holds the ddg
	private ProvenanceData provObject;

	/**
	 * Creates a ddg builder 
	 * @param script the program that the ddg is for
	 * @param provData the provenance object that holds the ddg
	 * @param dbWriter the object to use to write to the DB.  If null, 
	 *    the ddg will not be written to the DB as it is built
	 */
	public DDGBuilder(String script, ProvenanceData provData, JenaWriter dbWriter) {
		provObject = provData;
		
		//write to database
		if (dbWriter != null) {
			provObject.addProvenanceListener(dbWriter);
		}
		provObject.notifyProcessStarted(script);
	}
	
	/**
	 * Determines what kind of procedure node to create and adds it
	 * 
	 * @param type the type of procedure node, can be leaf, start or finish
	 * @param id the id number of the node
	 * @param name the name of the node
	 * @param elapsedTime 
         * @param lineNum 
         * @param scriptNum
	 * @return the new procedure instance node
	 */
	public abstract ProcedureInstanceNode addProceduralNode(String type, int id, String name, double elapsedTime, int lineNum, int scriptNum);

	/**
	 * Determines what kind of procedure node to create and adds it
	 * 
	 * @param type the type of procedure node, can be leaf, start or finish
	 * @param id the id number of the node
	 * @param name the name of the node
	 * @param value the definition of the procedure
	 * @param elapsedTime 
         * @param lineNum 
         * @param scriptNum
	 * @return the new procedure instance node
	 */
	public ProcedureInstanceNode addProceduralNode(String type, int id, String name, String value, double elapsedTime, int lineNum, int scriptNum) {
		ProcedureInstanceNode pin = addProceduralNode(type, id, name, elapsedTime, lineNum, scriptNum);
		pin.setProcedureDefinition(value);
		return pin;
	}

	/**
	 * Links two ProcedureInstanceNodes with predecessor / successor
	 * 
	 * @param predId
	 *            the id of the predecessor node
	 * @param succId
	 *            the id of the successor node
	 */
	public void addPredSuccLink(int predId, int succId) {
		ProvenanceData provObject = getProvObject();
		ProcedureInstanceNode predNode = provObject.findPin(predId);
		ProcedureInstanceNode succNode = provObject.findPin(succId);
		succNode.addPredecessor(predNode);
		predNode.addSuccessor(succNode);
	}
	
	/**
	 * Creates a new DataInstanceNode that will be added to the provenance graph
	 * 
	 * @param type type of data node to add
	 * @param id id number assigned to the data node
	 * @param name name of the data node
	 * @param value optional value associated with the data node
	 * @param time the optional timestamp of the data node
	 * @param location if this is a file node, it identifies the full path to the original
	 * 		location of the file.  It will be null if this is not a file node.
	 * @return the new data instance node
	 */
	public abstract DataInstanceNode addDataNode(String type, int id, String name,
			String value, String time, String location);

	/**
	 * Will connect the corresponding producer of a data node to the data node
	 * 
	 * @param data id of the data node to be linked 
	 * @param producer id of the function node to be linked
	 * @throws NoSuchNodeException if either there is no data node with the data id given
	 *    or a procedure node with the producer id given.
	 * @throws ReportErrorException to provide an error message to display to the user
	 */
	public void addDataProducer(int data, int producer) throws NoSuchNodeException, ReportErrorException {
		DataInstanceNode dataNode = provObject.findDin(data);
		if (dataNode == null) {
			throw new NoSuchDataNodeException(data);
		}
		
		ProcedureInstanceNode prodNode = provObject.findPin(producer);
		if (prodNode == null) {
			if (dataNode.getType().equals("Exception")) {
				throw new ReportErrorException(dataNode.getValue().toString());
			}
			throw new NoSuchProcNodeException(producer);
		}

		prodNode.addOutput(dataNode.getName(), dataNode);
		dataNode.setProducer(prodNode, dataNode);
	}

	/**
	 * Links data consumer function to the data
	 * 
	 * @param consumer id of the function node to be connect as a consumer of the data node
	 * @param data id number of the relevant data node 
	 * @throws NoSuchNodeException if either there is no data node with the data id given
	 *    or a procedure node with the consumer id given.
	 */
	public void addDataConsumer(int consumer, int data) throws NoSuchNodeException {
		DataInstanceNode dataNode = provObject.findDin(data);
		if (dataNode == null) {
			throw new NoSuchDataNodeException(data);
		}

		ProcedureInstanceNode consumeNode = provObject.findPin(consumer);
		if (consumeNode == null) {
			throw new NoSuchProcNodeException(consumer);
		}

		// Connect the data node to the pin it is an input for
		consumeNode.addInput(dataNode.getName(), dataNode);
		dataNode.addUserPIN(consumeNode);
	}
	
	/**
	 * Notifies the provenance data object when the process has finished
	 */
	public void ddgBuilt(){
		//System.out.println(getProvObject().toString());
		getProvObject().notifyProcessFinished();
	}
	
	/**
	 * 
	 * @return the ddg being built
	 */
	public ProvenanceData getProvObject() {
		return provObject;
	}

	/**
	 * Creates a table mapping node descriptions to colors to be displayed
	 * in a legend.  Different languages may use different subsets of the 
	 * node types, or may want to describe them differently, so it is up
	 * to the subclasses to implement this.
	 * @return the table mapping a label for a color with the color to 
	 * be displayed in a legend.
	 */
	public static ArrayList<LegendEntry> createNodeLegend() {
		return null;
	}

	/**
	 * Creates a table mapping edge descriptions to colors to be displayed
	 * in a legend.  Different languages may use different subsets of the 
	 * edge types, or may want to describe them differently, so it is up
	 * to the subclasses to implement this.
	 * @return the table mapping a label for a color with the color to 
	 * be displayed in a legend.
	 */
	public static ArrayList<LegendEntry> createEdgeLegend() {
		return null;
	}

	/**
	 * Return a string used to determine how to display the ddg's attributes.
	 * This version just finds all attributes and adds them to the text in 
	 * the order that they come out of the hash table.
	 * 
	 * If a particular language wants to customize this, they should define a 
	 * method with this signature in the language specific ddg builder.  In 
	 * this code, you might control the order the attributes come out, which
	 * ones are displayed, the attribute names displayed, or how the values are 
	 * displayed, for example.  
	 * @param attributes all the attributes attached to the ddg
	 * @return the string to display to the user describing the attribute values.
	 */
	public static String getAttributeString(Attributes attributes) {
		StringBuilder attrText = new StringBuilder();
		for (String attrName : attributes.names()) {
			attrText.append(attrName + " = " + attributes.get(attrName) + "\n");
		}
		return attrText.toString();
	}
}