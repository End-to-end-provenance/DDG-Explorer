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
import laser.ddg.visualizer.WorkflowGraphBuilder;

/**
 * Parser for ddgs written in Json syntax.
 * 
 * @author Barbara Lerner
 * @version Oct 20, 2016
 *
 */
public class WorkflowJSonParser extends WorkflowParser {
	private JsonElement jsonRoot;
	private BufferedReader reader;

	/**
	 * Create a Json parser
	 * @param file the json file to parser
	 * @param builder the object that builds the visual graph
	 * @throws IOException
	 */
	public WorkflowJSonParser(File file, WorkflowGraphBuilder builder) throws IOException {
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
	 * @throws IOException if the header is not formatted properly or there is
	 *   a problem reading from the input stream.
	 */
	@Override
	protected void parseHeader() throws IOException {
        JsonObject wholeThing = jsonRoot.getAsJsonObject();
        JsonObject activities = wholeThing.getAsJsonObject("activity");
		JsonObject environment = activities.getAsJsonObject("environment");

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
			else if (attributeName.equals(Attributes.JSON_SOURCED_SCRIPTS)) {
				String scriptFile = attributes.get(Attributes.MAIN_SCRIPT_NAME);
				String scriptDir = scriptFile.substring(0, scriptFile.lastIndexOf(File.separator) + 1);
				if (attributeValue.isJsonArray()) {
					List<ScriptInfo> sourcedScriptInfo = parseSourcedScripts(scriptDir, attributeValue.getAsJsonArray());
					attributes.setSourcedScriptInfo(sourcedScriptInfo);
				}
			}
			else if (attributeName.equals(Attributes.JSON_INSTALLED_PACKAGES)) {
				if (attributeValue.isJsonArray()) {
					List<String> packages = parsePackages(attributeValue.getAsJsonArray());
					attributes.setPackages(packages);
				}
			}
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
	 * @param scriptDir the directory where the script is stored
	 * @param sourcedScripts the json attribute for sourced scripts
	 * @return a list of sourced script objects 
	 */
	private static List<ScriptInfo> parseSourcedScripts(String scriptDir, JsonArray sourcedScripts) {
		List<ScriptInfo> sourcedScriptInfo = new ArrayList<>();
		for (JsonElement sourcedScriptElem : sourcedScripts) {
			JsonObject sourcedScript = sourcedScriptElem.getAsJsonObject();
			String name = sourcedScript.get("name").getAsString();
			String timestamp = sourcedScript.get("timestamp").getAsString();
			sourcedScriptInfo.add(new ScriptInfo(scriptDir + File.separator + name, timestamp));
		}
		return sourcedScriptInfo;
	}

	/**
	 * Parses the installed packages information
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
    
	/** Parses all the procedural nodes and adds them to the provenance data and visual graph */
	private void parseProcNodes(JsonObject procNodes) {
		// This is the Json syntax for a procedural node
//		"p12" : {
//		"rdt:name" : "SimpleFunction.R",
//		"rdt:type" : "Finish",
//		"rdt:elapsedTime" : "0.0869999999999997",
//		"rdt:scriptNum" : "NA",
//		"rdt:startLine" : "NA",
//		"rdt:startCol" : "NA",
//		"rdt:endLine" : "NA",
//		"rdt:endCol" : "NA"
//		} ,

		Set<Entry<String, JsonElement> > procNodeSet = procNodes.entrySet();
		
		for (Entry <String, JsonElement> procNode : procNodeSet) {
			String id = procNode.getKey();
			if (!id.equals("environment")) {
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
				addProcNode (type, id, label, null, elapsedTime, script, startLine, startCol, endLine, endCol);
			}
		}
	}
	
	/** Parses all the data nodes and adds them to the provenance data and visual graph */
	private void parseDataNodes(JsonObject dataNodes) {
		// This is the json syntax for a data node
//		"d5" : {
//		"rdt:name" : "y",
//		"rdt:value" : "2",
//		"rdt:type" : "Data",
//		"rdt:scope" : "0x10c32da00",
//		"rdt:fromEnv" : "FALSE",
//		"rdt:timestamp" : "",
//		"rdt:location" : ""
//		} ,

		Set<Entry<String, JsonElement> > dataNodeSet = dataNodes.entrySet();
		
		for (Entry <String, JsonElement> dataNode : dataNodeSet) {
			String id = dataNode.getKey();
			JsonObject nodeDef = (JsonObject) dataNode.getValue(); 
			String type = nodeDef.get("rdt:type").getAsString();
			//System.out.println("Found data node: " + id + " with type " + type);
			
			String name = nodeDef.get("rdt:name").getAsString();
			String value = nodeDef.get("rdt:value").getAsString();
			if(type.equals("File") || type.equals("Snapshot")){
				if (builder != null) {
					File relative = new File(builder.getSourceDDGDirectory(), value);
					value = relative.getAbsolutePath();
				}
			}
			
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
			
			addDataNode (type, id, label, value, timestamp, location);
		}

	}

	/** Parses all the control flow edges and adds them to the provenance data and visual graph */
	private void parseControlFlowEdges(JsonObject cfEdges) {
		// This is the json syntax for a control flow edge
//		"e1" : {
//		"prov:informant" : "p1",
//		"prov:informed" : "p2"
//		} ,
		
		Set<Entry<String, JsonElement> > cfEdgeSet = cfEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : cfEdgeSet) {
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String pred = nodeDef.get("prov:informant").getAsString();
			String succ = nodeDef.get("prov:informed").getAsString();
			//System.out.println("Found cf edge from " + pred + " to " + succ);
			
			addControlFlowEdge(pred, succ);
		}
	}

	/** Parses all the data output edges and adds them to the provenance data and visual graph */
	private void parseOutputEdges(JsonObject outputEdges) {
		// This is the json syntax for a data out edge
//		"e2" : {
//		"prov:entity" : "d1",
//		"prov:activity" : "p2"
//		} ,
		
		Set<Entry<String, JsonElement> > outputEdgeset = outputEdges.entrySet();
		
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
		// This is the json syntax for a data in edge
//		"e18" : {
//		"prov:activity" : "p10",
//		"prov:entity" : "d4"
//		} ,
		
		Set<Entry<String, JsonElement> > inputEdgeset = inputEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : inputEdgeset) {
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString();
			String data = nodeDef.get("prov:entity").getAsString();
			//System.out.println("Found cf edge from " + data + " to " + proc);
			
			try {
				addDataConsumerEdge(proc, data);
			} catch (NoSuchDataNodeException | NoSuchProcNodeException e) {
				// Nothing to do.  The error message is produced inside addDataConsumerEdge.
			}
		}
		
	}

}
