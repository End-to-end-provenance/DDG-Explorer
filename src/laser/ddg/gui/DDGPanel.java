package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import laser.ddg.Attributes;
import laser.ddg.ProvenanceData;
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaWriter;
import laser.ddg.search.OperationSearchElement;
import laser.ddg.search.SearchElement;
import laser.ddg.search.SearchIndex;
import laser.ddg.visualizer.DDGDisplay;
import laser.ddg.visualizer.DDGVisualization;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import prefuse.Display;

/**
 * The JPanel that holds the DDG graph and the widgets to interact with the
 * graph.
 * 
 * @author Barbara Lerner
 * @version Sep 10, 2013
 * 
 */
public class DDGPanel extends JPanel {

	// Describes the main attributes of the ddg
	private JLabel descriptionArea;

	// Panel holding ddgDisplays and everything else besides the toolbar.
	// (needed for Legend's use)
	private JPanel ddgMain;
	
	// Where error messages are displayed.
	private JTextArea errorLog;

	// The DDG data
	private ProvenanceData provData;

	// The object used to write to the DB
	private DBWriter dbWriter;

	// The visualization of the ddg
	private DDGVisualization vis;
	
	// The object that manages the visible nodes
	private PrefuseGraphBuilder builder;

	// The panel containing the complete legend
	private Legend legendBox = new Legend();

	// The toolBar interacting with the main DDGDisplay
	private Toolbar toolbar;

	// enables the the search list to be horizontally resize by the user
	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	// Search results object that enables user to find nodes in the graph
	private SearchResultsGUI searchList;

	// Contains information to make nodes easier to find when searching
	private SearchIndex searchIndex;
	
	// Remembers if the line numbers should be shown in the node labels
	private boolean showLines = false;
	
	// Remembers the direction of the arrows.  REVERSE == down
	private int arrowDirection = prefuse.Constants.EDGE_ARROW_REVERSE;
	
	/**
	 * Create a frame to display the DDG graph in
	 */
	public DDGPanel() {
		super(new BorderLayout());
	}

	/**
	 * Create a frame to display the DDG graph in
	 * 
	 * @param dbWriter
	 *            the object that knows how to write to a database
	 */
	public DDGPanel(DBWriter dbWriter) {
		super(new BorderLayout());
		this.dbWriter = dbWriter;
	}

	/**
	 * Creates the layout for the panel.
	 * 
	 * @param builder
	 * 		the object that manages the visible nodes and edges
	 * @param vis
	 *            the visualization to display
	 * @param ddgDisplay
	 *            the ddg display
	 * @param ddgOverview
	 * @param provData
	 *            the ddg data being displayed
	 */
	public void displayDDG(PrefuseGraphBuilder builder, DDGVisualization vis, final Display ddgDisplay,
			final Display ddgOverview, ProvenanceData provData) {
		this.builder = builder;
		this.vis = vis;
		this.provData = provData;
		setBackground(Color.WHITE);

		// Set up toolbarPanel and inside, ddgPanel:
		// ddgPanel to hold description, ddgDisplay, ddgOverview, legend,
		// search...
		createMainPanel(ddgDisplay, ddgOverview);
		
		toolbar = new Toolbar((DDGDisplay) ddgDisplay);

		// hold toolbarPanel and everything inside
		add(toolbar, BorderLayout.NORTH);

		// set the DDG on the right of JSplitPane and later the DDG Search
		// Results on the Left
		splitPane.setRightComponent(ddgMain);

		add(splitPane, BorderLayout.CENTER);
		
		// add log to bottom of frame
		JPanel logPanel = createLogPanel();
		add(logPanel, BorderLayout.SOUTH);


	}

	private JPanel createLogPanel() {
		JLabel logLabel = new JLabel("Error Log");
		errorLog = new JTextArea();
		errorLog.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(errorLog);
		logScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		logScrollPane.setViewportBorder(BorderFactory
				.createLoweredBevelBorder());
		logScrollPane.setPreferredSize(new Dimension(logScrollPane
				.getPreferredSize().width, 80));
		JPanel logPanel = new JPanel(new BorderLayout());
		Border raised = BorderFactory.createRaisedBevelBorder();
		Border lowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		logPanel.setBorder(BorderFactory.createCompoundBorder(raised, lowered));
		logPanel.add(logLabel, BorderLayout.NORTH);
		logPanel.add(logScrollPane, BorderLayout.CENTER);
		return logPanel;
	}

	private void createMainPanel(Display ddgDisplay, final Display ddgOverview) {
		ddgMain = new JPanel(new BorderLayout());
		ddgMain.setBackground(Color.WHITE);
		ddgMain.add(createDescriptionPanel(), BorderLayout.NORTH);
		ddgMain.add(ddgDisplay, BorderLayout.CENTER);
		ddgOverview.setBorder(BorderFactory.createTitledBorder("Overview"));
		ddgMain.add(ddgOverview, BorderLayout.EAST);
		// legend added to WEST through preferences
		// TODO searchbar added to SOUTH! (through button press?)

		// resize components within layers
		ddgMain.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int panelHeight = ddgMain.getHeight();
				Rectangle prevBounds = ddgOverview.getBounds();
				ddgOverview.setBounds(prevBounds.x, prevBounds.y,
						prevBounds.width, panelHeight - 16);
			}
		});
	}

	/**
	 * Creates the panel that holds the main attributes
	 * 
	 * @return the panel
	 */
	private Component createDescriptionPanel() {
		descriptionArea = new JLabel("", SwingConstants.CENTER);

		if (provData != null) {
			updateDescription();
		}

		return descriptionArea;
	}

	/**
	 * Updates the basic attributes displayed.
	 */
	private void updateDescription() {
		descriptionArea.setText(provData.getQuery());
	}

	/**
	 * save this DDG to the Database
	 */
	public void saveToDB() {
		dbWriter.persistDDG(provData);
	}

	/**
	 * find whether this DDG is already saved in the database
	 * 
	 * @return boolean for saved/unsaved
	 */
	public boolean alreadyInDB() {
		try {
			String processPathName = provData.getProcessName();
			String executionTimestamp = provData.getTimestamp();
			String language = provData.getLanguage();
			return ((JenaWriter) dbWriter).alreadyInDB(processPathName,
					executionTimestamp, language);
		} catch (Exception e) {
			System.out.println("DDGPanel's alreadyInDB unsuccessful");
			return false;
		}
	}

	/**
	 * Parses the attributes to find the main ones. Sets the title.
	 * 
	 * @param attrList
	 *            the attribute list
	 */
	public void setAttributes(Attributes attrList) {
		if (attrList.contains("Script")) {
			setTitleToScriptAndTime(attrList.get("Script"));
		} else {
			setName("DDG Viewer");
		}
	}

	private void setTitleToScriptAndTime(String attr) {
		String[] items = attr.split("[\\n]");

		String script = "";
		String time = "";

		// Get the name of the file from the attributes list
		for (int s = 0; s < items.length; s++) {
			if (items[s].startsWith("Script")) {
				// get rid of any spaces
				String theLine = items[s].replaceAll("[\\s]", "");

				// find the last '/' in the path name since the script name will
				// be after that
				int startAt = theLine.lastIndexOf('/') + 1;

				// use the index and go from there to the end
				script = theLine.substring(startAt);
			}

			else if (items[s].startsWith("DateTime")) {
				// get rid of any spaces
				String theLine = items[s].replaceAll("[\\s]", "");

				// use the length of the word 'datetime' and go from there to
				// the end
				time = theLine.substring("DateTime=".length());

				// Remove fractions of seconds
				if (time.length() == 19) {
					time = time.substring(0, 16);
				}
			}
		}

		setTitle(script, time);
	}

	/**
	 * Set the panel title
	 * 
	 * @param title
	 *            the name of the process / script. This cannot be null.
	 * @param timestamp
	 *            the time at which it was run. This can be null.
	 */
	public void setTitle(String title, String timestamp) {
		if (timestamp == null) {
			setName(title);
		} else {
			setName(title + " " + timestamp);
		}
	}

	/**
	 * Sets the provenance data and updates the main attributes for this ddg.
	 * 
	 * @param provData
	 *            the ddg data
	 */
	public void setProvData(ProvenanceData provData) {
		this.provData = provData;
		if (descriptionArea != null) {
			updateDescription();
		}
		// set the Title for DDGs opened by file. Otherwise they have no title
		// DDGs opened from the DB will have the title set later.
		// unlike ddgs opened from the DB, ddgs opened by file will have the
		// Language filled in.
		if (this.getName() == null && provData.getLanguage() != null) {
			// System.out.println("empty title and language = " +
			// provData.getLanguage());
			String fileName = FileUtil.getPathDest(provData.getProcessName(),
					provData.getLanguage());
			setTitle(fileName, provData.getTimestamp());
		}
	}

	public ProvenanceData getProvData() {
		return provData;
	}

	public void setArrowDirectionDown() {
		vis.setRenderer(prefuse.Constants.EDGE_ARROW_REVERSE, showLines);
		vis.repaint();
	}

	public void setArrowDirectionUp() {
		vis.setRenderer(prefuse.Constants.EDGE_ARROW_FORWARD, showLines);
		vis.repaint();
	}
	
	public void showLineNumbers(boolean show) {
		vis.setRenderer(arrowDirection, show);
		vis.repaint();
	}

	public void addLegend() {
		ddgMain.add(legendBox, BorderLayout.WEST);
		ddgMain.validate();
	}

	public void drawLegend(ArrayList<LegendEntry> nodeLegend,
			ArrayList<LegendEntry> edgeLegend) {
		legendBox.drawLegend (nodeLegend, edgeLegend);
	}

	/**
	 * Remove the legend from the display
	 */
	public void removeLegend() {
		ddgMain.remove(legendBox);
		ddgMain.validate();
	}

	public void setSearchIndex(SearchIndex searchIndex) {
		this.searchIndex = searchIndex;
	}

	public SearchIndex getSearchIndex() {
		return searchIndex;
	}

	public void showSearchResults(ArrayList<? extends SearchElement> resultList) {
		if (searchList == null) {
			searchList = new SearchResultsGUI(resultList);
			splitPane.setLeftComponent(searchList);
			validate();
		} else {
			searchList.updateSearchList(resultList);
		}
	}

	public void showErrMsg(String str) {
		errorLog.append(str);
	}

	public void setHighlighted(int id, boolean value) {
		builder.setHighlighted(id, value);
	}

	public void createCopiedGroup(String groupName){
		builder.createCopiedGroup(groupName);
	}

	public void updateCopiedGroup(int id, String groupname){
		builder.updateCopiedGroup(id, groupname);
	}

	public void focusOn(String name) {
		builder.focusOn(name);
	}


}
