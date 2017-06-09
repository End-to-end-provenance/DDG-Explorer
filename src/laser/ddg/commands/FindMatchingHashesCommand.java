package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import laser.ddg.search.SearchElement;

public class FindMatchingHashesCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent args0) {
		// Based off of FindTimeCommand. This only finds the nodes/snapshot csv files, however.
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		ArrayList<SearchElement> nodeList = 
				(ArrayList<SearchElement>) panel.getSearchIndex().getFileList().clone();
		for (int i = 0; i < nodeList.size(); i++) {
			String name = nodeList.get(i).getName();
		}
		
		
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		System.out.println(hashtable.getAbsolutePath());
		try {
			BufferedReader br = new BufferedReader(new FileReader(hashtable));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	

}
