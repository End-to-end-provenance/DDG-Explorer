package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import laser.ddg.search.SearchElement;
import laser.ddg.search.SearchIndex;

public class SearchResultsGUI extends JPanel{

  private JList <SearchElement> searchList;
  private DefaultListModel <SearchElement> model; 
  private SearchIndex searchIndex;
  
  public SearchResultsGUI(ArrayList <SearchElement> nodesList, JSplitPane splitPane, JPanel ddgPanel){
	JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
	
	model = new DefaultListModel<SearchElement>();
	for(SearchElement entry : nodesList) {
		model.addElement(entry);
	}
	
	searchList = new JList<SearchElement>(model);
    searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    searchList.setCellRenderer(new NodeCellRenderer());
    searchList.setVisibleRowCount(-1);

    //update the focus in the DDG to focus on selected node from search results
    searchList.addListSelectionListener(new ListSelectionListener(){
    	@Override
		public void valueChanged(ListSelectionEvent listener){
    		SearchElement entry = searchList.getSelectedValue();
    		if(entry != null) {
    			try {
					searchIndex.updateNodeFocus(entry);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(SearchResultsGUI.this,
							"Can't display node: " + entry.getName(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
    		}
    	}
    });
    
    JScrollPane pane = new JScrollPane(searchList);
    pane.setPreferredSize(new Dimension(150, 200));
    panel.add(pane);        

    //set it on the left of splitPane (the DDG is set to be on the right)
    splitPane.setLeftComponent(panel);
    ddgPanel.validate();
  }

  /**
   * Updates the JList if a new search is done
   * @param nodesList
   */
  public void updateSearchList(ArrayList <SearchElement> nodesList){
  	model.clear();

  	for(SearchElement entry : nodesList)
  		model.addElement(entry);
  }
  
  /**
   * Customizes the JList to be colorized and display information about the Nodes
   * @author Marios Dardas
   */
  class NodeCellRenderer implements ListCellRenderer {
    private Color HIGHLIGHT_COLOR = new Color(193,253,51); // 255,206,26 <- Nice shade of bright orange
    
    @Override
	public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus) {
      JLabel rendererComponent = new JLabel(); 
      rendererComponent.setOpaque(true);
      rendererComponent.setIconTextGap(12);
      
      SearchElement entry = (SearchElement) value;
      rendererComponent.setText(entry.getName());
      
      if (isSelected) {
    	  rendererComponent.setBackground(HIGHLIGHT_COLOR);
    	  rendererComponent.setForeground(Color.BLACK);
      } else {
    	  rendererComponent.setBackground(entry.getColor());
    	  //Sets text to be black for contrast
    	  if(entry.getColor() != Color.BLACK)
        	rendererComponent.setForeground(Color.BLACK);
    	  else
        	rendererComponent.setForeground(Color.WHITE);
      }
      return rendererComponent;
    }

  }
}