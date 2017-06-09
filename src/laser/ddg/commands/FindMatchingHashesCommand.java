package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FindMatchingHashesCommand implements ActionListener {

	private Map<String,String> hashkeys = new HashMap<String,String>();
	
	@Override
	public void actionPerformed(ActionEvent args0) {
		
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

		FileReader fr = new FileReader(hashtable);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] entries = line.split(",");
			hashkeys.put(entries[0] + entries[5], entries[4]);
		}
		br.close();
	}


}
