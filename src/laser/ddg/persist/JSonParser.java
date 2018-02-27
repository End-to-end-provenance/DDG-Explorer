package laser.ddg.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
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
	protected void parseHeader() throws IOException {
        JsonObject wholeThing = jsonRoot.getAsJsonObject();
        JsonObject entity = wholeThing.getAsJsonObject("entity");
		JsonObject environment = entity.getAsJsonObject("environment");

		Set<Entry<String, JsonElement> > attributeSet = environment.entrySet();
		for (Entry <String, JsonElement> attribute : attributeSet) {
			String attributeName = attribute.getKey();
			JsonElement attributeValue = attribute.getValue();

			if(attributeName.equals(Attributes.JSON_LANGUAGE)){
				language = attributeValue.getAsString();
				attributes.set(Attributes.LANGUAGE, language);
			}
			else if(attributeName.equals(Attributes.JSON_MAIN_SCRIPT_NAME)){
				scrpt = attributeValue.getAsString();
				attributes.set(Attributes.MAIN_SCRIPT_NAME, scrpt);
			}
			else if(attributeName.equals(Attributes.JSON_EXECUTION_TIME)){
				// R puts : in the timestamp value, but we can't use that in a directory name on Windows.
				timestamp = attributeValue.getAsString().replaceAll(":", ".");
				attributes.set(Attributes.MAIN_SCRIPT_TIMESTAMP, timestamp);
			}
			
			
			/* EDIT - need a new way of handling sourced scripts
			else if (attributeName.equals(Attributes.JSON_SOURCED_SCRIPTS)) {
				String scriptFile = attributes.get(Attributes.MAIN_SCRIPT_NAME);
				String scriptDir = scriptFile.substring(0, scriptFile.lastIndexOf(File.separator) + 1);
				if (attributeValue.isJsonArray()) {
					List<ScriptInfo> sourcedScriptInfo = parseSourcedScripts(scriptDir, attributeValue.getAsJsonArray());
					attributes.setSourcedScriptInfo(sourcedScriptInfo);
				}
			}
			*/
			/* EDIT - installed packages are in separate node
			else if (attributeName.equals(Attributes.JSON_INSTALLED_PACKAGES)) {
				if (attributeValue.isJsonArray()) {
					List<String> packages = parsePackages(attributeValue.getAsJsonArray());
					attributes.setPackages(packages);
				}
			}
			*/
			else {
				try {
					attributes.set(attributeName, attributeValue.getAsString());
				} catch (IllegalStateException | UnsupportedOperationException e) {
					// Ignore any other attributes that are not simple strings
				}

			}
		}
	}

	/**
	 * Parses the json attribute that contains the sourced script information
	 * 
	 * @param scriptDir the directory where the script is stored
	 * @param sourcedScripts the json attribute for sourced scripts
	 * @return a list of sourced script objects 
	 */
	private static List<ScriptInfo> parseSourcedScripts(String scriptDir, JsonArray sourcedScripts) {
		List<ScriptInfo> sourcedScriptInfo = new ArrayList<>();
		
		
		/*for (JsonElement sourcedScriptElem : sourcedScripts) {
			JsonObject sourcedScript = sourcedScriptElem.getAsJsonObject();
			String name = sourcedScript.get("name").getAsString();
			String timestamp = sourcedScript.get("timestamp").getAsString();
			sourcedScriptInfo.add(new ScriptInfo(scriptDir + File.separator + name, timestamp));
		}*/
		
		
		return sourcedScriptInfo;
	}

	/**
	 * Parses the installed packages information
	 * 
	 * @param packages the json attribute for the packages
	 * @return a list of the packages
	 */
	private static List<String> parsePackages(JsonArray packages) {
		List<String> packageInfo = new ArrayList<>();
		for (JsonElement packageElem : packages) {
			JsonObject packageObj = packageElem.getAsJsonObject();
			String name = packageObj.get("package").getAsString();
			String version = packageObj.get("version").getAsString();
			packageInfo.add(name + " " + version);
		}
		return packageInfo;
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
            
            JsonObject dataNodes = wholeThing.getAsJsonObject("entity");
            parseDataNodes (dataNodes);
            
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
		 * 	"p6": {
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
		
		for (Entry <String, JsonElement> procNode : procNodeSet) {
			String id = procNode.getKey();
			
			JsonObject nodeDef = (JsonObject) procNode.getValue(); 
			String type = nodeDef.get("rdt:type").getAsString();
			//System.out.println("Found proc node: " + id + " with type " + type);
			
			String name = nodeDef.get("rdt:name").getAsString();
			double elapsedTime = Double.parseDouble(nodeDef.get("rdt:elapsedTime").getAsString());
			
			String script = nodeDef.get("rdt:scriptNum").getAsString();
			String startLine = nodeDef.get("rdt:startLine").getAsString();
			String startCol = nodeDef.get("rdt:startCol").getAsString();
			String endLine = nodeDef.get("rdt:endLine").getAsString();
			String endCol = nodeDef.get("rdt:endCol").getAsString();
			
			int idNum = Integer.parseInt(id.substring(1));
			String label = ""+idNum+"-"+name;
			addProcNode(type, id, label, null, elapsedTime, script, startLine, startCol, endLine, endCol);
		}
	}
	
	/** 
	 * Parses all the data nodes and adds them to the provenance data and visual graph 
	 */
	private void parseDataNodes(JsonObject dataNodes) {
		/*
		 * This is the json syntax for a data node
		 *  
		 * 	"d2": {
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
		 */		
		
		Set<Entry<String, JsonElement> > dataNodeSet = dataNodes.entrySet();
		
		for (Entry <String, JsonElement> dataNode : dataNodeSet) {
			
			String id = dataNode.getKey();
			
			if( id.charAt(0) != 'd')
				break;
			
			JsonObject nodeDef = (JsonObject) dataNode.getValue(); 
			
			String type = nodeDef.get("rdt:type").getAsString();
			//System.out.println("Found data node: " + id + " with type " + type);
			
			String name = nodeDef.get("rdt:name").getAsString();
			String value = nodeDef.get("rdt:value").getAsString();
			
			// If we are loading from a local file, we need to get the full path
			// to the file.  URL nodes that lack :// are saved copies of
			// webpages.  URLs that start with -> are actually socket connections.
			if(type.equals("File") || type.equals("Snapshot") || 
					(type.equals("URL") && value.indexOf("://") == -1 && value.indexOf("->") == -1)){
				if (builder != null) {
					File relative = new File(builder.getSourceDDGDirectory(), value);
					value = relative.getAbsolutePath();
				}
			}
			
			// If we ever want to do anything interesting with valType in DDG Explorer,
			// we will need to parse ValType instead of just storing it as a string.
			String valType = nodeDef.get("rdt:valType").toString();
			
			String timestamp = nodeDef.get("rdt:timestamp").getAsString();
			if (timestamp.equals("")) {
				timestamp = null;
			}
			
			String location = nodeDef.get("rdt:location").getAsString();
			if (location.equals("")) {
				location = null;
			}

			int idNum = Integer.parseInt(id.substring(1));
			String label = ""+idNum+"-"+name;
			
			addDataNode (type, id, label, value, valType, timestamp, location);
		}

	}

	/** 
	 * Parses all the control flow edges and adds them to the provenance data and visual graph 
	 */
	private void parseControlFlowEdges(JsonObject cfEdges) {
		/*
		 * This is the json syntax for a control flow edge (procedure-to-procedure)
		 * 
		 * 	"pp1": {
		 *		"prov:informant": "rdt:p1",
		 *		"prov:informed": "rdt:p2"
		 *	},
		 */
		
		Set<Entry<String, JsonElement>> cfEdgeSet = cfEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : cfEdgeSet) 
		{
			JsonObject nodeDef = (JsonObject) cfEdge.getValue();
			
			String pred = nodeDef.get("prov:informant").getAsString();
			String succ = nodeDef.get("prov:informed").getAsString();
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
		 * 	"pd1": {
		 *		"prov:activity": "rdt:p6",
		 *		"prov:entity": "rdt:d1"
		 *	},
		 */
		
		Set<Entry<String, JsonElement>> outputEdgeset = outputEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : outputEdgeset) {
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString();
			String data = nodeDef.get("prov:entity").getAsString();
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
		 * 	"dp1": {
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
			if( edge.getKey().substring(0,2).equals("fp") )
				break ;
			
			JsonObject nodeDef = (JsonObject) edge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString();
			String data = nodeDef.get("prov:entity").getAsString();
			//System.out.println("Found input edge from " + data + " to " + proc);
			
			try {
				addDataConsumerEdge(proc, data);
			} catch (NoSuchDataNodeException | NoSuchProcNodeException e) {
				// Nothing to do.  The error message is produced inside addDataConsumerEdge.
			}
		}
	}
}
