package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.net.URL;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.tuple.TupleSet;
import prefuse.util.GraphicsLib;
import prefuse.util.ColorLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.DragControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ControlAdapter;

import prefuse.action.ActionList;
import prefuse.controls.PanControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;

import javax.swing.JSplitPane;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;
import javax.swing.event.MouseInputListener;

import laser.ddg.persist.JenaLoader;
import laser.ddg.persist.JenaWriter;
import laser.ddg.persist.Parser;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.DDGDisplay;
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
import laser.ddg.visualizer.ExpandCollapseControl;
import laser.ddg.visualizer.DDGDisplay;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.qarks.util.files.diff.ui.DiffPanel;

/**
 * The panel used for the Explorer tabs that show differences between 2 DDGs side-by-side with unified scrolling.
 *
 * @author Hafeezul Rahman
 * @version July 29, 2016
 *
 */
public class GraphComp extends JPanel {

  // Object that can load information from the database
  private JenaLoader jenaLoader;

  // The panel that shows the side-by-side files and their differences
  private DiffPanel diffPanel = new DiffPanel("");

  // The object used to load R scripts that are not in the database
  private JFileChooser chooser;

  // The file shown on the left side
  private File leftFile;

  // The file shown on the right side
  private File rightFile;

  //The panel to display the ddg for the left file
  private DDGPanel leftDdgPanel;

  //The panel to display the ddg for the right file
  private DDGPanel rightDdgPanel;

  //Display for the left graph
  private DDGDisplay display_left;

  //Display for the right graph
  private DDGDisplay display_right;

  //The overview of the left graph
  private DDGDisplay displayOverview_left;

  //The overview of the right graph
  private DDGDisplay displayOverview_right;

  // The button used to select the left file from the file system
  private JButton selectFile1Button = new JButton("Select from file");

  // The button used to select the left file from the database
  private JButton selectFromDB1Button = new JButton("Select from database");

  // The button used to select the right file from the file system
  private JButton selectFile2Button = new JButton("Select from file");

  // The button used to select the right file from the database
  private JButton selectFromDB2Button = new JButton("Select from database");

  // The field to display the left file name
  private JTextField leftFileField = new JTextField();

  // The field to display the right file name
  private JTextField rightFileField = new JTextField();

  // references for pop-ups
  private JFrame frame;

  // The parser to parse the left file
  private Parser parserLeft;

  //The parser to parse the right file
  private Parser parserRight;

  //The string to store the diff result of two files
  private String diffOutput;

  //Graph builder for the left file
  private PrefuseGraphBuilder builderLeft;

  //Graph builder for the right file
  private PrefuseGraphBuilder builderRight;

  /**
   * Create the window that allows the user to select files to compare and
   * to see the results of the comparison
   * @param frame
   * @param jenaLoader the object that reads from the database
   */
  public GraphComp(JFrame frame, JenaLoader jenaLoader) {
    super(new BorderLayout());
    this.frame = frame;
    this.jenaLoader = jenaLoader;
    JPanel northPanel = new JPanel();
    JPanel leftPanel = createButtonPanel(selectFile1Button, selectFromDB1Button, leftFileField);
    leftPanel.setBorder(BorderFactory.createTitledBorder("Left file"));
    JPanel rightPanel = createButtonPanel(selectFile2Button, selectFromDB2Button, rightFileField);
    rightPanel.setBorder(BorderFactory.createTitledBorder("Right file"));
    northPanel.setLayout(new GridLayout(1, 2, 8, 0));
    northPanel.add(leftPanel);
    northPanel.add(rightPanel);
    add(northPanel, BorderLayout.NORTH);
    //add(diffPanel, BorderLayout.WEST);
  }

  /**
   * Creates a panel containing a button to select from a file, a button to select from a
   * database, and a field to display the name of the selected file
   * @param selectFileButton
   * @param selectDdgButton
   * @param fileField
   * @return the panel constructed
   */
  private JPanel createButtonPanel(
      JButton selectFileButton, JButton selectFromDBButton, JTextField fileField) {
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    JPanel topRow = new JPanel();
    selectFileButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            try {
              selectFile(e.getSource());
            } catch (Exception e1) {
              JOptionPane.showMessageDialog(
                  buttonPanel,
                  "Unable to load file: " + e1.getMessage(),
                  "Error loading file",
                  JOptionPane.ERROR_MESSAGE);
            }
          }
        });
    topRow.add(selectFileButton);

    selectFromDBButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            try {
              selectFromDB(e.getSource());
            } catch (Exception e1) {
              JOptionPane.showMessageDialog(
                  buttonPanel,
                  "Unable to load file from the database: " + e1.getMessage(),
                  "Error loading file from the database",
                  JOptionPane.ERROR_MESSAGE);
            }
          }
        });
    topRow.add(selectFromDBButton);
    buttonPanel.add(topRow);

    fileField.setEditable(false);
    buttonPanel.add(fileField);

    return buttonPanel;
  }

  /**
   * Selects a file and displays its filename.  If both files have been
   * selected, the file contents are displayed and the diff is executed, with
   * the results displayed.
   * @param button the button clicked.  We need this to determine if we
   * 		are setting the left or right file
   */
  private void selectFile(Object button) {
    if (chooser == null) {
      chooser = new JFileChooser(System.getProperty("user.home"));
    }

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      if (button == selectFile1Button) {
        selectLeftFile(selectedFile);
      } else {
        selectRightFile(selectedFile);
      }
      if (leftFile != null && rightFile != null) {
        displayGraphDiff();
      }
    }
  }
  /**
   * Set the information for the right file
   * @param f the file selected.
   */
  private void selectRightFile(File f) {
    if (f == null) {
      JOptionPane.showMessageDialog(diffPanel, "Could not open " + f);
      return;
    }
    rightFile = f;
    rightFileField.setText(rightFile.getAbsolutePath());
    selectFile2Button.setEnabled(false);
    selectFromDB2Button.setEnabled(false);
  }

  /**
   * Set the information for the left file
   * @param f the file selected
   */
  private void selectLeftFile(File f) {
    if (f == null) {
      JOptionPane.showMessageDialog(diffPanel, "Could not open " + f);
      return;
    }
    leftFile = f;
    leftFileField.setText(leftFile.getAbsolutePath());
    selectFile1Button.setEnabled(false);
    selectFromDB1Button.setEnabled(false);
  }

  /**
   * Displays a DDG Browser that allows the user to select a file from the databsae.
   * Displays its filename in the text field.  If both files have been
   * selected, the file contents are displayed and the diff is executed, with
   * the results displayed.
   * @param button the button clicked.  We need this to determine if we
   * 		are setting the left or right file
   */
  private void selectFromDB(final Object button) {
    final JDialog selectFrame = new JDialog(frame, "Select from Database", true);

    final DBBrowser browser = new ScriptBrowser(jenaLoader);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            selectFrame.dispose();
          }
        });

    JButton openButton = new JButton("Open");
    openButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            File selectedFile = browser.getSelectedFile();
            selectFrame.dispose();

            try {
              if (button == selectFromDB1Button) {
                selectLeftFile(selectedFile);
              } else {
                selectRightFile(selectedFile);
              }
              if (leftFile != null && rightFile != null) {
                displayGraphDiff();
              }
            } catch (Exception e1) {
              JOptionPane.showMessageDialog(
                  selectFrame,
                  "Unable to compare files: " + e1.getMessage(),
                  "Error comparing files",
                  JOptionPane.ERROR_MESSAGE);
            }
          }
        });

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
    buttonPanel.setBorder(padding);
    buttonPanel.add(openButton);
    buttonPanel.add(cancelButton);

    selectFrame.add(browser, BorderLayout.CENTER);
    selectFrame.add(buttonPanel, BorderLayout.SOUTH);
    selectFrame.pack();
    selectFrame.setLocationRelativeTo(this);
    selectFrame.setVisible(true);
  }

  /**
   * Run the diff algorithm on the nodes of the left ddg and the nodes of the right ddg.
   * If a node is present in left ddg and missing in the right ddg, it appears in Red.
   * If a node is present in right ddg and missing in the left ddg, it appears in Green.
   * The rest of the nodes in the left and right ddgs are colored white.
   */
  private void displayGraphDiff() {

    JenaWriter jenaWriterLeft = JenaWriter.getInstance();
    JenaWriter jenaWriterRight = JenaWriter.getInstance();

    builderLeft = new PrefuseGraphBuilder(false, jenaWriterLeft);
    builderRight = new PrefuseGraphBuilder(false, jenaWriterRight);

    File selectedFileLeft = leftFile;
    String selectedFileNameLeft = leftFile.getName();

    File selectedFileRight = rightFile;
    String selectedFileNameRight = rightFile.getName();

    DDGExplorer.loadingDDG();

    builderLeft.setCompareDDG(true);
    builderRight.setCompareDDG(true);

    builderLeft.createCopiedGroup("left_group");
    builderRight.createCopiedGroup("right_group");

    builderLeft.processStarted(selectedFileNameLeft, null);
    builderRight.processStarted(selectedFileNameRight, null);

    try {
      parserLeft = new Parser(selectedFileLeft, builderLeft);
      parserLeft.addNodesAndEdges();

      parserRight = new Parser(selectedFileRight, builderRight);
      parserRight.addNodesAndEdges();
    } catch (Exception e) {
      System.out.println("Exceptions caught");
    }

    //System.out.println("Left DDG Build Complete");
    //System.out.println("Right DDG Build Complete");

    // int totalLeftNodes = builderLeft.getDataNodeCount() + parserLeft.numPins;
    // int totalRightNodes = builderRight.getDataNodeCount() + parserRight.numPins;

    // NodeItem rootLeft = builderLeft.getNode(1);
    // String rootNameLeft = builderLeft.getName(rootLeft);
    // System.out.println("LeftGraphRoot: "+rootNameLeft);

    // NodeItem rootRight = builderRight.getNode(1);
    // String rootNameRight = builderRight.getName(rootRight);
    // System.out.println("RightGraphRoot: "+rootNameRight);

    createDiffFiles();

    computeDiffResult();

    builderLeft.processFinished();

    builderRight.processFinished();

    initializeDisplay();

    populateDisplay();

    diffOutput = new ExecuteShellCommand().executeCommand("rm leftTemp.txt rightTemp.txt").trim();
  }

  /**
   * Writes the nodes from left ddg snf the right ddg to temporary text files to generate diff result
   */
  private void createDiffFiles() {
    Writer writerLeft = null, writerRight = null;
    try {
      writerLeft =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream("leftTemp.txt"), StandardCharsets.UTF_8));
      writerRight =
          new BufferedWriter(
              new OutputStreamWriter(
                  new FileOutputStream("rightTemp.txt"), StandardCharsets.UTF_8));
    } catch (Exception e) {
      System.out.println("File creation exception raised" + e.getMessage());
    }

    for (int i = 1; i <= parserLeft.numPins; i++) {
      String extractLineNum = "";
      int position = Integer.MAX_VALUE;
      boolean isPresent = false;
      String curNodeName = builderLeft.getName(builderLeft.getNode(i));
      System.out.println(curNodeName.replaceAll("\\s+", ""));
      try {
        String dummy = curNodeName.replaceAll("\\s+", "");
        if (dummy.indexOf("[") < 0) {
          String add = dummy.substring(dummy.indexOf('-') + 1);
          writerLeft.write(add + "\n");
        } else {
          String add = dummy.substring(dummy.indexOf('-') + 1, dummy.indexOf('['));
          writerLeft.write(add + "\n");
        }
      } catch (IOException e) {
        System.out.println("writing to left file error");
      }
    }

    for (int i = 1; i <= parserRight.numPins; i++) {
      String extractLineNum = "";
      int position = Integer.MAX_VALUE;
      boolean isPresent = false;
      String curNodeName = builderRight.getName(builderRight.getNode(i));
      System.out.println(curNodeName.replaceAll("\\s+", ""));
      try {
        String dummy = curNodeName.replaceAll("\\s+", "");
        if (dummy.indexOf("[") < 0) {
          String add = dummy.substring(dummy.indexOf('-') + 1);
          writerRight.write(add + "\n");
        } else {
          String add = dummy.substring(dummy.indexOf('-') + 1, dummy.indexOf('['));
          writerRight.write(add + "\n");
        }
      } catch (IOException e) {
        System.out.println("writing to right file error" + e.getMessage());
      }
    }

    try {
      writerLeft.close();
      writerRight.close();
    } catch (IOException e) {
      System.out.println("Exception caught in write close" + e.getMessage());
    }
  }

  /**
   * Computes the Unix diff result on the left and right DDGs and groups them.
   * Groups are populated to color the nodes on the graph accordingly.
   * Left Group consists of nodes which are uniquely present in the left DDG and colored in Red
   * Right Group consists of nodes which are uniquely present in the right DDG and colored in Green 
   */
  private void computeDiffResult() {
    diffOutput =
        new ExecuteShellCommand().executeCommand("diff -y -w -b leftTemp.txt rightTemp.txt").trim();
    System.out.println(diffOutput);
    String[] diffOutputArray = diffOutput.split("\n");
    int leftnode = 1, rightnode = 1;
    for (int i = 0; i < diffOutputArray.length; i++) {

      String currString = diffOutputArray[i].trim();
      String[] dummyString = currString.split("\\s+");
      if (dummyString.length == 3) {
        if (dummyString[1].equals("|")) ;
        {
          //System.out.println("added left and right "+"leftnode:"+leftnode+" rightnode:"+rightnode);
          builderLeft.updateCopiedGroup(leftnode, "left_group");
          builderRight.updateCopiedGroup(rightnode, "right_group");
          leftnode = leftnode + 1;
          rightnode = rightnode + 1;
        }
      }
      if (dummyString.length == 2) {
        if (dummyString[0].equals(">")) {
          //System.out.println("added right node "+"rightnode:"+rightnode);
          builderRight.updateCopiedGroup(rightnode, "right_group");
          rightnode = rightnode + 1;
        } else if (dummyString[1].equals("<")) {
          //System.out.println("added left node "+"leftnode:"+leftnode);
          builderLeft.updateCopiedGroup(leftnode, "left_group");
          leftnode = leftnode + 1;
        } else {
          leftnode = leftnode + 1;
          rightnode = rightnode + 1;
        }
      }
    }
  }
  /**
   * Retrives the Graph displays and corresponding GraphOverview displays and adds Action Listeners to these displays.
   */
  private void initializeDisplay() {
    display_left = builderLeft.getDisplay();
    display_right = builderRight.getDisplay();

    displayOverview_left = builderLeft.getOverview();
    displayOverview_right = builderRight.getOverview();
    PanMyControl panning = new PanMyControl(display_left, display_right);

    display_left.addControlListener(new DragControl());
    display_left.addControlListener(panning);
    display_left.addControlListener(new ZoomControl());
    display_left.addControlListener(new ExpandCollapseControl(builderLeft));
    display_left.addPaintListener(new updateOverview(displayOverview_left));

    display_right.addControlListener(new DragControl());
    display_right.addControlListener(panning);
    display_right.addControlListener(new ZoomControl());
    display_right.addControlListener(new ExpandCollapseControl(builderRight));
    display_right.addPaintListener(new updateOverview(displayOverview_right));

    displayOverview_left.addItemBoundsListener(new FitOverviewListener());
    displayOverview_left.addPaintListener(new vfBorders(display_left, displayOverview_left));

    displayOverview_right.addItemBoundsListener(new FitOverviewListener());
    displayOverview_right.addPaintListener(new vfBorders(display_right, displayOverview_right));

    vfListener vfL =
        new vfListener(display_left, displayOverview_left, display_right, displayOverview_right);
    displayOverview_left.addMouseMotionListener(vfL);
    displayOverview_left.addMouseListener(vfL);

    displayOverview_right.addMouseMotionListener(vfL);
    displayOverview_right.addMouseListener(vfL);

    display_left.repaint();
    display_right.repaint();
  }
  /**
   * Populates the JPanel with the Graph displays and GraphOverview displays.
   */
  private void populateDisplay() {
    JPanel newPanel_left = new JPanel(new BorderLayout());
    newPanel_left.setBackground(Color.WHITE);
    newPanel_left.add(display_left, BorderLayout.CENTER);

    displayOverview_left.setBorder(BorderFactory.createTitledBorder("Overview"));
    newPanel_left.add(displayOverview_left, BorderLayout.EAST);
    newPanel_left.addComponentListener(
        new ComponentAdapter() {

          @Override
          public void componentResized(ComponentEvent e) {
            int panelHeight = newPanel_left.getHeight();
            Rectangle prevBounds = displayOverview_left.getBounds();
            displayOverview_left.setBounds(
                prevBounds.x, prevBounds.y, prevBounds.width, panelHeight - 16);
          }
        });

    JPanel newPanel_right = new JPanel(new BorderLayout());
    newPanel_right.setBackground(Color.WHITE);
    newPanel_right.add(display_right, BorderLayout.CENTER);

    displayOverview_right.setBorder(BorderFactory.createTitledBorder("Overview"));

    ToolbarCompare toolbar = new ToolbarCompare(display_left, display_right);
    newPanel_right.add(displayOverview_right, BorderLayout.EAST);
    newPanel_right.addComponentListener(
        new ComponentAdapter() {

          @Override
          public void componentResized(ComponentEvent e) {
            int panelHeight = newPanel_right.getHeight();
            Rectangle prevBounds = displayOverview_right.getBounds();
            displayOverview_right.setBounds(
                prevBounds.x, prevBounds.y, prevBounds.width, panelHeight - 16);
          }
        });
    JPanel finalContent = new JPanel();
    finalContent.setLayout(new GridLayout(1, 2));
    add(toolbar, BorderLayout.NORTH);
    finalContent.add(newPanel_left, BorderLayout.WEST);
    finalContent.add(newPanel_right, BorderLayout.EAST);
    add(finalContent);
  }
}
/**
 * Toolbar to allow for zooming a graph.
 * Zooming is unified for both the left and right ddgs.
 */
class ToolbarCompare extends JToolBar implements ActionListener {
  private DDGDisplay ddgDisplay_left;
  private DDGDisplay ddgDisplay_right;
  private JComponent[] tools;
  private JSlider zoomSetting;
  private int sliderSetting = 10;

  public ToolbarCompare(DDGDisplay ddgDisplay) {
    super("DDG Tools", SwingConstants.HORIZONTAL);
    this.ddgDisplay_left = ddgDisplay;
    populateTools();
    addTools();

    ddgDisplay.addPaintListener(new ZoomListener(zoomSetting));
    this.addPropertyChangeListener(createListener());
  }

  public ToolbarCompare(DDGDisplay ddgDisplay_left, DDGDisplay ddgDisplay_right) {
    super("DDG Tools", SwingConstants.HORIZONTAL);
    this.ddgDisplay_left = ddgDisplay_left;
    this.ddgDisplay_right = ddgDisplay_right;
    populateTools();
    addTools();
    ZoomListener listener = new ZoomListener(zoomSetting);
    ddgDisplay_left.addPaintListener(listener);
    ddgDisplay_right.addPaintListener(listener);
    this.addPropertyChangeListener(createListener());
  }

  private void populateTools() {

    zoomSetting = new JSlider(JSlider.HORIZONTAL, 1, 50, sliderSetting);
    zoomSetting.setPreferredSize(new Dimension(10, 20));
    zoomSetting.setMinorTickSpacing(10);
    zoomSetting.setPaintTicks(true);
    zoomSetting.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            double currentScale_left = 0.0, currentScale_right = 0.0;
            double zoomedScale_left = zoomSetting.getValue();

            zoomedScale_left = zoomedScale_left / 10;
            currentScale_left = ddgDisplay_left.getScale();

            if (currentScale_left != zoomedScale_left) {
              double scaleFactor_left = (zoomedScale_left / currentScale_left);
              Rectangle frameBounds_left = ddgDisplay_left.getBounds();
              int xMiddle_left = (int) (frameBounds_left.getWidth() - frameBounds_left.getX()) / 2;
              int yMiddle_left = (int) (frameBounds_left.getHeight() - frameBounds_left.getY()) / 2;
              Point2D centerScreen_left = new Point(xMiddle_left, yMiddle_left);
              //call zoom!
              ddgDisplay_left.zoom(centerScreen_left, scaleFactor_left);
              ddgDisplay_left.repaint();
            }

            double zoomedScale_right = zoomSetting.getValue();
            zoomedScale_right = zoomedScale_right / 10;
            currentScale_right = currentScale_left;

            if (currentScale_right != zoomedScale_right) {
              double scaleFactor_right = (zoomedScale_right / currentScale_right);
              Rectangle frameBounds_right = ddgDisplay_right.getBounds();
              int xMiddle_right =
                  (int) (frameBounds_right.getWidth() - frameBounds_right.getX()) / 2;
              int yMiddle_right =
                  (int) (frameBounds_right.getHeight() - frameBounds_right.getY()) / 2;
              Point2D centerScreen_right = new Point(xMiddle_right, yMiddle_right);
              ddgDisplay_right.zoom(centerScreen_right, scaleFactor_right);
              ddgDisplay_right.repaint();
            }
          }
        });

    JButton refocuser = new JButton("Zoom");
    refocuser.setOpaque(false);
    refocuser.setEnabled(false);
    tools = new JComponent[2];
    tools[0] = zoomSetting;
    tools[1] = refocuser;
  }

  /**
   * adds components from array to the JToolBar
   */
  private void addTools() {
    for (int i = 0; i < tools.length; i++) {
      JComponent current = tools[i];
      if (current != null) {
        this.add(current);
      }
    }
  }

  /**
   * creates a PropertyChangeListener that will listen for a change
   * in the toolbar's orientation and set the slider to that direction
   * @return	propertyChangeListener
   */
  private PropertyChangeListener createListener() {
    PropertyChangeListener propListener =
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent e) {
            String propName = e.getPropertyName();
            if ("orientation".equals(propName)) {
              if ((Integer) e.getNewValue() == JToolBar.VERTICAL) {
                zoomSetting.setOrientation(JSlider.VERTICAL);
              } else {
                zoomSetting.setOrientation(JSlider.HORIZONTAL);
              }
            }
          }
        };
    return propListener;
  }
  
  public static class ZoomListener implements PaintListener {
    private JSlider slider;

    public ZoomListener(JSlider slider) {
      super();
      this.slider = slider;
    }

    @Override
    public void prePaint(Display displayGiven, Graphics2D g) {
      // TODO Auto-generated method stub
    }

    @Override
    public void postPaint(Display displayGiven, Graphics2D g) {
      int zoom = (int) Math.round((displayGiven.getScale() * 10));
      //System.out.println("zoomListener resetting slider to value " + zoom);
      if (slider.getValue() != zoom) {
        slider.setValue(zoom);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO Auto-generated method stub
  }
}
/**
 * Listen for clicks or drags in the overview, move
 * the viewFinder accordingly
 */
class vfListener implements MouseInputListener {
  private DDGDisplay userDisplay_left;
  private DDGDisplay overview_left;
  private DDGDisplay userDisplay_right;
  private DDGDisplay overview_right;
  private boolean draggingRect;
  private Point prev;

  public vfListener(
      DDGDisplay userDisplay_left,
      DDGDisplay overview_left,
      DDGDisplay userDisplay_right,
      DDGDisplay overview_right) {
    super();
    this.userDisplay_left = userDisplay_left;
    this.overview_left = overview_left;
    this.userDisplay_right = userDisplay_right;
    this.overview_right = overview_right;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    Rectangle viewFinder_left = calcViewFinder(userDisplay_left, overview_left);
    Rectangle viewFinder_right = calcViewFinder(userDisplay_right, overview_right);
    if (viewFinder_left.contains(e.getPoint()) || viewFinder_right.contains(e.getPoint())) {
      prev = e.getPoint();
      draggingRect = true;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    //find where mouse was clicked on the Overview, transform it
    //out of the overview and onto the userDisplay. Then pan to that location
    if (!draggingRect) {
      Point p = transPoint(e.getPoint());
      userDisplay_left.animatePanTo(p, 1000);
      userDisplay_right.animatePanTo(p, 1000);
    } else {
      draggingRect = false; //reset draggingRect for next time.
    }
  }

  /**
   * translate point from overview coordinates to userDisplay coordinates
   * @param p Point in question
   * @return transformed point
   */
  private Point transPoint(Point p) {
    AffineTransform overTransI = overview_left.getInverseTransform();
    overTransI.transform(p, p);
    AffineTransform userTrans = userDisplay_left.getTransform();
    userTrans.transform(p, p);
    return p;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (draggingRect) {
      Point p = transPoint(e.getPoint());
      prev = transPoint(prev);

      int xMovement = prev.x - p.x;
      int yMovement = prev.y - p.y;
      userDisplay_left.animatePan(xMovement, yMovement, 1);
      userDisplay_right.animatePan(xMovement, yMovement, 1);
      prev = e.getPoint();
    }
  }

  public static Rectangle calcViewFinder(Display userDisplay, Display overview) {
    //retrieve width and height of the userDisplay's window on the screen
    Rectangle compBounds = userDisplay.getBounds();
    Point topLeft = new Point(0, (int) compBounds.getMinY());
    Point bottomRight =
        new Point((int) (compBounds.getMaxX() - compBounds.getMinX()), (int) compBounds.getMaxY());
    AffineTransform userTransI = userDisplay.getInverseTransform();
    userTransI.transform(topLeft, topLeft);
    userTransI.transform(bottomRight, bottomRight);
    AffineTransform overTrans = overview.getTransform();
    overTrans.transform(topLeft, topLeft);
    overTrans.transform(bottomRight, bottomRight);

    int x = topLeft.x;
    int y = topLeft.y;
    int width = bottomRight.x - x;
    int height = bottomRight.y - y;

    return new Rectangle(x, y, width, height);
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent arg0) {}

  @Override
  public void mouseExited(MouseEvent arg0) {}

  @Override
  public void mouseMoved(MouseEvent arg0) {}
}

/**
 * To animate the display when the Graph display is dragged with the mouse.
 */
class PanMyControl extends ControlAdapter {

  private boolean m_panOverItem;
  private int m_xDown, m_yDown;
  private int m_button;
  private Display display_left;
  private Display display_right;

  /**
   * Create a new PanControl.
   */
  public PanMyControl(Display display_left, Display display_right) {
    this(LEFT_MOUSE_BUTTON, false);
    this.display_left = display_left;
    this.display_right = display_right;
  }

  /**
   * Create a new PanControl.
   * @param panOverItem if true, the panning control will work even while
   * the mouse is over a visual item.
   */
  public PanMyControl(boolean panOverItem) {
    this(LEFT_MOUSE_BUTTON, panOverItem);
  }

  /**
   * Create a new PanControl.
   * @param mouseButton the mouse button that should initiate a pan. One of
   * {@link Control#LEFT_MOUSE_BUTTON}, {@link Control#MIDDLE_MOUSE_BUTTON},
   * or {@link Control#RIGHT_MOUSE_BUTTON}.
   */
  public PanMyControl(int mouseButton) {
    this(mouseButton, false);
  }

  /**
   * Create a new PanControl
   * @param mouseButton the mouse button that should initiate a pan. One of
   * {@link Control#LEFT_MOUSE_BUTTON}, {@link Control#MIDDLE_MOUSE_BUTTON},
   * or {@link Control#RIGHT_MOUSE_BUTTON}.
   * @param panOverItem if true, the panning control will work even while
   * the mouse is over a visual item.
   */
  public PanMyControl(int mouseButton, boolean panOverItem) {
    m_button = mouseButton;
    m_panOverItem = panOverItem;
  }

  // ------------------------------------------------------------------------

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {
    if (UILib.isButtonPressed(e, m_button)) {
      e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      m_xDown = e.getX();
      m_yDown = e.getY();
    }
  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  public void mouseDragged(MouseEvent e) {
    if (UILib.isButtonPressed(e, m_button)) {
      //Display display_left = (Display)e.getComponent();
      int x = e.getX(), y = e.getY();
      int dx = x - m_xDown, dy = y - m_yDown;
      display_left.pan(dx, dy);
      display_right.pan(dx, dy);
      m_xDown = x;
      m_yDown = y;
      display_left.repaint();
      display_right.repaint();
    }
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e) {
    if (UILib.isButtonPressed(e, m_button)) {
      e.getComponent().setCursor(Cursor.getDefaultCursor());
      m_xDown = -1;
      m_yDown = -1;
    }
  }

  /**
   * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
   */
  public void itemPressed(VisualItem item, MouseEvent e) {
    if (m_panOverItem) mousePressed(e);
  }

  /**
   * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
   */
  public void itemDragged(VisualItem item, MouseEvent e) {
    if (m_panOverItem) mouseDragged(e);
  }

  /**
   * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
   */
  public void itemReleased(VisualItem item, MouseEvent e) {
    if (m_panOverItem) mouseReleased(e);
  }
} // end of class PanMyControl

/**
 * Keeps track of bounds of left DDG and right DDG so that the corresponding overviews will accommodate changes
 */
class FitOverviewListener implements ItemBoundsListener {
  private Rectangle2D m_bounds = new Rectangle2D.Double();
  private Rectangle2D m_temp = new Rectangle2D.Double();
  private double m_d = 15;

  public FitOverviewListener() {
    super();
  }

  @Override
  public void itemBoundsChanged(Display displayGiven) {
    displayGiven.getItemBounds(m_temp);
    GraphicsLib.expand(m_temp, 25 / displayGiven.getScale());
    double dd = m_d / displayGiven.getScale();
    double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
    double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
    double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
    double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
    if (xd > dd || yd > dd || wd > dd || hd > dd) {
      m_bounds.setFrame(m_temp);
      DisplayLib.fitViewToBounds(displayGiven, m_bounds, 0);
    }
  }
}
/**
 * Draws viewFinder's borders onto the overview after paint is called for both left and right ddgs.
 */
class vfBorders implements PaintListener {
  private DDGDisplay userDisplay;
  private DDGDisplay overview;

  public vfBorders(DDGDisplay userDisplay, DDGDisplay overview) {
    super();
    this.userDisplay = userDisplay;
    this.overview = overview;
  }

  @Override
  public void prePaint(Display overview, Graphics2D g) {}

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

  public static Rectangle calcViewFinder(Display userDisplay, Display overview) {
    Rectangle compBounds = userDisplay.getBounds();
    Point topLeft =
        new Point(
            0, (int) compBounds.getMinY()); //(int)compBounds.getMinX(), (int)compBounds.getMinY());
    Point bottomRight =
        new Point((int) (compBounds.getMaxX() - compBounds.getMinX()), (int) compBounds.getMaxY());
    AffineTransform userTransI = userDisplay.getInverseTransform();
    userTransI.transform(topLeft, topLeft);
    userTransI.transform(bottomRight, bottomRight);
    AffineTransform overTrans = overview.getTransform();
    overTrans.transform(topLeft, topLeft);
    overTrans.transform(bottomRight, bottomRight);

    int x = topLeft.x;
    int y = topLeft.y;
    int width = bottomRight.x - x;
    int height = bottomRight.y - y;

    return new Rectangle(x, y, width, height);
  }
}
/**
 * To update the display overview
 */
class updateOverview implements PaintListener {
  private DDGDisplay overview;

  public updateOverview(DDGDisplay overview) {
    super();
    this.overview = overview;
  }

  @Override
  public void prePaint(Display d, Graphics2D g) {}

  @Override
  public void postPaint(Display d, Graphics2D g) {
    overview.repaint();
  }
}
/**
 * To execute a shell level command in the Java Runtime.
 */
class ExecuteShellCommand {

  public String executeCommand(String command) {

    StringBuffer output = new StringBuffer();

    Process p;
    try {
      p = Runtime.getRuntime().exec(command);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return output.toString();
  }
}
