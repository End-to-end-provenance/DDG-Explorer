package laser.ddg.commands;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import laser.ddg.SearchElement;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;

public class FindTImeCommand implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		DDGExplorer ddgExplorer = DDGExplorer.getInstance(); 
		ArrayList<SearchElement> nodeList =DDGExplorer.getCurrentDDGPanel().getSearchIndex().getAllList();  
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		panel.showSearchResults(nodeList); //show the search results of the entire node. 

	}
	
	

}
