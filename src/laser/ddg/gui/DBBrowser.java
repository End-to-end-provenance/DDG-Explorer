package laser.ddg.gui;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;

import laser.ddg.persist.JenaLoader;

/**
 * A browser for DDGs stored in a Jena database.  It has two panels.  In the first panel,
 * the user selects a process/script from the database.  In the second panel, the user 
 * selects the timestamp for an execution of the database.
 * 
 * Subclasses define what happens with the selected information by defining the 
 * getSelectedFile method.
 * 
 * @author Barbara Lerner
 * @version Oct 21, 2013
 *
 */
public abstract class DBBrowser extends JPanel{
	// Listeners to the selections made in the process and timestamp lists.
	private Set<DBBrowserListener> listeners = new HashSet<>();
	
	// The timestamp list
	private JList<String> timestampList;
	
	// The process selected by the user
	private String selectedProcessName;
	
	// The timestamp selected by the user
	private String selectedTimestamp;

	// Process names to display
	private List<String> processNames;
	
	/**
	 * Create the browser panel.  Initially, the process list contains the names of
	 * all the processes that have DDGs store in the database and the timestamp list
	 * is empty.
	 * @param jenaLoader the object that can read information from the database
	 */
	public DBBrowser(final JenaLoader jenaLoader) {
		processNames = new ArrayList<>(jenaLoader.getAllProcessNames());
		Collections.sort(processNames, (String s1, String s2) -> s1.toLowerCase().compareTo(s2.toLowerCase()));
		final JList<String> processNameList = new JList<>(processNames.toArray(new String[1]));
		processNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Create an emtpy timestamp list
		final Vector<String> timestampVector = new Vector<>();
		timestampList = new JList<>(timestampVector);
		timestampList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		processNameList.addListSelectionListener((ListSelectionEvent e) -> {
                    selectedProcessName = processNameList.getSelectedValue();
                    if (selectedProcessName == null) {
                        return;
                    }
                    
                    updateTimestampList(jenaLoader);
                    notifyScriptSelected();
                } /**
                 * When the user selects a ddg name, update the timestamp list.
                 */ );
		
		timestampList.addListSelectionListener((ListSelectionEvent e) -> {
                    selectedTimestamp = timestampList.getSelectedValue();
                    if (selectedTimestamp == null) {
                        return;
                    }
                    notifyTimestampSelected();
                } /**
                 * When the user selects a timestamp, enable the appropriate buttons.
                 */ );
				
		// Build the GUI layout
		JPanel processPanel = new JPanel();
		processPanel.setLayout(new BoxLayout(processPanel, BoxLayout.Y_AXIS));
		processPanel.add(new JLabel("Script Name"));
		JScrollPane processScroller = new JScrollPane(processNameList);
		processScroller.setPreferredSize(new Dimension(200, 250));
		processPanel.add(processScroller);
		
		JPanel timestampPanel = new JPanel();
		timestampPanel.setLayout(new BoxLayout(timestampPanel, BoxLayout.Y_AXIS));
		timestampPanel.add(new JLabel("Execution Timestamp"));
		JScrollPane timestampScroller = new JScrollPane(timestampList);
		timestampScroller.setPreferredSize(new Dimension(200, 250));
		timestampPanel.add(timestampScroller);
		
		setLayout(new BoxLayout (this, BoxLayout.X_AXIS));
		add(processPanel);
		add(timestampPanel);
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 0, 10);
		setBorder(padding);
		setMinimumSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(600, 400));
	}
	
	/**
	 * Changes the timestamp list to hold the timestamps for the currently selected ddg name
	 * @param timestampList the GUI component that is updated
	 */
	private void updateTimestampList(JenaLoader jenaLoader) {
		DefaultListModel<String> timestampData = new DefaultListModel<>();
		List<String> timestamps = jenaLoader.getTimestamps(selectedProcessName);
                timestamps.stream().forEach((timestamp) -> {
                    timestampData.addElement(timestamp);
                });
		timestampList.setModel(timestampData);
	}
	
	/**
	 * Return a list of the timestamps currently displayed.
	 * @return a list of the timestamps currently displayed.  Returns an empty
	 * 	list if there are none displayed.
	 */
	public List<String> getDisplayedTimestamps() {
		ListModel<String> listModel = timestampList.getModel();
		List<String> timestamps = new ArrayList<>();
		for (int i = 0; i < listModel.getSize(); i++) {
			timestamps.add(listModel.getElementAt(i));
		}
		return timestamps;
	}
	
	/**
	 * @return the process/script name selected by the user.  Returns null
	 * 	if none are selected
	 */
	public String getSelectedProcessName() {
		return selectedProcessName;
	}
	
	/**
	 * @return the timestamp selected by the user.  Returns null if none are selected.
	 */
	public String getSelectedTimestamp() {
		return selectedTimestamp;
	}

	/**
	 * Adds a listener for selections to the process and timestamp lists.
	 * @param listener the listener to add
	 */
	public void addDBBrowserListener(DBBrowserListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Tells the listeners which process/script the user just selected.
	 */
	private void notifyScriptSelected() {
            listeners.stream().forEach((l) -> {
                l.scriptSelected(selectedProcessName);
            });
	}

	/**
	 * Tells the listeners which timestamp the user just selected.
	 */
	private void notifyTimestampSelected() {
            listeners.stream().forEach((l) -> {
                l.timestampSelected(selectedTimestamp);
            });
	}

	/**
	 * @return the file selected by the user based on the selected process/script, 
	 * 		timestamp, and purpose of the particular browser.
	 */
	public abstract File getSelectedFile();

	/**
	 * Remove all timestamps from the timestamp list
	 */
	public void clearTimestamps() {
		((DefaultListModel<String>) timestampList.getModel()).removeAllElements();
	}

	/**
	 * Remove one timestamp from the timestamp list
	 * @param timestamp the timestamp to remove
	 */
	public void removeTimestamp(String timestamp) {
		((DefaultListModel<String>) timestampList.getModel()).removeElement(timestamp);
	}

	/**
	 * Remove one process from the process list
	 * @param process the process to remove
	 */
	public void removeProcess(String process) {
		processNames.remove(process);
	}
}
