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
	
	// The modification date of the file containing this script.
	// Note that the timestamp is included in the attributes section of
	// ddg.txt.  It thus arrives as a string, which we then use when
	// we store the script files in the database, so that we can distinguish
	// different versions of a script based on when it was last modified.
	// It is important that the timestamp string that is used in the database
	// uses this format:  yyyy-MM-dd'T'HH.mm.ssz
	final private String timestamp;
	
	// Name of the script with the directory removed
	final private String name;

	public ScriptInfo(String filepath, String timestamp) {
		assert timestamp != null;
		
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

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public String toString () {
		return name;
	}

}
