/**
 * 
 */
package laser.ddg.persist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import laser.ddg.ScriptInfo;
import laser.ddg.gui.DDGExplorer;

/**
 * @author xiang
 * 
 */
public class FileUtil {
	/** Directory where all DDG related things should go */
	public static final String DDG_DIRECTORY
		= System.getProperty("user.home") + File.separatorChar + ".ddg" + File.separatorChar;
	
	public static synchronized void rewritetofile(String filename,
			String content) {
		try {
			FileWriter fstream = new FileWriter(filename, false);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	/**
	 * Returns the time at which the file was last modified.  The timestamp is
	 * formatted as yyyy-MM-dd'T'HH:mm:ss
	 * @param programFile the file whose timestamp is returned
	 * @return the timestamp
	 */
	public static String getTimestamp(File programFile) {
		long timestamp = programFile.lastModified();
		// Formats like this: 2013-07-23T16:42:51
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ssz");
		return dateFormat.format(new Date(timestamp));
	}
	
	/**
	 * Returns the last file in the given path
	 * @param fullPath the path to the file
	 * @param language the language we are working in. Needed to decide on '/' or '\'
	 * @return the file name
	 */
	public static String getPathDest(String fullPath, String language){
		//ErrorLog.showErrMsg("\nscript path = " + processPathName + "\n");
		// When R generates file names, it always uses Unix file separators, even when run 
		// on Windows.  We first translate to the native OS style.  Sigh.
		//processName = processPathName.replaceAll("/", File.separator);  // This did not work.  Subtlety in having \ in replacement string.
		char fileSep;
		if (language != null && language.equals("R")) {
			fileSep = '/';
		}
		else {
			fileSep = File.separatorChar;
		}
		//ErrorLog.showErrMsg("DOS script path = " + processName + "\n");
		
		// Keep just the filename, not the full path
		String pathDest;
		int lastFileSeparatorPos = fullPath.lastIndexOf(fileSep);
		//ErrorLog.showErrMsg("Last \\ at " + lastFileSeparatorPos + "\n");
		if (lastFileSeparatorPos != -1) {
			pathDest = fullPath.substring(lastFileSeparatorPos + 1);
		}
		else {
			pathDest = fullPath;
		}
		return pathDest;
	}

}