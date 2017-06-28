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

import laser.ddg.DataInstanceNode;
import laser.ddg.DataNodeVisitor;
import laser.ddg.ProvenanceData;
import laser.ddg.ScriptNode;
import laser.ddg.Workflow;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaWriter;
import laser.ddg.r.RDataInstanceNode;
import laser.ddg.visualizer.WorkflowGraphBuilder;

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
	private ArrayList<RDataInstanceNode> fileNodes = new ArrayList<RDataInstanceNode>();

	@Override
	public void actionPerformed(ActionEvent args0) {
		
		DataNodeVisitor dataNodeVisitor = new DataNodeVisitor();
		ArrayList<DataInstanceNode> dins = dataNodeVisitor.getDins();
		ArrayList<String[]> matches = new ArrayList<String[]>();
		dataNodeVisitor.visitNodes();

		ArrayList<String> nodehashes = new ArrayList<String>();
		for (int i = 0; i < dins.size(); i++) {
			nodehashes.add(dins.get(i).getHash());
		}

		ProvenanceData currDDG = DDGExplorer.getInstance().getCurrentDDG();
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
		flow.setFileNodeList(fileNodes);
		flow.setScriptNodeList(scrnodes);
		
		JenaWriter jenaWriter = JenaWriter.getInstance();
		WorkflowGraphBuilder builder = new WorkflowGraphBuilder(false, jenaWriter);
		builder.buildNodeAndEdgeTables();
		int j = 0;
		for (ScriptNode node : flow.getScriptNodeList()) {
			builder.addNode(node, j);
			j++;
		} 
		builder.drawGraph();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ddgExplorer.addTab("Script Workflow", builder.getPanel());
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
			String hash = entries[5];
			if (nodehashes.contains(hash)) {
				csvmap.add(entries);
			}
		}
		br.close();
	}

	private ArrayList<RDataInstanceNode> generateFileNodes(ArrayList<String[]> matches) {
		for (String[] match : matches) {
			// This constructor information needs fixing
			RDataInstanceNode file = new RDataInstanceNode("File", match[3], match[8], match[7], match[1], match[5], match[6]);
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
