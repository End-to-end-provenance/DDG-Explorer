package laser.ddg.commands;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import laser.ddg.ProvenanceData;
import laser.ddg.gui.DBBrowserListener;
import laser.ddg.gui.DDGBrowser;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaLoader;
import laser.ddg.persist.JenaWriter;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Command to load a DDG from the database and display it in a tab.
 * Creates a dialog box to allow the user to select the ddg to load.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 *
 */
public class LoadFromDBCommand implements ActionListener {
	private static final DDGExplorer ddgExplorer = DDGExplorer.getInstance();
	
	// The process that the user selected.
	private String selectedProcessName;

	// The timestamp for the DDG that the user selected.
	private String selectedTimestamp;
	
	/**
	 * Displays a window that allows the user to specify details about what they wish to load
	 * from the database.  On return, the window has been disposed.
	 */
	void execute() {
		final JDialog loadFromDBFrame = new JDialog(ddgExplorer, "Open from Database", true);
		loadFromDBFrame.setLocationRelativeTo(ddgExplorer);

		// Create the buttons used to pick an action
		final JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			/**
			 * Loads an entire DDG
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					loadFromDBFrame.dispose();
					loadDDGFromDB(selectedProcessName, selectedTimestamp);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(ddgExplorer,
							"Unable to load the DDG: " + e1.getMessage(),
							"Error loading DDG", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
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

		// Build the GUI layout
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
		buttonPanel.setBorder(padding);
		openButton.setEnabled(false);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);

		JenaLoader jenaLoader = JenaLoader.getInstance();
		DDGBrowser dbBrowser = new DDGBrowser(jenaLoader);
		dbBrowser.addDBBrowserListener(new DBBrowserListener() {
			@Override
			public void scriptSelected(String script) {
				selectedProcessName = script;
			}

			@Override
			public void timestampSelected(String timestamp) {
				selectedTimestamp = timestamp;
				openButton.setEnabled(true);
			}
		});

		loadFromDBFrame.add(dbBrowser, BorderLayout.CENTER);
		loadFromDBFrame.add(buttonPanel, BorderLayout.SOUTH);
		loadFromDBFrame.pack();
		loadFromDBFrame.setVisible(true);
	}
	
	/**
	 * Loads a ddg from the database and creates the visual graph
	 * @param processName the name of the process executed
	 * @param timestamp the time the ddg was created
	 * @return the object that builds the visual graph
	 */
	public static PrefuseGraphBuilder loadDDGFromDB (String processName, String timestamp) {
		final ProvenanceData provData = new ProvenanceData(processName);
		final JenaWriter jenaWriter = JenaWriter.getInstance();
		final PrefuseGraphBuilder graphBuilder = new PrefuseGraphBuilder(false, jenaWriter);
		graphBuilder.setProvData(provData);
		graphBuilder.setTitle(processName, timestamp);
		provData.addProvenanceListener(graphBuilder);
		provData.setQuery("Entire DDG");
		JenaLoader jenaLoader = JenaLoader.getInstance();
		jenaLoader.loadDDG(processName, timestamp, provData);
		graphBuilder.createLegend(provData.getLanguage());
		ddgExplorer.addTab(graphBuilder.getPanel().getName(), graphBuilder.getPanel());
		return graphBuilder;
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			execute();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to load the DDG: " + e.getMessage(),
					"Error loading DDG", JOptionPane.ERROR_MESSAGE);
		}
	}

}
