package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import laser.ddg.search.SearchElement;

public class FindMatchingHashesCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent args0) {
		// Based off of FindTimeCommand
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		ArrayList<SearchElement> nodeList = 
				(ArrayList<SearchElement>) panel.getSearchIndex().getFileList().clone();
		
	}
	

}
