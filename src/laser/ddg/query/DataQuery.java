package laser.ddg.query;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

import laser.ddg.DDGBuilder;
import laser.ddg.DataInstanceNode;
import laser.ddg.LanguageConfigurator;
import laser.ddg.Node;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.SourcePos;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaLoader;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * Abstract class that provides the framework that require the user to select
 * a data name and value before performing a query.  The query results will 
 * include both procedure and data nodes, but only data edges.  There will be
 * no control flow edges.
 * 
 * Subclasses need to define:
 * <ul>
 *   <li>getMenuItem
 *   <li>getFrameTitle
 *   <li>doQuery
 *   <li>getQuery
 *   <li>getSingletonMessage
 * </ul>
 * 
 * @author Barbara Lerner
 * @version Aug 5, 2013
 *
 */
public abstract class DataQuery extends AbstractQuery {
	private static final Dimension PREFERRED_MENU_SIZE = new Dimension(300, 50);

	// Object to load from the database
	private JenaLoader dbLoader;
	
	// Process being queried
	private String processName;
	
	// Timestamp of the DDG being queried
	private String timestamp;
	
	// Window in which the query is performed 
	private JFrame queryFrame;
	
	// Menu containing the names of all the data objects
	private JComboBox<String> nameMenu;
	
	// Menu containing the values of all the data objects
	//private JComboBox<String> valueMenu;
	private JTextField valueField;
	
	// The resource selected in the menu
	private Resource selectedResource;
	
	// List of procedure resources that should be part of the query result
	private List<ProcedureInstanceNode> allPinsToShow = new ArrayList<>();
	
	// List of data resources that should be part of the query result
	//private List<Resource> allDinsToShow = new ArrayList<Resource>();
	private List<DataInstanceNode> allDinsToShow = new ArrayList<>();
	
	private PrefuseGraphBuilder graphBuilder;
	
	private DDGBuilder builder;

	/**
	 * @return the string to display in the menu to select the query
	 */
	@Override
	public abstract String getMenuItem();
	
	/**
	 * Initialize the query. Call this to set up the query when you are planning
	 * to add the details without prompting the user for them.  Use performQuery instead
	 * to pull up popup menus to allow the user to provide further details for 
	 * the query.
	 * @param dbLoader the object that can read from the db 
	 * @param processName the name of the process to be loaded from the db
	 * @param timestamp the timestamp of the ddg
	 */
	public void initQuery(JenaLoader dbLoader, String processName,
			String timestamp) {
		this.dbLoader = dbLoader;
		this.processName = processName;
		this.timestamp = timestamp;
	}

	/**
	 * Performs the query.
	 * @param dbLoader the object that can query the db
	 * @param processName the name of the process whose DDG is searched
	 * @param timestamp the timestamp of the DDG to search
	 * @param invokingComponent the GUI component that causes the query to be performed
	 */
	@Override
//	public void performQuery(JenaLoader dbLoader, String processName,
//			String timestamp, Component invokingComponent) {
//		initQuery (dbLoader, processName, timestamp);
//		
//		SortedSet<String> dinNames = dbLoader.getAllDinNames(processName, timestamp);
//		
//		Vector<String> names = new Vector<>();
//        dinNames.stream().forEach((dinName) -> {
//        	names.add(dinName);
//        });
//		
//		queryFrame = new JFrame (getFrameTitle());
//		final JPanel varQueryPanel = new JPanel();
//		nameMenu = new JComboBox<>(names);
//		final JButton okButton = new JButton("OK");
//		
//		nameMenu.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				showValueOf (nameMenu.getSelectedItem().toString());
//				okButton.setEnabled(true);
//			}
//			
//		});
//		
//		okButton.addActionListener((ActionEvent e) -> {
//        	allDinsToShow.clear();
//            allPinsToShow.clear();
//            try {
//            	doQuery (selectedResource);
//            } catch (HeadlessException e1) {
//            	JOptionPane.showMessageDialog(queryFrame,
//                	"Unable to complete the query: " + e1.getMessage(),
//                    "Error completing the query", JOptionPane.ERROR_MESSAGE);
//            }
//            queryFrame.dispose();
//        });
//		okButton.setEnabled(false);
//		
//		JButton cancelButton = new JButton ("Cancel");
//		cancelButton.addActionListener((ActionEvent arg0) -> {
//        	queryFrame.setVisible(false);
//        });
//		JPanel buttonPanel = new JPanel();
//		buttonPanel.add(okButton);
//		buttonPanel.add(cancelButton);
//		
//		
//		JLabel varTitle = new JLabel("Select a variable...");
//		varQueryPanel.setLayout(new BorderLayout());
//		varQueryPanel.add(varTitle, BorderLayout.NORTH);
//		varQueryPanel.add(nameMenu, BorderLayout.CENTER);
//		varQueryPanel.setPreferredSize(PREFERRED_MENU_SIZE);
//		
//		JPanel valueQueryPanel = new JPanel();
//		valueQueryPanel.setLayout(new BorderLayout());
//		JLabel valueTitle = new JLabel("Value");
//		valueQueryPanel.add(valueTitle, BorderLayout.NORTH);
//		valueField = new JTextField();
//		valueField.setEditable(false);
//		valueQueryPanel.add(valueField, BorderLayout.CENTER);
//		valueQueryPanel.setPreferredSize(PREFERRED_MENU_SIZE);
//		
//		queryFrame.getContentPane().add(varQueryPanel, BorderLayout.WEST);
//		queryFrame.getContentPane().add(valueQueryPanel, BorderLayout.EAST);
//		queryFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
//		queryFrame.pack();
//		queryFrame.setLocationRelativeTo(invokingComponent);
//		queryFrame.setVisible(true);
//		
//	}
//	public void performQuery(JenaLoader dbLoader, String processName,
//	String timestamp, Component invokingComponent) {
//initQuery (dbLoader, processName, timestamp);
//
//SortedSet<String> dinNames = dbLoader.getAllDinNames(processName, timestamp);
//
//Vector<String> names = new Vector<>();
//dinNames.stream().forEach((dinName) -> {
//	names.add(dinName);
//});
//
//queryFrame = new JFrame (getFrameTitle());
//final JPanel varQueryPanel = new JPanel();
//nameMenu = new JComboBox<>(names);
//final JButton okButton = new JButton("OK");
//
//nameMenu.addActionListener(new ActionListener() {
//
//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//		showValueOf (nameMenu.getSelectedItem().toString());
//		okButton.setEnabled(true);
//	}
//	
//});
//
//okButton.addActionListener((ActionEvent e) -> {
//	allDinsToShow.clear();
//    allPinsToShow.clear();
//    try {
//    	doQuery (selectedResource);
//    } catch (HeadlessException e1) {
//    	JOptionPane.showMessageDialog(queryFrame,
//        	"Unable to complete the query: " + e1.getMessage(),
//            "Error completing the query", JOptionPane.ERROR_MESSAGE);
//    }
//    queryFrame.dispose();
//});
//okButton.setEnabled(false);
//
//JButton cancelButton = new JButton ("Cancel");
//cancelButton.addActionListener((ActionEvent arg0) -> {
//	queryFrame.setVisible(false);
//});
//JPanel buttonPanel = new JPanel();
//buttonPanel.add(okButton);
//buttonPanel.add(cancelButton);
//
//
//JLabel varTitle = new JLabel("Select a variable...");
//varQueryPanel.setLayout(new BorderLayout());
//varQueryPanel.add(varTitle, BorderLayout.NORTH);
//varQueryPanel.add(nameMenu, BorderLayout.CENTER);
//varQueryPanel.setPreferredSize(PREFERRED_MENU_SIZE);
//
//JPanel valueQueryPanel = new JPanel();
//valueQueryPanel.setLayout(new BorderLayout());
//JLabel valueTitle = new JLabel("Value");
//valueQueryPanel.add(valueTitle, BorderLayout.NORTH);
//valueField = new JTextField();
//valueField.setEditable(false);
//valueQueryPanel.add(valueField, BorderLayout.CENTER);
//valueQueryPanel.setPreferredSize(PREFERRED_MENU_SIZE);
//
//queryFrame.getContentPane().add(varQueryPanel, BorderLayout.WEST);
//queryFrame.getContentPane().add(valueQueryPanel, BorderLayout.EAST);
//queryFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
//queryFrame.pack();
//queryFrame.setLocationRelativeTo(invokingComponent);
//queryFrame.setVisible(true);
//
//}
public void performQuery(Component invokingComponent) {
//	
//	SortedSet<String> dinNames = dbLoader.getAllDinNames(processName, timestamp);
//	
//	Vector<String> names = new Vector<>();
//    dinNames.stream().forEach((dinName) -> {
//    	names.add(dinName);
//    });
//	
//	queryFrame = new JFrame (getFrameTitle());
//	final JPanel varQueryPanel = new JPanel();
//	nameMenu = new JComboBox<>(names);
//	final JButton okButton = new JButton("OK");
//	
//	nameMenu.addActionListener(new ActionListener() {
//
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			showValueOf (nameMenu.getSelectedItem().toString());
//			okButton.setEnabled(true);
//		}
//		
//	});
//	
//	okButton.addActionListener((ActionEvent e) -> {
//    	allDinsToShow.clear();
//        allPinsToShow.clear();
//        try {
//        	doQuery (selectedResource);
//        } catch (HeadlessException e1) {
//        	JOptionPane.showMessageDialog(queryFrame,
//            	"Unable to complete the query: " + e1.getMessage(),
//                "Error completing the query", JOptionPane.ERROR_MESSAGE);
//        }
//        queryFrame.dispose();
//    });
//	okButton.setEnabled(false);
//	
//	JButton cancelButton = new JButton ("Cancel");
//	cancelButton.addActionListener((ActionEvent arg0) -> {
//    	queryFrame.setVisible(false);
//    });
//	JPanel buttonPanel = new JPanel();
//	buttonPanel.add(okButton);
//	buttonPanel.add(cancelButton);
//	
//	
//	JLabel varTitle = new JLabel("Select a variable...");
//	varQueryPanel.setLayout(new BorderLayout());
//	varQueryPanel.add(varTitle, BorderLayout.NORTH);
//	varQueryPanel.add(nameMenu, BorderLayout.CENTER);
//	varQueryPanel.setPreferredSize(PREFERRED_MENU_SIZE);
//	
//	JPanel valueQueryPanel = new JPanel();
//	valueQueryPanel.setLayout(new BorderLayout());
//	JLabel valueTitle = new JLabel("Value");
//	valueQueryPanel.add(valueTitle, BorderLayout.NORTH);
//	valueField = new JTextField();
//	valueField.setEditable(false);
//	valueQueryPanel.add(valueField, BorderLayout.CENTER);
//	valueQueryPanel.setPreferredSize(PREFERRED_MENU_SIZE);
//	
//	queryFrame.getContentPane().add(varQueryPanel, BorderLayout.WEST);
//	queryFrame.getContentPane().add(valueQueryPanel, BorderLayout.EAST);
//	queryFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
//	queryFrame.pack();
//	queryFrame.setLocationRelativeTo(invokingComponent);
//	queryFrame.setVisible(true);
	
}
	
	/**
	 * @return the string to display as the window title
	 */
	protected abstract String getFrameTitle();

	/**
	 * Execute the query
	 * @param resource the resource the user selected from the menu
	 */
	protected void doQuery(DataInstanceNode dNode) {
		loadNodes(dNode);
		displayDDG(dNode);
	}
	
	/**
	 * Load the nodes that are returned by the query and display the resulting DDG.
	 * @param qResource The Jena resource for the data node whose derivation information
	 * 		is being loaded
	 * @param dataName the name of the node whose derivation is being loaded
	 * @param dataValue the value of the node whose derivation is being loaded
	 */
	public void doQuery(DataInstanceNode qResource, String dataName, String dataValue) {
		loadNodes(qResource);
		displayDDG(qResource, dataName, dataValue);
	}

	/**
	 * Loads the nodes that correspond to the given query beginning at 
	 * the resource passed in.
	 * @param qResource the resource at which the query should start
	 */
	//protected abstract void loadNodes(Resource qResource);
	protected abstract void loadNodes(DataInstanceNode dNode);

	/**
	 * Puts all the values associated with the name into the valueMenu
	 * @param dinName the name whose values are displayed
	 */
	private void showValueOf(String dinName) {
		SortedSet<Resource> dins = dbLoader.getDinsNamed(processName, timestamp, dinName);
		assert dins.size() == 1;
		selectedResource = dins.first();
		valueField.setText(dbLoader.retrieveDinValue(selectedResource));
	}
	
	/**
	 * Displays the ddg for the query result
	 */
	protected void displayDDG() {
		displayDDG (null);
	}

	/**
	 * Displays the ddg resulting from a query using the name and value passed in
	 * @param selectedName the node name that the query is done on
	 * @param selectedValue the node value that the query is done on
	 */
	protected void displayDDG(String selectedName, String selectedValue) {
		displayDDG (null, selectedName, selectedValue);
	}
	/**
	 * Displays the ddg for the query result
	 * @param rootResource the resource that should appear at the top of the DDG
	 */
	protected void displayDDG(final DataInstanceNode rootNode) {
		String selectedName = nameMenu.getSelectedItem().toString();
		String selectedValue = valueField.getText();
		displayDDG(rootNode, "Name", "Value");
	}

	private void displayDDG(final DataInstanceNode rootNode, String selectedName, String selectedValue) {
		System.out.println("In DataQuery.displayDDG");
		final ProvenanceData provData = new ProvenanceData(processName); 
		graphBuilder = new PrefuseGraphBuilder(false, true);
		String queryString = getQuery(rootNode.getName(), "");
		graphBuilder.setTitle(queryString, "");
		
		provData.addProvenanceListener(graphBuilder);
		provData.setQuery(queryString);
		new Thread() {
			@Override
			public void run() {
				System.out.println("Running load query thread");
				// Load the database in a separate thread so that it does not tie
				// up the Swing thread.  This allows us to see the DDG being 
				// built incrementally as it is read from the DB.
				//dbLoader.loadAttributes(processName, timestamp, provData);
				DataQuery.this.builder = LanguageConfigurator.createDDGBuilder(language, "Query result", provData, null);
				graphBuilder.createLegend(language);
				DDGExplorer.loadingDDG();
				System.out.println("Loading query result");
				loadQueryResult(provData, rootNode);
				System.out.println("Done loading query result");
				DDGExplorer.doneLoadingDDG();
				//also used to set Save to DB as disabled. This should be handled somewhere else
			}
		}.start();
	}

	/**
	 * Load the query result from the database into the ddg
	 * @param pd the ddg
	 * @param rootResource the root of the ddg
	 */
	protected void loadQueryResult(ProvenanceData pd, DataInstanceNode rootNode) {
    	Collections.sort(allPinsToShow, (ProcedureInstanceNode res0, ProcedureInstanceNode res1) -> res0.getId() - res1.getId());
		for (ProcedureInstanceNode res : allPinsToShow) {
			ProcedureInstanceNode pin = addProcResourceToProvenance(res, pd);
			System.out.println("loadQueryResult added: " + pin);
			loadInputs(pd, rootNode, res, pin);
			loadOutputs(pd, res, pin);
		}
		
		// If graph contains no procedure nodes, display a message.  It might contain
		// one or more data nodes.
		if (allPinsToShow.isEmpty()) {
			DDGExplorer.showErrMsg(getSingletonMessage("name", "value"));
		}
		
		// Cause the drawing to occur
		pd.notifyProcessFinished();
		notifyQueryFinished(graphBuilder.getPanel().getName(), graphBuilder.getPanel());
	}
	
	public ProcedureInstanceNode addProcResourceToProvenance(ProcedureInstanceNode res, ProvenanceData provData) {
		int id = res.getId();
		String type = res.getType();
		String name = res.getName();
		String value;
		Object procDef = res.getProcedureDefinition();
		if (procDef == null) {
			value = null;
		}
		else {
			value = procDef.toString();
		}
		Double elapsedTime = res.getElapsedTime();
		SourcePos sourcePos = res.getSourcePos();
		ProcedureInstanceNode pin = addSinToProvData(name,
				type, value, elapsedTime, sourcePos, res, id, provData);
		System.out.println("addProcResourceToProvenance: Adding sin" + id + ": "
				+ pin.toString());
		return pin;
	}

	private ProcedureInstanceNode addSinToProvData(String name, String type, String value, double elapsedTime, SourcePos sourcePos,
			ProcedureInstanceNode res, int id, ProvenanceData provData) {
		if (!nodesToResContains(res, provData)) {
			ProcedureInstanceNode pin = createProcedureInstanceNode (name, type, id, value, elapsedTime, sourcePos);
			System.out.println("addSinToProvData " + pin);
			provData.addPIN(res, pin);
			return pin;
		}

		return null;
	}

	private static boolean nodesToResContains(ProcedureInstanceNode r, ProvenanceData provData) {
		return provData.containsResource(r);
	}
	
	private static boolean nodesToResContains(DataInstanceNode r, ProvenanceData provData) {
		return provData.containsResource(r);
	}
	
	protected ProcedureInstanceNode createProcedureInstanceNode (String name, String type, int id, String procDef, double elapsedTime, SourcePos sourcePos) {
		return builder.addProceduralNode(type, id, name, procDef, elapsedTime, sourcePos);
	}
	

	

	/**
	 * Load all of the outputs of a procedure node that we want to include from the database
	 * @param pd the ddg
	 * @param pin the procedure node whose outputs are examined
	 */
	private void loadOutputs(ProvenanceData pd, ProcedureInstanceNode oldNode, ProcedureInstanceNode newNode) {
		System.out.println("In loadOutputs");
		Iterator<DataInstanceNode> outputs = oldNode.outputParamValues();
		while (outputs.hasNext()) {
			DataInstanceNode nextData = outputs.next();
			if (allDinsToShow.contains(nextData)) {
				if (!nodesToResContains(nextData, pd)) {
					DataInstanceNode din = addDataResourceToProvenance(nextData, pd);
					System.out.println("loadOutputs added: " + din);
					newNode.addOutput(din.getName(), din);
				}
			}
		}
	}
	
	public DataInstanceNode addDataResourceToProvenance(DataInstanceNode dataResource, ProvenanceData provData) {
		DataInstanceNode din;
		String name = dataResource.getName();
		int dinId= dataResource.getId();
		String type = dataResource.getType();
		String currentVal = dataResource.getValue().toString();
		String timestamp = dataResource.getCreatedTime();
		String location = dataResource.getLocation();
		din = addDinToProvData(
			name, type, dataResource,
			currentVal, dinId, timestamp, provData, location);
		return din;
	}

	private DataInstanceNode addDinToProvData(String currentName,
			String currentType, DataInstanceNode currentRes, String currentVal, int id, String dataTimestamp, ProvenanceData provData, String location) {

		DataInstanceNode din = createDataInstanceNode(currentName, currentType, id, currentVal, dataTimestamp, location);
		System.out.println("addDinToProvData " + id);
		provData.addDIN(currentRes, din);
		return din;
	}

	protected DataInstanceNode createDataInstanceNode(String name, String type,
			int id, String currentVal, String dataTimestamp, String location) {
		return builder.addDataNode(type, id, name, currentVal, dataTimestamp, location);
	}

	/**
	 * Load all of the inputs of a procedure node that we want to include from the database
	 * @param pd the ddg
	 * @param rootResource the root of the ddg, if the root is a data node.  This will be
	 * 	null if the root is a procedure node
	 * @param pin the procedure node whose outputs are examined
	 */
	private void loadInputs(ProvenanceData pd, DataInstanceNode rootNode,
			ProcedureInstanceNode origNode, ProcedureInstanceNode newNode) {
		System.out.println("In loadInputs");
		Iterator<DataInstanceNode> inputs = origNode.inputParamValues();
		while (inputs.hasNext()) {
			DataInstanceNode nextInput = inputs.next();
			if (allDinsToShow.contains(nextInput)) {
				DataInstanceNode din = loadDin(pd, rootNode, nextInput);
				newNode.addInput(din.getName(), din);
				din.addUserPIN(newNode);
			}
		}
	}

	private DataInstanceNode loadDin(ProvenanceData pd, Node rootNode,
			DataInstanceNode inputNode) {
		// We may have already loaded it as it might be an output previously loaded, or
		// it might be an input to more than one procedure node.
		DataInstanceNode din = (DataInstanceNode) pd.getResource(inputNode);
		if (din != null) {
			return din;
		}
		
		// If it is not yet loaded, load it.
		din = addDataResourceToProvenance(inputNode, pd);
			
		// If this node should be the root, let the ddg know
		if (inputNode.equals(rootNode)) {
			pd.setRoot(din);
		}
		return din;
	}

	/**
	 * Get a description of the query performed
	 * @param name the name of the data item
	 * @param value the value of the data item
	 * @return a description of the query
	 */
	protected abstract String getQuery(String name, String value);
	
	/**
	 * Get a message to display if there is just one node in the query result
	 * @param name the name of the data item
	 * @param value the value of the data item
	 * @return the message to display
	 */
	protected abstract String getSingletonMessage(String name, String value);
	
	/**
	 * Adds the resource to the list of data resources to load from the db if
	 * it is not already there.  Does nothing if it is in the list already.
	 * @param res the resource to add
	 */
	protected void showDin(DataInstanceNode res) {
		if (!allDinsToShow.contains(res)) {
			allDinsToShow.add(res);			
		}
	}
	
	/**
	 * @return the number of dins that will be included in the result
	 */
	protected int numDinsToShow() {
		return allDinsToShow.size();
	}
	
	/**
	 * Get the data resource at a position 
	 * @param index the position
	 * @return the data resource
	 */
	protected DataInstanceNode getDin(int index) {
		return allDinsToShow.get(index);
	}
	
	/**
	 * Adds the resource to the list of procedure resources to load from the db if
	 * it is not there already.  Does nothing if it is already in the list.
	 * @param nextProcResource the resource
	 */
	protected void showPin(ProcedureInstanceNode nextProcResource) {
		if (!allPinsToShow.contains(nextProcResource)) {
			allPinsToShow.add(nextProcResource);
		}
	}

	/**
	 * Get the resource that corresponds to the node that create the data
	 * @param din the data resource
	 * @return the producer of the data
	 */
	protected Resource getProducer(Resource din) {
		return dbLoader.getProducer(processName, timestamp, din);
	}

	/**
	 * Get an iterator over the procedure resources that use a data resource 
	 * @param din the data resource 
	 * @return the procedure resources that use the data
	 */
	protected Iterator<Resource> getConsumers(Resource din) {
		return dbLoader.getConsumers(processName, timestamp, din);
	}

	/**
	 * Perform a query to get all the inputs to a procedure resource
	 * @param procRes the resource
	 * @param queryVarName the name to use when performing the query
	 * @return the query result
	 */
	protected ResultSet getAllInputs(Resource procRes, String queryVarName) {
		int procId = dbLoader.retrieveSinId(procRes);
		return dbLoader.getAllInputs(processName, timestamp, procId, queryVarName);
	}

	/**
	 * Perform a query to get all the outputs from a procedure resource
	 * @param procRes the resource
	 * @param queryVarName the name to use when performing the query
	 * @return the query result
	 */
	protected ResultSet getAllOutputs(Resource procRes, String queryVarName) {
		int procId = dbLoader.retrieveSinId(procRes);
		return dbLoader.getAllOutputs(processName, timestamp, procId, queryVarName);
	}

}
