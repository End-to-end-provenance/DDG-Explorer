package laser.ddg;

import java.io.File;

/**
 * Information about files stored in a ddg
 * @author Barbara Lerner
 * @version Oct 24, 2013
 *
 */
public class FileInfo implements Comparable<FileInfo>{
	// Full path to the file
	private final String longFilename;
	
	// Filename without the path
	private final String filename;
	
	// Process that used or created the file
	private final String process;
	
	// Timestamp of the ddg that used or created the file
	private final String ddgTimestamp;
	
	// Name of the node within the ddg
	private final String nodename;
	
	/**
	 * Creates the file information structure, extracting it from the full 
	 * path name.  We assume that the path looks like:
	 * 
	 * <path>/<process>/<process-timestamp>/<ddg-timestamp>/<nodename>
	 * 
	 * where the nodename is <id>-<filename>
	 * 
	 * @param longFilename the full path to the file
	 */
	public FileInfo (String longFilename) {
		this.longFilename = longFilename;
		int lastSlashPos = longFilename.lastIndexOf(File.separatorChar);
		nodename = longFilename.substring(lastSlashPos + 1);
		filename = nodename.substring(nodename.indexOf("-") + 1);
		
		String s = longFilename.substring(0, lastSlashPos);
		lastSlashPos = s.lastIndexOf(File.separatorChar);
		ddgTimestamp = s.substring(lastSlashPos + 1);
		s = s.substring(0, lastSlashPos);
		
		// Remove and throw away the process version timestamp
		lastSlashPos = s.lastIndexOf(File.separatorChar);
		s = s.substring(0, lastSlashPos);
		
		lastSlashPos = s.lastIndexOf(File.separatorChar);
		process = s.substring(lastSlashPos + 1);
	}

	public String getFilename() {
		return filename;
	}

	public String getProcess() {
		return process;
	}

	public String getDdgTimestamp() {
		return ddgTimestamp;
	}

	public String getNodename() {
		return nodename;
	}
	
	public String getPath() {
		return longFilename;
	}
	
	@Override
	public String toString() {
		String s = "";
		s = s + "Process = " + process + "\n";
		s = s + "DDG timstamp = " + ddgTimestamp + "\n";
		s = s + "Filename = " + filename + "\n";
		s = s + "Node name = " + nodename + "\n";
		s = s + "Long Filename = " + longFilename + "\n";
		return s;
	}

	/**
	 * Sorts first by filename, then process name, then timestamp, and then node name.
	 */
	@Override
	public int compareTo(FileInfo o) {
		if (!filename.equals(o.filename)) {
			return filename.compareTo(o.filename); 
		}
		
		if (!process.equals(o.process)) {
			return process.compareTo(o.process); 
		}
		
		if (!ddgTimestamp.equals(o.ddgTimestamp)) {
			return ddgTimestamp.compareTo(o.ddgTimestamp); 
		}
		
		if (!nodename.equals(o.nodename)) {
			return nodename.compareTo(o.nodename); 
		}
		
		return 0;
	}

}
