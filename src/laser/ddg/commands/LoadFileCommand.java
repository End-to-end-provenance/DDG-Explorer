package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.ServerSocket;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import laser.ddg.DDGServer;
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

	private static final JFileChooser FILE_CHOOSER = new JFileChooser(System.getProperty("user.dir"));
	/**
	 * Loads a text file containing a ddg
	 * @throws Exception thrown if the file cannot be loaded
	 */
	public static void execute() throws Exception {
		JenaWriter jenaWriter = JenaWriter.getInstance();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		if (FILE_CHOOSER.showOpenDialog(ddgExplorer) == JFileChooser.APPROVE_OPTION) {
			PrefuseGraphBuilder builder = new PrefuseGraphBuilder(false, jenaWriter);
			File selectedFile = FILE_CHOOSER.getSelectedFile();
			String selectedFileName = selectedFile.getName();
			DDGExplorer.loadingDDG();
			builder.processStarted(selectedFileName, null);
			Parser parser = new Parser(selectedFile, builder);
			parser.addNodesAndEdges();

			//new tab!
			ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
			DDGExplorer.doneLoadingDDG();
		}
	}


	public static void loadFile(File ddgtxtFile) throws Exception{
		JenaWriter jenaWriter = JenaWriter.getInstance();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		PrefuseGraphBuilder builder = new PrefuseGraphBuilder(false, jenaWriter);
		String ddgtxtFileName = ddgtxtFile.getName();
		DDGExplorer.loadingDDG();
		builder.processStarted(ddgtxtFileName, null);
		Parser parser = new Parser(ddgtxtFile, builder);
		parser.addNodesAndEdges();
		//new tab!
		ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
		DDGExplorer.doneLoadingDDG();
	}


	public static void executeIncrementalDrawing(String fileName, DDGServer ddgSocket) throws Exception {
		JenaWriter jenaWriter = JenaWriter.getInstance();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();

		PrefuseGraphBuilder builder = new PrefuseGraphBuilder(true, jenaWriter);
		String selectedFileName = fileName;
		DDGExplorer.loadingDDG();
		builder.processStarted(selectedFileName, null);
		Parser parser = new Parser(ddgSocket, builder);
		//new tab!
		ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
		DDGExplorer.doneLoadingDDG();
//		parser.addNodesAndEdges();

	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			execute();
		} catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to load the file: " + e.getMessage(),
					"Error loading file", JOptionPane.ERROR_MESSAGE);
		}
	}
}
