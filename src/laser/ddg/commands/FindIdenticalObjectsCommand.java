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

	@Override
	public void actionPerformed(ActionEvent args0) {
		ProvenanceData currDDG = DDGExplorer.getInstance().getCurrentDDG();
		DataNodeVisitor dataNodeVisitor = new DataNodeVisitor();
		ArrayList<DataInstanceNode> dins = dataNodeVisitor.getDins();
		ArrayList<String[]> matches = new ArrayList<String[]>();
		dataNodeVisitor.visitNodes();

		ArrayList<String> nodehashes = new ArrayList<String>();
		for (int i = 0; i < dins.size(); i++) {
			nodehashes.add(dins.get(i).getHash());
		}

		ArrayList<WorkflowNode> wfns = new ArrayList<WorkflowNode>();
		
		try {
			readHashtable(currDDG.getSourcePath(), nodehashes, matches);
			wfns = constructWorkflowNodes(matches);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		Workflow flow = new Workflow(currDDG.getProcessName(), currDDG.getTimestamp());
		flow.setWfnodes(wfns);
	}

	private void readHashtable(String currDDGDir, ArrayList<String> nodehashes, ArrayList<String[]> csvmap) throws IOException {
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		String line = "";
		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] entries = line.replaceAll("\"", "").split(","); 
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
