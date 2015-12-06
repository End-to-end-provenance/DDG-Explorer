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
		
		DDGExplorer ddgExplorer = DDGExplorer.getInstance(); 
		ArrayList<SearchElement> nodeList =DDGExplorer.getCurrentDDGPanel().getSearchIndex().getOperationList();  
		//order nodelist and show in GUI. 
		
		   Collections.sort(nodeList, new Comparator<SearchElement>() {
		        @Override public int compare(SearchElement p1, SearchElement p2) {
		        	if(p2.getTimeTaken()  < p1.getTimeTaken() ){
		        		return -1; 
		        	}
		        	else if(p2.getTimeTaken() > p2.getTimeTaken()){
		        		return 1; 
		        	}
		            return 0;  
		        }
		   }); 

		
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		panel.showSearchResults(nodeList); 

	}
	

	

}
