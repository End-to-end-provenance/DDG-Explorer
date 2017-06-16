package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import laser.ddg.DataInstanceNode;
import laser.ddg.DataNodeVisitor;
import laser.ddg.ProvenanceData;
import laser.ddg.Workflow;
import laser.ddg.WorkflowNode;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.Parser;

/**
 * Command to examine a DDG and determine if any of the MD5 hashes of its
 * file nodes match with the MD5 hashes contained in the hashtable.csv file.
 * 
 * @author Connor Gregorich-Trevor
 * @version June 13, 2017
 *
 */

public class FindIdenticalObjectsCommand implements ActionListener {

	private ArrayList<String[]> csvmap;
	private DataNodeVisitor dataNodeVisitor;
	private ArrayList<DataInstanceNode> dins;

	public FindIdenticalObjectsCommand() {
		csvmap = new ArrayList<String[]>();
	}

	@Override
	public void actionPerformed(ActionEvent args0) {
		// Find matched objs and dins
		dataNodeVisitor = new DataNodeVisitor();
		dins = dataNodeVisitor.getDins();
		dataNodeVisitor.visitNodes();

		ArrayList<String> nodehashes = new ArrayList<String>();
		for (int i = 0; i < dins.size(); i++) {
			nodehashes.add(dins.get(i).getHash());
		}

		ArrayList<WorkflowNode> wfns = new ArrayList<WorkflowNode>();
		
		try {
			ProvenanceData currDDG = DDGExplorer.getInstance().getCurrentDDG();
			String currDDGDir = currDDG.getSourcePath();
			readHashtable(currDDGDir, nodehashes);
			wfns = constructWorkflowNodes(csvmap);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		System.out.println(wfns);
		System.out.println("All done!");
	}

	private void readHashtable(String currDDGDir, ArrayList<String> nodehashes) throws IOException {
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		String line = "";
		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] entries = line.replaceAll("\"", "").split(","); 
			// Up until here is good.
			String hash = entries[4];
			if (nodehashes.contains(hash)) {
				csvmap.add(entries);
			}
		}
		br.close();
	}

	private ArrayList<WorkflowNode> constructWorkflowNodes(ArrayList<String[]> sarrays) throws Exception {
		ArrayList <WorkflowNode> wfns = new ArrayList<WorkflowNode>();
		for (String[] s : sarrays) {
			WorkflowNode wf = new WorkflowNode();
			wf.setFilepath(s[0]);
			wf.setDdgpath(s[1]);
			wf.setNodepath(s[2]);
			wf.setNodenumber(Integer.parseInt(s[3]));
			wf.setMd5hash(s[4]);
			wf.setRw(s[5]);
			wf.setTimestamp(s[6]);
			wf.setProvData(loadFileNoPrefuse(s[1]));
			wf.setDin(wf.getProvData().findDin(wf.getNodenumber()));
			wfns.add(wf);
		}
		return wfns;
	}

	private static ProvenanceData loadFileNoPrefuse(String path) throws Exception {
		File selectedFile = new File(path + "/ddg.json");
		Parser parser = Parser.createParser(selectedFile, null);
		ProvenanceData provData = parser.addNodesAndEdges();
		return provData;
	}

}
