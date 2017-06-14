package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import laser.ddg.DataInstanceNode;
import laser.ddg.DataNodeVisitor;
import laser.ddg.ProvenanceData;
import laser.ddg.Workflow;
import laser.ddg.WorkflowNode;
import laser.ddg.gui.DDGExplorer;

/**
 * Command to examine a DDG and determine if any of the MD5 hashes of its
 * file nodes match with the MD5 hashes contained in the hashtable.csv file.
 * 
 * @author Connor Gregorich-Trevor
 * @version June 13, 2017
 *
 */

public class FindIdenticalObjectsCommand implements ActionListener {

	private HashMap<String, ArrayList<String[]>> csvmap;
	private ArrayList<String[]> localddginfo;
	private DataNodeVisitor dataNodeVisitor;
	private ArrayList<DataInstanceNode> dins;

	public FindIdenticalObjectsCommand() {
		csvmap = new HashMap<String,ArrayList<String[]>>();
		localddginfo = new ArrayList<String[]>();
		
	}

	@Override
	public void actionPerformed(ActionEvent args0) {

		ProvenanceData currDDG = DDGExplorer.getInstance().getCurrentDDG();
		String currDDGDir = currDDG.getSourcePath();

		// Attempt to read the hashtable.
		try {
			readHashtable(currDDGDir);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Find matched objs and dins
		dataNodeVisitor = new DataNodeVisitor();
		dins = dataNodeVisitor.getDins();
		dataNodeVisitor.visitNodes();
		
		ArrayList<String> nodehashes = new ArrayList<String>();
		for (int i = 0; i < dins.size(); i++) {
			nodehashes.add(dins.get(i).getHash());
		}
		ArrayList<String[]> matchedObjs = findMatchingHashes(nodehashes);
		ArrayList<WorkflowNode> localObjs = makeLocalWorkflowObjects();
	}

	private void readHashtable(String currDDGDir) throws IOException {
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		String line = "";
		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] entries = line.split(",");
			String hash = entries[4].substring(1, 33); 
			// String level manipulation in order to eliminate hashtable entries of the same DDG
			if(!currDDGDir.contains(entries[1].substring(2, entries[1].length()-1))) {
				if (csvmap.get(hash) == null) {
					csvmap.put(hash, new ArrayList<String[]>());
				}
				csvmap.get(hash).add(entries);
			} else {
				localddginfo.add(entries);
			}
		}
		br.close();
	}

	private ArrayList<String[]> findMatchingHashes(ArrayList<String> nodehashes) {
		ArrayList<String[]> matches = new ArrayList<String[]>();
		for (int i = 0; i < nodehashes.size(); i++) {
			if (csvmap.get(nodehashes.get(i)) != null) {
				matches.addAll(csvmap.get(nodehashes.get(i)));
			}
		}
		return matches;
	}

	private ArrayList<WorkflowNode> makeLocalWorkflowObjects() {
		// very unsafe code.
		ArrayList<WorkflowNode> fileData = new ArrayList<WorkflowNode>();
		for (int i = 0; i < localddginfo.size(); i++) {
			WorkflowNode wf = new WorkflowNode(localddginfo.get(i)[6], dins.get(i),localddginfo.get(i)[5]);
			fileData.add(wf);
		}
		return fileData;
	}
	
}
