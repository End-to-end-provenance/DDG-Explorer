package laser.ddg.diff.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import laser.ddg.diff.GraphComp;
import laser.ddg.gui.DBBrowser;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.ScriptBrowser;
import laser.ddg.persist.JenaLoader;

/**
 * The panel used for the Explorer tabs that show differences between 2 DDGs
 * side-by-side with unified scrolling.
 *
 * @author Hafeezul Rahman
 * @version July 29, 2016
 *
 */
public class DDGDiffTab extends JPanel {

	// Object that can load information from the database
	private JenaLoader jenaLoader;

	// The object used to load R scripts that are not in the database
	private static JFileChooser chooser;

	// The panel that shows the side-by-side files and their differences
	private DDGDiffPanel diffPanel = new DDGDiffPanel();

	// The file shown on the left side
	private File leftFile;

	// The file shown on the right side
	private File rightFile;

	// The button used to select the left file from the file system
	private JButton selectFile1Button = new JButton("Select from file");

	// The button used to select the left file from the database
	private JButton selectFromDB1Button = new JButton("Select from database");

	// The button used to select the right file from the file system
	private JButton selectFile2Button = new JButton("Select from file");

	// The button used to select the right file from the database
	private JButton selectFromDB2Button = new JButton("Select from database");

	// The field to display the left file name
	JTextField leftFileField = new JTextField();

	// The field to display the right file name
	JTextField rightFileField = new JTextField();

	// references for pop-ups
	private JFrame frame;

	/**
	 * Create the window that allows the user to select files to compare and to
	 * see the results of the comparison
	 * 
	 * @param frame
	 * @param jenaLoader
	 *            the object that reads from the database
	 */
	public DDGDiffTab(JFrame frame, JenaLoader jenaLoader) {
		super(new BorderLayout());
		this.frame = frame;
		this.jenaLoader = jenaLoader;

		JPanel leftPanel = createButtonPanel(selectFile1Button, selectFromDB1Button, leftFileField, "Left file");
		JPanel rightPanel = createButtonPanel(selectFile2Button, selectFromDB2Button, rightFileField, "Right file");

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 2, 8, 0));
		northPanel.add(leftPanel);
		northPanel.add(rightPanel);
		add(northPanel, BorderLayout.NORTH);
		add(diffPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a panel containing a button to select from a file, a button to
	 * select from a database, and a field to display the name of the selected
	 * file
	 * 
	 * @param selectFileButton
	 * @param selectDdgButton
	 * @param fileField
	 * @param panelLabel
	 * @return the panel constructed
	 */
	private JPanel createButtonPanel(JButton selectFileButton, JButton selectFromDBButton, JTextField fileField,
			String panelLabel) {
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel topRow = new JPanel();
		selectFileButton.addActionListener((ActionEvent e) -> {
			try {
				selectFile(e.getSource());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(buttonPanel, "Unable to load file: " + e1.getMessage(),
						"Error loading file", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		});
		topRow.add(selectFileButton);

		selectFromDBButton.addActionListener((ActionEvent e) -> {
			try {
				selectFromDB(e.getSource());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(buttonPanel, "Unable to load file from the database: " + e1.getMessage(),
						"Error loading file from the database", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		});
		topRow.add(selectFromDBButton);
		buttonPanel.add(topRow);

		fileField.setEditable(false);
		buttonPanel.add(fileField);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(panelLabel));
		return buttonPanel;
	}

	/**
	 * Selects a file and displays its filename. If both files have been
	 * selected, the file contents are displayed and the diff is executed, with
	 * the results displayed.
	 * 
	 * @param button
	 *            the button clicked. We need this to determine if we are
	 *            setting the left or right file
	 */
	private void selectFile(Object button) throws IOException {
		if (chooser == null) {
			chooser = new JFileChooser(System.getProperty("user.home"));
		}

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (button == selectFile1Button) {
				selectLeftFile(selectedFile, selectedFile.getAbsolutePath());
			} else {
				selectRightFile(selectedFile, selectedFile.getAbsolutePath());
			}
			if (leftFile != null && rightFile != null) {
				GraphComp.doDiff(diffPanel, leftFile, rightFile);
			}
		}
	}

	/**
	 * Set the information for the right file
	 * 
	 * @param f
	 *            the file selected.
	 */
	private void selectRightFile(File f, String nameToDisplay) {
		if (f == null) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "Could not open " + f);
			return;
		}
		rightFile = f;
		rightFileField.setText(nameToDisplay);

		selectFile2Button.setEnabled(false);
		selectFromDB2Button.setEnabled(false);
	}

	/**
	 * Set the information for the left file
	 * 
	 * @param f
	 *            the file selected
	 */
	private void selectLeftFile(File f, String nameToDisplay) {
		if (f == null) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "Could not open " + f);
			return;
		}
		leftFile = f;
		leftFileField.setText(nameToDisplay);

		selectFile1Button.setEnabled(false);
		selectFromDB1Button.setEnabled(false);
	}

	/**
	 * Displays a DDG Browser that allows the user to select a file from the
	 * databsae. Displays its filename in the text field. If both files have
	 * been selected, the file contents are displayed and the diff is executed,
	 * with the results displayed.
	 * 
	 * @param button
	 *            the button clicked. We need this to determine if we are
	 *            setting the left or right file
	 */
	private void selectFromDB(final Object button) {
		final JDialog selectFrame = new JDialog(frame, "Select from Database", true);

		final DBBrowser browser = new ScriptBrowser(jenaLoader);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((ActionEvent e) -> {
			selectFrame.dispose();
		});

		JButton openButton = new JButton("Open");
		openButton.addActionListener((ActionEvent e) -> {
			String selectedScript = browser.getSelectedFile().getName();
			String timestamp = browser.getSelectedTimestamp();
			File selectedFile = new File(
					browser.getSelectedFile().getParent() + "/" + browser.getSelectedTimestamp() + "/ddg.txt");
			selectFrame.dispose();
			if (button == selectFromDB1Button) {
				selectLeftFile(selectedFile, selectedScript + " " + timestamp);
			} else {
				selectRightFile(selectedFile, selectedScript + " " + timestamp);
			}
			if (leftFile != null && rightFile != null) {
				try {
					GraphComp.doDiff(diffPanel, leftFile, rightFile);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(selectFrame, "Unable to load ddg: " + e1.getMessage(),
							"Error loading file", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
		buttonPanel.setBorder(padding);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);

		selectFrame.add(browser, BorderLayout.CENTER);
		selectFrame.add(buttonPanel, BorderLayout.SOUTH);
		selectFrame.pack();
		selectFrame.setLocationRelativeTo(this);
		selectFrame.setVisible(true);
	}

}

