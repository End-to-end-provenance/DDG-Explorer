package laser.ddg;

import java.io.File;

/**
 * Manages the information about a script involved in the creation of a DDG.
 * There may be multiple scripts involved in a single ddg.
 * 
 * @author Barbara Lerner
 * @version Jul 26, 2016
 *
 */
public class ScriptInfo {
	// Filename
	final private String filepath;
	
	// The modification date of the file containing this script
	final private String timestamp;
	
	// Name of the script with the directory removed
	final private String name;

	public ScriptInfo(String filepath, String timestamp) {
		//System.out.println("Creating ScriptInfo for " + filepath + " " + timestamp);
		this.filepath = filepath;
		this.timestamp = timestamp;
		
		// Convert to a file so that we do not need to worry 
		// about what directory separator the filepath uses
		File file = new File(filepath);
		name = file.getName();
	}

	public String getFilepath() {
		return filepath;
	}

	public String getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString () {
		return name;
	}

}
