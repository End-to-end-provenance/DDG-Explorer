package laser.ddg.visualizer;

import laser.ddg.visualizer.DDGSearchGUI;

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
import javax.swing.JComponent;
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
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaWriter;
import laser.ddg.visualizer.PrefuseGraphBuilder.tupleElement;
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

	//Returns PrefuseGraphBuilder Object to retrieve list of nodes
	public PrefuseGraphBuilder getBuilder() {
		return builder;
	}
	
	//Constructs and updates list of search results 
	public void SearchList(ArrayList <tupleElement> nodesList, boolean isText, String searchText){
		ArrayList <tupleElement> newList = new ArrayList<tupleElement>();
		
		//if user entered information into the search bar
		if(isText){
			for(tupleElement entry : nodesList)
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
			//toolbarPanel to hold ddgMain, surrounded by space for the toolbar
			JPanel toolbarPanel = new JPanel(new BorderLayout());
				toolbar = new Toolbar((DDGDisplay)ddgDisplay);
				toolbarPanel.add(ddgMain, BorderLayout.CENTER);
		
		//hold toolbarPanel and everything inside
		setBackground(Color.WHITE);
		add(toolbar, BorderLayout.NORTH);
	   
		//set the DDG on the right of JSplitPane and later the DDG Search Results on the Left
	    splitPane.setRightComponent(toolbarPanel);		
	    
	    
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
	 * Saves the current settings to a preference file.
	 */
	private static void savePreferences() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(PREFERENCE_FILE));
			out.println("# DDG Explorer preferences");
			for (String prefVar : preferences.keySet()) {
				out.println(prefVar + " = " + preferences.get(prefVar));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * Create DDG Menu to be placed into the menuBar
	 * @return JMenu DDG menu
	 */
	public JMenu createDDGMenu() {
		final JMenu DDGMenu = new JMenu("DDG");
		DDGMenu.setBackground(MENU_COLOR);
		
		JMenuItem attributesItem = new JMenuItem("Show attributes");
		attributesItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (attributes == null) {
					try {
						attributes = provData.getAttributes();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(DDGMenu, 
								"Unable to get the attributes: " + e1.getMessage(), 
								"Error getting the attributes", JOptionPane.ERROR_MESSAGE);
					}
				}
				createAttributeFrame(attributes);
			}
		});
		DDGMenu.add(attributesItem);
		
		JMenuItem showScriptItem = new JMenuItem("Show R script");
		showScriptItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String scriptFileName = provData.getScript();
				FileViewer fileViewer = new FileViewer(scriptFileName, "");
				fileViewer.displayFile();
			}
			
		});
		DDGMenu.add(showScriptItem);
		
		DDGMenu.add(createPreferencesMenu());	//preferences submenu
		
		return DDGMenu;
	}
	
	/**
	 * Creates the menu with user preferences
	 * @return 
	 */
	public JMenu createPreferencesMenu() {
		loadPreferences();
		
		JMenu prefMenu = new JMenu("Preferences");
		prefMenu.setBackground(MENU_COLOR);
		final JCheckBoxMenuItem inToOutMenuItem = new JCheckBoxMenuItem("Draw arrows from inputs to outputs", 
				preferences.get("ArrowDirection").toLowerCase().equals("intoout"));
		setArrowDirection(inToOutMenuItem);
		inToOutMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setArrowDirection(inToOutMenuItem);
				vis.repaint();
				savePreferences();
			}


		});

		prefMenu.add(inToOutMenuItem);
		
		showLegendMenuItem = new JCheckBoxMenuItem("Show legend", preferences.get("ShowLegend").toLowerCase().equals("true"));
		
		showLegendMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (showLegendMenuItem.isSelected()) {
					ddgMain.add(legendBox, BorderLayout.WEST);
					preferences.put("ShowLegend", "true");
					savePreferences();
					ddgMain.validate();
				}
				else {
					removeLegend();
				}
			}

			
		});
		prefMenu.add(showLegendMenuItem);
		
		return prefMenu;
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
	 * Create a new MenuBar for this DDG
	 * @param fileMenu the file menu used for all tabs
	 * @return a full MenuBar with File, DDG, and Help.
	 */
	public JMenuBar createMenuBarDDG(JMenu fileMenu){
		JMenuBar DDGbar = new JMenuBar();
		DDGbar.setBackground(MENU_COLOR);
		
		//update Save to Database in file menu
		JMenuItem save = new JMenuItem("Save to Database");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//ErrorLog.showErrMsg("Calling persistDDG\n");
				//System.out.println("provData in DDGPanel = " + provData.toString());
				dbWriter.persistDDG (provData);
			}
		});
		save.setEnabled(!alreadyInDB());
		fileMenu.remove(2); //replace disabled Save with working Save
		fileMenu.insert(save, 2);
		
		//add all three menus to the menu bar
		DDGbar.add(fileMenu);
		DDGbar.add(createDDGMenu());
		DDGbar.add(createHelpMenu());
		return DDGbar;
	}
	
	/**
	 * Create help menu
	 * @return 
	 */
	public JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setBackground(MENU_COLOR);
		
		JMenuItem commandOverviewItem = new JMenuItem("Command overview");
		commandOverviewItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createCommandOverviewFrame();
			}

		});
		helpMenu.add(commandOverviewItem);
		return helpMenu;
	}
	
	/**
	 * Create a pop-up window with command summary
	 */
	private void createCommandOverviewFrame() {
		StringBuffer help = new StringBuffer();
		
		help.append("To collapse a section of the graph\n");
		help.append("   Left click on a green start or finish node.\n\n");

		help.append("To expand a collapsed node\n");
		help.append("   Left click on a light blue node.\n\n");

		help.append("To move a node\n");
		help.append("   Drag the node\n\n");

		help.append("To scroll to a different portion of the DDG\n");
		help.append("   Drag the overview box OR\n");
		help.append("   Drag on the background\n\n");

		help.append("To re-center the DDG\n");
		help.append("   Click the Refocus button\n\n");

		help.append("To change the magnification\n");
		help.append("   Use the slider at the top of the window\n\n");

		JOptionPane.showMessageDialog(this, help.toString(), "Command Overview", JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Creates a window to display all the attributes of the ddg
	 * @param attributes2 the list of attibutes
	 */
	private void createAttributeFrame(Attributes attributes2){
		JFrame f = new JFrame("Attribute List");
		f.setSize(new Dimension(200, 200));
		JFrame.setDefaultLookAndFeelDecorated(true);

		// make a new JTextArea appear will all the attribute values
		JTextArea attrText = new JTextArea(15, 40);
		attrText.setText(createAttributeText(provData.getLanguage(), attributes2));
		attrText.setEditable(false);
		attrText.setLineWrap(true);
		
		f.add(attrText);
		f.pack();
		f.setVisible(true);
	}

	/**
	 * Creates the text to display to the user showing attribute names and values
	 * @param language the language to add the legend for
	 * @param attrs the attributes to turn into text
	 * @return the text to display to the user or null there is an error creating the text
	 */
	public static String createAttributeText(String language, Attributes attrs) {
		Class<DDGBuilder> ddgBuilderClass = LanguageConfigurator.getDDGBuilder(language);
		try {
			String text = (String) ddgBuilderClass.getMethod("getAttributeString", Attributes.class).invoke(null, attrs);
			return text;
		} catch (Exception e) {
			System.out.println("Can't create attribute text");
			e.printStackTrace();
			return null;
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
	private void removeLegend() {
		ddgMain.remove(legendBox);
		showLegendMenuItem.setSelected(false);
		preferences.put("ShowLegend", "false");
		savePreferences();
		ddgMain.validate();
	}


}
