package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import laser.ddg.search.SearchElement;

public class FindMatchingHashesCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent args0) {
		// Based off of FindTimeCommand. This only finds the nodes/snapshot csv files, however.
		/*
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		ArrayList<SearchElement> nodeList = 
				(ArrayList<SearchElement>) panel.getSearchIndex().getFileList().clone();
		for (int i = 0; i < nodeList.size(); i++) {
			String name = nodeList.get(i).getName();
		}
		 */
		try {
			readHashtable();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readHashtable() throws IOException {
		// https://www.javatpoint.com/java-bufferedreader-class
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		System.out.println(hashtable.getAbsolutePath());
		String line = "";
		String[] entries = null;

		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine(); // To 
		while((line = br.readLine()) != null) {
			entries = line.split(",");
			System.out.println(entries[4]);
		}
		br.close();
	}


}
