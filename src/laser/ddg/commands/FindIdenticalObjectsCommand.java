package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import laser.ddg.DataNodeVisitor;

public class FindIdenticalObjectsCommand implements ActionListener {

	private ArrayList<String> hashkeys;
	private DataNodeVisitor dataNodeVisitor;
	
	public FindIdenticalObjectsCommand() {
		hashkeys = new ArrayList<String>();
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
			hashkeys.add(entries[4] + entries[0] + entries[1] + entries[5]);
		}
		br.close();
	}
	
	public ArrayList<String> findMatchingHashes(ArrayList<String> nodehashes) {
		ArrayList<String> matches = new ArrayList<String>();
		for (int i = 0; i < nodehashes.size(); i++) {
			
			for (int j = 0; j < hashkeys.size(); j++) {
				if (hashkeys.get(j).substring(1, 33).equals(nodehashes.get(i))){
					matches.add(hashkeys.get(j).substring(34));
				}
			}
		}
		for (int k = 0; k < matches.size(); k++) {
			System.out.println(matches.get(k));;
		}
		return matches;
	}

}
