package laser.ddg.visualizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputListener;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.DataBindingEvent;
import laser.ddg.DataBindingEvent.BindingEvent;
import laser.ddg.DataInstanceNode;
import laser.ddg.LanguageConfigurator;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.ProvenanceDataVisitor;
import laser.ddg.ProvenanceListener;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import laser.ddg.gui.LegendEntry;
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.Parser;
import laser.ddg.search.SearchIndex;
import laser.ddg.visualizer.DDGDisplay.AutoPanAction;
import laser.ddg.visualizer.DDGDisplay.PopupMenu;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;

/**
 * Builds a visual DDG graph using prefuse.
 *
 * @author Barbara Lerner, Antonia Miruna Oprescu
 *
 */
public class PrefuseGraphBuilder implements ProvenanceListener, ProvenanceDataVisitor {
	// Smallest value used for a data node
	private static final int MIN_DATA_ID = ((Integer.MAX_VALUE) / 3) * 2;

	// Smallest value for a node that represents a collapsed step
	private static final int MIN_STEP_NODE_ID = Integer.MAX_VALUE / 3;

	private static final String GRAPH = "graph";
	static final String GRAPH_NODES = GRAPH + ".nodes";
	private static final String GRAPH_EDGES = GRAPH + ".edges";

	/*  Colors used in drawing the graph */
	public static final int DATA_FLOW_COLOR = ColorLib.rgb(255, 0, 0);
	public static final int CONTROL_FLOW_COLOR = ColorLib.rgb(0, 0, 148);  // ColorLib.rgb(0, 255, 0);
	private static final int SIMPLE_HANDLER_COLOR = ColorLib.rgb(140, 209, 207);
	private static final int VIRTUAL_COLOR = ColorLib.rgb(217, 132, 181);
	public static final int EXCEPTION_COLOR = ColorLib.rgb(209, 114, 110);
	public static final int LEAF_COLOR = ColorLib.rgb(255, 255, 98);
	public static final int DATA_COLOR = ColorLib.rgb(175, 184, 233);
	public static final int FILE_COLOR = ColorLib.rgb(255, 204, 153);
	public static final int URL_COLOR = ColorLib.rgb(255, 204, 229);
	public static final int NONLEAF_COLOR = ColorLib.rgb(175, 217, 123);
	public static final int NONLEAF_COLOR_SELECTED_COLOR = ColorLib.rgb(190,
			190, 190);
	public static final int LEAF_COLOR_SELECTED_COLOR = ColorLib.rgb(176, 196,
			222);
	public static final int CHECKPOINT_COLOR = ColorLib.rgb(204, 255, 229);
	public static final int RESTORE_COLOR = ColorLib.rgb(102, 255, 255);

	// Colors for collapsed steps
	public static final int STEP_COLOR = ColorLib.rgb(176, 226, 255);
	//private static final int STEP_EDGE_COLOR = ColorLib.rgb(0, 0, 0);

	// Used for nodes introduced by the interpreter rather than the program
	public static final int INTERPRETER_COLOR = ColorLib.rgb(255, 254, 231);

	// The parts of the graph
	private Table nodes = new Table();
	private Table edges = new Table();
	private Graph graph;

	// True if the root has been drawn
	private boolean rootDrawn = false;

	// visualization and display tools
	private DDGVisualization vis = new DDGVisualization();
	private BufferedReader in = new BufferedReader(new InputStreamReader(
			System.in));
	//private PINClickControl pinClickControl = new PINClickControl(this);

	// display
	private DDGDisplay display = new DDGDisplay(this);
	private AutoPanAction autoPan = display.new AutoPanAction();
	private String group = Visualization.FOCUS_ITEMS;
	private int pinID;

	//display overview
	private DDGDisplay displayOverview = new DDGDisplay(this);

	//private Object lock = new Object();
	DDGPanel ddgPanel;
	private DDGLayout ddgLayout;

	// The root of the provenance graph, where layout begins
	private NodeItem root;

	// If true, execution pauses after each node is connected to the graph so the user
	// can see the updates
	private boolean incremental = true;

	// If true, means that we are drawing a data derivation, not a full DDG.
	// The graph might not contain any control flow edges, which affects the way
	// layout is done.
	private boolean dataDerivation = false;

	// Choices are prefuse.Constants.EDGE_ARROW_FORWARD and prefuse.Constants.EDGE_ARROW_REVERSE
	// FORWARD draws arrows from outputs to inputs.  REVERSE draws from inputs to outputs.
	// This is historical.  DDGs are data *derivation* graphs, so the arrows go from derived data
	// to what they are derived from.  Many users find pointing from inputs to outputs more
	// natural.
	private static final int DEFAULT_ARROW_DIRECTION = prefuse.Constants.EDGE_ARROW_REVERSE;

	// True indicates that the graph is complete
	private boolean processFinished = false;

	private ProvenanceData provData;

	private int numPins;
	
	private SearchIndex searchIndex = new SearchIndex();

	/**
	 * Creates an object that builds a visual graph.  Creates a window in which
	 * to display error messages.
	 */
	public PrefuseGraphBuilder() {
		ddgPanel = new DDGPanel();
		ddgPanel.setSearchIndex (searchIndex);
	}

	/**
	 * Creates an object that builds a visual graph.
	 * @param incremental if true, pauses after adding each node to the graph so
	 * 		that the user can see the updates
	 * @param jenaWriter the object used to write to the DB
	 */
	public PrefuseGraphBuilder (boolean incremental, DBWriter jenaWriter) {
		this.incremental = incremental;
		ddgPanel = new DDGPanel(jenaWriter);
		ddgPanel.setSearchIndex (searchIndex);
	}

	/**
	 * Creates an object that builds a visual graph.
	 * @param incremental if true, pauses after adding each node to the graph so
	 * 		that the user can see the updates
	 * @param dataDerivation if true, indicates that the graph being drawn represents
	 * 		a data derivation, not a full DDG.
	 */
	public PrefuseGraphBuilder (boolean incremental, boolean dataDerivation) {
		this.incremental = incremental;
		this.dataDerivation = dataDerivation;
		ddgPanel = new DDGPanel();
		ddgPanel.setSearchIndex (searchIndex);
	}

	/**
	 * return the DDGPanel to place into the
	 * DDGExplorer's tabbed pane
	 * @return ddgPanel
	 */
	public DDGPanel getPanel(){
		return ddgPanel;
	}

	/**
	 * save DDG to the database
	 * (method called in DDGTab)
	 */
	public void saveToDB(){
		ddgPanel.saveToDB();
	}

	/**
	 * check if DDG is already in the database
	 * (needed for DDGTab)
	 * @return boolean inside DB
	 */
	public boolean alreadyInDB(){
		return ddgPanel.alreadyInDB();
	}

	/**
	 * Sets the title displayed in the window
	 * @param name The name of the program that created the DDG
	 * @param timestamp the timestamp when the DDG was created
	 */
	public void setTitle(String name, String timestamp) {
		ddgPanel.setTitle(name, timestamp);
	}

	/**
	 * @return the pinID
	 */
	public int getPinID() {
		return pinID;
	}

	private static String getStepNameFromFinishNode(String nodeFinishName) {
		return nodeFinishName.substring(0,
				nodeFinishName.lastIndexOf("Finish"));
	}

	private String getStepNameFromFinishNode(Node finishNode) {
		return getStepNameFromFinishNode(PrefuseUtils.getName(finishNode));
	}

	/**
	 * adds a step node to the graph
	 *
	 * @param name
	 * @param string 
	 * @return the id of the node just added
	 */
	private NodeItem addCollapsedNode(String name, String value, String time) {

		int rowNum = nodes.addRow();
		int id = rowNum + MIN_STEP_NODE_ID;

		nodes.setString(rowNum, PrefuseUtils.TYPE, PrefuseUtils.STEP);
		nodes.setInt(rowNum, PrefuseUtils.ID, id);
		nodes.setString(rowNum, PrefuseUtils.NAME, name);
		nodes.setString(rowNum, PrefuseUtils.VALUE, value);
		nodes.setString(rowNum, PrefuseUtils.TIMESTAMP, time);

		return getNode(id);
	}

	/**
	 * updates the focus group to the node which is being added to the DDG
	 *
	 * @param nodeId
	 */
	private void updateFocusGroup(int nodeId) {
		synchronized (vis) {
			TableNodeItem item = getTableNodeItem(nodeId);
			assert item != null;
			updateFocus(item);
		}
	}

	private void updateFocus(TableNodeItem item) {
		//System.out.println("Setting focus to " + item);
		TupleSet ts = vis.getFocusGroup(group);
		ts.setTuple(item);
		vis.run("animate");
	}

	private TableNodeItem getTableNodeItem(int nodeId) {

		Iterator<VisualItem> items = vis.items();
		VisualItem item = null;
		while (items.hasNext()) {
			item = items.next();
			if (item instanceof TableNodeItem && PrefuseUtils.getId((NodeItem) item) == nodeId) {
				return (TableNodeItem) item;
			}
		}
		return null;
	}

	/**
	 * Finds the node that has the given name
	 * @param nodeName the name of the node to search for
	 * @return the node with that name or null if no node is found
	 */
	private TableNodeItem getTableNodeItem(String nodeName) {

		Iterator<VisualItem> items = vis.items();
		VisualItem item = null;
		while (items.hasNext()) {
			item = items.next();
			if (item instanceof TableNodeItem && PrefuseUtils.getName((NodeItem) item).equals(nodeName)) {
				return (TableNodeItem) item;
			}
		}
		return null;
	}

	/**
	 * Changes the focus of the current ddg to be a particular node.  If the node
	 * is currently visible, it scrolls so the node is in the center.  If the node
	 * is not currently visible, it expands the entire ddg and then scrolls to
	 * the desired node.
	 * @param nodeName the name of the node to focus on
	 */
	public void focusOn(String nodeName) {
		TableNodeItem focusNode = getTableNodeItem(nodeName);
		assert focusNode != null;
		TableNodeItem tableNode = focusNode;
		if (!tableNode.isVisible()) {
			//System.out.println("In focusOn if statement (Collapse)");
			collapse(root);
			NodeItem expandedRoot = expandRecursively(vis.getCollapsedStartFinish(root));
			layout(expandedRoot);
		}
		updateFocus(focusNode);
		//System.out.println("Node name: " + nodeName + "\n" + "XStart: " + focusNode.getStartX() + " XEnd: " + focusNode.getEndX() + "\n" + "YStart: " + focusNode.getStartY() + " YEnd: " + focusNode.getEndY() + "\n");
		repaint();
	}

	NodeItem getNode(int nodeId) {
		Iterator items = vis.items();
		Object item = null;
		while (items.hasNext()) {
			item = items.next();
			if (item instanceof Node && PrefuseUtils.getId((Node)item) == nodeId) {
				return (NodeItem) item;
			}
		}
		return null;
	}

	/**
	 * Build the visual graph
	 *
	 * @param ddg
	 *            the data derivation graph data
	 */

	private void buildGraph(ProvenanceData ddg) {
		provData = ddg;
		addNodesAndEdges(ddg);

		graph = new Graph(nodes, edges, true, PrefuseUtils.ID, PrefuseUtils.SOURCE, PrefuseUtils.TARGET);

	}

	/**
	 * Builds a visual ddg from a textual ddg in a file
	 *
	 * @param file
	 *            the file containing the ddg
	 * @throws IOException
	 *             if the file cannot be read
	 */
	private void buildGraph(File file) throws IOException {
		Parser parser = new Parser(file, this);
		parser.addNodesAndEdges();
		graph = new Graph(nodes, edges, true, PrefuseUtils.ID, PrefuseUtils.SOURCE, PrefuseUtils.TARGET);

	}

	private void buildNodeAndEdgeTables() {
		synchronized (vis) {
			nodes.addColumn(PrefuseUtils.TYPE, String.class);
			nodes.addColumn(PrefuseUtils.ID, int.class);
			nodes.addColumn(PrefuseUtils.NAME, String.class);
			nodes.addColumn(PrefuseUtils.VALUE, String.class);
			nodes.addColumn(PrefuseUtils.TIMESTAMP, String.class);
			nodes.addColumn(PrefuseUtils.LOCATION, String.class);
			nodes.addColumn(PrefuseUtils.LINE, int.class);
			edges.addColumn(PrefuseUtils.TYPE, String.class);
			edges.addColumn(PrefuseUtils.SOURCE, int.class);
			edges.addColumn(PrefuseUtils.TARGET, int.class);
		}
	}

	private void addNodesAndEdges(ProvenanceData ddg) {
		ddg.visitPins(this);
		ddg.visitDins(this);
		ddg.visitDataflowEdges(this);

	}

	@Override
	public void visitPin(ProcedureInstanceNode pin) {
		addNode(pin.getType(), pin.getId(),
				pin.getNameAndType(), null, pin.getElapsedTime(), null, pin.getLineNumber());
		provData.visitControlFlowEdges(pin, this);
		numPins++;
	}

	@Override
	public void visitDin(DataInstanceNode din) {
		addNode(din.getType(), din.getId() + numPins,
				din.getName(), din.getValue().toString(),din.getCreatedTime(), din.getLocation(), -1);
	}

	@Override
	public void visitControlFlowEdge(ProcedureInstanceNode predecessor,
			ProcedureInstanceNode successor) {
		addEdge(PrefuseUtils.CONTROL_FLOW, successor.getId(), predecessor.getId());
	}

	@Override
	public void visitInputEdge(DataInstanceNode input,
			ProcedureInstanceNode consumer) {
		addEdge(PrefuseUtils.DATA_FLOW, consumer.getId(), input.getId() + numPins);
	}

	@Override
	public void visitOutputEdge(ProcedureInstanceNode producer,
			DataInstanceNode output) {
		addEdge(PrefuseUtils.DATA_FLOW, output.getId() + numPins, producer.getId());
	}

	/**
	 * Adds a node to the prefuse graph
	 *
	 * @param type
	 *            the type of node
	 * @param id
	 *            the node's id
	 * @param name
	 *            the node's name
	 * @param value
	 * 			value of the node (could be null)
	 * @param time
	 * 			timestamp of the node (could be null)
	 * @param location if this is a file node, this will be the full path to the
	 * 		original file.  If it is not a file node, it will be null
	 * @param the line number in the script where the node is derived from
	 * @return the row of the table where the new node is added
	 */
	public int addNode(String type, int id, String name, String value, double time, String location, int lineNum) {
		String formattedTime = PrefuseUtils.elapsedTimeFormat.format(time);
		return addNode (type, id, name, value, formattedTime, location, lineNum);
	}
	
	/**
	 * Adds a node to the prefuse graph
	 *
	 * @param type
	 *            the type of node
	 * @param id
	 *            the node's id
	 * @param name
	 *            the node's name
	 * @param value
	 * 			value of the node (could be null)
	 * @param time
	 * 			timestamp of the node (could be null)
	 * @param location if this is a file node, this will be the full path to the
	 * 		original file.  If it is not a file node, it will be null
	 * @param the line number in the script where the node is derived from
	 * @return the row of the table where the new node is added
	 */
	public int addNode(String type, int id, String name, String value, String time, String location, int lineNum) {
		try {
			synchronized (vis) {
				if (id < 1) {
					DDGExplorer.showErrMsg("Adding node " + id + " " + name + "\n");
					DDGExplorer.showErrMsg("*** ERROR negative id " + id + " for node " + name + " !!\n\n");
				}
				if (getNode(id) != null) {
					DDGExplorer.showErrMsg("Adding node " + id + " " + name + "\n");
					DDGExplorer.showErrMsg("*** ERROR node id " + id + " for node " + name + " already in use!!\n\n");
				}
				int rowNum = nodes.addRow();
				nodes.setString(rowNum, PrefuseUtils.TYPE, type);
				nodes.setInt(rowNum, PrefuseUtils.ID, id);
				nodes.setString(rowNum, PrefuseUtils.NAME, name);
				nodes.setString(rowNum, PrefuseUtils.VALUE, value);
				nodes.setString(rowNum, PrefuseUtils.TIMESTAMP, time);
				nodes.setString(rowNum, PrefuseUtils.LOCATION, location);
				nodes.setInt(rowNum, PrefuseUtils.LINE, lineNum);

				searchIndex.addToSearchIndex(type, id, name, time);
				return rowNum;
			}
		} catch (Exception e) {
			DDGExplorer.showErrMsg("Adding node " + id + " " + name + "\n");
			DDGExplorer.showErrMsg("*** Error adding node *** \n ");
			throw new IllegalArgumentException(e);
		}

		// write to a file
		/* outFile.println(id+" \""+name+"\" "+type); */
	}

	/**
	 * Adds a node to the prefuse graph
	 *
	 * @param type
	 *            the type of node
	 * @param id
	 *            the node's id
	 * @param name
	 *            the node's name
	 * @param value
	 * 			value of the node (could be null)
	 * @param location
	 * 			original location of the file (could be null)
	 * @param lineNum
	 * 		    line number that the corresponding code is on in the script.  Is -1 if not recorded.
	 * @return the row of the table where the new node is added
	 */
	public int addNode(String type, int id, String name, String value, String location, int lineNum) {
		return addNode (type, id, name, value, null, location, lineNum);
	}

	/**
	 * Adds an edge to a prefuse ddg
	 *
	 * @param type
	 *            the type of the edge
	 * @param source
	 *            the source of the edge
	 * @param target
	 *            the target of the edge
	 */
	public void addEdge(String type, int source, int target) {
		try {
			synchronized (vis) {
				if (getNode(source) == null) {
					DDGExplorer.showErrMsg("Adding edge between " + source + " and " + target + "\n");
					DDGExplorer.showErrMsg("*** ERROR:  source node " + source + " does not exist!!\n\n");
				}
				if (getNode(target) == null) {
					DDGExplorer.showErrMsg("Adding edge between " + source + " and " + target + "\n");
					DDGExplorer.showErrMsg("*** ERROR:  target node " + target + " does not exist!!\n\n");
				}
				int rowNum = edges.addRow();
				edges.setString(rowNum, PrefuseUtils.TYPE, type);
				edges.setInt(rowNum, PrefuseUtils.SOURCE, source);
				edges.setInt(rowNum, PrefuseUtils.TARGET, target);
				// write to a file

//				if(type.equals("CF")) {
//					System.out.println("Adding control flow edge from " + source+" to "+target);
//				}
//
//				else if(type.equals("DF")) {
//				   System.out.println("Adding data flow edge from " + source+ " to "+ target);
//				}

			}
		} catch (Exception e) {
			DDGExplorer.showErrMsg("Adding edge between " + source + " and " + target + "\n");
			DDGExplorer.showErrMsg("*** Error adding the edge ***");
			throw new IllegalArgumentException(e);
		}

	}

	/**
	 * Display a DDG visually
	 *
	 * @param ddg
	 *            the ddg to display
	 */
	public void drawGraph(ProvenanceData ddg) {


		// -- 1. load the data ------------------------------------------------

		synchronized (vis) {
			//System.out.println("Building node and edge tables.");
			buildNodeAndEdgeTables();

			//System.out.println("Building graph");
			buildGraph(ddg);

			//System.out.println("Drawing graph");
			initializeDisplay();

			// assign the colors
			vis.run("color");
			// start up the animated layout
			vis.run("layout");
			// do the repaint
			vis.run("repaint");
		}

	}

	private void initializeDisplay() {
		// -- 2. the visualization --------------------------------------------

		vis.add(GRAPH, graph);
		vis.setInteractive(GRAPH_EDGES, null, false);

		// -- 3. the renderers and renderer factory ---------------------------

		vis.setRenderer(DEFAULT_ARROW_DIRECTION, false);

		// -- 4. the processing actions ---------------------------------------

		ActionList color = assignColors();

		// create an action list with an animated layout
		ActionList layout = new ActionList();
		ddgLayout = new DDGLayout(GRAPH, dataDerivation);
		layout.add(ddgLayout);

		ActionList repaint = new ActionList();
		repaint.add(new RepaintAction());

		// add the actions to the visualization
		vis.putAction("color", color);
		vis.putAction("layout", layout);
		vis.putAction("repaint", repaint);

		// -- 5. the display and interactive controls -------------------------
		// DDGDisplay
		display.setVisualization(vis);
		// display size
		display.setSize(720, 500);
		// drag individual items around
		display.addControlListener(new DragControl());
		// pan with left-click drag on background
		display.addControlListener(new PanControl());
		// zoom with right-click drag
		display.addControlListener(new ZoomControl());
		// make node and incident edges invisible
		//d.addControlListener(mControl);
		display.addControlListener(new ExpandCollapseControl(this));
		//d.addControlListener(pinClickControl);
		display.addPaintListener(new updateOverview(displayOverview));


		//set up the display's overview
		//(no drag, pan, or zoom control needed)
		displayOverview.setVisualization(vis);
		// display size
		displayOverview.setSize(175, 500);
		//To force overview's shape and zoom when bounds change
		displayOverview.addItemBoundsListener(new FitOverviewListener());
		//keep track of the display's view and draw Overview's square accordingly
		displayOverview.addPaintListener(new vfBorders(display));
		//keep track of mouse clicks to move the grey rectangle
		vfListener vfL = new vfListener(display, displayOverview);
		displayOverview.addMouseMotionListener(vfL);
		displayOverview.addMouseListener(vfL);

		// focus action
		ActionList animate = new ActionList();
		animate.add(autoPan);
		vis.putAction("animate", animate);

		// -- 6. launch the visualization -------------------------------------

		ddgPanel.displayDDG(this, vis, display, displayOverview, provData);

		// new code
		PopupMenu options = display.new PopupMenu();
		options.createPopupMenu();
	}

	private static ActionList assignColors() {
		ColorAction stroke = new ColorAction(GRAPH_NODES,
				VisualItem.STROKECOLOR);

		// map data values to colors using our provided palette
		ColorAction fill = new ColorAction(GRAPH_NODES,
				VisualItem.FILLCOLOR);
		
		// highlight node if selected from search results
		fill.add("_highlight", ColorLib.rgb(193,253,51));
		
		fill.add(ExpressionParser.predicate("Type = 'Binding'"),
				INTERPRETER_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Start'"),
				NONLEAF_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Finish'"),
				NONLEAF_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Interm'"),
				NONLEAF_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Leaf'"), LEAF_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Operation'"), LEAF_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Data'"), DATA_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Snapshot'"), DATA_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'CheckpointFile'"), FILE_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'File'"), FILE_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'URL'"), URL_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Exception'"),
				EXCEPTION_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'SimpleHandler'"),
				SIMPLE_HANDLER_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'VStart'"),
				VIRTUAL_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'VFinish'"),
				VIRTUAL_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'VInterm'"),
				VIRTUAL_COLOR);
		// color for Steps
		fill.add(ExpressionParser.predicate("Type = 'Step'"), STEP_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Checkpoint'"), CHECKPOINT_COLOR);
		fill.add(ExpressionParser.predicate("Type = 'Restore'"), RESTORE_COLOR);
		
		// use black for node text
		ColorAction text = new ColorAction(GRAPH_NODES,
				VisualItem.TEXTCOLOR, ColorLib.gray(0));
		// set text of highlighted node to black (or other color if you change your mind) 
		text.add("_highlight", ColorLib.rgb(0,0,0));
		
		ColorAction edgeColors = new ColorAction(GRAPH_EDGES,
				VisualItem.STROKECOLOR, ColorLib.gray(0));
		edgeColors.add(ExpressionParser.predicate("Type = 'CF'"),
				CONTROL_FLOW_COLOR);
		edgeColors.add(ExpressionParser.predicate("Type = 'DF'"),
				DATA_FLOW_COLOR);
		edgeColors.add(ExpressionParser.predicate("Type = 'StepCF'"),
				CONTROL_FLOW_COLOR);
		edgeColors.add(ExpressionParser.predicate("Type = 'StepDF'"),
				DATA_FLOW_COLOR);

		ColorAction arrowColors = new ColorAction(GRAPH_EDGES,
			VisualItem.FILLCOLOR, ColorLib.gray(200));
		arrowColors.add(ExpressionParser.predicate("Type = 'CF'"),
						CONTROL_FLOW_COLOR);
		arrowColors.add(ExpressionParser.predicate("Type = 'DF'"),
					DATA_FLOW_COLOR);
		arrowColors.add(ExpressionParser.predicate("Type = 'Step'"),
				CONTROL_FLOW_COLOR);
		arrowColors.add(ExpressionParser.predicate("Type = 'StepDF'"),
				DATA_FLOW_COLOR);
		arrowColors.add(ExpressionParser.predicate("Type = 'StepCF'"),
				CONTROL_FLOW_COLOR);


		// create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(stroke);
		color.add(fill);
		color.add(text);
		color.add(edgeColors);
		color.add(arrowColors);
		return color;
	}

	/**
	 * Adds a legend to the display for the given language.
	 * @param language the language to add the legend for
	 */
	public void createLegend(String language) {
		Class<DDGBuilder> ddgBuilderClass = LanguageConfigurator.getDDGBuilder(language);
		try {
			ArrayList<LegendEntry> nodeLegend = (ArrayList<LegendEntry>) ddgBuilderClass.getMethod("createNodeLegend").invoke(null);
			ArrayList<LegendEntry> edgeLegend = (ArrayList<LegendEntry>) ddgBuilderClass.getMethod("createEdgeLegend").invoke(null);
			ddgPanel.drawLegend(nodeLegend, edgeLegend);
		} catch (Exception e) {
			System.out.println("Can't create legend");
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the prefuse tables.
	 */
	@Override
	public void processStarted(String processName, ProvenanceData provData) {
		// initialize file
		/* initFile(); */

		// Do not synchronize on vis in this method.  It results in deadlock.
		// We are just initializing the display here, not actually drawing a graph.

		this.provData = provData;
		buildNodeAndEdgeTables();
		graph = new Graph(nodes, edges, true, PrefuseUtils.ID,
				PrefuseUtils.SOURCE, PrefuseUtils.TARGET);
		initializeDisplay();
	}

	/**
	 * Initializes the prefuse tables.
	 */
	@Override
	public void processStarted(String processName, ProvenanceData provData, String timestamp, String language) {

		// Do not synchronize on vis in this method.  It results in deadlock.
		// We are just initializing the display here, not actually drawing a graph.

		processStarted(processName, provData);
	}

	/**
	 * Repaints the finished ddg
	 */
	@Override
	public void processFinished() {


		// close file
		/* outFile.close(); */

		//System.out.println("Drawing DDG");
		processFinished = true;
		display.stopRefocusing();
		displayOverview.stopRefocusing();
		if (!incremental) {
			drawFullGraph();
		}
		repaint();
	}

	/**
	 * Adds a node to the visualization
	 */
	@Override
	public void procedureNodeCreated(ProcedureInstanceNode pin) {
		//System.out.println("Adding procedure node " + pin.getName());
		synchronized (vis) {
			int pinId = pin.getId();
			if (pinId >= MIN_DATA_ID) {
				throw new RuntimeException("PIN id is too big for prefuse!");
			}

			Object procDef = pin.getProcedureDefinition();
			String procName = null;
			if (procDef != null && procDef instanceof String) {
				procName = (String) procDef;
			}

			//add the procedure node passing in null value since pin's do not have values
			addNode(pin.getType(), pinId, pin.getNameAndType(),procName, pin.getElapsedTime(), "", pin.getLineNumber());
			if (root == null) {
				root = getNode(pinId);
				//System.out.println("procedureNodeCreated:  root set to " + root);
			}

			// Draw the root node immediately, but delay drawing the other nodes
			// until
			// there is an edge connecting them. Otherwise, they just go in the top
			// left
			// corner of the window.
			if (incremental && !rootDrawn) {
				vis.run("layout");
				//System.out.println("Updating focus from procedureNodeCreated");
				updateFocusGroup(pinId);
				repaint();
				rootDrawn = true;

			}
		}

	}

	private void repaint() {

		synchronized (vis) {
			vis.run("color");
			vis.run("repaint");
		}

		try {
			if (incremental) {
				System.out.println("Hit return to continue.");
				in.readLine();
			}
		} catch (IOException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		}

	}

	/**
	 * Draw the complete graph at once instead of incrementally.
	 */
	public void drawFullGraph() {
		synchronized (vis) {
			if (root == null) {
				setRoot();
			}
			addCollapsedNodes();
			display.stopRefocusing();
			displayOverview.stopRefocusing();
			layout(root);
		}
	}

	/**
	 * Draw the complete graph at once instead of incrementally.
	 * @param attrList
	 */
	public void drawFullGraph(Attributes attrList) {
		ddgPanel.setAttributes(attrList);
		synchronized (vis) {
			drawFullGraph();
		}
	}

	/**
	 * Re-display the graph.  Change the focus to the node passed in.
	 * @param focusNode the node to focus on
	 */
	void layout(Node focusNode) {
		synchronized (vis) {
			vis.run("layout");
			updateFocusGroup(PrefuseUtils.getId(focusNode));
			vis.run("color");
			vis.run("repaint");
		}
	}

	private void setRoot() {
		laser.ddg.Node rootNode = provData.getRoot();
		if (rootNode == null) {
			root = getTableNodeItem(1);
			//System.out.println("setRoot: Root set to " + root);
		}
		ddgLayout.setLayoutRoot(root);
	}

	private void setCollapsedRoot(NodeItem collapsedRoot) {
		ddgLayout.setLayoutCollapsedRoot(collapsedRoot);
	}

	@Override
	public void rootSet(laser.ddg.Node rootNode) {
		if (rootNode instanceof ProcedureInstanceNode) {
			root = getTableNodeItem(rootNode.getId());
		}
		else {
			root = getTableNodeItem (MIN_DATA_ID + rootNode.getId());
		}
		//System.out.println("rootSet:  root set to " + rootNode);
		ddgLayout.setLayoutRoot(root);
	}

	public void setSelectedProcedureNodeID(int pinID) {
		this.pinID = pinID;
	}

	/**
	 * Walks the graph to find all the finish and restore nodes.  Creates the collapsed nodes and
	 * displays those in place of the expanded versions.
	 */
	private void addCollapsedNodes() {
		Set<NodeItem> rootMembers = new HashSet<NodeItem>();
		if (root == null) {
			setRoot();
		}
		NodeItem nextRoot = root;
		Queue<NodeItem> roots = new LinkedList<NodeItem>();
		while (nextRoot != null) {
			NodeItem rootFinish = addStartFinishCollapsedNodes(nextRoot, rootMembers, 0.0);
			if (rootFinish != null) {
				NodeItem collapsedRoot = addCollapsedNode(nextRoot, rootFinish, rootMembers);

				// If the root being collapsed is the overall root of the layout, then
				// remember which node to use as the layout root when the root node
				// is collapsed.
				if (nextRoot == root) {
					setCollapsedRoot(collapsedRoot);
				}

				rootMembers = new HashSet<NodeItem>();
				Iterator<NodeItem> successorIter = rootFinish.inNeighbors();
				while (successorIter.hasNext()) {
					NodeItem successor = successorIter.next();
					if (PrefuseUtils.isProcNode(successor)) {
						roots.add(successor);
					}
				}
			}
			nextRoot = roots.poll();
		}
		addCheckpointRestoreCollapsedNodes();

		expand(root);
	}

	/**
	 * Adds a collapsed node for each restore node.  It collapses everything between
	 * the checkpoint and restore into a single node that the user can expand to
	 * see the details.
	 */
	private void addCheckpointRestoreCollapsedNodes() {
		NodeItem restoreNode = vis.getLastRestore();
		while (restoreNode != null) {
			NodeItem checkpointNode = vis.getCheckpoint(restoreNode);
			Set<NodeItem> restoreMembers = vis.getRestoreMembers(restoreNode);
			addCollapsedNode(checkpointNode, restoreNode, restoreMembers);
			restoreNode = vis.getLastRestoreBefore(restoreNode);
		}
	}

	/**
	 * Find all the nodes rooted at startNode that should belong to the
	 * same collapsed node
	 * @param startNode the start node we are searching for
	 * @return the matching finish node
	 */
	private NodeItem addStartFinishCollapsedNodes(NodeItem startNode, Set<NodeItem> memberNodes, double totalElapsedTime) {
		Queue<NodeItem> nodesReached = new LinkedList<NodeItem>();
		addSuccessorsToQueue(startNode, nodesReached);
		NodeItem finishNode = null;

		// All the nodes that will be collapsed into one step node
		memberNodes.add(startNode);

		// Do a breadth-first search from startNode
		while (!nodesReached.isEmpty()) {
			NodeItem next = nodesReached.poll();
			vis.add(next);
			String nextName = PrefuseUtils.getName(next);

			// Begin a new search when encounter a new Start node
			if (nextName.endsWith(" Start")) {
				Set<NodeItem> nestedMembers = new HashSet<NodeItem>();
				NodeItem nestedFinish = addStartFinishCollapsedNodes(next, nestedMembers, 0.0);

				if (nestedFinish != null) {
					addSuccessorsToQueue(nestedFinish, nodesReached);
					NodeItem collapsedNode = addCollapsedNode(next, nestedFinish, nestedMembers);
					memberNodes.add(collapsedNode);
					totalElapsedTime = totalElapsedTime + Double.parseDouble(PrefuseUtils.getTimestamp(collapsedNode));
				}
			}
			else {
				memberNodes.add(next);
				totalElapsedTime = totalElapsedTime + Double.parseDouble(PrefuseUtils.getTimestamp(next));

				// Remember the finish node
				if (nextName.endsWith(" Finish")) {
					finishNode = next;

					String finishName = PrefuseUtils.getName(finishNode);
					finishName = finishName.substring(finishName.indexOf('-')+1, finishName.indexOf(" Finish"));

					String startName = PrefuseUtils.getName(startNode);
					int startStarts = startName.indexOf(" Start");
					if (startStarts != -1) {
						startName = startName.substring(startName.indexOf('-')+1, startStarts);
						totalElapsedTime = totalElapsedTime + Double.parseDouble(PrefuseUtils.getTimestamp (finishNode));
						PrefuseUtils.setTimestamp(startNode, totalElapsedTime);
						PrefuseUtils.setTimestamp(finishNode, totalElapsedTime);
					}
					if (startStarts == -1 || !startName.equals(finishName)) {
						DDGExplorer.showErrMsg("Start and Finish nodes not paired up correctly.\n");
						DDGExplorer.showErrMsg("    Start = " + startName + "\n");
						DDGExplorer.showErrMsg("    Finish = " + finishName + "\n\n");
					}
				}

				// Neither a start nor a finish node.  Add its successors to
				// the nodes walked.
				else {
					addSuccessorsToQueue(next, nodesReached);
				}
			}
		}

		//System.out.println(PrefuseUtils.getName(startNode) + " member:");
		//for (Node member : memberNodes) {
		//	System.out.println("    " + PrefuseUtils.getName(member));
		//}

		return finishNode;
	}

	/**
	 * Add a collapsed node encapsulating the nodes between startNode and finishNode
	 * @param startNode A start node or a checkpoint node
	 * @param finshNode The corresponding finish or restore node
	 */
	private NodeItem addCollapsedNode(NodeItem startNode, NodeItem finishNode, Set<NodeItem> memberNodes) {
		NodeItem collapsedNode = vis.getCollapsed(startNode, finishNode);

		if (collapsedNode != null) {
			return collapsedNode;
		}

		//System.out.println("Adding collapsed node for " + startNode);

		// Adds the node
		int collapsedNodeId;
		if (PrefuseUtils.isStartNode(startNode)) {
			collapsedNode = addCollapsedNode(getStepNameFromFinishNode(finishNode), PrefuseUtils.getValue(finishNode), 
					PrefuseUtils.getTimestamp(finishNode));
		}

		else if (PrefuseUtils.isCheckpointNode(startNode)) {
			collapsedNode = addCollapsedNode(PrefuseUtils.getName(finishNode), PrefuseUtils.getValue(finishNode), 
					PrefuseUtils.getTimestamp(finishNode));
		}

		else {
			return null;
		}

		collapsedNodeId = PrefuseUtils.getId(collapsedNode);
		vis.add(collapsedNode, startNode, finishNode, memberNodes);

		// Add edges to the collapsed node
		addSuccessorEdgesToCollapsedNode(finishNode, collapsedNodeId);
		addPredecessorEdgesToCollapsedNode(startNode, collapsedNodeId);
		addDataEdgesToCollapsedNode(memberNodes, collapsedNode);

		return collapsedNode;
	}


	/**
	 * Add data edges to the collapsed node.  One for each data
	 * edge to a member node if other endpoint is not also a member, or a
	 * member of a member recursively.  Be careful to not add duplicate edges
	 * between same pair of nodes.
	 *
	 * @param memberNodes the members of the collapsed node
	 * @param collapsedNode the collapsed node we are adding edges on
	 */
	private void addDataEdgesToCollapsedNode(Set<NodeItem> memberNodes, NodeItem collapsedNode) {
		//System.out.println("Adding step data edges");

		for (Node member : memberNodes) {
			addIncomingDataEdgesToCollapsedNode(collapsedNode, member);
			addOutgoingDataEdgesFromCollapsedNode(collapsedNode, member);
		}
	}

	/**
	 * Adds an edge from a collapsed node to a data node if there is a consumer of
	 * the data inside the collapsed node, but its producer is outside the collapsed
	 * node.
	 * @param collapsedNode the node we are adding edges to
	 * @param member The member of the collapsed node whose outgoing edges we are
	 * 		searching
	 */
	private void addOutgoingDataEdgesFromCollapsedNode(Node collapsedNode, Node member) {
		int collapsedNodeId = PrefuseUtils.getId(collapsedNode);
		Iterator<Node> outNeighbors = member.outNeighbors();

		// Search the outgoing edges of one member
		while (outNeighbors.hasNext()) {
			Set<Node> edgesAddedTo = new HashSet<Node>();
			Node neighbor = outNeighbors.next();

			// Check if it is a data node
			if (PrefuseUtils.isAnyDataNode(neighbor)) {
				Iterator<Node> dataProducers = neighbor.outNeighbors();

				// Find the producer of the data
				if (dataProducers.hasNext()) {
					Node producer = dataProducers.next();

					// If the producer is outside the collapsed node, and we do not already have
					// an edge between the data node and this member, add an edge.
					if (!vis.nestedContains(collapsedNode,producer) && !edgesAddedTo.contains(neighbor)) {
						// System.out.println("Adding step edge to producer " + collapsedNode + " to " + neighbor);
						addEdge(PrefuseUtils.STEPDF, collapsedNodeId, PrefuseUtils.getId(neighbor));
						edgesAddedTo.add(neighbor);
					}
				}
			}
		}
	}

	/**
	 * Adds an edge from a data node to a collapsed node if there is a consumer of
	 * the data outside the collapsed node, but its producer is inside the collapsed
	 * node.
	 * @param collapsedNode the node we are adding edges to
	 * @param member The member of the collapsed node whose incoming edges we are
	 * 		searching
	 */
	private void addIncomingDataEdgesToCollapsedNode(Node collapsedNode, Node member) {
		int collapsedNodeId = PrefuseUtils.getId(collapsedNode);
		Set<Node> edgesAddedFrom = new HashSet<Node>();

		// Get the incoming edges for this member
		Iterator<Node> inNeighbors = member.inNeighbors();
		while (inNeighbors.hasNext()) {
			Node neighbor = inNeighbors.next();

			// Check that the edge comes from a data node
			if (PrefuseUtils.isAnyDataNode(neighbor)) {
				Iterator<Node> dataConsumers = neighbor.inNeighbors();

				// Find the consumers of the data node
				while (dataConsumers.hasNext()) {
					Node nextConsumer = dataConsumers.next();

					// If any consumer is outside the collapsed node and we do not already have an
					// edge from this data node to the collapsed node, add it.
					if (!vis.nestedContains(collapsedNode,nextConsumer) && !edgesAddedFrom.contains(neighbor)) {
						// System.out.println("Adding step edge from consumer " + neighbor + " to " + collapsedNode);
						addEdge(PrefuseUtils.STEPDF, PrefuseUtils.getId(neighbor), collapsedNodeId);
						edgesAddedFrom.add(neighbor);
						break;
					}
				}
			}
		}
	}

	/**
	 * Find the predecessors of a start node and add edges from the new collapsed node to
	 * each of the predecessors
	 * @param startNode the start node whose predecessors we are searching for
	 * @param collapsedNodeId the id of the node to add the new edges to.  This must
	 *    be the collapsed node that corresponds to the collapsing of startNode
	 */
	private void addPredecessorEdgesToCollapsedNode(Node startNode, int collapsedNodeId) {
		Iterator<Node> predecessorIter = startNode.outNeighbors();
		while (predecessorIter.hasNext()) {
			Node predecessor = predecessorIter.next();
			if (PrefuseUtils.isProcNode(predecessor)) {
				addEdge(PrefuseUtils.STEPCF, collapsedNodeId, PrefuseUtils.getId(predecessor));
			}
		}
	}

	/**
	 * Find the successors of a finish node and add edges from each successor to
	 * the new collapsed node
	 * @param finishNode the finish node whose successors we are searching for
	 * @param collapsedNodeId the id of the node to add the new edges to.  This must
	 *    be the collapsed node that corresponds to the collapsing of finishNode
	 */
	private void addSuccessorEdgesToCollapsedNode(Node finshNode, int collapsedNodeId) {
		Iterator<Node> successorIter = finshNode.inNeighbors();
		while (successorIter.hasNext()) {
			Node successor = successorIter.next();
			if (PrefuseUtils.isProcNode(successor)) {
				addEdge(PrefuseUtils.STEPCF, PrefuseUtils.getId(successor), collapsedNodeId);
			}
		}
	}

	/**
	 * Add the successors of node that are procedural nodes to the queue
	 * @param node the node whose successors are checked
	 * @param reachedNodes the queue to add the nodes to
	 */
	private static void addSuccessorsToQueue(Node node, Queue<NodeItem> reachedNodes) {
		Iterator<NodeItem> successorIter = node.inNeighbors();
		while (successorIter.hasNext()) {
			NodeItem successor = successorIter.next();
			if (PrefuseUtils.isProcNode(successor)) {
				reachedNodes.add(successor);
			}
		}
	}

	/**
	 * Expand one level, rooted at the given node.
	 * @param root the node to expand
	 * @return the start node associated with the root
	 */
	NodeItem expand(NodeItem root) {
		// Expanding a collapsed node.  In this case, the members are currently
		// not displayed, so we just need to hide the collapsed node and
		// show the members instead.
		if (PrefuseUtils.isCollapsed(root)) {
			//System.out.println("expand: Making root invisible: " + root);
			root.setVisible(false);
			showMembers(root);
			setAllDataNodeVisibility();
			return vis.getStart(root);
		}

		//System.out.println("expand:  Making root visible: " + root);
		root.setVisible(true);
		NodeItem collapsedRoot = null;
		if (PrefuseUtils.isStartNode(root) && processFinished) {
			collapsedRoot = vis.getCollapsedStartFinish(root);
			if (collapsedRoot == null) {
				String rootName = PrefuseUtils.getName(root);
				DDGExplorer.showErrMsg("Finish node missing for " + rootName.substring(0, rootName.indexOf(" Start"))+"\n\n");
			}
			else {
				//System.out.println("expand: Making collapsed node invisible: " + collapsedRoot);
				collapsedRoot.setVisible(false);
			}
		}
		//System.out.println("expand: Making visible: " + root);
		Iterator<NodeItem> successors = root.inNeighbors();
		collapse(successors);
		if (collapsedRoot != null) {
			collapseCheckpoints(collapsedRoot);
		}
		setAllDataNodeVisibility();
		return root;

	}

	/**
	 * Expand this item and all of its members recursively.
	 * @param item the node to expand
	 * @return the start node for the node we expanded
	 */
	NodeItem expandRecursively(NodeItem item) {
		//assert PrefuseUtils.isCollapsed(item);
		if (!PrefuseUtils.isCollapsed(item)) {
			collapse(item);
			item = vis.getCollapsedStartFinish(item);
		}
		//System.out.println("expandRecursively: Making Invisible: " + item);
		item.setVisible(false);
		showMembersRecursively(item);
		setAllDataNodeVisibility();
		return vis.getStart(item);
	}

	/**
	 * Display the members of a collapsed node to complete depth
	 * @param collapsedNode the node whose (recursive) members
	 *    should be shown
	 */
	private void showMembersRecursively(NodeItem collapsedNode) {
		assert PrefuseUtils.isCollapsed(collapsedNode);
		Iterator<NodeItem> memberIter = vis.getMembers(collapsedNode);
		while (memberIter.hasNext()) {
			NodeItem nextMember = memberIter.next();
			if (PrefuseUtils.isCollapsedNode(nextMember)) {
				showMembersRecursively(nextMember);
			}
			else {
				System.out.println("showMembersRecursively: Making visible: " + nextMember);
				nextMember.setVisible(true);
			}
		}
	}

	/**
	 * Show the next level of nodes
	 * @param collapsedNode the node being expanded
	 */
	private void showMembers(NodeItem collapsedNode) {
		Iterator<NodeItem> memberIter = vis.getMembers(collapsedNode);
		while (memberIter.hasNext()) {
			NodeItem nextMember = memberIter.next();
			//System.out.println("showMembers: Making visible: " + nextMember);
			nextMember.setVisible(true);
		}

		collapseCheckpoints(collapsedNode);
	}

	/**
	 * Collapses the checkpoint/restore nodes that have become visible because of
	 * expanding the collapsed node.
	 * @param collapsedNode
	 */
	private void collapseCheckpoints(NodeItem collapsedNode) {
		// Start the search for restore nodes at the finish node that corresponds to the collapsed node
		NodeItem finishNode = vis.getFinish(collapsedNode);
		if (PrefuseUtils.isRestoreNode(finishNode)) {
			collapsedNode = vis.getCollapsedStartFinish(finishNode);
			//System.out.println("collapseCheckpoints: Searching start-finish collapsed node: " + collapsedNode);
		}

		//System.out.println("collapseCheckpoints: Got finish node of collapsed node: " + finishNode);

		// Find the first restore node that precedes the finished node
		NodeItem restoreNode = vis.getLastRestoreBefore(finishNode);
//		if (restoreNode == null) {
//			System.out.println("collapseCheckpoints: No restore node before finish node");
//		}
//		else {
//			System.out.println("collapseCheckpoints: Found restore node before finish node: " + restoreNode);
//		}

		// If the restore node is inside the collapsed node, find its checkpoint counterpart.
		// If they are both inside this node, show the collapsed node.  Then search for the
		// preceding restore.
		while (restoreNode != null && vis.nestedContains(collapsedNode, restoreNode)) {
			//System.out.println("collapseCheckpoints: Found a contained restore node");
			NodeItem checkpointNode = vis.getCheckpoint(restoreNode);
			//System.out.println("collapseCheckpoints: Found checkpoint node: " + checkpointNode);

			// Do the collapse and look for preceding restore.
			if (vis.contains(collapsedNode, restoreNode) && vis.contains(collapsedNode,checkpointNode)) {
				//System.out.println("collapseCheckpoints: Found a contained checkpoint-restore pair");
				collapse(vis.getCollapsedCheckpoint(restoreNode));
				restoreNode = vis.getLastRestoreBefore(checkpointNode);
			}

			// No collapse.  Look for the restore that precedes the current restore.
			else {
				//System.out.println("collapseCheckpoints: Did not find a contained checkpoint-restore pair");
				restoreNode = vis.getLastRestoreBefore(restoreNode);
			}
		}
	}

	/**
	 * Show the collapsed and other nodes at this level, but hide members of
	 * the collapsed nodes
	 * @param successors
	 */
	private void collapse(Iterator<NodeItem> successors) {
		while (successors.hasNext()) {
			NodeItem successor = successors.next();
			if (PrefuseUtils.isStartNode(successor)) {
				collapse(successor);
			}
			else if (PrefuseUtils.isProcNode(successor)){
				collapse(successor.inNeighbors());
			}
		}
	}

	/**
	 * Show the collapsed version of this node
	 * @param item the node whose collapsed version should be displayed
	 */
	private void collapse(NodeItem item) {
		if (PrefuseUtils.isCollapsedNode(item)) {
			//System.out.println("collapse: Making item visible: " + item);
			item.setVisible(true);
			hideCollapsedMembers(item);
			if (PrefuseUtils.isStartNode(vis.getStart(item))) {
				// If we have a Checkpoint node as the start node, we are
				// in the middle of a walk up the DDG, not down it.
				collapse(item.inNeighbors());
			}
		}

		else if (PrefuseUtils.isStartNode(item)) {
			NodeItem collapsedNode = vis.getCollapsedStartFinish(item);
			if (collapsedNode != null) {
				//System.out.println("collapse: Making collapsed node visible: " + collapsedNode);
				collapsedNode.setVisible(true);
				hideCollapsedMembers(collapsedNode);
			}
		}

		else if (PrefuseUtils.isProcNode(item)){
			//System.out.println("collapse: Making proc node visible: " + item);
			item.setVisible(true);
			collapse(item.inNeighbors());
		}
	}

	/**
	 * Show the collapsed version of a node
	 * @param item the node to collapse
	 */
	void collapseStartNode(NodeItem item) {
		if (PrefuseUtils.isStart(item)) {
			collapse(item);
		}
		setAllDataNodeVisibility();
	}

	/**
	 * Recursively hide all the members of this node
	 * @param collapsedNode
	 */
	private void hideCollapsedMembers(NodeItem collapsedNode) {
		Iterator<NodeItem> members = vis.getMembers(collapsedNode);
		while (members.hasNext()) {
			NodeItem next = members.next();
			assert next != collapsedNode;
			//System.out.println("hideCollapsedMembers: Hiding member: " + next);
			next.setVisible(false);
			if (PrefuseUtils.isCollapsedNode(next)) {
				hideCollapsedMembers(next);
			}
			else if (PrefuseUtils.isRestoreNode(next) && vis.getCollapsedCheckpoint(next) != collapsedNode) {
				NodeItem checkpointRestore = vis.getCollapsedCheckpoint(next);

				// It's possible that the checkpoint/restore collapsed nodes have
				// not been created yet.
				if (checkpointRestore != null) {
					//System.out.println("hideCollapsedMembers: Hiding checkpoint/restore: " + checkpointRestore);
					checkpointRestore.setVisible(false);
				}

//				else {
//					System.out.println("hideCollapsedMembers: Did not find checkpoint/restore for: " + next);
//				}
			}
		}
	}

	/**
	 * Make a data node visible if either its producer is visible or
	 * one of its consumers is visible
	 */
	private void setAllDataNodeVisibility() {
		// Walk all the items (nodes and edges)
		Iterator graphNodes = vis.items();
		while (graphNodes.hasNext()) {
			Object next = graphNodes.next();

			// Skip the edges
			if (next instanceof NodeItem) {
				NodeItem node = (NodeItem) next;

				// Skip procedural nodes
				if (PrefuseUtils.isAnyDataNode(node)) {
					// System.out.println("Checking data " + node);

					setDataNodeVisibility(node);
				}
			}
		}

		// Iterate again looking for data nodes connected with StepDF edges.
		// We are looking for data nodes where both the source and target
		// are collapsed nodes.  The earlier code would leave those invisible.
		graphNodes = vis.items();
		while (graphNodes.hasNext()) {
			Object next = graphNodes.next();

			// If the edge goes from a collapsed STEP node to a data node, make
			// the data node visible
			if (PrefuseUtils.isStepDFEdge((VisualItem) next)) {
				EdgeItem edge = (EdgeItem) next;
				//System.out.println ("Found a DF edge.  Want to make data it points to visible!");
				//System.out.println ("How do I get the data node it points to???");
				//System.out.println ("Source: " + edge.getSourceItem());
				//System.out.println ("Target: " + edge.getTargetItem());

				NodeItem source = edge.getSourceItem();
				NodeItem target = edge.getTargetItem();
				if (PrefuseUtils.isAnyDataNode(source) && target.isVisible()) {
					//System.out.println ("setAllDataNodeVisibility: Setting StepDF source visible " + source.getString(PrefuseUtils.NAME));
					source.setVisible(true);
				}
				else {
					if (PrefuseUtils.isAnyDataNode(target) && source.isVisible()) {
						//System.out.println ("setAllDataNodeVisibility: Setting StepDF target visible " +  target.getString(PrefuseUtils.NAME));
						target.setVisible(true);
					}
				}
			}

		}
	}

	/**
	 * Makes this data node visible if either its producer or any of
	 * its consumers is visible.  If none are, the data node is invisible.
	 * @param node the node whose visibility is checked.  This must be a data node.
	 */
	private static void setDataNodeVisibility(NodeItem node) {
		// Find the producer and the consumers of the data node
		Iterator<NodeItem> dataNeighbors = node.neighbors();
		boolean visible = false;
		while (dataNeighbors.hasNext()) {
			NodeItem dataNeighbor = dataNeighbors.next();

			// If the producer or any consumer is visible, make
			// the data node visible
			if (dataNeighbor.isVisible() && !PrefuseUtils.isCollapsedNode(dataNeighbor)) {
				visible = true;
				break;
			}
		}
//		if (visible) {
//			System.out.println("setDataNodeVisibility: Making data visible: " + PrefuseUtils.getName(node));
//		}
//		else {
//			System.out.println("Making data INvisible: " + PrefuseUtils.getName(node));
//		}
		node.setVisible(visible);
	}

	/**
	 * Add a data node to the visualization. Does not redraw immediately since
	 * the node won't appear in the right place unless there is an edge
	 * connecting it to the graph.
	 */
	@Override
	public void dataNodeCreated(DataInstanceNode din) {
		synchronized(vis) {
			int dinId = din.getId() + MIN_DATA_ID;
			//add the data node, passing in the optional associated value and timestamp
			Object value = din.getValue();
			if (value == null) {
				addNode(din.getType(), dinId, din.getName(), null, din.getCreatedTime(), -1);
			}
			else {
				addNode(din.getType(), dinId, din.getName(), din.getValue().toString(), din.getCreatedTime(), -1);
			}
			NodeItem dataNode = getNode(dinId);

			if (dataDerivation && (root == null)) {
				root = dataNode;
				//System.out.println("dataNodeCreated:  root set to " + root);
			}
		}
	}

	/**
	 * Add a control flow edge to the visualization and redraw the graph
	 */
	@Override
	public void successorEdgeCreated(
			ProcedureInstanceNode predecessor, ProcedureInstanceNode successor) {
		synchronized (vis) {
			int successorId = successor.getId();
			int predecessorId = predecessor.getId();
			//System.out.println("Creating successor edge from " + successor.getNameAndType() + " to " + predecessor.getNameAndType());
			addEdge(PrefuseUtils.CONTROL_FLOW, successorId, predecessorId);
			if (!incremental) {
				return;
			}

			// Creating an edge where the predecessor is a finish node
			if (predecessor.getType().equals("Finish")) {

				Node collapsedPredecessor = vis.getCollapsedStartFinish(getNode(predecessorId));

				// If the predecessor has not been collapsed yet, do so now.
				if (collapsedPredecessor == null) {
					//System.out.println("Trying to collapse...");
					//System.out.println("Predecessor = " + getNode(predecessorId));
					//System.out.println("Successor = " + getNode(successorId));
					addCollapsedNodes();
				}

				// The predecessor was already collapsed.  Add an edge to the collapsed node
				else {
					addEdge(PrefuseUtils.STEPCF, successorId, PrefuseUtils.getId(collapsedPredecessor));
				}
			}

			// change the focus to recently added node
			vis.run("layout");
			//System.out.println("Updating focus from successorEdgeCreated");
			NodeItem collapsedSuccessor = vis.getCollapsedStartFinish(getNode(successorId));

			// The successor is not collapsed, so focus on the expanded successor
			if (collapsedSuccessor == null || !collapsedSuccessor.isVisible()) {
				updateFocusGroup(successorId);
			}

			// The successor is collapsed, so focus on the collapsed successor.
			else {
				updateFocusGroup(PrefuseUtils.getId(collapsedSuccessor));
			}
		}

		repaint();

	}

	/**
	 * Add a dataflow edge to the graph and redraw it.
	 */
	@Override
	public void bindingCreated(DataBindingEvent e) {

		synchronized (vis) {
			int dataNodeId = e.getDataNode().getId() + MIN_DATA_ID;
			int procNodeId = e.getProcNode().getId();
			if (e.getEvent() == BindingEvent.INPUT) {
				addEdge(PrefuseUtils.DATA_FLOW, procNodeId, dataNodeId);

				// if the producer of the data node is inside a collapsed node,
				// add an edge from the data node to the collapsed node
				Node dataNode = getNode(dataNodeId);
				Iterator<NodeItem> producers = dataNode.outNeighbors();
				if (producers.hasNext()) {
					// There should be just one!
					NodeItem producerNode = producers.next();

					// If the producer is buried several levels deep, add an edge
					// to each of its enclosing collapsed steps.
					NodeItem collapsedNode = vis.getCollapsedStartFinish(producerNode);
					while (collapsedNode != null) {
						addEdge(PrefuseUtils.STEPDF, dataNodeId, PrefuseUtils.getId(collapsedNode));
						collapsedNode = vis.getCollapsedStartFinish(collapsedNode);
					}
				}

			} else {
				addEdge(PrefuseUtils.DATA_FLOW, dataNodeId, procNodeId);

			}

			setAllDataNodeVisibility();

			// change the focus to recently added node
			if (incremental) {
				vis.run("layout");
				vis.run("animate");
			}
		}
		repaint();


	}

	/**
	 * Handles clicking on a node
	 * @param nodeItem the node that was clicked on
	 */
	void handleNodeClick(NodeItem nodeItem) {
		if (PrefuseUtils.isStart(nodeItem)) {
			//System.out.println("Clicked on start node: " + item);
			collapseStartNode(nodeItem);
			Node collapsedNode = vis.getCollapsedStartFinish(nodeItem);

			// The collapsed node can be null if the execution was aborted
			// due to an exception.  In that case, we may have start nodes, but
			// not the corresponding finish nodes.
			if (collapsedNode != null) {
				layout(collapsedNode);
			}
		}

		else if (PrefuseUtils.isRestoreNode(nodeItem)) {
			NodeItem collapsedNode = vis.getCollapsedCheckpoint(nodeItem);
			collapse(collapsedNode);
			setAllDataNodeVisibility();
			if (collapsedNode != null) {
				layout(collapsedNode);
			}
		}

		else if (PrefuseUtils.isFinish(nodeItem)) {
			//System.out.println("Clicked on finish node: " + item);
			Node collapsedNode = vis.getCollapsedStartFinish(nodeItem);
			handleNodeClick(vis.getStart(collapsedNode));
		}

 		else if (PrefuseUtils.isCollapsedNode(nodeItem)) {
			expand(nodeItem);
			NodeItem start = vis.getStart(nodeItem);
			if (start.isVisible()) {
				layout(start);
			}
			else {
				layout(vis.getFinish(nodeItem));
			}
		}

		//else {
			// System.out.println("*** Clicked on " + item);
		//}
	}

	/**
	 * Returns the definition of a function.  Returns an error string if there is
	 * no function with that name, or there is more than one function with that name
	 * @param functionName the name of the function
	 * @return the definition
	 */
	public String getFunctionBody(String functionName) {
		// Remove any parameters if they appear in the string
		int blankIndex = functionName.indexOf(" ");
		if (blankIndex != -1) {
			functionName = functionName.substring(0, blankIndex);
		}
		
		int parenIndex = functionName.indexOf("(");
		if (parenIndex != -1) {
			functionName = functionName.substring(0, parenIndex);
		}
		
		return provData.getFunctionBody(functionName);
	}

	/**
	 * Returns the definition of a block.  Returns an error string if there is
	 * no function with that name, or there is more than one function with that name
	 * @param blockName the name of the block
	 * @return the block
	 */
	public String getBlockBody(String blockName) {
		return provData.getBlockBody(blockName);
	}

	public void setProvData(ProvenanceData provData) {
		this.provData = provData;
		ddgPanel.setProvData(provData);
	}

	/**
	 * calculate viewFinder's bounds
	 * @param userDisplay larger, maleable user display
	 * @param overview corner overview of DDG
	 * @return rectangle transformed to place directly onto overview
	 */
	public static Rectangle calcViewFinder(Display userDisplay, Display overview){
		//retrieve width and height of the userDisplay's window on the screen
		Rectangle compBounds = userDisplay.getBounds();
		Point topLeft = new Point(0, (int)compBounds.getMinY()); //(int)compBounds.getMinX(), (int)compBounds.getMinY());
		Point bottomRight = new Point ((int)(compBounds.getMaxX()-compBounds.getMinX()), (int)compBounds.getMaxY());

		//transform point off of the user's display and onto the overview's transformation
		AffineTransform userTransI = userDisplay.getInverseTransform();
		userTransI.transform(topLeft, topLeft);
		userTransI.transform(bottomRight, bottomRight);
		AffineTransform overTrans = overview.getTransform();
		overTrans.transform(topLeft, topLeft);
		overTrans.transform(bottomRight, bottomRight);

		int x = topLeft.x;
		int y = topLeft.y;
		int width = bottomRight.x-x;
		int height = bottomRight.y-y;

		return new Rectangle(x,  y,  width,  height);
	}

	/**
	 * Keeps track of bounds of DDG so that Overview will accommodate changes
	 * @author Nicole
	 */
	 public static class FitOverviewListener implements ItemBoundsListener {
			private Rectangle2D m_bounds = new Rectangle2D.Double();
			private Rectangle2D m_temp = new Rectangle2D.Double();
			private double m_d = 15;

			public FitOverviewListener() {
				super();
			}

			@Override
			public void itemBoundsChanged(Display displayGiven) {
			    displayGiven.getItemBounds(m_temp);
			    //expand a rectangle by the given amount
			    GraphicsLib.expand(m_temp, 25/displayGiven.getScale());

			    double dd = m_d/displayGiven.getScale();
			    //difference between past and present bounds in x, y, width, or height
			    double xd = Math.abs(m_temp.getMinX()-m_bounds.getMinX());
			    double yd = Math.abs(m_temp.getMinY()-m_bounds.getMinY());
			    double wd = Math.abs(m_temp.getWidth()-m_bounds.getWidth());
			    double hd = Math.abs(m_temp.getHeight()-m_bounds.getHeight());
			    if ( xd>dd || yd>dd || wd>dd || hd>dd ) {
			    	m_bounds.setFrame(m_temp);
			    	DisplayLib.fitViewToBounds(displayGiven, m_bounds, 0);
			    }
			}
	 }

	 public static class updateOverview implements PaintListener{
		 private DDGDisplay overview;
		public updateOverview(DDGDisplay overview){
			super();
			this.overview = overview;
		}
		@Override
		public void prePaint(Display d, Graphics2D g) {
		}
		@Override
		public void postPaint(Display d, Graphics2D g) {
			overview.repaint();
		}
	 }

	/**
	 * Draws viewFinder's borders onto the overview after paint is called
	 * @author Nicole
	 */
	public static class vfBorders implements PaintListener{
    	private DDGDisplay userDisplay;

    	public vfBorders(DDGDisplay userDisplay){
    		super();
    		this.userDisplay = userDisplay;
    	}

		@Override
		public void prePaint(Display overview, Graphics2D g) {
		}

		@Override
		/**
		 * after both ddg displays have been drawn, create a rectangle
		 * in the overview that represents the regular display's view.
		 */
		public void postPaint(Display overview, Graphics2D g) {
			//retrieve rectangle for viewFinder
			Rectangle rect = calcViewFinder(userDisplay, overview);

			//draw the rectangle
			int x = rect.x;
			int y = rect.y;
			int width = rect.width;
			int height = rect.height;
			g.setColor(Color.LIGHT_GRAY);
			g.drawRoundRect(x, y, width, height, 10, 10);
			g.setColor(new Color(150, 150, 200, 50));
			g.fillRoundRect(x, y, width, height, 10, 10);
		}
	}


	/**
	 * Listen for clicks or drags in the overview, move
	 * the viewFinder accordingly
	 */
	 public static class vfListener implements MouseInputListener{
		 private DDGDisplay userDisplay;
		 private DDGDisplay overview;
		 private boolean draggingRect;
		 private Point prev;

	    public vfListener(DDGDisplay userDisplay, DDGDisplay overview){
	    	super();
	    	this.userDisplay = userDisplay;
	    	this.overview = overview;
	    }

		@Override
		public void mousePressed(MouseEvent e) {
			Rectangle viewFinder = calcViewFinder(userDisplay, overview);
			if (viewFinder.contains(e.getPoint())){ //inside rectangle
				prev = e.getPoint();
				draggingRect = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			//find where mouse was clicked on the Overview, transform it
			//out of the overview and onto the userDisplay. Then pan to that location
			if (!draggingRect){
				Point p = transPoint(e.getPoint());
				userDisplay.animatePanTo(p, 1000);
			}else{
				draggingRect = false;	//reset draggingRect for next time.
			}
		}

		/**
		 * translate point from overview coordinates to userDisplay coordinates
		 * @param p Point in question
		 * @return transformed point
		 */
		private Point transPoint(Point p){
			//System.out.println(p.x + ", " + p.y + " absolute point");
			AffineTransform overTransI = overview.getInverseTransform();
			overTransI.transform(p, p);
			AffineTransform userTrans = userDisplay.getTransform();
			userTrans.transform(p, p);
			//System.out.println(p.getX() + ", " + p.getY() + " transformed point");
			return p;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (draggingRect){
				Point p = transPoint(e.getPoint());
				prev = transPoint(prev);

				int xMovement = prev.x - p.x;
				int yMovement = prev.y - p.y;
				userDisplay.animatePan(xMovement, yMovement, 1);

				prev = e.getPoint();
				//System.out.println("x movement: " + xMovement + " and y movement: " + yMovement);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
		}

	 }




	/**
	 * Displays a file chooser for textual DDGs and displays the result
	 * visually.
	 *
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
		PrefuseGraphBuilder builder = new PrefuseGraphBuilder();
		builder.buildNodeAndEdgeTables();

		// -- 1. load the data ------------------------------------------------
		if (args.length == 0) {
			JFileChooser fileChooser = new JFileChooser(
					System.getProperty("user.dir"));
			try {
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					builder.setTitle(selectedFile.getName(), "");
					builder.buildGraph(selectedFile);
				}
				builder.initializeDisplay();
			} catch (HeadlessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Cannot read the file");
			}
		} else {
			try {
				builder.buildGraph(new File(args[0]));
				builder.initializeDisplay();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setHighlighted(int id, boolean value) {
		getNode(id).setHighlighted(value);
	}

	public String getScriptPath() {
		return provData.getScript();
	}

	public NodeItem getFirstMember(VisualItem collapsedNode) {
		return vis.getStart((Node) collapsedNode);
	}

	public NodeItem getLastMember(VisualItem collapsedNode) {
		return vis.getFinish((Node) collapsedNode);
	}



}
