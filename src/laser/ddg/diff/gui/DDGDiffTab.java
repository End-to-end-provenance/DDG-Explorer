package laser.ddg.diff.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import laser.ddg.diff.GraphComp;
import laser.ddg.gui.DDGExplorer;

/**
 * The panel used for the Explorer tabs that show differences between 2 DDGs
 * side-by-side with unified scrolling.
 *
 * @author Hafeezul Rahman
 * @version July 29, 2016
 *
 */
public class DDGDiffTab extends JPanel {

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

	// The button used to select the right file from the file system
	private JButton selectFile2Button = new JButton("Select from file");

	// The field to display the left file name
	JTextField leftFileField = new JTextField();

	// The field to display the right file name
	JTextField rightFileField = new JTextField();

	/**
	 * Create the window that allows the user to select files to compare and to
	 * see the results of the comparison
	 * 
	 */
	public DDGDiffTab() {
		super(new BorderLayout());

		JPanel leftPanel = createButtonPanel(selectFile1Button, leftFileField, "Left file");
		JPanel rightPanel = createButtonPanel(selectFile2Button, rightFileField, "Right file");

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 2, 8, 0));
		northPanel.add(leftPanel);
		northPanel.add(rightPanel);
		add(northPanel, BorderLayout.NORTH);
		add(diffPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a panel containing a button to select from a file
	 * and a field to display the name of the selected
	 * file
	 * 
	 * @param selectFileButton
	 * @param fileField
	 * @param panelLabel
	 * @return the panel constructed
	 */
	private JPanel createButtonPanel(JButton selectFileButton, JTextField fileField,
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
	}

}
