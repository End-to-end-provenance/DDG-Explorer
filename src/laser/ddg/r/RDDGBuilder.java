package laser.ddg.r;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.ScriptInfo;
import laser.ddg.SourcePos;
import laser.ddg.gui.LegendEntry;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Builds the nodes used by ddgs that represent the execution of R scripts.
 * 
 * @author Barbara Lerner
 * @version Jul 8, 2013
 *
 */
public class RDDGBuilder extends DDGBuilder {
	
	/**
	 * Creates the builder for R scripts
	 * @param script the filename containing the script that was executed
	 * @param provData the object that holds the ddg
	 */
	public RDDGBuilder(String script, ProvenanceData provData){
		super(script, provData);
	}
	
	/**
	 * Creates the legend to describe the node colors used, using R terminology
	 * @return the items to appear in the legend
	 */
	public static ArrayList<LegendEntry> createNodeLegend () {
		ArrayList<LegendEntry> legend = new ArrayList<>();
		legend.add(new LegendEntry("Collapsible Operation", PrefuseGraphBuilder.NONLEAF_COLOR));
		legend.add(new LegendEntry("Expandable Operation", PrefuseGraphBuilder.STEP_COLOR));
		legend.add(new LegendEntry("Simple Operation", PrefuseGraphBuilder.LEAF_COLOR));
		legend.add(new LegendEntry("Details Omitted", PrefuseGraphBuilder.INCOMPLETE_COLOR));
		legend.add(new LegendEntry("Parameter Binding", PrefuseGraphBuilder.INTERPRETER_COLOR));
		//legend.add(new LegendEntry("Checkpoint Operation", PrefuseGraphBuilder.CHECKPOINT_COLOR));
		//legend.add(new LegendEntry("Restore Operation", PrefuseGraphBuilder.RESTORE_COLOR));
		legend.add(new LegendEntry("Script", PrefuseGraphBuilder.SCRIPT_COLOR));
		legend.add(new LegendEntry("Data", PrefuseGraphBuilder.DATA_COLOR));
		legend.add(new LegendEntry("Error", PrefuseGraphBuilder.EXCEPTION_COLOR));
		legend.add(new LegendEntry("Standard Output", PrefuseGraphBuilder.STANDARD_OUTPUT_COLOR));
		legend.add(new LegendEntry("File", PrefuseGraphBuilder.FILE_COLOR));
		legend.add(new LegendEntry("URL", PrefuseGraphBuilder.URL_COLOR));
		legend.add(new LegendEntry("Graphics Device", PrefuseGraphBuilder.DEVICE_COLOR));
		return legend;
	}

	/**
	 * Creates the legend to describe the edge colors used, using R terminology
	 * @return the items to appear in the legend
	 */
	public static ArrayList<LegendEntry> createEdgeLegend () {
		ArrayList<LegendEntry> legend = new ArrayList<>();
		legend.add(new LegendEntry("Control Flow", PrefuseGraphBuilder.CONTROL_FLOW_COLOR));
		legend.add(new LegendEntry("Data Flow", PrefuseGraphBuilder.DATA_FLOW_COLOR));
		legend.add(new LegendEntry("Script Read", PrefuseGraphBuilder.SCRIPT_READ_COLOR));
		legend.add(new LegendEntry("Script Write", PrefuseGraphBuilder.SCRIPT_WRITE_COLOR));
		return legend;
	}

	/**
	 * Determines what kind of R function node to create and adds it
	 * 
	 * @param type the type of procedure node, can be leaf, start or finish
	 * @param id the id number of the node
	 * @param nodeName the name of the node
	 * @param funcName the name of the function executed
	 * @param sourcePos the location in the source file that this node corresponds to
	 * @return the node that is created
	 */
	@Override
	public ProcedureInstanceNode addProceduralNode(String type, int id, String nodeName, String funcName, double elapsedTime, SourcePos sourcePos){
		RFunctionInstanceNode newFuncNode = null;
		ProvenanceData provObject = getProvObject();
		if(type.equals("Start")){
			newFuncNode = new RStartNode(nodeName, funcName, provObject, elapsedTime, sourcePos);
		}
		else if(type.equals("Leaf") || type.equals("Operation") || type.equals("Incomplete")){
			newFuncNode = new RLeafNode(nodeName, funcName, provObject, elapsedTime, sourcePos);
		}
		else if(type.equals("Finish")){
			newFuncNode = new RFinishNode(nodeName, provObject, elapsedTime, sourcePos);
		}
		else if(type.equals("Interm")){
			// This type is not currently produced by RDataTracker.
			newFuncNode = new RIntermNode(nodeName, provObject, elapsedTime, sourcePos);
		}
		else if(type.equals("Binding")){
			// This type is not currently produced by RDataTracker.
			newFuncNode = new RBindingNode(nodeName, provObject, elapsedTime, sourcePos);
		}
		else if (type.equals("Checkpoint")) {
			newFuncNode = new RCheckpointNode(nodeName, provObject, elapsedTime, sourcePos);
		}
		else if (type.equals("Restore")) {
			newFuncNode = new RRestoreNode(nodeName, provObject, elapsedTime, sourcePos);
		}
		if (newFuncNode != null) {
			provObject.addPIN(newFuncNode, id);
		}
		return newFuncNode;
	}
	
	/**
	 * Determines what kind of R function node to create and adds it
	 * 
	 * @param type the type of procedure node, can be leaf, start or finish
	 * @param id the id number of the node
	 * @param name the name of the node
	 * @return the node that is created
	 */
	@Override
	public ProcedureInstanceNode addProceduralNode(String type, int id,	String name, double elapsedTime, SourcePos sourcePos) {
		return addProceduralNode(type, id, name, null, elapsedTime, sourcePos);
	}

	/**
	 * Creates a new RDataInstanceNode that will be added to the provenance graph
	 * 
	 * @param type type of data node to add
	 * @param id id number assigned to the data node
	 * @param name name of the data node
	 * @param value optional value associated with the data node
	 * @param time the timestamp of the data node
	 * @param location the original location of a file, null if not a file node
     * @return the node created
	 */
	@Override
	public DataInstanceNode addDataNode(String type, int id, String name, String value, String time, String location){
		RDataInstanceNode dataNode = new RDataInstanceNode(type, name, value, time, location);
		getProvObject().addDIN(dataNode, id);
		return dataNode;
	}

	/**
	 * Produces a string describing the attributes to display to the user.  The standard attributes always appear
	 * in the same order and display with attribute names familiar to an R programmer.
	 * @param attributes all the attributes
	 * @return the attribute description to display to the user
	 */
	public static String getAttributeString(Attributes attributes) {
		// Get a consistent order when showing attributes

		// Names used in the DB
		String[] dbAttrNames = {"Architecture", "OperatingSystem", "Language", "LanguageVersion", "Script", "ProcessFileTimestamp", 
				"WorkingDirectory", "DDGDirectory", "DateTime"};

		// Names to display to the user
		String[] printAttrNames = {"Architecture", "Operating System", "Language", "Language Version", "Script", "Script Timestamp", 
				"Working Directory", "DDG Directory", "DDG Timestamp"};
		
		StringBuilder attrText = new StringBuilder();
		int which = 0;
		for (String attrName : dbAttrNames) {
			String attrValue = attributes.get(attrName);
			if (attrValue != null) {
				attrText.append(printAttrNames[which] + " = " + attrValue + "\n");
			}
			which++;
		}
		
		// Print out any extra ones, deliberately skipping the one named "processName"
		List<String> attrNameList = Arrays.asList(dbAttrNames);
        attributes.names().stream().filter((attrName) -> (!attrName.equals("processName") && !attrNameList.contains(attrName))).forEach((attrName) -> {
        	attrText.append(attrName + " = " + attributes.get(attrName) + "\n");
        });
        
        List<ScriptInfo> scriptInfoList = attributes.getSourcedScriptInfo();
        if (scriptInfoList != null) {
    		attrText.append("Sourced scripts = \n");
        	for (ScriptInfo scriptInfo : scriptInfoList) {
        		attrText.append("   " + scriptInfo.getName() + "  " + scriptInfo.getTimestamp() + "\n");
        	}
        }

        List<String> packageList = attributes.getPackages();
        if (packageList != null) {
    		attrText.append("Installed packages = \n");
        	for (String packageInfo : packageList) {
        		attrText.append("   " + packageInfo + "\n");
        	}
        }

		return attrText.toString();
	}

}
