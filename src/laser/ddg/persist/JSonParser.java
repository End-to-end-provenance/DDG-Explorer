package laser.ddg.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import laser.ddg.Attributes;
import laser.ddg.NoSuchDataNodeException;
import laser.ddg.NoSuchProcNodeException;
import laser.ddg.ScriptInfo;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Parser for ddgs written in Json syntax.
 * 
 * @author Barbara Lerner
 * @version Oct 20, 2016
 *
 */
public class JSonParser extends Parser {
	
	/** The prefix that is appended to the name of each node*/
	private static String PREFIX = "rdt:" ;
	
	private JsonElement jsonRoot;
	private BufferedReader reader;

	/**
	 * Create a Json parser
	 * 
	 * @param file the json file to parser
	 * @param builder the object that builds the visual graph
	 * @throws IOException
	 */
	public JSonParser(File file, PrefuseGraphBuilder builder) throws IOException {
		super (file, builder);

    	reader = new BufferedReader (new FileReader(file));
    	
    	// Read the entire file in to a string
    	StringBuffer s = new StringBuffer();
    	String line = reader.readLine();
    	while (line != null) {
    		s.append(line + "\n");
    		line = reader.readLine();
    	}
    	String json = s.toString();
    	
    	// Parse the json
        JsonParser parser = new JsonParser();
        jsonRoot = parser.parse(json);
	}
	
	/**
	 * Parses the attributes and their values and the pin counter.
	 * 
	 * @throws IOException if the header is not formatted properly or there is
	 *   a problem reading from the input stream.
	 */
	@Override
	protected void parseHeader() throws IOException 
	{
        JsonObject wholeThing = jsonRoot.getAsJsonObject();
        JsonObject entity = wholeThing.getAsJsonObject("entity");
		JsonObject environment = entity.getAsJsonObject(PREFIX+"environment");
		
		Set<Entry<String, JsonElement> > attributeSet = environment.entrySet();
		//System.out.println( attributeSet ) ;
		
		String scriptDir = null ;
		Iterator<JsonElement> sourcedScripts = null ;
		Iterator<JsonElement> ssTimestamps = null ;
		String provDir = "";
		
		// parsing the environment node
		for (Entry <String, JsonElement> attribute : attributeSet) 
		{
			String attributeName = attribute.getKey();
			JsonElement attributeValue = attribute.getValue();

			if(attributeName.equals(Attributes.JSON_LANGUAGE))
			{
				language = attributeValue.getAsString();
				attributes.set(Attributes.LANGUAGE, language);
			}
			else if(attributeName.equals(Attributes.JSON_MAIN_SCRIPT_NAME))
			{
				scrpt = attributeValue.getAsString();
				attributes.set(Attributes.MAIN_SCRIPT_NAME, scrpt);
			}
			
			else if(attributeName.equals(Attributes.JSON_EXECUTION_TIME))
			{
				// R puts : in the timestamp value, but we can't use that in a directory name on Windows.
				timestamp = attributeValue.getAsString().replaceAll(":", ".");
				attributes.set(Attributes.MAIN_SCRIPT_TIMESTAMP, timestamp);
			}
			// sourced scripts, if any. 
			else if( attributeName.equals(Attributes.JSON_SOURCED_SCRIPTS) )
			{
				String scriptFile = attributes.get(Attributes.MAIN_SCRIPT_NAME);
				scriptDir = scriptFile.substring(0, scriptFile.lastIndexOf('/') + 1);
				
				if( attributeValue.isJsonArray() )
					sourcedScripts = attributeValue.getAsJsonArray().iterator() ;
			}
			// sourced script timestamps, if any.
			else if( attributeName.equals(Attributes.JSON_SOURCED_SCRIPT_TIMESTAMPS) && attributeValue.isJsonArray() )
			{
				ssTimestamps = attributeValue.getAsJsonArray().iterator() ;
			}
			else if( attributeName.equals(Attributes.JSON_PROV_DIRECTORY))
			{
				provDir = attributeValue.getAsString();;
				attributes.set(Attributes.PROV_DIRECTORY, provDir);
			}
			else 
			{
				try 
				{
					attributes.set(attributeName, attributeValue.getAsString());
				} 
				catch (IllegalStateException | UnsupportedOperationException e) 
				{
					// Ignore any other attributes that are not simple strings
				}

			}
		}
		
		ArrayList<ScriptInfo> sourcedScriptInfo = new ArrayList<ScriptInfo>() ;
		
		// Add the main script
		sourcedScriptInfo.add(new ScriptInfo (scrpt, timestamp, provDir));
		
		// parse source script information, if any
		if( sourcedScripts == null )
			return;
		
		while( sourcedScripts.hasNext() && ssTimestamps != null && ssTimestamps.hasNext())
		{
			String filepath = sourcedScripts.next().getAsString() ;
			String timestamp = ssTimestamps.next().getAsString() ;
			
			sourcedScriptInfo.add( new ScriptInfo(filepath, timestamp, provDir) ) ;
		}
		
		attributes.setSourcedScriptInfo(sourcedScriptInfo) ;
	}

	/**
	 * Parses all the nodes and edges and adds everything to the provenance
	 * data and visual graph.
	 */
	@Override
	protected void parseNodesAndEdges () {

        if (jsonRoot.isJsonObject()) {
            JsonObject wholeThing = jsonRoot.getAsJsonObject();
            JsonObject procNodes = wholeThing.getAsJsonObject("activity");
            parseProcNodes (procNodes);
            
            JsonObject entity = wholeThing.getAsJsonObject("entity");
            parseDataAndLibraryNodes(entity);
            
            JsonObject cfEdges = wholeThing.getAsJsonObject("wasInformedBy");
            parseControlFlowEdges (cfEdges);

            JsonObject outputEdges = wholeThing.getAsJsonObject("wasGeneratedBy");
            parseOutputEdges (outputEdges);

            JsonObject inputEdges = wholeThing.getAsJsonObject("used");
            parseInputEdges (inputEdges);
        }

        try {
			reader.close();
		} catch (IOException e) {
			// Do nothing
		}
    }
    
	/** 
	 * Parses all the procedural nodes and adds them to the provenance data and visual graph 
	 */
	private void parseProcNodes(JsonObject procNodes) {
		/*
		 * This is the Json syntax for a procedural node
		 * 
		 * 	"rdt:p6": {
		 *		"rdt:name": "a <- c(1:10)",
		 *		"rdt:type": "Operation",
		 *		"rdt:elapsedTime": 0.95,
		 *		"rdt:scriptNum": 0,
		 *		"rdt:startLine": 19,
		 *		"rdt:startCol": 1,
		 *		"rdt:endLine": 19,
		 *		"rdt:endCol": 12
		 *	},
		 */

		Set<Entry<String, JsonElement>> procNodeSet = procNodes.entrySet();
		int idNum = 0;
		for (Entry <String, JsonElement> procNode : procNodeSet) {
			String id = procNode.getKey().substring(PREFIX.length());	// strip off prefix `rdt:` from node name
			
			JsonObject nodeDef = (JsonObject) procNode.getValue(); 
			String type = nodeDef.get(PREFIX+"type").getAsString();
			//System.out.println("Found proc node: " + id + " with type " + type);
			
			String name = nodeDef.get(PREFIX+"name").getAsString();
			
			// parse elapsed time (',' or '.' can be used as a digit separator and/or digit grouping)
			double elapsedTime = parseTime(nodeDef.get(PREFIX+"elapsedTime").getAsString());
			
			String script = nodeDef.get(PREFIX+"scriptNum").getAsString();
			String startLine = nodeDef.get(PREFIX+"startLine").getAsString();
			String startCol = nodeDef.get(PREFIX+"startCol").getAsString();
			String endLine = nodeDef.get(PREFIX+"endLine").getAsString();
			String endCol = nodeDef.get(PREFIX+"endCol").getAsString();
			
			idNum = Integer.parseInt(id.substring(1));
			String label = ""+idNum+"-"+name;
			addProcNode(type, id, label, null, elapsedTime, script, startLine, startCol, endLine, endCol);
		}
		numPins = idNum;
	}
	
	/**
	 * Parses and returns the 'elapsedTime' string value as a double.
	 * There will always be a decimal separator in the string.
	 */
	private double parseTime( String str ) {
		
		try {
			// '.' as the decimal separator lets the string convert to double easily.
			return( Double.parseDouble(str) );
		}
		catch(NumberFormatException nfe) {
			
			// This catches the cases where the number string is formatted such that there are:
			// ',' or '.' used to group digits, and/or
			// ',' or '.' is used as a decimal separator
			
			String regex = "(,|\\.)";	// regular expression for ',' and/or '.'
			
			// Split the string into parts where the separators are
			// Add decimal separator (before last part)
			String[] parts = str.split(regex);
			parts[parts.length-1] = '.' + parts[parts.length-1];
			
			// combine and convert
			str = "";
			
			for(int i = 0 ; i < parts.length ; i++) {
				str += parts[i];
			}
			
			return( Double.parseDouble(str) );
		}
	}
	
	/** 
	 * Parses all the data nodes and adds them to the provenance data and visual graph 
	 */
	private void parseDataAndLibraryNodes(JsonObject entity) {
		/*
		 * json syntax for a data node:
		 *  
		 * 	"rdt:d2": {
		 *		"rdt:name": "a",
		 *		"rdt:value": "data/2-a.csv",
		 *		"rdt:valType": "{\"container\":\"vector\", \"dimension\":[2], \"type\":[\"character\"]}",
		 *		"rdt:type": "Snapshot",
		 *		"rdt:scope": "R_GlobalEnv",
		 *		"rdt:fromEnv": false,
		 *		"rdt:MD5hash": "",
		 *		"rdt:timestamp": "2018-01-31T09.36.39EST",
		 *		"rdt:location": ""
		 *	},
		 *
		 * json syntax for a library node:
		 * 
		 * 	"rdt:l1": {
		 *		"name": "base",
		 *		"version": "3.4.3",
		 *		"prov:type": {
		 *			"$": "prov:Collection",
		 *			"type": "xsd:QName"
		 *		}
		 *	},
		 */		
		
		Set<Entry<String, JsonElement> > nodeSet = entity.entrySet();
		ArrayList<String> libraries = new ArrayList<String>();
		
		for (Entry<String, JsonElement> node : nodeSet) 
		{	
			String id = node.getKey().substring(PREFIX.length());	// strip off prefix `rdt:` from node name
			
			// data nodes
			if( id.charAt(0) == 'd' )
			{
				JsonObject nodeDef = (JsonObject) node.getValue(); 
				
				String type = nodeDef.get(PREFIX+"type").getAsString();
				//System.out.println("Found data node: " + id + " with type " + type);
				
				String name = nodeDef.get(PREFIX+"name").getAsString();
				String value = nodeDef.get(PREFIX+"value").getAsString();
				
				// If we are loading from a local file, we need to get the full path
				// to the file.  URL nodes that lack :// are saved copies of
				// webpages.  URLs that start with -> are actually socket connections.
				if(type.equals("File") || type.equals("Snapshot") || type.equals("StandardOutputSnapshot") || 
						(type.equals("URL") && value.indexOf("://") == -1 && value.indexOf("->") == -1)){
					if (builder != null) {
						File relative = new File(builder.getSourceDDGDirectory(), value);
						value = relative.getAbsolutePath();
					}
				}
				
				// If we ever want to do anything interesting with valType in DDG Explorer,
				// we will need to parse ValType instead of just storing it as a string.
				String valType = nodeDef.get(PREFIX+"valType").toString();
				
				String timestamp = nodeDef.get(PREFIX+"timestamp").getAsString();
				if (timestamp.equals("")) {
					timestamp = null;
				}
				
				String location = nodeDef.get(PREFIX+"location").getAsString();
				if (location.equals("")) {
					location = null;
				}
				
				int idNum = Integer.parseInt(id.substring(1));
				String label = ""+idNum+"-"+name;
			
				addDataNode (type, id, label, value, valType, timestamp, location);
			}
			// environment node: skip!
			else if( id.equals("environment") )
			{
				continue;
			}
			// library nodes: add library nodes to list
			else if( id.charAt(0) == 'l' )
			{
				JsonObject obj = (JsonObject) node.getValue() ;
				
				String name = obj.get("name").getAsString() ;
				String version = obj.get("version").getAsString() ;
				
				libraries.add(name + " " + version) ;
			}
			// exit loop for everything else (function nodes)
			else
			{
				break;
			}
		}	// end for
		
		// set list of libraries in attributes
		attributes.setPackages(libraries) ;
	}

	/** 
	 * Parses all the control flow edges and adds them to the provenance data and visual graph 
	 */
	private void parseControlFlowEdges(JsonObject cfEdges) {
		/*
		 * This is the json syntax for a control flow edge (procedure-to-procedure)
		 * 
		 * 	"rdt:pp1": {
		 *		"prov:informant": "rdt:p1",
		 *		"prov:informed": "rdt:p2"
		 *	},
		 */
		
		// Edge case
		if (cfEdges == null) {
			return;
		}
		
		Set<Entry<String, JsonElement>> cfEdgeSet = cfEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : cfEdgeSet) 
		{
			JsonObject nodeDef = (JsonObject) cfEdge.getValue();
			
			String pred = nodeDef.get("prov:informant").getAsString().substring(PREFIX.length());	// strip off prefix `rdt:` from referenced node name
			String succ = nodeDef.get("prov:informed").getAsString().substring(PREFIX.length());	// strip off prefix `rdt:` from referenced node name
			//System.out.println("Found cf edge from " + pred + " to " + succ);
			
			addControlFlowEdge(pred, succ);
		}
	}

	/**
	 * Parses all the data output edges and adds them to the provenance data and visual grap
	 */
	private void parseOutputEdges(JsonObject outputEdges) {
		/*
		 * This is the json syntax for a data out edge (procedure-to-data)
		 * 
		 * 	"rdt:pd1": {
		 *		"prov:activity": "rdt:p6",
		 *		"prov:entity": "rdt:d1"
		 *	},
		 */
		
		// base case: no edges
		if( outputEdges == null )
			return ;
		
		Set<Entry<String, JsonElement>> outputEdgeset = outputEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : outputEdgeset) {
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString().substring(PREFIX.length());	// strip off prefix `rdt:` from node name
			String data = nodeDef.get("prov:entity").getAsString().substring(PREFIX.length());		// strip off prefix `rdt:` from node name
			//System.out.println("Found df edge from " + proc + " to " + data);
			
			try {
				addDataProducerEdge(proc, data);
			} catch (NoSuchDataNodeException | NoSuchProcNodeException | ReportErrorException e) {
				// Nothing to do.  The error message is produced inside addDataProducerEdge.
			}
		}
	}

	/** Parses all the data input edges and adds them to the provenance data and visual graph */
	private void parseInputEdges(JsonObject inputEdges) {
		/*
		 * This is the json syntax for a data in edge (data-to-procedure)
		 * 
		 * 	"rdt:dp1": {
		 *		"prov:entity": "rdt:d1",
		 *		"prov:activity": "rdt:p7"
		 *	},
		 */
		
		// base case: no edges
		if( inputEdges == null )
			return ;
		
		Set<Entry<String, JsonElement>> inputEdgeset = inputEdges.entrySet();
		
		for (Entry <String, JsonElement> edge : inputEdgeset) 
		{	
			// data-to-procedure edges occur before function-to-procedure edges
			if( edge.getKey().startsWith(PREFIX+"fp") )
				break ;
			
			JsonObject nodeDef = (JsonObject) edge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString().substring(PREFIX.length());	// strip off prefix `rdt:` from node name
			String data = nodeDef.get("prov:entity").getAsString().substring(PREFIX.length());		// strip off prefix `rdt:` from node name
			//System.out.println("Found input edge from " + data + " to " + proc);
			
			try {
				addDataConsumerEdge(proc, data);
			} catch (NoSuchDataNodeException | NoSuchProcNodeException e) {
				// Nothing to do.  The error message is produced inside addDataConsumerEdge.
			}
		}
	}
}
