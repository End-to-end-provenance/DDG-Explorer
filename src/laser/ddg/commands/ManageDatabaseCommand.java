package laser.ddg.commands;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import laser.ddg.gui.DBBrowserListener;
import laser.ddg.gui.DDGBrowser;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaLoader;

/**
 * Command to allow the user to modify the database.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 *
 */
public class ManageDatabaseCommand implements ActionListener {
	// The process that the user selected.
	private String selectedProcessName;

	// The timestamp for the DDG that the user selected.
	private String selectedTimestamp;

	/**
	 * Displays a window that allows the user load and removed DDGs, and
	 * show Values. On return, the window has been disposed.
	 */
	private void execute() {
		final DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		final JDialog loadFromDBFrame = new JDialog(ddgExplorer, "Manage Database", true);
		final JenaLoader jenaLoader = JenaLoader.getInstance();
		final DDGBrowser dbBrowser = new DDGBrowser(jenaLoader);

		// Create the buttons used to pick an action
		final JButton deleteAllButton = new JButton("Delete all");
		final JButton deleteOneButton = new JButton("Delete DDG");
		final JButton openButton = new JButton("Open DDG");
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			/**
			 * Remove the window on cancel.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFromDBFrame.dispose();
			}

		});

		openButton.addActionListener(new ActionListener() {

			/**
			 * Loads an entire DDG
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					loadFromDBFrame.dispose();
					new LoadFromDBCommand().execute();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(ddgExplorer,
							"Unable to load the DDG: " + e1.getMessage(),
							"Error loading DDG", JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		deleteOneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(loadFromDBFrame,
						"Are you sure that you want to delete this DDG permanently from the database?") == JOptionPane.YES_OPTION) {
					try {
						deleteDDG(jenaLoader, selectedProcessName, selectedTimestamp);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ddgExplorer,
								"Unable to delete the DDG: " + e1.getMessage(),
								"Error deleting DDG", JOptionPane.ERROR_MESSAGE);
					}
					// loadFromDBFrame.dispose();
					dbBrowser.removeTimestamp(selectedTimestamp);
					deleteOneButton.setEnabled(false);
					deleteAllButton.setEnabled(true);
					openButton.setEnabled(false);
				}
			}

		});

		deleteAllButton.addActionListener (new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(loadFromDBFrame,
						"Are you sure that you want to delete *ALL* DDGs with this name permanently from the database?") == JOptionPane.YES_OPTION) {
					try {
						deleteAll(jenaLoader, dbBrowser.getDisplayedTimestamps());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ddgExplorer,
								"Unable to delete all DDGs: " + e1.getMessage(),
								"Error deleting DDGs", JOptionPane.ERROR_MESSAGE);
					}
					// loadFromDBFrame.dispose();
					dbBrowser.clearTimestamps();
					dbBrowser.removeProcess(selectedProcessName);
				}
			}
		});

		// Build the GUI layout
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		deleteAllButton.setEnabled(false);
		deleteOneButton.setEnabled(false);
		openButton.setEnabled(false);
		buttonPanel.add(deleteAllButton);
		buttonPanel.add(deleteOneButton);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);

		dbBrowser.addDBBrowserListener(new DBBrowserListener() {
			@Override
			public void scriptSelected(String script) {
				selectedProcessName = script;
				deleteAllButton.setEnabled(true);
			}

			@Override
			public void timestampSelected(String timestamp) {
				selectedTimestamp = timestamp;
				deleteAllButton.setEnabled(false);
				deleteOneButton.setEnabled(true);
				openButton.setEnabled(true);
			}
		});

		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout (selectionPanel, BoxLayout.X_AXIS));
		selectionPanel.add(dbBrowser);
		selectionPanel.add(buttonPanel);

		loadFromDBFrame.add(selectionPanel);
		loadFromDBFrame.setMinimumSize(new Dimension(800, 400));
		loadFromDBFrame.setLocationRelativeTo(ddgExplorer);
		loadFromDBFrame.setVisible(true);
	}

	/**
	 * Delete all the DDGs in the database for the selected process name.
	 * @param listModel all of the timestamps associated with the selected process
	 */
	private void deleteAll(JenaLoader jenaLoader, List<String> timestamps) {
		for (String timestamp : timestamps) {
			deleteDDG(jenaLoader, selectedProcessName, timestamp);
		}
	}

	/**
	 * Delete the selected DDG from the database.
	 * @param timestamp
	 * @param processName
	 */
	private static void deleteDDG(JenaLoader jenaLoader, String processName, String timestamp) {
		try {
			FileUtil.deleteFiles (processName, timestamp);
		} catch (IOException e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(ddgExplorer, "Could not delete saved files\n");
		}
		jenaLoader.deleteDDG(processName, timestamp);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			execute();
		} catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to manage the database: " + e.getMessage(),
					"Error managing the database",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
