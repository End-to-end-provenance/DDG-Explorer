package laser.ddg.search;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import prefuse.visual.NodeItem;
import laser.ddg.visualizer.PrefuseGraphBuilder;

public class SearchIndex {
	private PrefuseGraphBuilder builder;
	
	//Keep track of selected nodes from search results
	private int prevNodeId; 
	private boolean prevNodeHighlighted = false;
	
	//2D Array to hold information on each type of node for search within Current DDG
	private ArrayList <SearchElement> errorList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> dataList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> fileList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> urlList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> operationList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> allList = new ArrayList<SearchElement>();

	public SearchIndex (PrefuseGraphBuilder builder) {
		this.builder = builder;
	}
	
	public void addToSearchIndex(String type, int id, String name) {
		// hold individual node information
		SearchElement element = new SearchElement(builder, type, name, id);

		// store each node with associated type
		if(type.equals("Exception"))
			errorList.add(element);
		else if(type.equals("Data"))
			dataList.add(element);
		else if(type.equals("File"))
			fileList.add(element);
		else if(type.equals("URL"))
			urlList.add(element);
		else if(type.equals("Operation"))
			operationList.add(element);

		// keep track of all nodes in DDG
		allList.add(element);
	}

	/**
	 * updates the focus on the DDGExplorer's graph
	 * @param entry 
	 */
	public void updateNodeFocus(SearchElement entry) {
		// if a search result was previously selected, then remove
		// highlighting from node
		if (prevNodeHighlighted) {
			builder.setHighlighted (prevNodeId, false);
		}

		// get selected search result's node and highlight it
		int entryId = entry.getId();
		builder.setHighlighted(entryId, true);

		// bring node of graph into focus
		builder.focusOn(entry.getName());

		// keep track of highlighted node to remove highlighting in the
		// future
		prevNodeId = entryId;
		prevNodeHighlighted = true;
	}

	/**
	 * @return associated error nodes in the search list
	 */
	public ArrayList<SearchElement> getErrorList() {
		return errorList;
	}

	/**
	 * @return associated data nodes in the search list
	 */
	public ArrayList<SearchElement> getDataList() {
		return dataList;
	}

	/**
	 * @return associated URL nodes in the search list
	 */
	public ArrayList<SearchElement> getURLList() {
		return urlList;
	}

	/**
	 * @return associated file nodes in the search list
	 */
	public ArrayList<SearchElement> getFileList() {
		return fileList;
	}

	/**
	 * @return associated operation nodes in the search list
	 */
	public ArrayList<SearchElement> getOperationList() {
		return operationList;
	}

	/**
	 * @return all nodes in the search list
	 */
	public ArrayList<SearchElement> getAllList() {
		return allList;
	}

}
