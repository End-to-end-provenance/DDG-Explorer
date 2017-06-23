package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import laser.ddg.AbstractDataInstanceNode;
import laser.ddg.DataInstanceNode;
import laser.ddg.DataNodeVisitor;
import laser.ddg.ProvenanceData;
import laser.ddg.ScriptNode;
import laser.ddg.Workflow;
import laser.ddg.gui.DDGExplorer;

import laser.ddg.persist.WorkflowParser;
import laser.ddg.r.RDataInstanceNode;

/**
 * Command to examine a DDG and determine if any of the MD5 hashes of its
 * file nodes match with the MD5 hashes contained in the hashtable.csv file.
 * 
 * @author Connor Gregorich-Trevor
 * @version June 13, 2017
 *
 */

public class FindIdenticalObjectsCommand implements ActionListener {

	private static final JFileChooser FILE_CHOOSER = new JFileChooser(System.getProperty("user.home"));
	private ArrayList<ScriptNode> scrnodes = new ArrayList<ScriptNode>();

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

		try {
			readHashtable(currDDG.getSourcePath(), nodehashes, matches);
			generateFileNodes(matches);
		} catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to load the file: " + e.getMessage(),
					"Error loading file", JOptionPane.ERROR_MESSAGE);
		}

		Workflow flow = new Workflow(currDDG.getProcessName(), currDDG.getTimestamp());
		System.out.println("All done!");
	}

	private static ProvenanceData loadFileNoPrefuse(String path) throws Exception {
		File selectedFile = new File(path + "/ddg.json");
		WorkflowParser parser = WorkflowParser.createParser(selectedFile, null);
		ProvenanceData provData = parser.addNodesAndEdges();
		return provData;
	}

	private void readHashtable(String currDDGDir, ArrayList<String> nodehashes, ArrayList<String[]> csvmap) throws IOException {
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		if (!hashtable.exists()) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			if (FILE_CHOOSER.showOpenDialog(ddgExplorer) == JFileChooser.APPROVE_OPTION) {
				hashtable = FILE_CHOOSER.getSelectedFile();
			}
		}
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

	private ArrayList<AbstractDataInstanceNode> generateFileNodes(ArrayList<String[]> matches) {
		ArrayList<AbstractDataInstanceNode> fileNodes = new ArrayList<AbstractDataInstanceNode>();
		for (String[] match : matches) {
			RDataInstanceNode file = new RDataInstanceNode("File", match[2], match[2], match[6], match[0], match[4]);
			fileNodes.add(file);
			generateScriptNode(scrnodes, file, match[0]);
		}
		return fileNodes;
	}

	private ArrayList<ScriptNode> generateScriptNode(ArrayList<ScriptNode> scrnodes, RDataInstanceNode file, String path) {
		if (scrnodes.size() == 0) {
			ScriptNode toAdd = new ScriptNode(0.0, path);
			toAdd.addWorkflowNode(file);
			scrnodes.add(toAdd);
		} else {
			boolean added = false;
			for (ScriptNode scrnode : scrnodes) {
				if (scrnode.getName().equals(path)) {
					scrnode.addWorkflowNode(file);
					added = true;
				}
			}
			if (!added) {
				ScriptNode toAdd = new ScriptNode(0.0, path);
				toAdd.addWorkflowNode(file);
				scrnodes.add(toAdd);
			}
		}
		return scrnodes;
	}


}
