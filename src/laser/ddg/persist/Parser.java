package laser.ddg.persist;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.LanguageConfigurator;
import laser.ddg.NoSuchDataNodeException;
import laser.ddg.NoSuchNodeException;
import laser.ddg.NoSuchProcNodeException;
import laser.ddg.ProvenanceData;
import laser.ddg.SourcePos;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Reads a textual description of a DDG and constructs a prefuse graph for it.
 * The syntax for a textual DDG is:
 * For most of the terminal symbols below, we are relying on intuition rather 
 * than explicitly showing legal syntax.  We have identified terminal symbols 
 * by using all uppercase, like <HOUR>.  Here are a few points of clarification 
 * where we have been a little sloppy below:
 *   - The names used for nodes can either be simple words or quoted strings to 
 *     allow embedded blanks.
 *   - The DataNodeId and ProcedureNodeId, should really be terminal symbols, 
 *     being the letter d or p followed by a positive integer, with no intervening 
 *     space.
 *     Filenames can be either Unix style or Windows style or a bizarre hybrid.  
 *     It turns out that if you ask R for a filename, it uses Windows prefix if 
 *     run on Windows, like C:, but then / instead of \.
 *   - Attribute values, node values and timestamps can be quoted strings or just 
 *     words.
 * 
 * <DDG> -> <Attributes>*<PinCounter> <Declaration>*
 * <Declaration> ->�� <EdgeDecl> | <NodeDecl>��
 * <EdgeDecl> -> <ControlFlowDecl> | <DataFlowDecl>
 * <ControlFlowDecl> -> <CF_TOKEN> <ProcedureNodeID><ProcedureNodeID>
 * <DataFlowDecl> -> <DF_TOKEN> <DataFlowEdgeDecl>
 * <DataFlowEdgeDecl> -> <DataNodeID><ProcedureNodeID> | <ProcedureNodeID><DataNodeID>
 * <NodeDecl> -> <DataNode> | <ProcedureNode>
 * <ProcedureNode> -> <ProcedureNodeType> <ProcedureNodeID> <NAME> ["Time" "="�� <Timestamp >] <Attributes>*
 * <ProcedureNodeType> -> "Start" | "Finish" | "Interm" | "Leaf" | "Operation" | "SimpleHandler" | "VStart" | "VFinish" | "VInterm" | "Checkpoint" | "Restore"
 * <DataNode> -> <DataNodeType> <DataNodeID> <NAME> ["Value" "="<Value> ]["ValType" "=" <ValType>]["Time" "="�� <Timestamp >]["Location" "=" <FILENAME>]
 * <DataNodeType> -> "Data" | "Exception" | "URL" | "File" | "Snapshot"
 * <Value> -> <URL> | <FILENAME> | <STRING>
 * <Timestamp> -> <YEAR>"-"<MONTH>"-"<DATE>["T"<HOUR>":"<MINUTE>[":"<SECOND>["."<FRACTIONAL>]]]
 * <Attributes> -><NAME>["="]<AttrValue>
 * <AttrValue> -> <STRING> | <INT>
 * <PinCounter> -> <INT>
 * <DataNodeID> -> 'd' <INT>
 * <ProcedureNodeID> -> 'p' <INT>
 * <CF_TOKEN> -> "CF"
 * <DF_TOKEN> -> "DF"
 * 
 * @author Barbara Lerner
 */
public abstract class Parser {
	/** The object that builds the prefuse graph */
	protected PrefuseGraphBuilder builder;
	
	/** The object that builds the internal ddg graph */
	protected DDGBuilder ddgBuilder;
	
	/** The number of step/procedure nodes.  Default to half the integers if it is not set.
	    The purpose is for data and procedure nodes to have different numbers inside Prefuse. */
	protected int numPins = Integer.MAX_VALUE / 2;
	
	/** The name of the script */
	protected String scrpt;
	
	/** The timestamp on the script */
	protected String timestamp;
	
	/** The language of the script */
	protected String language;
	
	/** String of attribute names and values */
	protected Attributes attributes = new Attributes();
	
	private File fileBeingParsed;
	
	/**
	 * Initializes the parser
	 * @param file the file to read the DDG from
	 * @param builder the prefuse object that will build the graph
	 */
	protected Parser(File file, PrefuseGraphBuilder builder)  {
	    this.builder = builder;
		ddgBuilder = null;
		fileBeingParsed = file;
	}

	/**
	 * Creates either a TextParser or a JsonParser depending on the type of the file passed in
	 * @param file the file to parse.  Its name should end in .txt or .json
	 * @param prefuseGraphBuilder the object to build the visual graph
	 * @return the parser that can handle the given file
	 * @throws IOException if there are problems reading the file
	 */
	public static Parser createParser(File file, PrefuseGraphBuilder prefuseGraphBuilder) throws IOException {
		String fileName = file.getName();
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		if (extension.equals("txt")) {
			return new TextParser (file, prefuseGraphBuilder);
		}
		else if (extension.equals("json")) {
			return new JSonParser (file, prefuseGraphBuilder);
		}
		else {
			throw new IllegalArgumentException("No parser available for " + fileName);
		}
	}

	/**
	 * Adds the nodes and edges from the DDG to the graph.
	 * @throws IOException if there is a problem reading the file
	 */
	public void addNodesAndEdges() throws IOException {
		parseHeader();
		
		// If there was no script attribute, use the filename.
		if (scrpt == null) {
			scrpt = fileBeingParsed.getName();
		}
		ProvenanceData provData = new ProvenanceData(scrpt, timestamp, language);
		
		// Store the file path to the selected file in attributes
		provData.setSourceDDGFile(fileBeingParsed.getAbsolutePath());
		provData.setAttributes(attributes);
		
		provData.setQuery("Entire DDG");
		builder.setProvData(provData);
		
		try {
			if (language == null) {
				language = "Little-JIL";
			}
			ddgBuilder = LanguageConfigurator.createDDGBuilder(language, scrpt, provData, null);
			builder.createLegend(language);

			//System.out.println("Using " + ddgBuilder.getClass().getName());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "No DDG Builder for " + language + ".  Cannot add the DDG to the database.\n\n");
			e.printStackTrace(System.err);
		}
		
		parseNodesAndEdges();
		
		//System.out.println("Done parsing");
		
		if (ddgBuilder != null) {
			ddgBuilder.ddgBuilt();
		}
		builder.processFinished();
	}

	/**
	 * Parse all the nodes and edges in the file and add them to the visual graph and the provenance data
	 * @throws IOException if there are problems reading the file
	 */
	protected abstract void parseNodesAndEdges() throws IOException;

	/**
	 * Parses the attributes and their values and the pin counter.
	 * @throws IOException if the header is not formatted properly or there is
	 *   a problem reading from the input stream.
	 */
	protected abstract void parseHeader() throws IOException;

	/**
	 * Add a node to the provenance data and the visual graph
	 * @param nodeType the type of the node, such as "Start", "Operation", "Finish"
	 * @param nodeId the node's unique id
	 * @param name the label that should appear on the node
	 * @param value the node's value; may be null
	 * @param elapsedTime the time recorded for the node
	 * @param script the script number that created the node
	 * @param startLine the first line in the script associated with this node
	 * @param startCol the column number on the first line
	 * @param endLine the last line in the script associated with this node
	 * @param endCol the column on the last line
	 */
	protected void addProcNode (String nodeType, String nodeId, String name, String value, double elapsedTime, String script, String startLine, String startCol, String endLine, String endCol) {
		//System.out.println("Adding proc node " + nodeId);
		SourcePos sourcePos = buildSourcePos(script, startLine, startCol, endLine, endCol);
		builder.addNode(nodeType, extractUID(nodeId), 
					constructName(nodeType, name), value, elapsedTime, null, sourcePos);
		int idNum = Integer.parseInt(nodeId.substring(1));
			
		ddgBuilder.addProceduralNode(nodeType, idNum, name, value, elapsedTime, sourcePos);
	}
	
	/**
	 * Creates a SourcePos object given the information from the ddg.  The ddg may contain NA for some values.
	 * These are translated into appropriate integers.
	 * @param script the script number or NA
	 * @param startLine the starting line number or NA
	 * @param startCol the starting column or NA
	 * @param endLine the ending line number or NA
	 * @param endCol the ending column or NA
	 * @return the same information encapsulated inside a SourcePos object
	 */
	protected static SourcePos buildSourcePos (String script, String startLine, String startCol, String endLine, String endCol) {
		int scriptNum;
		int startLineNum;
		int startColNum;
		int endLineNum;
		int endColNum;
		
		if (script.equals("NA")) {
			scriptNum = -1;
		}
		else {
			scriptNum = Integer.parseInt(script);
		}
		
		if (startLine.equals("NA")) {
			startLineNum = -1;
		}
		else {
			startLineNum = Integer.parseInt(startLine);
		}
		
		if (startCol.equals("NA")) {
			startColNum = 0;
		}
		else {
			startColNum = Integer.parseInt(startCol);
		}
		
		if (endLine.equals("NA")) {
			endLineNum = -1;
		}
		else {
			endLineNum = Integer.parseInt(endLine);
		}
		
		if (endCol.equals("NA")) {
			endColNum = 0;
		}
		else {
			endColNum = Integer.parseInt(endCol);
		}
		return new SourcePos (scriptNum, startLineNum, startColNum, endLineNum, endColNum);
	}

	/**
	 * Add a data node to the provenance data and the visual graph
	 * @param nodeType the type of node, such as "File", "Data", "Exception"
	 * @param nodeId the node's unique id
	 * @param name the label to display
	 * @param value the data value
	 * @param valType the type of the data value
	 * @param timestamp the timestamp for the data
	 * @param location the file location if the data is a file or snapshot
	 */
	protected void addDataNode (String nodeType, String nodeId, String name, String value, String valType, String timestamp, String location) {
		//System.out.println("Adding data node " + nodeId + " with type " + nodeType);
		int idNum = Integer.parseInt(nodeId.substring(1));
		if (ddgBuilder != null) {
			ddgBuilder.addDataNode(nodeType,idNum,name,value,timestamp, location);
		}
		builder.addNode(nodeType, extractUID(nodeId), 
					constructName(nodeType, name), value, timestamp, location, null);
	}
	

	/**
	 * @return the string of all attributes
	 */
	public Attributes getAttributes(){
		return attributes;
	}

	/**
	 * @return the number of Procedural Nodes
	 */
	public int getNumPins(){
		return numPins;
	}

	/**
	 * Constructs the name to use for the node from the tokens
	 * @param tokens the tokens from the declaration
	 * @return the name to use
	 */
	static String constructName(String nodeType, String nodeName) {
		if(nodeName == null){
			DDGExplorer.showErrMsg("Invalid node construct. No name given.");
			return null;
		}
		String str = nodeName;
		
		// Concatenate node name and type for non-leaf nodes to distinguish
		// start, finish, etc. nodes
		if (isMultipleNodePIN(nodeType)){
			return str + " " + nodeType;
		}
		return str;
	}

	/**
	 * Returns true if this type of node corresponds to part of the execution of 
	 * a step.
	 * @param type the type of the node
	 * @return true if this type of node corresponds to part of the execution of 
	 * a step.
	 */
	private static boolean isMultipleNodePIN(String type) {
		// Parts of a non-leaf step
		if (type.equals("Start") || type.equals("Interm") || type.equals("Finish")) {
			return true;
		}

		// Parts of a virtual step
		if (type.equals("VStart") || type.equals("VInterm") || type.equals("VFinish")) {
			return true;
		}
		
		return false;
	}

	/**
	 * Extracts the id from a token, leaving out the 'p' or 'd' tag
	 * @param idToken the id token
	 * @return the numeric value of the id
	 */
	 private int extractUID(String idToken) {
		int uid = Integer.parseInt(idToken.substring(1));
		
		// Prefuse requires each entry to have a unique id, but our data nodes and
		// step nodes both start at 1.  We therefore offset the uid for the data nodes
		// by adding on the number of step nodes.
		if (idToken.charAt(0) == 'd') {
			uid = uid + numPins;
		}
		return uid;
	}
	
	/**
	 * Adds an edge to the visual graph
	 * @param edgeType the type of edge
	 * @param source the node at the tail
	 * @param destination the node at the head
	 */
	private void addEdge(String edgeType, int source, int destination) {
		builder.addEdge(edgeType, destination, source);
	}

	/**
	 * Add a control flow edge to the provenance data and the visual graph
	 * @param predId id of the node that executed first
	 * @param succId id fo the node that executed second
	 */
	protected void addControlFlowEdge(String predId, String succId) {
		//System.out.println("Adding CF edge from " + predId + " to " + succId);
		int pred = Integer.parseInt(predId.substring(1));
		int succ = Integer.parseInt(succId.substring(1));
		ddgBuilder.addPredSuccLink(pred, succ);
		addEdge ("CF", pred, succ);
	}

	/**
	 * Add a data flow edge that goes from a data node to a procedural node
	 * to the provenance data and the visual graph
	 * @param procId the id of the procedural node
	 * @param dataId the id of the data node
	 * @throws NoSuchDataNodeException if there is no data node with that id
	 * @throws NoSuchProcNodeException if there is no procedural node with that id
	 */
	protected void addDataConsumerEdge(String procId, String dataId) throws NoSuchDataNodeException, NoSuchProcNodeException {
		//System.out.println("Adding DF consumer edge from " + dataId + " to " + procId);
		int data = Integer.parseInt(dataId.substring(1));
		int consumer = Integer.parseInt(procId.substring(1));
		try {
			ddgBuilder.addDataConsumer(consumer, data);
			addEdge ("DF", data + numPins, consumer);
		} catch (NoSuchDataNodeException e) {
			String msg = "Can't create edge from data node " + data + " to procedure node " + consumer + "\n";
			msg = msg + "No data node with id " + data;
			DDGExplorer.showErrMsg(msg);
			throw e;
		} catch (NoSuchProcNodeException e) {
			String msg = "Can't create edge from data node " + data + " to procedure node " + consumer + "\n";
			msg = msg + "No procedure node with id " + consumer;
			DDGExplorer.showErrMsg(msg);
			throw e;
		} catch (NoSuchNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Add a data flow edge that goes from a procedure node to a data node
	 * to the provenance data and the visual graph
	 * @param procId the id of the procedural node
	 * @param dataId the id of the data node
	 * @throws NoSuchDataNodeException if there is no data node with that id
	 * @throws NoSuchProcNodeException if there is no procedural node with that id
	 * @throws ReportErrorException if another error has been reported to the user
	 */
	protected void addDataProducerEdge(String procId, String dataId) throws NoSuchDataNodeException, NoSuchProcNodeException, ReportErrorException {
		//System.out.println("Adding DF producer edge from " + procId + " to " + dataId);
		int data = Integer.parseInt(dataId.substring(1));
		int producer = Integer.parseInt(procId.substring(1));
		try {
			ddgBuilder.addDataProducer(data, producer);
			addEdge ("DF", producer, data + numPins);
		} catch (NoSuchDataNodeException e) {
			String msg = "Can't create edge from procedure node " + producer + " to data node " + data + "\n";
			msg = msg + "No data node with id " + data;
			DDGExplorer.showErrMsg(msg);
			throw e;
		} catch (NoSuchProcNodeException e) {
			String msg = "Can't create edge from procedure node " + producer + " to data node " + data + "\n";
			msg = msg + "No procedure node with id " + producer;
			DDGExplorer.showErrMsg(msg);
			throw e;
		} catch (NoSuchNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		} catch (ReportErrorException e) {
			// TODO Auto-generated catch block
			DDGExplorer.showErrMsg(e.getMessage());
			throw e;
		}
	}



}
