package laser.ddg.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import laser.ddg.visualizer.PrefuseGraphBuilder.tupleElement;

public class DDGSearchGUI extends JPanel{

  JList <tupleElement> searchList;
  DefaultListModel <tupleElement> model; 

  
  public DDGSearchGUI(ArrayList <tupleElement> nodesList, JSplitPane splitPane, JPanel ddgPanel){
	  generateSearchList(nodesList, splitPane, ddgPanel);
  }

  public void generateSearchList(ArrayList <tupleElement> nodesList, JSplitPane splitPane, JPanel ddgPanel){  
	//Checks if search results were already created so that multiple JPanels will not be created
    if(searchList == null){
    	JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
    	
    	model = new DefaultListModel<tupleElement>();
    	for(tupleElement entry : nodesList)
    		model.addElement(entry);
    	
    	searchList = new JList<tupleElement>(model);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.setCellRenderer(new NodeCellRenderer());
        searchList.setVisibleRowCount(-1);

        //update the focus in the DDG to focus on selected node from search results
        searchList.addListSelectionListener(new ListSelectionListener(){
        	public void valueChanged(ListSelectionEvent listener){
        		tupleElement entry = (tupleElement) searchList.getSelectedValue();
        		if(entry != null)
        			entry.updateNodeFocus();
        	}
        });
        
        
        JScrollPane pane = new JScrollPane(searchList);
        pane.setPreferredSize(new Dimension(150, 200));
        panel.add(pane);        

        //set it on the left of splitPane (the DDG is set to be on the right)
        splitPane.setLeftComponent(panel);
        ddgPanel.validate();
    }
  }

  //updates the JList if a new search is done
  public void updateSearchList(ArrayList <tupleElement> nodesList){
  	model.clear();

  	for(tupleElement entry : nodesList)
  		model.addElement(entry);
  }
  
  

  //Customizes the JList to be colorized and display information about the Nodes
  class NodeCellRenderer extends JLabel implements ListCellRenderer {
    Color HIGHLIGHT_COLOR = new Color(193,253,51); // 255,206,26 <- Nice shade of bright orange

    public NodeCellRenderer() {
      setOpaque(true);
      setIconTextGap(12);
    }

    public java.awt.Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus) {
      tupleElement entry = (tupleElement) value;
      setText(entry.getName());
      
      if (isSelected) {
        setBackground(HIGHLIGHT_COLOR);
        setForeground(Color.BLACK);
      } else {
        setBackground(entry.getColor());
        //Sets text to be black for contrast
        if(entry.getColor() != Color.BLACK)
            setForeground(Color.BLACK);
        else
            setForeground(Color.WHITE);
      }
      return this;
    }

  }
}