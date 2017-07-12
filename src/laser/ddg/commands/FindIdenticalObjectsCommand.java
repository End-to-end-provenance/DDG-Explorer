package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import laser.ddg.ScriptNode;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.r.RDataInstanceNode;
import laser.ddg.visualizer.WorkflowGraphBuilder;

/**
 * Command to examine the hashtable.csv and create a workflow from it.
 * 
 * @author Connor Gregorich-Trevor
 * @version June 13, 2017
 *
 */

public class FindIdenticalObjectsCommand extends MouseAdapter {

	private ArrayList<ScriptNode> scrnodes;
	private ArrayList<RDataInstanceNode> fileNodes;
	private int index = 1;
	private WorkflowGraphBuilder builder;
	private Workflow wf;

	/**
	 * Creates a drop down menu from the hashtable
	 */
	@Override
	public void mouseEntered(MouseEvent e) {

		scrnodes = new ArrayList<ScriptNode>();
		fileNodes = new ArrayList<RDataInstanceNode>();
		index = 1;
		builder = new WorkflowGraphBuilder();
		wf = new Workflow(builder);

		// Setup and Initialization
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();

		// Read in the hashtable and find nodes with matching hashes.
		try {
			ArrayList<String[]> entries = new ArrayList<String[]>();
			readHashtable(entries);
			generateFileNodes(entries, builder);
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
		}


		JMenu scriptMenu = (JMenu) e.getSource();
		scriptMenu.removeAll();
		for (ScriptNode scrnode : scrnodes) {
			JMenuItem scriptItem = new JMenuItem(scrnode.getFullpath());
			scriptItem.addActionListener((ActionEvent event) -> {
				load(ddgExplorer, scrnode);
			}
					);
			scriptMenu.add(scriptItem);
		}
	}

	/**
	 * Loads the workflow into DDGExplorer
	 * 
	 * @param ddgExplorer the relevant instance of ddgExplorer
	 * @param scrnode the script node which the user selected
	 */
	private void load(DDGExplorer ddgExplorer, ScriptNode scrnode) {
		DDGExplorer.loadingDDG();
		builder.buildNodeAndEdgeTables();
		wf.walkBeginning(builder, scrnode.getId());
		wf.findRoots();
		builder.drawGraph();
		builder.createLegend("R");
		builder.getPanel().addLegend();
		ddgExplorer.addTab(scrnode.getName() + " Workflow", builder.getPanel());
		DDGExplorer.doneLoadingDDG();
	}

	/**
	 * Reads in the hashtable.csv from the user's home/.ddg directory. If this
	 * directory does not exist, a file chooser will be displayed so that the
	 * user can select the location of the hashtable.csv on their system.
	 * 
	 * @param csvmap the ArrayList of string arrays to be modified
	 * @throws IOException
	 */
	private void readHashtable(ArrayList<String[]> csvmap) throws IOException {
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		if (!hashtable.exists()) {
			return;
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

	/**
	 * Generates the file nodes to be used in the workflow. It also makes calls to
	 * generateScriptNode.
	 * 
	 * @param entries a series of string arrays obtained from reading the hashtable.csv file.
	 * @param builder a workflowGraphBuilder
	 * @return a list of file nodes to add to the graph.
	 */
	private ArrayList<RDataInstanceNode> generateFileNodes(ArrayList<String[]> entries, WorkflowGraphBuilder builder) {
		for (String[] match : entries) {
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
					file.addNode(scrnode.getId(), "output");
					scrnode.addNode(file.getId(), "input");
					wf.addFile(file);
					wf.addEdge("SFR", file.getId(), scrnode.getId());
				} else if (match[6].equals("write")) {
					file.addNode(scrnode.getId(), "input");
					scrnode.addNode(file.getId(), "output");
					wf.addFile(file);
					wf.addEdge("SFW", scrnode.getId(), file.getId());
				}
			} else {
				if (match[6].equals("read")) {
					fileNodes.get(foundindex).addNode(file.getId(), "output");
					scrnode.addNode(fileNodes.get(foundindex).getId(), "input");
					wf.addFile(file);
					wf.addEdge("SFR", fileNodes.get(foundindex).getId(), scrnode.getId());
				} else if (match[6].equals("write")) {
					fileNodes.get(foundindex).addNode(file.getId(), "input");
					scrnode.addNode(fileNodes.get(foundindex).getId(), "output");
					wf.addFile(file);
					wf.addEdge("SFW", scrnode.getId(), fileNodes.get(foundindex).getId());
				}
			}
		}
		return fileNodes;
	}

	/**
	 * Generates a script node based off of a collection of information
	 * 
	 * @param scrnodes the list of script nodes
	 * @param file the name of the file
	 * @param path the full path to the file
	 * @param json the location of the associated ddg.json file
	 * @return the script node that was generated
	 */
	private ScriptNode generateScriptNode(ArrayList<ScriptNode> scrnodes, RDataInstanceNode file, String path, String json) {
		String name = path.substring(path.lastIndexOf('/') + 1);
		ScriptNode ret = new ScriptNode(0.0, name, json, path);
		if (scrnodes.size() == 0) {
			ScriptNode toAdd = new ScriptNode(0.0, name, json, path);
			toAdd.setId(index++);
			wf.addScript(toAdd);
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
				wf.addScript(toAdd);
				scrnodes.add(toAdd);
				ret = toAdd;
			}
		}
		return ret;
	}
}
