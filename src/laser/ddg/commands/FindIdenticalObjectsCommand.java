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

import laser.ddg.ScriptNode;
import laser.ddg.gui.DDGExplorer;
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
	private int index = 1;

	@Override
	public void actionPerformed(ActionEvent args0) {
		
		// Setup and Initialization
		WorkflowGraphBuilder builder = new WorkflowGraphBuilder();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		builder.buildNodeAndEdgeTables();
		
		// Read in the hashtable and find nodes with matching hashes.
		try {
			ArrayList<String[]> matches = new ArrayList<String[]>();
			readHashtable(matches);
			generateFileNodes(matches, builder);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to load the file: " + e.getMessage(),
					"Error loading file", JOptionPane.ERROR_MESSAGE);
		}
		
		// Add nodes to the graph
		for (ScriptNode node : scrnodes) {
			builder.addNode(node, node.getId());
		}
		for (RDataInstanceNode node : fileNodes) {
			builder.addNode(node.getType(), node.getId(), node.getName(), null,
					node.getCreatedTime(), node.getLocation(), null);
		}
		
		// Create graph and legend.
		builder.drawGraph();
		builder.createLegend("R");
		builder.getPanel().addLegend();
		ddgExplorer.addTab("Script Workflow", builder.getPanel());
	}

	private void readHashtable(ArrayList<String[]> csvmap) throws IOException {
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
			csvmap.add(entries);
		}
		br.close();
	}

	private ArrayList<RDataInstanceNode> generateFileNodes(ArrayList<String[]> matches, WorkflowGraphBuilder builder) {
		for (String[] match : matches) {
			String name = match[1].substring(match[1].lastIndexOf('/') + 1);
			RDataInstanceNode file = new RDataInstanceNode("File", name, match[8], match[7], match[1], match[5], match[0]);
			ScriptNode scrnode = generateScriptNode(scrnodes, file, match[0], match[2] + "/ddg.json");
			
			int foundindex = -1;
			for (int i = 0; i < fileNodes.size(); i++) {
				if (fileNodes.get(i).getHash().equals(file.getHash()) && 
						fileNodes.get(i).getName().equals(file.getName())) {
					foundindex = i;
				}
			}
			if (foundindex == -1) {
				fileNodes.add(file);
				file.setId(index++);
				if (match[6].equals("read")) {
					builder.addEdge("SFR", file.getId(), scrnode.getId());
				} else if (match[6].equals("write")) {
					builder.addEdge("SFW", scrnode.getId(), file.getId());
				}
			} else {
				if (match[6].equals("read")) {
					builder.addEdge("SFR", fileNodes.get(foundindex).getId(), scrnode.getId());
				} else if (match[6].equals("write")) {
					builder.addEdge("SFW", scrnode.getId(), fileNodes.get(foundindex).getId());
				}
			}
		}
		return fileNodes;
	}

	private ScriptNode generateScriptNode(ArrayList<ScriptNode> scrnodes, RDataInstanceNode file, String path, String json) {
		String name = path.substring(path.lastIndexOf('/') + 1);
		ScriptNode ret = new ScriptNode(0.0, name, json, path);
		if (scrnodes.size() == 0) {
			ScriptNode toAdd = new ScriptNode(0.0, name, json, path);
			toAdd.setId(index++);
			scrnodes.add(toAdd);
			ret = toAdd;
		} else {
			boolean added = false;
			for (ScriptNode scrnode : scrnodes) {
				if (scrnode.getFullpath().equals(path)) {
					added = true;
					ret = scrnode;
				}
			}
			if (!added) {
				ScriptNode toAdd = new ScriptNode(0.0, name, json, path);
				toAdd.setId(index++);
				scrnodes.add(toAdd);
				ret = toAdd;
			}
		}
		return ret;
	}
}
