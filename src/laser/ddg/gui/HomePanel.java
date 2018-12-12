package laser.ddg.gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import laser.ddg.commands.CompareGraphsCommand;
import laser.ddg.commands.CompareScriptsCommand;
import laser.ddg.commands.LoadFileCommand;

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

		// Create a button to allow the user to compare 2 R scripts
		JButton compareButton = new JButton("Compare R Scripts");
		compareButton.addActionListener(new CompareScriptsCommand());

		// Create a button to allow the user to compare 2 DDGs
		JButton compareDDGButton = new JButton("Compare DDGs");
		compareDDGButton.addActionListener(new CompareGraphsCommand());

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(loadFileButton);
		buttonPanel.add(compareButton);
		buttonPanel.add(compareDDGButton);
		add(buttonPanel, BorderLayout.PAGE_START);

	}
}
