package laser.ddg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Workflow {
	
	ArrayList<ProvenanceData> scripts;
	// The process name of the script that the workflow originated from
	String processName;
	// The timestamp of the script that the workflow originated from
	String timestamp;
	
	Map<String, ArrayList<String>> filedata;
	
	public Workflow(String scr, String timestamp) {
		this.scripts = new ArrayList<ProvenanceData>();
		// The keys are going to be the names of files.
		// The values will be some sort of list or object containing relevant file information.
		// Making it an array list of some sort of object that stores these values is probably
		// better, because of the situation where the same file appears multiple times with
		// different hashes.
		this.filedata = new HashMap<String, ArrayList<String>>();
		this.processName = scr;
		this.timestamp = timestamp;
	}
	
	public void put(ArrayList<String> matchedObjs, ArrayList<DataInstanceNode> dins) {
		
	}
	
	public void add(String info) {
		String[] parsedInfo = parseInfoString(info);
		ProvenanceData provData = constructProvData(parsedInfo);
		scripts.add(provData);
	}
	
	private String[] parseInfoString(String info) {
		String[] splitInfo = info.split("\"");
		String[] parsedInfo = new String[7];
		int index = 0;
		for (int i = 0; i < splitInfo.length; i++) {
			if (!splitInfo[i].equals("")) {
				parsedInfo[index++] = splitInfo[i];
			}
		}
		return parsedInfo;
	}
	
	private ProvenanceData constructProvData(String[] parsedInfo) {
		String script = parsedInfo[1].substring(2, parsedInfo[1].length() - 4) + ".R";
		String timestamp = parsedInfo[6];
		ProvenanceData prov = new ProvenanceData(script, timestamp, "R");
		return prov;
	}
	
}
