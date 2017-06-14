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

	private HashMap<String, ArrayList<String>> csvmap;
	private DataNodeVisitor dataNodeVisitor;
	
	public FindIdenticalObjectsCommand() {
		csvmap = new HashMap<String,ArrayList<String>>();
	}
	
	@Override
	public void actionPerformed(ActionEvent args0) {
		// Attempt to read the hashtable.
		try {
			readHashtable();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Set up workflow
		ProvenanceData currDDG = DDGExplorer.getInstance().getCurrentDDG();
		Workflow flow = new Workflow(currDDG.getProcessName(), currDDG.getTimestamp());
		
		// Find matched objs and dins
		dataNodeVisitor = new DataNodeVisitor();
		dataNodeVisitor.visitNodes();
		ArrayList<DataInstanceNode> dins = dataNodeVisitor.getDins();
		ArrayList<String> nodehashes = new ArrayList<String>();
		for (int i = 0; i < dins.size(); i++) {
			nodehashes.add(dins.get(i).getHash());
		}
		
		// Find the objects that match with
		ArrayList<String> matchedObjs = findMatchingHashes(nodehashes);
		
		
	}

	/**
	 * Reads the hashtable.csv into a multimap.
	 * @throws IOException if file is not found.
	 */
	private void readHashtable() throws IOException {
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		String line = "";
		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] entries = line.split(",");
			entries[4] = entries[4].substring(1, 33);
			if (csvmap.get(entries[4]) == null) {
				csvmap.put(entries[4], new ArrayList<String>());
			}
			csvmap.get(entries[4]).add(entries[0] + entries[1] + entries[2] + entries[3]
					+ entries[4] + entries[5] + entries[6]);
		}
		br.close();
	}
	
	/**
	 * Finds the nodes in the current DDG with MD5 hashes that match the MD5 hashes
	 * contained in the hashtable.csv file.
	 * 
	 * @param nodehashes A list of MD5 hashes derived from abstract data instance nodes
	 * in the DDG.
	 * @return matches A list containing information for each entry in hashtable.csv
	 * with an MD5 hash that matches one contained in nodehashes.
	 */
	private ArrayList<String> findMatchingHashes(ArrayList<String> nodehashes) {
		ArrayList<String> matches = new ArrayList<String>();
		for (int i = 0; i < nodehashes.size(); i++) {
			if (csvmap.get(nodehashes.get(i)) != null) {
				matches.addAll(csvmap.get(nodehashes.get(i)));
			}
		}
		return matches;
	}
	
}
