package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaWriter;
import laser.ddg.persist.Parser;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Command to load a DDG from a file and display it in a tab.
 * Pops up a file chooser to allow the user to select a file.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 *
 */
public class LoadFileCommand implements ActionListener {
	
	private static final JFileChooser FILE_CHOOSER = new JFileChooser(System.getProperty("user.home"));

	/**
	 * Loads a ddg text file from user selection
	 * @throws Exception thrown if the file cannot be loaded
	 */
	public static void execute() throws Exception {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		if (FILE_CHOOSER.showOpenDialog(ddgExplorer) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = FILE_CHOOSER.getSelectedFile();
			loadFile(selectedFile);
		}
	}
	
	/**
	 * Loads a ddg text file form the path it is stored
	 * @param ddgtxtPath
	 * @throws Exception thrown if the file cannot be loaded 
	 */
	public static void loadDDG(String ddgtxtPath) throws Exception{
		loadFile(new File(ddgtxtPath));
	}

	
	/**
	 * loads a file that contains ddg
	 * @param selectedFile
	 * @throws Exception
	 */
	public static void loadFile(File selectedFile) throws Exception {
		JenaWriter jenaWriter = JenaWriter.getInstance();
		PrefuseGraphBuilder builder = new PrefuseGraphBuilder(false, jenaWriter);
		String selectedFileName = selectedFile.getName();
		DDGExplorer.loadingDDG();
		builder.processStarted(selectedFileName, null);
		Parser parser = Parser.createParser(selectedFile, builder);
		parser.addNodesAndEdges();
		
		//new tab!
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
		DDGExplorer.doneLoadingDDG();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			execute();
		} catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to load the file: " + e.getMessage(),
					"Error loading file", JOptionPane.ERROR_MESSAGE);
		}
	}
}
