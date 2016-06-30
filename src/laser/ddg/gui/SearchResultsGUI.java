package laser.ddg.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import laser.ddg.search.SearchElement;

/**
 * Creates the GUI component to show search results.
 * 
 * @author Marios Dardas
 * @version Summer 2015
 *
 */
public class SearchResultsGUI extends JScrollPane {

	private DefaultListModel<SearchElement> model;
	
	//Keep track of selected nodes from search results
	private int prevNodeId; 
	private boolean prevNodeHighlighted = false;
	
	

	/**
	 * Creates the search results
	 * @param resultList the list of nodes whose names appear in the search results
	 */
	public SearchResultsGUI(ArrayList<? extends SearchElement> resultList) {

		model = new DefaultListModel<>();
		for (SearchElement entry : resultList) {
			model.addElement(entry);
		}

		final JList<SearchElement> searchList;
		searchList = new JList<>(model);
		searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchList.setCellRenderer(new NodeCellRenderer());
		searchList.setVisibleRowCount(-1);

		// update the focus in the DDG to focus on selected node from search
		// results
		searchList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent listener) {
				SearchElement entry = searchList.getSelectedValue();
				if (entry != null) {
					try {
						updateNodeFocus(entry);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(SearchResultsGUI.this,
								"Can't display node: " + entry.getName(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		setViewportView(searchList);
		setMinimumSize(new Dimension(150, 200));
	}

	/**
	 * Updates the JList if a new search is done
	 * 
	 * @param resultList
	 */
	public void updateSearchList(ArrayList<? extends SearchElement> resultList) {
		model.clear();

		for (SearchElement entry : resultList)
			model.addElement(entry);
	}

	/**
	 * updates the focus on the DDGExplorer's graph
	 * @param entry 
	 */
	public void updateNodeFocus(SearchElement entry) {
		DDGPanel curDDGPanel = DDGExplorer.getCurrentDDGPanel();
		
		// if a search result was previously selected, then remove
		// highlighting from node
		if (prevNodeHighlighted) {
			curDDGPanel.setHighlighted (prevNodeId, false);
		}

		// get selected search result's node and highlight it
		int entryId = entry.getId();
		curDDGPanel.setHighlighted(entryId, true);

		// bring node of graph into focus
		curDDGPanel.focusOn(entry.getName());

		// keep track of highlighted node to remove highlighting in the
		// future
		prevNodeId = entryId;
		prevNodeHighlighted = true;
	}

	/**
	 * Customizes the JList to be colorized and display information about the
	 * Nodes
	 * 
	 * @author Marios Dardas
	 */
	static class NodeCellRenderer implements ListCellRenderer<Object> {
		private Color HIGHLIGHT_COLOR = new Color(193, 253, 51); // 255,206,26
																	// <- Nice
																	// shade of
																	// bright
																	// orange

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
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
				// Sets text to be black for contrast
				if (entry.getColor() != Color.BLACK)
					rendererComponent.setForeground(Color.BLACK);
				else
					rendererComponent.setForeground(Color.WHITE);
			}
			return rendererComponent;
		}

	}
	


}