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

	/**
	 * Create an object to remember information about the script that is executed
	 * @param filepath absolute path to the file containing the script
	 * @param timestamp last modified time of the script
	 */
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

	/**
	 * 
	 * @return the absolute path to the script
	 */
	public String getFilepath() {
		return filepath;
	}

	/**
	 * 
	 * @return last modifiication time of the script
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * 
	 * @return filename of the script with the directory path removed
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public String toString () {
		return name;
	}

}
