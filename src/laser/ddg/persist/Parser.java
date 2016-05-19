package laser.ddg.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.LanguageConfigurator;
import laser.ddg.NoSuchDataNodeException;
import laser.ddg.NoSuchNodeException;
import laser.ddg.NoSuchProcNodeException;
import laser.ddg.ProvenanceData;
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
 * <ProcedureNode> -> <ProcedureNodeType> <ProcedureNodeID> <NAME>
 * <ProcedureNodeType> -> "Start" | "Finish" | "Interm" | "Leaf" | "Operation" | "SimpleHandler" | "VStart" | "VFinish" | "VInterm" | "Checkpoint" | "Restore"
 * <DataNode> -> <DataNodeType> <DataNodeID> <NAME> ["Value" "="<Value> ]["Time" "="�� <Timestamp >]["Location" "=" <FILENAME>]
 * <DataNodeType> -> "Data" | "Exception" | "URL" | "File" | "Snapshot"
 * <Value> -> <URL> | <FILENAME> | <STRING>
 * <Timestamp> -> <YEAR>"-"<MONTH>"-"<DATE>["T"<HOUR>":"<MINUTE>[":"<SECOND>["."<FRACTIONAL>]]]
 * <Attributes> -><NAME>["="]<AttrValue>
 * <AttrValue> -> <STRING>
 * <PinCounter> -> <INT>
 * <DataNodeID> -> 'd' <INT>
 * <ProcedureNodeID> -> 'p' <INT>
 * <CF_TOKEN> -> "CF"
 * <DF_TOKEN> -> "DF"
 * 
 * @author Barbara Lerner
 */
public class Parser {
	// Special characters
	private static final char QUOTE = '\"';
	
	// Attributes describing the entire DDG
	private static final String EXECUTION_TIME = "DateTime";
	private static final String SCRIPT = "Script";
	private static final String LANGUAGE = "Language";
	
	// Codes used to identify dataflow and control flow edges
	private static final String DATA_FLOW = "DF";
	private static final String CONTROL_FLOW = "CF";
	
	// Attribute names for data nodes
	private static final String VALUE = "Value";
	private static final String TIMESTAMP = "Time";
	private static final String LOCATION = "Location";

	private static final String LINE_NUMBER = "scriptLine";

	// The input stream
	private StreamTokenizer in;
	
	// The object that builds the prefuse graph
	private PrefuseGraphBuilder builder;
	
	//The object that builds the internal ddg graph
	private DDGBuilder ddgBuilder;
	
	// The number of step/procedure nodes.
	private int numPins;
	
	//The name of the script
	private String scrpt;
	
	//The timestamp on the script
	private String timestamp;
	
	//The language of the script
	private String language;
	
	//String of attribute names and values
	private Attributes attributes = new Attributes();
	
	// Edges are saved and processed after all the nodes have been added
	// to the graph.  That way there can be no references to edges that
	// are not yet created.
	private ArrayList<ArrayList<String>> savedEdges = new ArrayList<ArrayList<String>>();
	
	private File fileBeingParsed;
	
	// Time of the last procedure node encountered
	private double lastProcElapsedTime = 0.0;
	
	/**
	 * Initializes the parser
	 * @param file the file to read the DDG from
	 * @param builder the prefuse object that will build the graph
	 * @throws FileNotFoundException if the file to parse cannot be found
	 */
	public Parser(File file, PrefuseGraphBuilder builder) 
		throws FileNotFoundException {
		Reader r = new BufferedReader (new FileReader (file));
	    in = new StreamTokenizer(r);
	    in.eolIsSignificant(true);
	    in.resetSyntax();

	    // Only ; and = are special characters
	    in.wordChars('a', 'z');
	    in.wordChars('A', 'Z');
	    in.wordChars('0', '9');
	    in.wordChars('!', '/');
	    in.wordChars(':', ':');
	    in.wordChars('<', '<');
	    in.wordChars('>', '@');
	    in.wordChars('[', '`');
	    in.wordChars('{', '~');

	    in.quoteChar('\"');
	    in.whitespaceChars(0, ' ');

	    this.builder = builder;
		ddgBuilder = null;
		fileBeingParsed = file;
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
		
		provData.createFunctionTable();
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
			e.printStackTrace();
		}
		
		int nextToken = skipBlankLines();
		while (nextToken != StreamTokenizer.TT_EOF) {
			// System.out.println(in.sval);
			parseDeclaration(nextToken);
			nextToken = skipBlankLines();
		}
		addEdges();
		
		if (ddgBuilder != null) {
			ddgBuilder.ddgBuilt();
		}
		builder.processFinished();
	}

	/**
	 * Parses the attributes and their values and the pin counter.
	 * @throws IOException if the header is not formatted properly or there is
	 *   a problem reading from the input stream.
	 */
	private void parseHeader() throws IOException {
		// Skip over blank lines
		int nextToken = skipBlankLines();
		if (nextToken == StreamTokenizer.TT_EOF) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "The file is empty.\n\n");
			throw new IOException("The file is empty.");
		}
		
		// Loop until we find the number of procedure nodes.
		while(true){
			//System.out.println(nextToken);
			try {
				// System.out.println(in.sval);
				numPins = Integer.parseInt(in.sval);
				assert in.nextToken() == StreamTokenizer.TT_EOL;
				// System.out.println("Number of pins = " + numPins);
				break;
			}
			catch (NumberFormatException e) {
				// Haven't found the number of pins yet.  
				// This should be an attribute-value pair
				if (nextToken == StreamTokenizer.TT_WORD) {
					parseAttribute();
					nextToken = skipBlankLines();
					if (nextToken == StreamTokenizer.TT_EOF) {
						DDGExplorer.showErrMsg("Number of pins is missing from the file.\n\n");
						throw new IOException("Number of pins is missing from the file.");
					}
				}
				
				else {
					DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected attribute name or pin counter.\n\n");
					throw new IOException("Expected attribute name or pin counter.");
				}
			}
		}
	}

	/**
	 * Skip over blank lines
	 * @return the first non-EOL, non-EOF token found
	 * @throws IOException if can't read from the stream
	 */
	private int skipBlankLines() throws IOException {
		int nextToken = in.nextToken();
		while (nextToken == StreamTokenizer.TT_EOF || nextToken == StreamTokenizer.TT_EOL) {
			if (nextToken == StreamTokenizer.TT_EOF) {
				return nextToken;
			}
			nextToken = in.nextToken();
		}
		return nextToken;
	}

	/**
	 * Handles the next line of input.  If it is a node, it is parsed.
	 * If it is an edge, it saves it to parse later so that we are sure 
	 * the endpoints of the edge exist before the edge is parsed.
	 * @param nextLine the next line from the input.
	 * @throws IOException 
	 */
	private void parseDeclaration(int nextToken) throws IOException {
		if (nextToken == StreamTokenizer.TT_WORD) {
			if (in.sval.equals(CONTROL_FLOW) || in.sval.equals(DATA_FLOW)) {
				saveEdgeDeclaration();
			}
			else {
				parseNode();
			}
			
			nextToken = in.nextToken();
			if (nextToken == ';') {
				skipBlankLines();
				in.pushBack();
			}
			else if (nextToken != StreamTokenizer.TT_EOF && nextToken != StreamTokenizer.TT_EOL) {
				DDGExplorer.showErrMsg("Line " + in.lineno() + ": Unexpected tokens at end of line. Token:" + nextToken + "\n\n");
				
				// Consume the rest of the line.
				consumeRestOfLine();
			}
		}
		else {
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Unexpected first token.\n\n");
			consumeRestOfLine();
		}
	}

	/**
	 * Read tokens until reach the end of the line.  Then read the subsequent blank
	 * lines.  On return, the next token to process will be on the input stream.
	 * @throws IOException
	 */
	private void consumeRestOfLine() throws IOException {
		int nextToken = in.nextToken();
		while (nextToken != StreamTokenizer.TT_EOF && nextToken != StreamTokenizer.TT_EOL) {
			nextToken = in.nextToken();
		}
		nextToken = skipBlankLines();
		in.pushBack();
	}

	/**
	 * Adds all the tokens on this line to a list so they can be processed later.
	 * @throws IOException
	 */
	private void saveEdgeDeclaration() throws IOException {
		ArrayList<String> decl = new ArrayList<String>();
		decl.add(in.sval);
		
		try {
			while (true) {
				decl.add(convertNextTokenToString());
			}
		} catch (IllegalStateException e) {
			// Thrown when we reach the end of the line.
		}
		savedEdges.add(decl);
	}
	
	/**
	 * Adds a node to the graph
	 * @throws IOException 
	 */
	private void parseNode() throws IOException {
		String nodeType = in.sval;
		
		if (in.nextToken() != StreamTokenizer.TT_WORD) {
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected data or procedure node identifier:  " + nodeType + "\n\n");
			in.pushBack();
			consumeRestOfLine();
			return;
		}
		
		String nodeId = in.sval;
		// System.out.println("Parsing " + nodeId);
		if(nodeId.startsWith("p") && ddgBuilder != null){
			parseProcNode(nodeType, nodeId);
		}
		//add data nodes
		else if(nodeId.startsWith("d") && ddgBuilder != null){
			parseDataNode(nodeType, nodeId);
		}
		
	}

	/**
	 * Parses a procedure node declaration
	 * @param nodeType The type of node
	 * @param nodeId the node's id
	 * @throws IOException
	 */
	private void parseProcNode(String nodeType, String nodeId) throws IOException {
		String name = null;
		try {
			name = convertNextTokenToString ();
		} catch (IllegalStateException e) {
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Name is missing for node " + nodeId + "\n\n");
			in.pushBack();
			consumeRestOfLine();
			return;
		}
		
		String value = null;

		value = parseValue(nodeId);
		//System.out.println("name = " + name + "  value = " + value);
		
		double elapsedTime = 0;
		int lineNum = -1;
		
		// The remaining attributes are optional
		while (true) {
			int nextToken = in.nextToken();
		
			// Line number is optional.  This is the case where it is missing.
			if (nextToken == StreamTokenizer.TT_EOL || nextToken == StreamTokenizer.TT_EOF || nextToken == ';') {
				in.pushBack();
				break;
			}
			
			if (nextToken == StreamTokenizer.TT_WORD ) {
				if (in.sval.equals(TIMESTAMP)) {
					// get the timeStamp
					String time = parseElapsedTime(nodeId);

					if (time == null) {
						elapsedTime = 0;
					}
					// We later calculate the time for start/finish nodes to be the sum of the times of the internal
					// operations.
					else if (!nodeType.equals("Operation")) {
						elapsedTime = 0;
						time = null;
					}
					else {
						try {
							double elapsedTimeFromStart = Double.parseDouble(time);
							elapsedTime = elapsedTimeFromStart - lastProcElapsedTime;
							lastProcElapsedTime = elapsedTimeFromStart;
						} catch (NumberFormatException e) {
							// Old style file, probably storing a timestamp instead so just ignore
							time = null;
							elapsedTime = 0;
						}
					}
				}
			
				else if (in.sval.equals(LINE_NUMBER)) {
					lineNum = parseLineNumber();
				}
			}
		}
			
		System.out.println("Line number = " + lineNum);
		builder.addNode(nodeType, extractUID(nodeId), 
					constructName(nodeType, name), value, elapsedTime, null, lineNum);
		int idNum = Integer.parseInt(nodeId.substring(1));
			
		ddgBuilder.addProceduralNode(nodeType, idNum, name, value, elapsedTime, lineNum);
	}

	/** 
	 * Parse the line number attribute
	 * @return the line number value of the attribute, or -1 if there is no line number attribute 
	 * 
	 **/
	private int parseLineNumber() throws IOException {
		int nextToken = in.nextToken();
		if (nextToken != '=') {
			in.pushBack();
			return -1;
		}

		nextToken = in.nextToken();
		if (nextToken == StreamTokenizer.TT_NUMBER) {
			return (int) in.nval;
		}

		return -1;
	}

	private String parseElapsedTime(String nodeId) throws IOException {
		int nextToken = in.nextToken();
		if (nextToken != '=') {
			in.pushBack();
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected = after TIMESTAMP.\n\n");
			return null;
		}

		nextToken = in.nextToken();
		if (nextToken == QUOTE || nextToken == StreamTokenizer.TT_WORD) {
			return in.sval;
		}

		//DDGExplorer.showErrMsg("Line " + in.lineno() + ": Timestamp is missing for node " + nodeId + "\n\n");
		return null;
	}


	/**
	 * Parses a VALUE = <value> string
	 * @param nodeId the id of the node being parsed
	 * @return the value or null if there is no value 
	 * @throws IOException
	 */
	private String parseValue(String nodeId) throws IOException {
		int nextToken = in.nextToken();
		
		// Value is optional.  This is the case where it is missing.
		if (nextToken == StreamTokenizer.TT_EOL || nextToken == StreamTokenizer.TT_EOF) {
			in.pushBack();
			return null;
		}
		
		// If value is present, expect to see VALUE = "value" or VALUE = <value>
		if (nextToken == StreamTokenizer.TT_WORD ) {
			if (in.sval.equals(VALUE)) {
				nextToken = in.nextToken();
				if (nextToken != '=') {
					in.pushBack();
					DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected =.\n\n");
					consumeRestOfLine();
					return null;
				}
				
				nextToken = in.nextToken();
				if (nextToken == StreamTokenizer.TT_WORD || nextToken == QUOTE) {
					return in.sval;
				}
				
				in.pushBack();
				DDGExplorer.showErrMsg("Line " + in.lineno() + ": Value is missing for node " + nodeId + "\n\n");
				consumeRestOfLine();
				return null;
			}

			// Might be some other attribute, like timestamp instead.
			in.pushBack();
			return null;
		}

		in.pushBack();
		DDGExplorer.showErrMsg("Line " + in.lineno() + " Node " + nodeId + " unexpected token.\n\n");
		consumeRestOfLine();
		return null;
	}

	/**
	 * Parses a LOCATION = FILENAME string
	 * @param nodeId the id of the node being parsed
	 * @return the filename or null if there is no location
	 * @throws IOException
	 */
	private String parseLocation(String nodeId) throws IOException {
		int nextToken = in.nextToken();
		
		// Location is optional.  This is the case where it is missing.
		if (nextToken == StreamTokenizer.TT_EOL || nextToken == StreamTokenizer.TT_EOF || nextToken == ';') {
			in.pushBack();
			return null;
		}
		
		// If location is present, expect to see LOCATION = "filename"
		if (nextToken == StreamTokenizer.TT_WORD ) {
			if (in.sval.equals(LOCATION)) {
				nextToken = in.nextToken();
				if (nextToken != '=') {
					in.pushBack();
					DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected =.\n\n");
					consumeRestOfLine();
					return null;
				}
				
				nextToken = in.nextToken();
				if (nextToken == QUOTE) {
					return in.sval;
				}
				
				in.pushBack();
				DDGExplorer.showErrMsg("Line " + in.lineno() + ": Location is missing for node " + nodeId + "\n\n");
				consumeRestOfLine();
				return null;
			}

			// Might be some other attribute, like timestamp instead.
			in.pushBack();
			return null;
		}

		in.pushBack();
		DDGExplorer.showErrMsg("Line " + in.lineno() + " Node " + nodeId + " unexpected token.\n\n");
		consumeRestOfLine();
		return null;
	}

	/**
	 * Reads in the next token and converts it to a string no matter what type of token it is.
	 * @return the token as a string.
	 * @throws IOException
	 * @throws IllegalStateException if the next token is EOL or EOF
	 */
	private String convertNextTokenToString () throws IOException, IllegalStateException {
		int nextToken = in.nextToken();
		if (nextToken == StreamTokenizer.TT_WORD) {
			return in.sval;
		}
		else if (nextToken == QUOTE) {
			//return "\"" + in.sval + "\"";
			return in.sval;
		}
		else if (nextToken != StreamTokenizer.TT_EOF && nextToken != StreamTokenizer.TT_EOL) {
			return "" + (char) nextToken;
		}
		else {
			in.pushBack();
			throw new IllegalStateException("No more tokens");
		}
	}

	/**
	 * Parses the timestamp attribute.  Expect to see TIMESTAMP = <timestamp>
	 * @param nodeId the node whose timestamp is being parsed
	 * @return the timestamp value or null if there is no timestamp
	 * @throws IOException
	 */
	private String parseTimestamp(String nodeId) throws IOException {
		int nextToken = in.nextToken();
		
		// Timestamp is optional.  This is the case where it is missing.
		if (nextToken == StreamTokenizer.TT_EOL || nextToken == StreamTokenizer.TT_EOF || nextToken == ';') {
			in.pushBack();
			return null;
		}
		
		// If timestamp is present, expect to see TIMESTAMP = "timestamp"
		if (nextToken == StreamTokenizer.TT_WORD ) {
			if (in.sval.equals(TIMESTAMP)) {
				nextToken = in.nextToken();
				if (nextToken != '=') {
					in.pushBack();
					DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected = after TIMESTAMP.\n\n");
					consumeRestOfLine();
					return null;
				}
				
				nextToken = in.nextToken();
				if (nextToken == QUOTE || nextToken == StreamTokenizer.TT_WORD) {
					return in.sval;
				}
				
				DDGExplorer.showErrMsg("Line " + in.lineno() + ": Timestamp is missing for node " + nodeId + "\n\n");
				consumeRestOfLine();
				return null;
			}
			
			// No error.  It might be some other attribute.
			in.pushBack();
			return null;
		}
		in.pushBack();
		DDGExplorer.showErrMsg("Line " + in.lineno() + " Node " + nodeId + " unexpected token.\n\n");
		consumeRestOfLine();
		return null;
	}

	/**
	 * Parses a data node declaration
	 * @param nodeType the type of node
	 * @param nodeId the node's id
	 * @throws IOException
	 */
	private void parseDataNode(String nodeType, String nodeId) throws IOException {
		int idNum = Integer.parseInt(nodeId.substring(1));
		
		try {
			String name = convertNextTokenToString ();
			String value = null;
			String timestamp = null;
			String location = null;
			
			int nextToken = in.nextToken();
			if (nextToken == StreamTokenizer.TT_EOF || nextToken == StreamTokenizer.TT_EOL) {
				// No value or timestamp.  They are optional
				in.pushBack();
			}
			
			else if (nextToken == StreamTokenizer.TT_WORD) {
				while (nextToken == StreamTokenizer.TT_WORD) {
					//System.out.println("parseDataNode: Found " + in.sval);
					in.pushBack();
					boolean somethingMatched = false;
					
					// See if value is next.
					if (value == null) {
						value = parseValue(nodeId);
						if (value != null) {
							somethingMatched = true;
						}
					}
					
					if (timestamp == null) {
						timestamp = parseTimestamp(nodeId);
						if (timestamp != null) {
							somethingMatched = true;
						}
					}
						
					if (location == null) {
						location = parseLocation(nodeId);
						if (location != null) {
							somethingMatched = true;
						}
					}

					if (somethingMatched) {
						nextToken = in.nextToken();
					}
					else {
						// Neither value nor timestamp nor location
						DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expecting VALUE or TIMESTAMP or LOCATION for node " + nodeId + "\n\n");
						consumeRestOfLine();
						break;
					}
				}
				
			}
			
			else if (nextToken != ';') {
				DDGExplorer.showErrMsg("Line " + in.lineno() + ": Unexpected tokens for node " + nodeId + "\n\n");
				consumeRestOfLine();
			}
			
			//System.out.println("name = " + name + "  value = " + value + "  timestamp = " + timestamp + "\n\n");

			if (ddgBuilder != null) {
				ddgBuilder.addDataNode(nodeType,idNum,name,value,timestamp, location);
			}
			builder.addNode(nodeType, extractUID(nodeId), 
					constructName(nodeType, name), value, timestamp, location, -1);

			
		} catch (IllegalStateException e) {
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Name missing for node " + nodeId + "\n\n");
		}

	}
	
	/**
	 * Creates a string of all attributes and their given values.
	 * 
	 * @param tokens the input tokens that make up the attribute declaration
	 * @throws IOException 
	 */
	private void parseAttribute() throws IOException{
		String attributeName = in.sval;
		
		int nextToken = in.nextToken();
		if (nextToken != '=') {
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected = for attribute " + attributeName + "\n\n");
			consumeRestOfLine();
			return;
		}
		
		try {
			String attributeValue = convertNextTokenToString();
			//System.out.println("Found attribute, " + attributeName + " value: " + attributeValue);
			if(attributeName.equals(LANGUAGE)){
				language = attributeValue;
			}
			else if(attributeName.equals(SCRIPT)){
				scrpt = attributeValue;
			}
			else if(attributeName.equals(EXECUTION_TIME)){
				// R puts : in the timestamp value, but we can't use that in a directory name on Windows.
				attributeValue = attributeValue.replaceAll(":", ".");
				timestamp = attributeValue;
			}
			attributes.set(attributeName, attributeValue);
			
		} catch (IllegalStateException e) {
			DDGExplorer.showErrMsg("Line " + in.lineno() + ": Attribute value missing for " + attributeName + "\n\n");
		}
	}

	/**
	 * @return the string of all attributes
	 */
	public Attributes getAttributes(){
		return attributes;
	}
	

	/**
	 * Constructs the name to use for the node from the tokens
	 * @param tokens the tokens from the declaration
	 * @return the name to use
	 */
	private String constructName(String nodeType, String nodeName) {
		if(nodeName == null){
			DDGExplorer.showErrMsg("Invalid node construct. No name given.");
			return null;
		}
		StringBuilder str = new StringBuilder();
		str.append(nodeName);
		
		// Concatenate node name and type for non-leaf nodes to distinguish
		// start, finish, etc. nodes
		if (isMultipleNodePIN(nodeType)){
			str.append(" " + nodeType);
		}
		return str.toString();
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
	 * Add all the edges to the graph.
	 */
	private void addEdges() {
		for (ArrayList<String> nextEdge : savedEdges) {
			parseEdge(nextEdge);
		}
	}

	/**
	 * Parse the tokens that describe an edge and add it to the prefuse graph
	 * @param tokens the tokens that describe the edge
	 */
	private void parseEdge(ArrayList<String> tokens) {
		String edgeType = tokens.get(0);
		if(edgeType == null){
			DDGExplorer.showErrMsg("Invalid edge construct. Nothing to add.\n");
			return;
		}
		
		if(tokens.size() < 3){
			DDGExplorer.showErrMsg("Invalid edge construct. Need valid name, source and target.\n");
			return;
		}
		
		try {
			//System.out.println("Found edge " + tokens.get(1) + " to " + tokens.get(2));
			if(edgeType.equals("CF") && ddgBuilder != null){
				parseControlFlowEdge(tokens);
			}
			
			else if(edgeType.equals("DF") && ddgBuilder != null){
				parseDataFlowEdge(tokens);
			}

			builder.addEdge(edgeType, extractUID(tokens.get(2)),extractUID(tokens.get(1)));
		} catch (NoSuchDataNodeException e) {
			// Nothing to do.  The error message is produced inside parseDataFlowEdge.
		} catch (NoSuchProcNodeException e) {
			// Nothing to do.  The error message is produced inside parseDataFlowEdge.
		} catch (ReportErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseControlFlowEdge(ArrayList<String> tokens) {
		int pred = Integer.parseInt(tokens.get(1).substring(1));
		int succ = Integer.parseInt(tokens.get(2).substring(1));
		// System.out.println("Found CF edge from " + pred + " to " + succ);
		ddgBuilder.addPredSuccLink(pred, succ);
	}

	private void parseDataFlowEdge(ArrayList<String> tokens) throws NoSuchDataNodeException, NoSuchProcNodeException, ReportErrorException {
		//source is function node
		if(tokens.get(2).startsWith("p")){
			int data = Integer.parseInt(tokens.get(1).substring(1));
			int consumer = Integer.parseInt(tokens.get(2).substring(1));
			// System.out.println("Found input edge from " + data + " to " + consumer);
			try {
				ddgBuilder.addDataConsumer(consumer, data);
			} catch (NoSuchDataNodeException e) {
				String msg = "Can't create edge from data node " + data + " to procedure node " + consumer + "\n";
				msg = msg + "No data node with id " + data;
				DDGExplorer.showErrMsg(msg);
				displayTokens(tokens);
				throw e;
			} catch (NoSuchProcNodeException e) {
				String msg = "Can't create edge from data node " + data + " to procedure node " + consumer + "\n";
				msg = msg + "No procedure node with id " + consumer;
				DDGExplorer.showErrMsg(msg);
				displayTokens(tokens);
				throw e;
			} catch (NoSuchNodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//if target is function node
		else if(tokens.get(1).startsWith("p")){
			int data = Integer.parseInt(tokens.get(2).substring(1));
			int producer = Integer.parseInt(tokens.get(1).substring(1));
			// System.out.println("Found output edge from " + producer + " to " + data);
			try {
				ddgBuilder.addDataProducer(data, producer);
			} catch (NoSuchDataNodeException e) {
				String msg = "Can't create edge from procedure node " + producer + " to data node " + data + "\n";
				msg = msg + "No data node with id " + data;
				DDGExplorer.showErrMsg(msg);
				displayTokens(tokens);
				throw e;
			} catch (NoSuchProcNodeException e) {
				String msg = "Can't create edge from procedure node " + producer + " to data node " + data + "\n";
				msg = msg + "No procedure node with id " + producer;
				DDGExplorer.showErrMsg(msg);
				displayTokens(tokens);	
				throw e;
			} catch (NoSuchNodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReportErrorException e) {
				// TODO Auto-generated catch block
				DDGExplorer.showErrMsg(e.getMessage());
				throw e;
			}
		}
		else {
			DDGExplorer.showErrMsg("Neither source nor target of edge is a procedure node:  " + 
					tokens.get(0) + " " + tokens.get(1) + " " + tokens.get(2));
		}
	}

	private static void displayTokens(ArrayList<String> tokens) {
		String s = "";
		for (int i = 0; i < tokens.size(); i++) {
			s = s + tokens.get(i) + " ";
		}
		DDGExplorer.showErrMsg(s + "\n\n");
	}

}
