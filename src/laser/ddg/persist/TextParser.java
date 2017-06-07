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
import laser.ddg.NoSuchDataNodeException;
import laser.ddg.NoSuchProcNodeException;
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
 * <DataNode> -> <DataNodeType> <DataNodeID> <NAME> ["Value" "="<Value> ]["Time" "="�� <Timestamp >]["Location" "=" <FILENAME>]
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
public class TextParser extends Parser {
	// Special characters
	private static final char QUOTE = '\"';
	
	// Codes used to identify dataflow and control flow edges
	private static final String DATA_FLOW = "DF";
	private static final String CONTROL_FLOW = "CF";
	
	// Attribute names for data nodes
	private static final String VALUE = "Value";
	private static final String VALTYPE = "ValType";
	private static final String TIMESTAMP = "Time";
	private static final String LOCATION = "Location";

	private static final String LINE_NUMBER = "Line";
	private static final String POS = "Pos";

	// Attribute associated with a procedure node to identify the script number.
	private static final Object SCRIPT_NUMBER = "Script";

	// The input stream
	private StreamTokenizer in;
	
	// Edges are saved and processed after all the nodes have been added
	// to the graph.  That way there can be no references to edges that
	// are not yet created.
	private ArrayList<ArrayList<String>> savedEdges = new ArrayList<>();
	
	// Time of the last procedure node encountered
	private double lastProcElapsedTime = 0.0;

	// Used to read from the file being parsed.
	private Reader reader;
	
	/**
	 * Initializes the parser
	 * 
	 * @param file the file to read the DDG from
	 * @param builder the prefuse object that will build the graph
	 * @throws FileNotFoundException if the file to parse cannot be found
	 */
	public TextParser(File file, PrefuseGraphBuilder builder) 
		throws FileNotFoundException {
		super (file, builder);
		reader = new BufferedReader (new FileReader (file));
	    in = new StreamTokenizer(reader);
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

	}

	/**
	 * Adds the nodes and edges from the DDG to the graph.
	 * 
	 * @throws IOException if there is a problem reading the file
	 */
	@Override
	public void parseNodesAndEdges() throws IOException {
		int nextToken = skipBlankLines();
		while (nextToken != StreamTokenizer.TT_EOF) {
			// System.out.println(in.sval);
			parseDeclaration(nextToken);
			nextToken = skipBlankLines();
		}
		addEdges();
		reader.close();
	}

	/**
	 * Parses the attributes and their values and the pin counter.
	 * 
	 * @throws IOException if the header is not formatted properly or there is
	 *   a problem reading from the input stream.
	 */
	@Override
	protected void parseHeader() throws IOException {
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @throws IOException
	 */
	private void saveEdgeDeclaration() throws IOException {
		ArrayList<String> decl = new ArrayList<>();
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
	 * 
	 * @throws IOException 
	 */
	private void parseNode() throws IOException {
		String nodeType = in.sval;
		// System.out.println("Node Type:"+nodeType);
		
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
	 * 
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
		// System.out.println("name = " + name + "  value = " + value);
		// System.out.println("nodeid="+nodeId);
		
		double elapsedTime = 0;
		String startLine = "NA";
		String startCol = "NA";
		String endLine = "NA";
		String endCol = "NA";
		String script = "NA";
		
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
					String time = parseElapsedTime();

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
					int nextToken4 = in.nextToken();
					if (nextToken4 != '=') {
						in.pushBack();
					}

					nextToken4 = in.nextToken();
					if (nextToken4 == QUOTE) {
						startLine = in.sval;
					}
				}

				else if (in.sval.equals(POS)) {
					int nextToken2 = in.nextToken();
					if (nextToken2 != '=') {
						in.pushBack();
					}

					nextToken2 = in.nextToken();
					if (nextToken2 == QUOTE) {
						String[] lineCols = in.sval.split(",");
						if (lineCols.length > 1) {
							startLine = lineCols[0];
							startCol = lineCols[1];
							endLine = lineCols[2];
							endCol = lineCols[3];
						}
					}
				}

				else if (in.sval.equals(SCRIPT_NUMBER)) {
					int nextToken3 = in.nextToken();
					if (nextToken3 != '=') {
						in.pushBack();
					}

					nextToken3 = in.nextToken();
					if (nextToken3 == QUOTE) {
						script = in.sval;
					}
				}
			}
		}
			

		//System.out.println ("Parser:  Storing time in prefuse graph of " + time);
		//System.out.println ("Parser:  Storing time in ddg of " + elapsedTime);
		//System.out.println("Line number = " + lineNum);
		
		addProcNode (nodeType, nodeId, name, value, elapsedTime, script, startLine, startCol, endLine, endCol);
	}

	private String parseElapsedTime() throws IOException {
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
	 * 
	 * @param nodeId the id of the node being parsed
	 * @return the value or null if there is no value 
	 * @throws IOException
	 */
	String parseValue(String nodeId) throws IOException {
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
	 * Parses a {@code ValType = <ValType>} string.
	 * 
	 * @param nodeId The id of the node being parsed.
	 * @return The {@code ValType}, or null, if there is no value.
	 * @throws IOException
	 */
	private String parseValType(String nodeId) throws IOException {
		int nextToken = in.nextToken();
		
		// Case where ValType is missing
		if (nextToken == StreamTokenizer.TT_EOL || nextToken == StreamTokenizer.TT_EOF || nextToken == ';') {
			in.pushBack();
			return null;
		}
		
		// check for ValType token
		if (nextToken == StreamTokenizer.TT_WORD) {
			
			// ValType token found
			if (in.sval.equals(VALTYPE)) {
				nextToken = in.nextToken();
				
				if (nextToken != '=') {
					in.pushBack();
					DDGExplorer.showErrMsg("Line " + in.lineno() + ": Expected =.\n\n");
					consumeRestOfLine();
					return null;
				}
				
				nextToken = in.nextToken();
				
				if(nextToken == StreamTokenizer.TT_WORD || nextToken == QUOTE) {
					return in.sval;
				}
				
				// token not found
				in.pushBack();
				DDGExplorer.showErrMsg("Line " + in.lineno() + ": ValType is missing for node " + nodeId + "\n\n");
				consumeRestOfLine();
				return null;
			}
			
			// token is not ValType
			in.pushBack();
			return null;
		}
		
		// unexpected token
		in.pushBack();
		DDGExplorer.showErrMsg("Line " + in.lineno() + ": ValType is missing for node " + nodeId + "\n\n");
		consumeRestOfLine();
		return null;
	}

	/**
	 * Parses a LOCATION = FILENAME string
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @param nodeType the type of node
	 * @param nodeId the node's id
	 * @throws IOException
	 */
	private void parseDataNode(String nodeType, String nodeId) throws IOException {
		try {
			String name = convertNextTokenToString ();
			String value = null;
			String valType = null;
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
							if(nodeType.equals("File") || nodeType.equals("Snapshot")){
								File relative = new File(builder.getSourceDDGDirectory(), value);
								value = relative.getAbsolutePath();
							}
							somethingMatched = true;
						}
					}
					
					// valType
					if(valType == null) {
						valType = parseValType(nodeId);
						if(valType != null) {
							somethingMatched = true;
						}
					}
					
					// timestamp
					if (timestamp == null) {
						timestamp = parseTimestamp(nodeId);
						if (timestamp != null) {
							somethingMatched = true;
						}
					}
					
					// location
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
			
			addDataNode (nodeType, nodeId, name, value, valType, timestamp, location);

			
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
			if(attributeName.equals(Attributes.LANGUAGE)){
				language = attributeValue;
			}
			else if(attributeName.equals(Attributes.MAIN_SCRIPT_NAME)){
				scrpt = attributeValue;
			}
			else if(attributeName.equals(Attributes.EXECUTION_TIME)){
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
	 * Add all the edges to the graph.
	 */
	private void addEdges() {
    	savedEdges.stream().forEach((nextEdge) -> {
        	parseEdge(nextEdge);
        });
	}

	/**
	 * Parse the tokens that describe an edge and add it to the prefuse graph
	 * 
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

		} catch (NoSuchDataNodeException | NoSuchProcNodeException e) {
			// Nothing to do.  The error message is produced inside parseDataFlowEdge.
		}
                // Nothing to do.  The error message is produced inside parseDataFlowEdge.
        catch (ReportErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
	}

	private void parseControlFlowEdge(ArrayList<String> tokens) {
		addControlFlowEdge(tokens.get(1), tokens.get(2));
	}

	private void parseDataFlowEdge(ArrayList<String> tokens) throws NoSuchDataNodeException, NoSuchProcNodeException, ReportErrorException {
		//source is function node
		if(tokens.get(2).startsWith("p")){
			addDataConsumerEdge(tokens.get(2), tokens.get(1));
		}
		//if target is function node
		else if(tokens.get(1).startsWith("p")){
			addDataProducerEdge (tokens.get(1), tokens.get(2));
		}
		else {
			DDGExplorer.showErrMsg("Neither source nor target of edge is a procedure node:  " + 
					tokens.get(0) + " " + tokens.get(1) + " " + tokens.get(2));
		}
	}


}
