package laser.ddg.commands;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import laser.ddg.SearchElement;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;

public class FindTimeCommand implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//order nodelist by the higheset to lowest scripts. 
		//calculate the difference between the node times, and hten insert i. 
		DDGExplorer ddgExplorer = DDGExplorer.getInstance(); 
		ArrayList<SearchElement> nodeList =DDGExplorer.getCurrentDDGPanel().getSearchIndex().getOperationList();  
		//order nodelist by the higheset to lowest scripts. 
		//calculate the difference between the node times, and hten insert i. 
		double previous =0; 
		for(int i = 0; i < nodeList.size(); i++){
			//iterate through this, finding the difference 
			double current = nodeList.get(i).getTime();
			double timeTaken = current - previous; 
			System.out.println("The time taken between previous and current node is "+timeTaken);
			nodeList.get(i).setTimeTaken(timeTaken);
			previous = current; //resetting the previous to be the current. 
		}
		
		//now, sort by time taken.  
		   Collections.sort(nodeList, new Comparator<SearchElement>() {
		        @Override public int compare(SearchElement p1, SearchElement p2) {
		        	if(p2.getTimeTaken()  < p1.getTimeTaken() ){
		        		return -1; 
		        	}
		        	else if(p2.getTimeTaken() > p2.getTimeTaken()){
		        		return 1; 
		        	}
		            return 0; // equals each ohter. 
		        }
		   }); 

		//do merge sort 
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		panel.showSearchResults(nodeList); //show the search results of the entire node. 

	}
	
public void mergesort(ArrayList<SearchElement> arr, int high, int low){
	
	
}
	

}
