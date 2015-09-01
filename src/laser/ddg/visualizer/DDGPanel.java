package laser.ddg.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.LanguageConfigurator;
import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGSearchGUI;
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaWriter;
import laser.ddg.search.SearchElement;
import laser.ddg.search.SearchIndex;
import prefuse.Display;

/**
 * The JFrame that holds the DDG graph and the widgets to interact with the graph.
 * 
 * @author Barbara Lerner
 * @version Sep 10, 2013
 *
 */
public class DDGPanel extends JPanel {

	private static final Color MENU_COLOR = new Color(171,171,171);
	
	private static final int LEGEND_ENTRY_HEIGHT = 25;
	private static final Font LEGEND_FONT = new Font ("Helvetica", Font.PLAIN, 10);

	
	// Describes the main attributes of the ddg
	private JLabel descriptionArea;
	
	// Panel holding ddgDisplays and everything else besides the toolbar. (needed for Legend's use)
	private JPanel ddgMain;
	
	// The DDG data
	private ProvenanceData provData;

	// General information describing the program that created the ddg being drawn
	private Attributes attributes;
	
	// The object used to write to the DB
	private DBWriter dbWriter;
	
	// The visualization of the ddg
	private DDGVisualization vis;

	// The box containing the complete legend
	private Box legendBox;
	
	//The toolBar interacting with the main DDGDisplay
	private Toolbar toolbar;

	// Command that allows the user to decide whether to see the legend or not
	private JCheckBoxMenuItem showLegendMenuItem;

	//create a PrefuseGraphBuilder to assist with creating the search results list
	private PrefuseGraphBuilder builder;

	//enables the the search list to be horizontally resize by the user
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	//Search results object that enables user to find nodes in the graph
	private DDGSearchGUI searchList;

	private SearchIndex searchIndex; 

	//Returns PrefuseGraphBuilder Object to retrieve list of nodes
	public PrefuseGraphBuilder getBuilder() {
		return builder;
	}
	
	//Constructs and updates list of search results 
	public void searchList(ArrayList <SearchElement> nodesList, boolean isText, String searchText){
		ArrayList <SearchElement> newList = new ArrayList<SearchElement>();
		
		//if user entered information into the search bar
		if(isText){
			for(SearchElement entry : nodesList)
				if(entry.getName().toLowerCase().contains(searchText))
					newList.add(entry);
			if(searchList == null){
				searchList = new DDGSearchGUI(newList, splitPane, this);
			}
			else{
				searchList.updateSearchList(newList);
			}
		}

		//if text in search is empty then give all associated information
		else{
			if(searchList == null){
				searchList = new DDGSearchGUI(nodesList, splitPane, this);
			}
			else{
				searchList.updateSearchList(nodesList);
			}
		}
	}

	//preferences on Arrow Direction and the Legend
	private static Hashtable<String,String> preferences = new Hashtable<String, String>();
	private static final File PREFERENCE_FILE = new File(FileUtil.DDG_DIRECTORY + "prefs.txt");
	
	/**
	 * Create a frame to display the DDG graph in 
	 */
	public DDGPanel() {
		super(new BorderLayout());
		
		// TODO:  Need to check arrow preferences when creating a DDG Panel.
		// 1.  Arrow direction
		// 2.  Show legend or not.
	}
	
	/**
	 * Create a frame to display the DDG graph in 
	 * @param dbWriter the object that knows how to write to a database
	 */
	public DDGPanel (DBWriter dbWriter) {
		super(new BorderLayout());
		this.dbWriter = dbWriter;
	}
	
	/**
	 * Creates a frame.  The frame is not displayed until displayDDG is called.
	 * 
	 * @param vis the visualization to display
	 * @param ddgDisplay the ddg display
	 * @param ddgOverview
	 * @param provData the ddg data being displayed
	 */
	public void displayDDG(DDGVisualization vis, final Display ddgDisplay, final Display ddgOverview, ProvenanceData provData, PrefuseGraphBuilder builder) {
		this.vis = vis;
		this.provData = provData;
		this.builder = builder;
		//this.overview = ddgDisplay;	

		//Set up toolbarPanel and inside, ddgPanel:
			//ddgPanel to hold description, ddgDisplay, ddgOverview, legend, search...
			ddgMain = new JPanel(new BorderLayout());
				ddgMain.setBackground(Color.WHITE);
				ddgMain.add(createDescriptionPanel(), BorderLayout.NORTH);
				ddgMain.add(ddgDisplay, BorderLayout.CENTER);
				ddgOverview.setBorder(BorderFactory.createTitledBorder("Overview"));
				ddgMain.add(ddgOverview, BorderLayout.EAST);
				//legend added to WEST through preferences
				//TODO searchbar added to SOUTH! (through button press?)
				
				//resize components within layers
				ddgMain.addComponentListener(new ComponentAdapter(){
					@Override
					public void componentResized(ComponentEvent e){
						int panelHeight = ddgMain.getHeight();
						Rectangle prevBounds = ddgOverview.getBounds();
						ddgOverview.setBounds(prevBounds.x, prevBounds.y, prevBounds.width, panelHeight-16);					
					}
				});	

				toolbar = new Toolbar((DDGDisplay)ddgDisplay);
		
		//hold toolbarPanel and everything inside
		setBackground(Color.WHITE);
		add(toolbar, BorderLayout.NORTH);
	   
		//set the DDG on the right of JSplitPane and later the DDG Search Results on the Left
	    splitPane.setRightComponent(ddgMain);		
	    
	    
	    add(splitPane, BorderLayout.CENTER);
		loadPreferences();
	}

	/**
	 * Updates the basic attributes displayed.
	 */
	private void updateDescription() {
		descriptionArea.setText(provData.getQuery());
	}
	
	/**
	 * Sets the direction that arrows are drawn based on the setting of the
	 * corresponding menu item.
	 */
	private void setArrowDirection(
			final JCheckBoxMenuItem inToOutMenuItem) {
		if (inToOutMenuItem.isSelected()) {
			vis.setRenderer(prefuse.Constants.EDGE_ARROW_REVERSE);
			preferences.put("ArrowDirection", "InToOut");
		}
		else {
			vis.setRenderer(prefuse.Constants.EDGE_ARROW_FORWARD);
			preferences.put("ArrowDirection", "OutToIn");
		}
	}

	/**
	 * Loads user preferences from a file or sets to the default if 
	 * there is no preference file.
	 */
	private static void loadPreferences() {
		// Set default values
		preferences.put("ArrowDirection", "InToOut");
		preferences.put("ShowLegend", "true");
		
		if (PREFERENCE_FILE.exists()) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(PREFERENCE_FILE));
				String nextLine = in.readLine();
				while (nextLine != null) {
					String[] tokens = nextLine.split("[\\s=]+");
					if (!tokens[0].startsWith("#")) {
						String prefVar = tokens[0];
						String prefValue = tokens[1];
						preferences.put(prefVar, prefValue);
					}
					nextLine = in.readLine();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	
	
	
	/**
	 * save this DDG to the Database
	 */
	public void saveToDB(){
		dbWriter.persistDDG(provData);
	}
	
	/**
	 * find whether this DDG is already saved in the database
	 * @return boolean for saved/unsaved
	 */
	public boolean alreadyInDB(){
		try {
			String processPathName = provData.getProcessName();
			String executionTimestamp = provData.getTimestamp();
			String language = provData.getLanguage();
			return ((JenaWriter)dbWriter).alreadyInDB(processPathName, executionTimestamp, language);			
		} catch (Exception e) {
			System.out.println("DDGPanel's alreadyInDB unsuccessful");
			return false;
		}
	}
	
	/**
	 * Creates the panel that holds the main attributes
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
	 * Parses the attributes to find the main ones.  Sets the title.
	 * @param attrList the attribute list
	 */
	public void setAttributes(Attributes attrList) {
		this.attributes = attrList;

		if (attrList.contains("Script")) {
			setTitleToScriptAndTime(attrList.get("Script"));
		}
		else {
			setName("DDG Viewer");			
		}
	}

	private void setTitleToScriptAndTime(String attr) {
		String[] items = attr.split("[\\n]");
		
		String script = "";
		String time = "";
		
		//Get the name of the file from the attributes list
		for (int s = 0; s < items.length; s++) {
			if (items[s].startsWith("Script")) {
				//get rid of any spaces
				String theLine = items[s].replaceAll("[\\s]", "");
				
				//find the last '/' in the path name since the script name will be after that
				int startAt = theLine.lastIndexOf('/') + 1;

				//use the index and go from there to the end
				script = theLine.substring(startAt);
			}
			
			else if(items[s].startsWith("DateTime")) {
				//get rid of any spaces
				String theLine = items[s].replaceAll("[\\s]", "");
				
				//use the length of the word 'datetime' and go from there to the end
				time = theLine.substring("DateTime=".length());
				
				// Remove fractions of seconds
				if(time.length() == 19){
					time = time.substring(0,16);
				}
			}
		}
		
		setTitle(script, time);
	}
	
	/**
	 * Set the window title
	 * @param title the name of the process / script.  This cannot be null.
	 * @param timestamp the time at which it was run.  This can be null.
	 */
	public void setTitle(String title, String timestamp) {
		if (timestamp == null) {
			setName(title);
		}
		else {
			setName(title + " " + timestamp);
		}
	}

	/**
	 * Sets the provenance data and updates the main attributes for this
	 * ddg.
	 * @param provData the ddg data
	 */
	public void setProvData(ProvenanceData provData) {
		this.provData = provData;
		if (descriptionArea != null) {
			updateDescription();
		}
		//set the Title for DDGs opened by file. Otherwise they have no title
		//DDGs opened from the DB will have the title set later.
		//unlike ddgs opened from the DB, ddgs opened by file will have the Language filled in.
		if (this.getName() == null && provData.getLanguage() != null){
			//System.out.println("empty title and language = " + provData.getLanguage());
			String fileName = FileUtil.getPathDest(provData.getProcessName(), provData.getLanguage());
			setTitle(fileName, provData.getTimestamp());
		}
	}

	/**
	 * Draw the legend for this graph.  The legend is specific to the language that the
	 * DDG is for, so each language must create the label and color pairings to be displayed
	 * in the legend, using the vocabulary natural for that language.
	 * @param nodeColors the node label, color pairs.  May be null.
	 * @param edgeColors the edge label, color pairs.  May be null. 
	 */
	public void drawLegend(ArrayList<LegendEntry> nodeColors, 
			ArrayList<LegendEntry> edgeColors) {
		
		if (nodeColors == null && edgeColors == null) {
			return;
		}
		
		int numEntries = 0;
		
		JPanel legend = new JPanel();
		legend.setLayout(new GridLayout(0, 1));
		
		Box headerPanel = new Box(BoxLayout.X_AXIS);
		headerPanel.add(new JLabel("Legend"));
		headerPanel.add(Box.createHorizontalGlue());
		JButton closeLegendButton = new JButton("X");
		closeLegendButton.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		closeLegendButton.setToolTipText("Hide legend.");
		closeLegendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// System.out.println("Close button clicked");
				removeLegend();
			}
			
		});
		headerPanel.add(closeLegendButton);
		legend.add(headerPanel);
		numEntries = 1;
		
		if (nodeColors != null) {
			addNodesToLegend(nodeColors, legend);
			
			numEntries = nodeColors.size() + 1;
	
			if (edgeColors != null) {
				legend.add(new JPanel());
				//System.out.println("Adding spacer");
				numEntries ++;
			}
		}
		
		if (edgeColors != null) {
			addEdgesToLegend(edgeColors, legend);
			
			numEntries = numEntries + edgeColors.size() + 1;
		}
		
		legend.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		legend.setPreferredSize (new Dimension(125, numEntries * LEGEND_ENTRY_HEIGHT));

		legendBox = new Box(BoxLayout.Y_AXIS);
		legendBox.add(Box.createVerticalGlue());
		legendBox.add(legend);
		legendBox.add(Box.createVerticalGlue());
		
		if (preferences.get("ShowLegend").toLowerCase().equals("true")) {
			ddgMain.add(legendBox, BorderLayout.WEST);
		}
	}

	/**
	 * Add the edge labels and colors to the legend.
	 * @param edgeColors the edge label, color pairs
	 * @param legend the legend to add them to.
	 */
	private static void addEdgesToLegend(ArrayList<LegendEntry> edgeColors,
			JPanel legend) {
		legend.add(new JLabel("Edges"));
		//System.out.println("Adding edges header");
		for (LegendEntry entry : edgeColors) {
			JLabel next = new JLabel(entry.getLabel(), SwingConstants.CENTER);
			next.setFont(LEGEND_FONT);
			next.setForeground(entry.getColor());
			legend.add(next);
			//System.out.println("Adding " + entry.getLabel());
		}
	}

	/**
	 * Add the node labels and colors to the legend.
	 * @param nodeColors the node label, color pairs
	 * @param legend the legend to add them to.
	 */
	private static void addNodesToLegend(ArrayList<LegendEntry> nodeColors,
			JPanel legend) {
		legend.add(new JLabel("Nodes"));
		//System.out.println("Adding node header");
		for (LegendEntry entry : nodeColors) {
			JLabel next = new JLabel(entry.getLabel(), SwingConstants.CENTER);
			next.setFont(LEGEND_FONT);
			next.setOpaque(true);
			next.setBackground(entry.getColor());
			legend.add(next);
			//System.out.println("Adding " + entry.getLabel());
		}
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

	public ProvenanceData getProvData() {
		return provData;
	}

	public void setArrowDirectionDown() {
		vis.setRenderer(prefuse.Constants.EDGE_ARROW_REVERSE);
		vis.repaint();
	}

	public void setArrowDirectionUp() {
		vis.setRenderer(prefuse.Constants.EDGE_ARROW_FORWARD);
		vis.repaint();
	}

	public void addLegend() {
		ddgMain.add(legendBox, BorderLayout.WEST);  
		ddgMain.validate();	
	}


}
