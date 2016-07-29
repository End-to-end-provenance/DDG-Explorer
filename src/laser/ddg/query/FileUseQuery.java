package laser.ddg.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import laser.ddg.FileInfo;
import laser.ddg.commands.LoadFromDBCommand;
import laser.ddg.gui.DDGBrowser;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaLoader;
import laser.ddg.visualizer.FileViewer;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * A query that allows the user to search for files used within a DDG database.
 * There are a variety of options that the user can set via the gui: - input
 * files, output files or both - file extension - search entire db, all ddgs for
 * one script, or one ddg
 * 
 * The results are displayed in a table showing file name, node name, script and
 * timestamp. The user can then select a file and operate on it.
 * 
 * @author Barbara Lerner
 * @version Oct 24, 2013
 *
 */
public class FileUseQuery extends AbstractQuery {
	// How the file is used
	private static final int INPUT = 0;
	private static final int OUTPUT = 1;
	private static final int IO = 2;

	private JPanel mainPanel;

	// The results returned by the query
	private Collection<FileInfo> filenames;

	// The script that the user wants to search or null if searching entire db
	private String selectedScript;

	// The timestamp of the ddg to search or null if the user is searching
	// entire db
	// or all ddgs from one script
	private String selectedTimestamp;

	// The field containing the ddg or script to search
	private JTextField ddgField;

	// The button indicating that the entire database should be searched
	private JRadioButton dbButton;

	// The button that allows the user to select a script or ddg to search
	private JRadioButton ddgButton;

	// The panel where the search results are displayed.
	private JPanel resultsPanel;

	private static final DDGExplorer ddgExplorer = DDGExplorer.getInstance();

	/**
	 * @return the command name for the query
	 */
	@Override
	public String getMenuItem() {
		return "Find File Uses";
	}

	/**
	 * Execute the query. This will create a panel (in a tab) allowing the user
	 * to select options. Hitting ok in the panel causes the query to run.
	 *
	 * @param dbLoader
	 * @param processName
	 * @param timestamp
	 * @param visualization
	 */
	@Override
	public void performQuery(final JenaLoader dbLoader, String processName, String timestamp, Component visualization) {
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setName("Find Data Files");
		JPanel searchPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		ActionListener dimResultsListener = (ActionEvent e) -> {
			disableFileTable();
		};

		// Allows user to select input, output, or both types of files
		JLabel ioLabel = new JLabel("File use as:  ", JLabel.RIGHT);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_END;
		constraints.insets = new Insets(10, 10, 0, 0);
		searchPanel.add(ioLabel, constraints);

		JPanel ioPanel = new JPanel();
		ioPanel.setLayout(new BoxLayout(ioPanel, BoxLayout.X_AXIS));
		final JRadioButton inputButton = new JRadioButton("Input");
		final JRadioButton outputButton = new JRadioButton("Output");
		final JRadioButton ioButton = new JRadioButton("Both");
		ioButton.setSelected(true);
		final ButtonGroup ioButtons = new ButtonGroup();
		ioButtons.add(inputButton);
		ioButtons.add(outputButton);
		ioButtons.add(ioButton);
		inputButton.addActionListener(dimResultsListener);
		outputButton.addActionListener(dimResultsListener);
		ioButton.addActionListener(dimResultsListener);
		ioPanel.add(inputButton);
		ioPanel.add(outputButton);
		ioPanel.add(ioButton);
		ioPanel.add(Box.createGlue());
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(10, 0, 0, 10);
		searchPanel.add(ioPanel, constraints);

		// Allows user to select by file extension
		JLabel fileTypeLabel = new JLabel("File type: ", JLabel.RIGHT);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.anchor = GridBagConstraints.LINE_END;
		constraints.insets = new Insets(0, 10, 0, 0);
		searchPanel.add(fileTypeLabel, constraints);

		JPanel fileTypePanel = new JPanel();
		fileTypePanel.setLayout(new BoxLayout(fileTypePanel, BoxLayout.X_AXIS));
		final JCheckBox csvBox = new JCheckBox("csv");
		final JCheckBox jpegBox = new JCheckBox("jpg");
		final JCheckBox txtBox = new JCheckBox("txt");
		final JCheckBox pdfBox = new JCheckBox("pdf");
		final JCheckBox htmlBox = new JCheckBox("html");
		final JCheckBox allBox = new JCheckBox("all");
		allBox.setSelected(true);

		ItemListener deselectAllListener = (ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				allBox.setSelected(false);
				disableFileTable();
			}
		};
		csvBox.addItemListener(deselectAllListener);
		jpegBox.addItemListener(deselectAllListener);
		txtBox.addItemListener(deselectAllListener);
		pdfBox.addItemListener(deselectAllListener);
		htmlBox.addItemListener(deselectAllListener);

		allBox.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				csvBox.setSelected(false);
				jpegBox.setSelected(false);
				txtBox.setSelected(false);
				pdfBox.setSelected(false);
				htmlBox.setSelected(false);
				disableFileTable();
			}
		});

		fileTypePanel.add(csvBox);
		fileTypePanel.add(jpegBox);
		fileTypePanel.add(txtBox);
		fileTypePanel.add(pdfBox);
		fileTypePanel.add(htmlBox);
		fileTypePanel.add(allBox);
		fileTypePanel.add(Box.createGlue());
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(0, 0, 0, 10);
		searchPanel.add(fileTypePanel, constraints);

		// Allows user to search entire db, ddgs for one script, or one ddg
		JLabel searchScopeLabel = new JLabel("Search: ", JLabel.RIGHT);
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.anchor = GridBagConstraints.FIRST_LINE_END;
		constraints.insets = new Insets(0, 10, 0, 0);
		searchPanel.add(searchScopeLabel, constraints);

		JPanel searchScopePanel = new JPanel();
		searchScopePanel.setLayout(new GridBagLayout());
		dbButton = new JRadioButton("Entire database");
		dbButton.setSelected(true);
		dbButton.addActionListener((ActionEvent e) -> {
			ddgField.setText("");
			selectedScript = null;
			selectedTimestamp = null;
			disableFileTable();
		} // Sets to search entire db
		);

		ddgButton = new JRadioButton("Select script or ddg");
		ddgField = new JTextField(30);
		ddgField.setEditable(false);
		ddgButton.addActionListener((ActionEvent e) -> {
			// Modal dialog. selectedDDG will be set on return.
			selectDDG(ddgExplorer, dbLoader);
			disableFileTable();
		} // Popus up a window so the user can select a script or ddg to search
		);
		ButtonGroup searchScopeGroup = new ButtonGroup();
		searchScopeGroup.add(dbButton);
		searchScopeGroup.add(ddgButton);

		GridBagConstraints searchConstraints = new GridBagConstraints();
		searchConstraints.gridx = 0;
		searchConstraints.gridy = 0;
		searchScopePanel.add(dbButton, searchConstraints);
		searchConstraints.gridx = 1;
		searchConstraints.gridy = 0;
		searchScopePanel.add(ddgButton, searchConstraints);
		searchConstraints.gridx = 1;
		searchConstraints.gridy = 1;
		searchScopePanel.add(ddgField, searchConstraints);
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(0, 0, 0, 10);
		searchPanel.add(searchScopePanel, constraints);

		// Search button causes query to execute
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener((ActionEvent e) -> {
			try {
				int selectedIO;
				if (inputButton.isSelected()) {
					selectedIO = INPUT;
				} else if (outputButton.isSelected()) {
					selectedIO = OUTPUT;
				} else {
					selectedIO = IO;
				}

				ArrayList<String> selectedExtensions = new ArrayList<>();
				if (!allBox.isSelected()) {
					if (csvBox.isSelected()) {
						selectedExtensions.add(".csv");
					}
					if (jpegBox.isSelected()) {
						selectedExtensions.add(".jpg");
						selectedExtensions.add(".jpeg");
					}
					if (txtBox.isSelected()) {
						selectedExtensions.add(".txt");
					}
					if (pdfBox.isSelected()) {
						selectedExtensions.add(".pdf");
					}
					if (htmlBox.isSelected()) {
						selectedExtensions.add(".htm");
						selectedExtensions.add(".html");
					}
				}
				displayFilenames(dbLoader, selectedIO, selectedExtensions);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "Unable to search for file uses.\n");
				JOptionPane.showMessageDialog(DDGExplorer.getInstance(), e1 + "\n");
				// e1.printStackTrace();
			}
		} // Look up the options the user set and execute the query
		);

		buttonPanel.add(Box.createGlue());
		buttonPanel.add(searchButton);
		constraints.gridy = 3;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(10, 0, 10, 10);
		searchPanel.add(buttonPanel, constraints);
		mainPanel.add(searchPanel, BorderLayout.NORTH);

		// new tab!
		ddgExplorer.addTab(mainPanel.getName(), mainPanel);
	}

	/**
	 * @return the files found by the query
	 */
	public Collection<FileInfo> getFilenames() {
		return filenames;
	}

	/**
	 * Displays a window with a ddg browser in it
	 * 
	 * @param parent
	 *            the parent window, used to control where this window pops up
	 * @param dbLoader
	 *            the object that can read from the db
	 */
	private void selectDDG(JFrame parent, JenaLoader dbLoader) {
		final JDialog f = new JDialog(parent, "Select from Database", true);
		final DDGBrowser browser = new DDGBrowser(dbLoader);
		f.add(browser, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
		buttonPanel.setBorder(padding);
		JButton openButton = new JButton("Open");
		openButton.addActionListener((ActionEvent e) -> {
			selectedScript = browser.getSelectedProcessName();
			selectedTimestamp = browser.getSelectedTimestamp();
			String selectedDDG;
			if (selectedTimestamp == null) {
				selectedDDG = selectedScript;
			} else {
				selectedDDG = selectedScript + File.separator + selectedTimestamp;
			}
			ddgField.setText(selectedDDG);
			dbButton.setSelected(false);
			f.dispose();
		} // Sets the script and timestamp selected by the user
		);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((ActionEvent e) -> {
			dbButton.setSelected(true);
			f.dispose();
		} // Reverts to searching entire db
		);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);
		f.add(buttonPanel, BorderLayout.SOUTH);
		f.pack();
		f.setLocationRelativeTo(parent);
		f.setVisible(true);
	}

	/**
	 * Displays the files found in a table
	 * 
	 * @param dbLoader
	 *            the object that can read from the db
	 * @param selectedIO
	 *            whether to return input files, output files or both
	 * @param selectedExtensions
	 *            the extensions limiting which types of files to return. An
	 *            empty list means all file extensions
	 */
	private void displayFilenames(JenaLoader dbLoader, int selectedIO, ArrayList<String> selectedExtensions) {
		try {
			resultsPanel = new JPanel(new BorderLayout());
			resultsPanel.setName("Data Files");
			Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
			resultsPanel.setBorder(padding);

			// Get files from the db, restricting to input, output or all based
			// on parameter passed in
			switch (selectedIO) {
			case INPUT:
				filenames = dbLoader.getInputFileNames();
				break;

			case OUTPUT:
				filenames = dbLoader.getOutputFileNames();
				break;

			default:
				filenames = dbLoader.getAllFileNames();
			}

			Collection<FileInfo> files = filenames;

			// Build the table of results
			final String[] columns = { "File name", "Script", "DDG Timestamp", "Node name" };
			final String[][] data = new String[files.size()][columns.length + 1];
			int row = 0;
			for (FileInfo file : files) {
				String filename = file.getFilename();
				// ErrorLog.showErrMsg("filename = " + filename + "\n");
				String extension = filename.substring(filename.lastIndexOf("."));
				// ErrorLog.showErrMsg("extension = " + extension + "\n");

				// Filter based on file extension
				if (selectedExtensions.isEmpty() || selectedExtensions.contains(extension)) {
					String script = file.getProcess();
					// ErrorLog.showErrMsg("script = " + script + "\n");

					// Filter based on script
					if (selectedScript == null || selectedScript.equals(script)) {
						String ddgTimestamp = file.getDdgTimestamp();
						// ErrorLog.showErrMsg("ddg timestamp = " + ddgTimestamp
						// + "\n");

						// Filter based on timestamp
						if (selectedTimestamp == null || selectedTimestamp.equals(ddgTimestamp)) {
							data[row][0] = filename;
							data[row][1] = script;
							data[row][2] = ddgTimestamp;
							data[row][3] = file.getNodename();
							// ErrorLog.showErrMsg("nodename = " + data[row][3]
							// + "\n");
							data[row][4] = file.getPath();
							// ErrorLog.showErrMsg("path = " + data[row][4] +
							// "\n");
							row++;
						}
					}
				}
			}

			final JTable fileTable = new JTable(new FileTableModel(columns, data, row));

			// Allows sorting by column values
			fileTable.setAutoCreateRowSorter(true);

			// Makes the script column wider than the others
			TableColumn scriptColumn = fileTable.getColumn("Script");
			scriptColumn.setPreferredWidth(250);

			JScrollPane fileScroller = new JScrollPane(fileTable);
			fileTable.setFillsViewportHeight(true);

			final JButton showFileButton = new JButton("Show File(s)");
			final JButton showDdgButton = new JButton("Show DDG(s)");
			// final JButton compareFilesButton = new JButton("Compare
			// File(s)");
			showFileButton.setEnabled(false);
			showDdgButton.setEnabled(false);
			// compareFilesButton.setEnabled(false);
			JPanel buttonPanel = new JPanel(); // use FlowLayout
			buttonPanel.add(showFileButton);
			buttonPanel.add(showDdgButton);
			// buttonPanel.add(compareFilesButton);

			fileTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
				int numSelected = fileTable.getSelectedRowCount();
				if (numSelected == 0) {
					showFileButton.setEnabled(false);
					showDdgButton.setEnabled(false);
					// compareFilesButton.setEnabled(false);
				} else if (numSelected >= 1) {
					showFileButton.setEnabled(true);
					showDdgButton.setEnabled(true);
					// if (numSelected == 2) {
					// compareFilesButton.setEnabled(true);
					// }
					// else {
					// compareFilesButton.setEnabled(false);
					// }
				}
			});

			showFileButton.addActionListener((ActionEvent e) -> {
				int[] selectedRows = fileTable.getSelectedRows();
				FileTableModel data1 = (FileTableModel) fileTable.getModel();
				for (int row1 : selectedRows) {
					int modelRow = fileTable.convertRowIndexToModel(row1);
					String selectedFile = data1.getFileAt(modelRow);
					try {
						FileViewer viewer = new FileViewer(selectedFile, data1.getTimeAt(modelRow));
						viewer.displayFile();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "Could not find " + selectedFile);
					}
				}
			});

			showDdgButton.addActionListener((ActionEvent e) -> {
				try {
					int[] selectedRows = fileTable.getSelectedRows();
					FileTableModel data1 = (FileTableModel) fileTable.getModel();
					for (int row1 : selectedRows) {
						int modelRow = fileTable.convertRowIndexToModel(row1);
						// ErrorLog.showErrMsg("Selected script = " +
						// data.getScriptAt(modelRow) + "\n");
						// ErrorLog.showErrMsg("Selected timestamp = " +
						// data.getTimeAt(modelRow) + "\n");
						PrefuseGraphBuilder graphBuilder = LoadFromDBCommand
								.loadDDGFromDB(data1.getScriptAt(modelRow), data1.getTimeAt(modelRow));
						graphBuilder.drawFullGraph();
						// ErrorLog.showErrMsg("Focusing on " +
						// data.getNodeNameAt(modelRow));
						graphBuilder.focusOn(data1.getNodeNameAt(modelRow));
					}
				} catch (Exception e1) {
					String msg = "Unable to show where the file is used.\n";
					msg = msg + e1;
					JOptionPane.showMessageDialog(DDGExplorer.getInstance(), msg);
					// e1.printStackTrace();
				}
			} /**
				 * Opens a window displaying the DDG that uses the selected
				 * file. The display centers on the file node. If more than 1
				 * file is selected, it opens multiple windows.
				 */
			);

			resultsPanel.add(fileScroller, BorderLayout.CENTER);
			resultsPanel.add(buttonPanel, BorderLayout.SOUTH);

			// add to content pane and refresh
			Component comp = ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
			if (comp != null) {
				mainPanel.remove(comp);
			}
			mainPanel.add(resultsPanel, BorderLayout.CENTER);
			mainPanel.validate();
			notifyQueryFinished("Find Data Files", resultsPanel);

		} catch (Exception e) {
			String msg = "Unable to display file names.\n";
			msg = msg + e;
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), msg);
			// e.printStackTrace();
		}
	}

	private void disableFileTable() {
		if (resultsPanel != null) {
			resultsPanel.removeAll();
			resultsPanel.invalidate();
			resultsPanel.repaint();
		}
	}

	private class FileTableModel extends AbstractTableModel {
		private String[] columns;
		private String[][] data;
		private int numRows;

		public FileTableModel(String[] columns, String[][] data, int numRows) {
			this.columns = columns;
			this.data = data;
			this.numRows = numRows;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col].toString();
		}

		@Override
		public int getRowCount() {
			// This is the number that passed the filters
			return numRows;
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public String getScriptAt(int row) {
			return data[row][1];
		}

		public String getTimeAt(int row) {
			return data[row][2];
		}

		public String getNodeNameAt(int row) {
			return data[row][3];
		}

		public String getFileAt(int row) {
			return data[row][4];
		}
	}

}
