package laser.ddg.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;

import laser.ddg.gui.DDGExplorer;

/**
 * Creates a window that allows the user to view a file.  For csv, text and jpeg
 * files, it creates its own Java components and window to display them in.  For
 * all other file types, it tries to use an application provided by the platform.
 * 
 * @author Barbara Lerner
 * @version Oct 30, 2013
 *
 */
public class FileViewer {
	// The full path and file name to display
	private String path;
	
	// The timestamp to put in the window header
	private String timestamp;
	
	// A table that displays the contents of a csv file
	private JTable fileTable;
	
	// A scroll pane containing the file contents
	private JScrollPane scroll;
	
	// Component to hold an image
	private JLabel plotted;
	
	// The contents to display.  The exact type of this depends on the file type.
	private JComponent contents;
	
	/**
	 * Create the structures needed to hold the file contents.  This does not 
	 * display the file.
	 * @param path the full path to the file
	 * @param time the timestamp.  If the file is a text, jpeg or csv file, the 
	 *    timestamp will be displayed in the window title.  For other file types,
	 *    the timestamp is not used.  
	 */
	public FileViewer(String path, String time) {
		if (path.startsWith("\"") && path.endsWith("\"")) {
			this.path = path.substring(1, path.length()-1);
		}
		else {
			this.path = path;
		}
		
		if (time == null) {
			DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date fileDate = new Date (new File(path).lastModified());
			this.timestamp = dFormat.format(fileDate);
		}
		else {
			this.timestamp = time;
		}
		
		if (path.endsWith(".csv")) {
			createTable();
		}
		else if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")){
			displayImage();
		}
		else if (path.endsWith(".txt") || path.endsWith(".r")) {
			displayText();
		}
		else if (path.endsWith(".htm") || path.endsWith(".html") || path.startsWith("http")){
			// Nothing to do.  Will launch browser
		}
		else { // pdf or any other type of file
			// Nothing to do.  Will try to launch a platform application  
		}
	}
	
	/**
	 * Creates an un-editable text area containing the file contents.
	 * The file should contain text.
	 */
	private void displayText() {
		JTextArea text = new JTextArea();
		try{
			//get and read in the file
			File theFile = new File(path);
			Scanner readFile = new Scanner(theFile);
			
			while(readFile.hasNextLine()){
				String line = readFile.nextLine();
				text.append(line + "\n");
			}
			
			readFile.close();

			text.setEditable(false);
			text.setCaretPosition(0);
			contents = new JScrollPane(text,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
		}catch (Exception ex){
			// Catch block that will print out exception
			DDGExplorer.showErrMsg("Error with file. "+ ex.getMessage());
			ex.printStackTrace();
		}		
		
	}

	/**
	 * Method to display the given URL value to user's default browser
	 * 
	 * @param urlString the url of the web page to display
	 */
	private void displayURL() {
		Desktop desktop = null;
		// Make sure Desktop API is supported
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
			URI uri;
			try {
				// create a URI with the given URL value and open in default
				// browser
				uri = new URI(path);
				desktop.browse(uri);
			} catch (URISyntaxException e1) {
				// Catch block that will print out exception for URI
				JOptionPane.showMessageDialog(null, "Error with URL. " + e1.getMessage());
				e1.printStackTrace();
			} catch (IOException e2) {
				// Catch block that will print out exception for IO when
				// opening browser
				JOptionPane.showMessageDialog(null, "Error loading URL. " + e2.getMessage());
				e2.printStackTrace();
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Unable to run browser.");
		}
	}
	
	/**
	 * Display a file using the operating system's native viewer for that file type
	 */
	private void displayNatively() {
		Desktop desktop = null;
		// Make sure Desktop API is supported
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
			try {
		        File myFile = new File(path);
		        desktop.open(myFile);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Error loading file. " + ex.getMessage());
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Unable to run native viewer.");
		}
	}

	/**
	 * Builds a JTable for the contents of a csv file.  Assumes the file 
	 * contains comma-separated values and the first row of the file contains
	 * column names.
	 */
	private void createTable() {
		try{
			//get and read in the file
			File theFile = new File(path);
			Scanner readFile = new Scanner(theFile);
			
			//The first line will be the column headings
			String[] colNames = getColumnNames(readFile.nextLine());

			ArrayList<String[]> rowData = readRows(readFile);
			readFile.close();

			//Create table without warning border
			createFileTable(colNames, rowData);
			scroll = new JScrollPane(fileTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			contents = scroll;
			
		}catch (Exception ex){
			// Catch block that will print out exception
			DDGExplorer.showErrMsg("Error with file. "+ ex.getMessage());
			ex.printStackTrace();
		}		

	}
	
	/**
	 * Get the column names from the line
	 * @param line the line that holds column names
	 * @return an array with one entry for each column name
	 */
	private static String[] getColumnNames(String line) {
		String cols = line.replace('\"', ' ');
		String[] colNames = cols.split("[,]");
		return colNames;
	}

	/**
	 * Read the rows of data from a csv file
	 * @param readFile the scanner for the file we are reading from
	 * @return an ArrayList with one entry per row.  Each entry contains
	 * 		an array of strings, one for each value in the row
	 */
	private static ArrayList<String[]> readRows(Scanner readFile) {
		//This will hold all the row information
		ArrayList<String[]> rowData= new ArrayList<String[]>(); 
		
		//each row takes up one line, each value is split by ","
		while(readFile.hasNextLine()){
			String rowLine = readFile.nextLine();
			String[] rowInfo = rowLine.split("[,]");
			rowData.add(rowInfo);
		}
		return rowData;
	}

	/**
	 * Add a bright border around the table
	 * @param c the color of the border to add
	 */
	public void addBorder(Color c) {
		Border border = BorderFactory.createLineBorder(c);
		
		if (plotted == null) {
			fileTable.setBorder(border);
			JTableHeader header = fileTable.getTableHeader();
			header.setBorder(border);
			scroll.setBorder(border);
		}
		else {
			plotted.setBorder(border);
		}
	}

	/**
	 * Create the JTable that holds the values from the file
	 * @param colNames the column names
	 * @param rowData the data from the file
	 */
	private void createFileTable(String[] colNames,
			ArrayList<String[]> rowData) {
		Object[][] rows = rowData.toArray(new Object[rowData.size()][colNames.length]);
		fileTable = new JTable(rows, colNames);
		fileTable.doLayout();
		fileTable.setShowGrid(true);
		fileTable.setShowHorizontalLines(true);
		fileTable.setShowVerticalLines(true);
		fileTable.setGridColor(Color.black);
		fileTable.setEnabled(false);
		fileTable.setFillsViewportHeight(true);
	}

	/**
	 * Creates a component that displays an image.  Assumes that the
	 * file contains an image.
	 */
	private void displayImage() {
		// clear out the image buffer
		ImageIcon icon = new ImageIcon(path);
		icon.getImage().flush();
		
		
		plotted = new JLabel(icon);
		plotted.setHorizontalAlignment(JLabel.CENTER);
		contents = plotted;
	}
	
	/**
	 * Displays the table in a new window
	 */
	public void displayFile() {
		if (path.endsWith(".html") || path.endsWith(".htm") || path.startsWith("http")) {
			displayURL();
		}
		
		else if (path.endsWith(".csv") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".txt") || path.endsWith(".r")) {
			//find the last '/' in the path name since the file name to be viewed will be after that
			int startAt = path.lastIndexOf('/') + 1;
			int len = path.length();
			
			//use the index and go from there to the end
			String title = path.substring(startAt, len);
			
			// Open up the file in a table in a new frame
			JFrame fileFrame = new JFrame();
			
			//set the title to the name of the file 
			fileFrame.setTitle(title + " " + timestamp);
			fileFrame.setSize(new Dimension(500, 500));
			
			//Add the table to the frame
			fileFrame.getContentPane().add(contents, BorderLayout.CENTER);
			fileFrame.setLocationByPlatform(true);
			fileFrame.setVisible(true);
		}
		
		else {
			// pdfs and other files not handled above will try to use 
			// a platform-specific application
			displayNatively();
			return;
		}
		

	}

}
