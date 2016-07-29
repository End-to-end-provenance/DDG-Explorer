package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import laser.ddg.ClientConnection;
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
	private static JenaWriter jenaWriter = JenaWriter.getInstance();
	private static DDGExplorer ddgExplorer = DDGExplorer.getInstance();

	/**
	 * Loads a ddg text file from user selection
	 * @throws Exception thrown if the file cannot be loaded
	 */
	public static void execute() throws Exception {
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
	public static void loadFile(File selectedFile) throws Exception{
		PrefuseGraphBuilder builder = new PrefuseGraphBuilder(false, jenaWriter);
		String selectedFileName = selectedFile.getName();
		Parser parser = new Parser(selectedFile, builder);
		startLoadingDDG(builder,selectedFileName);
		parser.addNodesAndEdges();
		finishLoadingDDG(builder);
	}

	/**
	 * for incremental drawing, build everything using information from the client side 
	 * @param clientConnection
	 * @throws Exception
	 */
	public static void executeIncrementalDrawing(ClientConnection clientConnection) throws Exception {
		PrefuseGraphBuilder builder = new PrefuseGraphBuilder(true, jenaWriter,clientConnection.getClientReader());
		String selectedFileName = clientConnection.getFileName();
		Parser parser = new Parser(clientConnection, builder);
		builder.setTitle(selectedFileName, clientConnection.getTimeStamp());
		startLoadingDDG(builder,selectedFileName);
		finishLoadingDDG(builder);
		parser.addNodesAndEdges();		
	}


	/**
	 * preparation to start loading DDG
	 * @param builder
	 * @param parser
	 * @param fileName
	 * @throws IOException
	 */
	public static void startLoadingDDG(PrefuseGraphBuilder builder,String fileName) throws IOException{
		DDGExplorer.loadingDDG();
		builder.processStarted(fileName, null);
	}
	
	/**
	 * process to finish loading DDG
	 * @param builder
	 */
	public static void finishLoadingDDG(PrefuseGraphBuilder builder){
		//new tab!
		ddgExplorer.addTab(builder.getPanel().getName(), builder.getPanel());
		DDGExplorer.doneLoadingDDG();
	
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
