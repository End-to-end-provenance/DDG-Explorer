package laser.ddg;

import java.util.ArrayList;

public class Workflow {
	
	ArrayList<ProvenanceData> scripts;
	// The process name of the script that the workflow originated from
	String processName;
	// The timestamp of the script that the workflow originated from
	String timestamp;
	
	public Workflow(String scr, String timestamp) {
		this.scripts = new ArrayList<ProvenanceData>();
		this.processName = scr;
		this.timestamp = timestamp;
	}
	
	public void add(String info) {
		ProvenanceData provData = constructProvData(info);
		scripts.add(provData);
	}
	
	private ProvenanceData constructProvData(String info) {
		String[] parsedInfo = info.split("\"");
		String script = parsedInfo[3].substring(2, parsedInfo[3].length() - 4) + ".R";
		String timestamp = parsedInfo[11];
		ProvenanceData prov = new ProvenanceData(script, timestamp, "R");
		return prov;
	}
	
}
