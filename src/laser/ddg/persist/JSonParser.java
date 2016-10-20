package laser.ddg.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.LanguageConfigurator;
import laser.ddg.NoSuchDataNodeException;
import laser.ddg.NoSuchNodeException;
import laser.ddg.NoSuchProcNodeException;
import laser.ddg.ProvenanceData;
import laser.ddg.SourcePos;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.r.RDDGBuilder;
import laser.ddg.visualizer.PrefuseGraphBuilder;

public class JSonParser extends Parser {
    private PrefuseGraphBuilder builder;
	private DDGBuilder ddgBuilder;
	private JsonElement jsonRoot;

	public JSonParser(File file, PrefuseGraphBuilder builder) throws IOException {
		super (file, builder);

    	BufferedReader reader = new BufferedReader (new FileReader(file));
    	
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
			
			try {
				attributes.set(attributeName, attributeValue.getAsString());
			} catch (IllegalStateException e) {
				// TODO:  Examine JSON attributes.  Sourced scripts do not come as a simple string, for exmaple.
			}
			
				
		
		}
	}

	protected void parseNodesAndEdges () {

        // use the isxxx methods to find out the type of jsonelement. In our
        // example we know that the root object is the Albums object and
        // contains an array of dataset objects
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

    }
    
	private void parseProcNodes(JsonObject procNodes) {
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
				System.out.println("Found proc node: " + id + " with type " + type);
				
				String name = nodeDef.get("rdt:name").getAsString();
				Double elapsedTime = Double.parseDouble(nodeDef.get("rdt:elapsedTime").getAsString());
				
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
	
	private void parseDataNodes(JsonObject dataNodes) {
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
			System.out.println("Found data node: " + id + " with type " + type);
			
			String name = nodeDef.get("rdt:name").getAsString();
			String value = nodeDef.get("rdt:value").getAsString();
			
			String timestamp = nodeDef.get("rdt:timestamp").getAsString();
			String location = nodeDef.get("rdt:location").getAsString();

			int idNum = Integer.parseInt(id.substring(1));
			String label = ""+idNum+"-"+name;
			
			addDataNode (type, id, label, value, timestamp, location);
		}

	}

	private void parseControlFlowEdges(JsonObject cfEdges) {
//		"e1" : {
//		"prov:informant" : "p1",
//		"prov:informed" : "p2"
//		} ,
		
		Set<Entry<String, JsonElement> > cfEdgeSet = cfEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : cfEdgeSet) {
			String id = cfEdge.getKey();
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String pred = nodeDef.get("prov:informant").getAsString();
			String succ = nodeDef.get("prov:informed").getAsString();
			System.out.println("Found cf edge from " + pred + " to " + succ);
			
			addControlFlowEdge(pred, succ);
		}
	}

	private void parseOutputEdges(JsonObject outputEdges) {
//		"e2" : {
//		"prov:entity" : "d1",
//		"prov:activity" : "p2"
//		} ,
		
		Set<Entry<String, JsonElement> > outputEdgeset = outputEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : outputEdgeset) {
			String id = cfEdge.getKey();
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString();
			String data = nodeDef.get("prov:entity").getAsString();
			System.out.println("Found df edge from " + proc + " to " + data);
			
			try {
				addDataProducerEdge(proc, data);
			} catch (NoSuchDataNodeException | NoSuchProcNodeException | ReportErrorException e) {
				// Nothing to do.  The error message is produced inside addDataProducerEdge.
			}
		}

	}

	private void parseInputEdges(JsonObject inputEdges) {
//		"e18" : {
//		"prov:activity" : "p10",
//		"prov:entity" : "d4"
//		} ,
		
		Set<Entry<String, JsonElement> > inputEdgeset = inputEdges.entrySet();
		
		for (Entry <String, JsonElement> cfEdge : inputEdgeset) {
			String id = cfEdge.getKey();
			JsonObject nodeDef = (JsonObject) cfEdge.getValue(); 
			String proc = nodeDef.get("prov:activity").getAsString();
			String data = nodeDef.get("prov:entity").getAsString();
			System.out.println("Found cf edge from " + data + " to " + proc);
			
			try {
				addDataConsumerEdge(proc, data);
			} catch (NoSuchDataNodeException | NoSuchProcNodeException e) {
				// Nothing to do.  The error message is produced inside addDataConsumerEdge.
			}
		}
		
	}

}
