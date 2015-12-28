package laser.ddg.search;

import java.util.ArrayList; 

import com.ibm.icu.util.Calendar;

import laser.ddg.search.SearchElement;
 
/**
 * Manages lists of nodes by type to facilitate searching.
 * 
 * @author Barbara Lerner
 * @version Sep 2, 2015
 *
 */
public class SearchIndex {
	//2D Array to hold information on each type of node for search within Current DDG
	private ArrayList <SearchElement> errorList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> dataList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> fileList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> urlList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> operationList = new ArrayList<SearchElement>();
	private ArrayList <SearchElement> allList = new ArrayList<SearchElement>();

	/**
	 * Adds a node to the appropriate search index based on the node's type.
	 * All nodes go into a general list as well.
	 * 
	 * @param type the type of node:  one of "Exception", "Data", "File", "URL", or "Operation"
	 * @param id the node id used by Prefuse
	 * @param name the node's label
	 */
	public void addToSearchIndex(String type, int id, String name, String time) {
		double newTime; 
		SearchElement element ; 
		if(time != null){
		 newTime = Double.parseDouble(time);
		element = new SearchElement(type, name, id, newTime);//defualt time right now.  
		System.out.println(newTime);  
	    
		}
		else{
			String newTime1 =  Calendar.getInstance().toString(); //for data nodes, should all be 1.0
			element = new SearchElement(type, name, id, newTime1);//defualt time right now.  
			System.out.println(newTime1); 
		
		}
		


		// store each node with associated type
		if(type.equals("Exception")){
			errorList.add(element); //add to the arraylist of the different types of nodes.
		}
		else if(type.equals("Data") || type.equals("Snapshot")) {
		
			dataList.add(element);
		}
		else if(type.equals("File")){
			fileList.add(element);
		}
		else if(type.equals("URL")){
			urlList.add(element);
		}
		else if(type.equals("Operation")) {
			element = addTimeTaken(element); 
			operationList.add(element); 
		}
			
		allList.add(element);
	}

	public SearchElement addTimeTaken(SearchElement element){
		double current = element.getTime(); 
		double timeTaken; 
		if(!operationList.isEmpty()){
		double previous = operationList.get(operationList.size() -1).getTime(); 
		
			//iterate through this, finding the difference 
			 timeTaken = current - previous; 
			System.out.println("The time taken between previous and current node is "+timeTaken);
			
		}else{ 
			 timeTaken = current - 0; 
		}
		element.setTimeTaken(timeTaken);
			return element; 
		}
	
	
	/**
	 * 
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
		System.out.println("Getting the operations list");; 
		return operationList;
	}

	/**
	 * @return all nodes in the search list
	 */
	public ArrayList<SearchElement> getAllList() {
		return allList;
	}

}
