package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import laser.ddg.DataNodeVisitor;

public class FindIdenticalObjectsCommand implements ActionListener {

	private Map<String,String> hashkeys;
	private DataNodeVisitor dataNodeVisitor;
	
	public FindIdenticalObjectsCommand() {
		hashkeys = new HashMap<String,String>();
		dataNodeVisitor = new DataNodeVisitor();
	}
	
	@Override
	public void actionPerformed(ActionEvent args0) {
		try {
			readHashtable();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataNodeVisitor.visitNodes();
		ArrayList<String> nodehashes = dataNodeVisitor.getNodeHashes();
		findMatchingHashes(nodehashes);
	}

	public void readHashtable() throws IOException {
		// https://www.javatpoint.com/java-bufferedreader-class
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		String home = System.getProperty("user.home");
		File hashtable = new File(home + "/.ddg/hashtable.csv");
		String line = "";

		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] entries = line.split(",");
			//hashkeys.put(entries[4] + entries[5], entries[0]);
			hashkeys.put(entries[4], entries[0]);
		}
		br.close();
	}
	
	public ArrayList<String> findMatchingHashes(ArrayList<String> nodehashes) {
		ArrayList<String> matches = new ArrayList<String>();
		for (int i = 0; i < nodehashes.size(); i++) {
			System.out.println("Nodehashes: " + nodehashes.get(i));
			String val = hashkeys.get(nodehashes.get(i));
			if (val != null) {
				matches.add(val);
			}
		}
		for (int j = 0; j < matches.size(); j++) {
			System.out.println("Match: " + matches.get(j));;
		}
		return matches;
	}

}
