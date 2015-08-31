package laser.ddg.gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import laser.ddg.commands.CompareScriptsCommand;
import laser.ddg.commands.FindFilesCommand;
import laser.ddg.commands.LoadFileCommand;
import laser.ddg.commands.LoadFromDBCommand;
import laser.ddg.commands.ManageDatabaseCommand;

/**
 * The empty panel that is displayed as the first tab. It contains buttons
 * redundant with the file menu to help the user get started.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 * 
 */
public class HomePanel extends JPanel {
	/**
	 * Creates the home panel and its buttons. 
	 */
	public HomePanel() {
		super(new BorderLayout());

		// Create a button to allow the user to load a DDG from a text file
		JButton loadFileButton = new JButton("Open from file");
		loadFileButton.addActionListener(new LoadFileCommand());

		// Create a button to allow the user to load a DDG from the database
		JButton loadFromDBButton = new JButton("Open from database");
		loadFromDBButton.addActionListener(new LoadFromDBCommand());

		// Create a button to allow the user to load a DDG from the database
		JButton compareButton = new JButton("Compare R Scripts");
		compareButton.addActionListener(new CompareScriptsCommand());

		// Create a button to allow the user to load a DDG from the database
		JButton findFilesButton = new JButton("Find Data Files");
		findFilesButton.addActionListener(new FindFilesCommand());

		// Create a button to allow the user to manage the database
		JButton manageButton = new JButton("Manage Database");
		manageButton.addActionListener(new ManageDatabaseCommand());

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(loadFileButton);
		buttonPanel.add(loadFromDBButton);
		buttonPanel.add(compareButton);
		buttonPanel.add(findFilesButton);
		buttonPanel.add(manageButton);
		add(buttonPanel, BorderLayout.PAGE_START);

	}
}
