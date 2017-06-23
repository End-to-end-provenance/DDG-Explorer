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
import laser.ddg.ScriptNode;
import laser.ddg.Workflow;
import laser.ddg.WorkflowNode;
import laser.ddg.gui.DDGExplorer;

import laser.ddg.persist.WorkflowParser;

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
		} catch (Exception e) {
			e.printStackTrace();
		}

		Workflow flow = new Workflow(currDDG.getProcessName(), currDDG.getTimestamp());
		flow.setWfnodes(wfns);
		flow.myDisplay(flow.getWfnodes().get(4).getProvData());
		ArrayList<ScriptNode> scriptNodes = generateScriptNodes(wfns);
		System.out.println("All done!");
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
			wf.setScriptPath(s[1].substring(0, s[1].length() - 4) + ".R");
			wfns.add(wf);
		}
		return wfns;
	}
	/*
	private void Connect(ArrayList<WorkflowNode> wfnodes) {
		for (WorkflowNode wfnode : wfnodes) {
			for (WorkflowNode check : wfnodes) {
				if (check.getMd5hash().equals(wfnode.getMd5hash())) {
					if (wfnode.getRw().equals("write") && check.getRw().equals("read")) {
						wfnode.addOutput(check);
						check.addInput(wfnode);
					} else if (wfnode.getRw().equals("read") && check.getRw().equals("write")) {
						check.addOutput(wfnode);
						wfnode.addInput(check);
					}
				}
			}
		}
	}
	 */
	private static ProvenanceData loadFileNoPrefuse(String path) throws Exception {
		File selectedFile = new File(path + "/ddg.json");
		WorkflowParser parser = WorkflowParser.createParser(selectedFile, null);
		ProvenanceData provData = parser.addNodesAndEdges();
		return provData;
	}

	// Just to make things easier, I think I want to add the actual script name to the
	// hashtable.csv file.


	private ArrayList<ScriptNode> generateScriptNodes(ArrayList<WorkflowNode> wfnodes) {
		ArrayList<ScriptNode> scrnodes = new ArrayList<ScriptNode>();
		for (WorkflowNode wfnode : wfnodes) {
			if (scrnodes.size() == 0) {
				ScriptNode toAdd = new ScriptNode(0.0, wfnode.getScriptPath());
				toAdd.addWorkflowNode(wfnode);
				scrnodes.add(toAdd);
			} else {
				boolean added = false;
				for (ScriptNode scrnode : scrnodes) {
					if (scrnode.getName().equals(wfnode.getScriptPath())) {
						scrnode.addWorkflowNode(wfnode);
						added = true;
					}
				}
				if (!added) {
					ScriptNode toAdd = new ScriptNode(0.0, wfnode.getScriptPath());
					toAdd.addWorkflowNode(wfnode);
					scrnodes.add(toAdd);
				}
			}
		}
		return scrnodes;
	}


}
