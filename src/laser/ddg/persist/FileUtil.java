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
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import laser.ddg.gui.DDGExplorer;

/**
 * @author xiang
 * 
 */
public class FileUtil {
	/** Directory where all DDG related things should go */
	public static final String DDG_DIRECTORY
		= System.getProperty("user.home") + File.separatorChar + ".ddg" + File.separatorChar;
	
	static {
		File ddgDir = new File(DDG_DIRECTORY);
		if (!ddgDir.exists()) {
			ddgDir.mkdir();
			
			// set to hidden on windows (need to test on Mac)
			try {
				String os = System.getProperty("os.name");
				if (os.startsWith("Windows")) {
					Files.setAttribute(ddgDir.toPath(), "dos:hidden", true);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				DDGExplorer.showErrMsg("Warning! Unable to set " + ddgDir
						+ "to hidden.");
			}
		}
	}
	
	/** Directory to place files that are copied to snapshot the program that is executed. */
	public static final String SAVED_FILE_DIRECTORY
		= DDG_DIRECTORY + "files" + File.separatorChar;

	public static synchronized void writetofile(String filename, String content) {
		try {
			FileWriter fstream = new FileWriter(filename, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

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

	public static String readfromFile(String filename) {
		StringBuilder filecontent = new StringBuilder();
		// Open the file that is the first
		// command line parameter
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				filecontent.append(strLine + "\n");
			}
			// Close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Get the object of DataInputStream
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filecontent.toString();

	}

	public static String printLines(String name, InputStream ins)
			throws Exception {
		StringBuilder result = new StringBuilder();
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			result.append("\t" + name + " " + line);
			result.append("\n");
		}
		return result.toString();
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

	/**
	 * Creates a file in a special directory where the file can be
	 * saved so the database can access the current version
	 * @param originalFile the file being copied
	 * @return the file object created.  Returns null if the path
	 * 		to the file does not already exist and it cannot create
	 * 		the path
	 */
	public static File createSavedFile(File originalFile) {
		try {
			File savedCopyDir = new File(SAVED_FILE_DIRECTORY);
			//ErrorLog.showErrMsg("Creating " + savedCopyDir + "\n");
			createDirectory(savedCopyDir);
			
			String filename = originalFile.getName();
			savedCopyDir = new File(SAVED_FILE_DIRECTORY + filename);
			//ErrorLog.showErrMsg("Creating " + savedCopyDir + "\n");
			createDirectory(savedCopyDir);
			
			String timestamp = getTimestamp(originalFile);
			savedCopyDir = new File(savedCopyDir.getAbsolutePath() + File.separatorChar + timestamp);
			//ErrorLog.showErrMsg("Creating " + savedCopyDir + "\n");
			createDirectory (savedCopyDir);
			
			File savedCopy = new File(savedCopyDir.getAbsolutePath() + File.separatorChar + originalFile.getName());
			//ErrorLog.showErrMsg("Will copy to " + savedCopy + "\n");
			return savedCopy;
		} catch (IllegalStateException e) {
			DDGExplorer.showErrMsg("Unable to save file " + originalFile.getAbsolutePath() + "\n");
			DDGExplorer.showErrMsg(e + "\n");
			return null;
		}
		
	}

	/**
	 * Creates the necessary directories and file to save information in a persistent location, 
	 * but outside of the database.  On return the directories will exist on the file system, 
	 * but the file will just be an internal Java object that can be written to.
	 * @param processSaveDir the directory where all files associated with a specific process should go
	 * @param executionTimestamp the timestamp associated with the execution time of the ddg whose
	 * 		data is being saved
	 * @param originalFile the file that will be copied into this location.
	 * @return the file object identifying where the file will be copied to
	 */
	public static File createSavedFile(String processSaveDir,
			String executionTimestamp, File originalFile) {
		try {
			//ErrorLog.showErrMsg("Saving to directory " + processSaveDir + File.separatorChar + executionTimestamp + "\n");
			File savedCopyDir = new File(processSaveDir + File.separatorChar + executionTimestamp);
			createDirectory(savedCopyDir);
			
			//ErrorLog.showErrMsg("Saving to file " + savedCopyDir.getAbsolutePath() + File.separatorChar + originalFile.getName() + "\n");
			File savedCopy = new File(savedCopyDir.getAbsolutePath() + File.separatorChar + originalFile.getName());
			return savedCopy;
		} catch (IllegalStateException e) {
			DDGExplorer.showErrMsg("Unable to save file " + originalFile.getAbsolutePath() + "\n");
			DDGExplorer.showErrMsg(e + "\n");
			return null;
		}
	}
				
	/**
	 * Creates the directory that the database uses to find saved files
	 * @param savedCopyDir the directory to create
	 * @throws IllegalStateException if it could not create the directory for any reason
	 */
	private static void createDirectory(File savedCopyDir) throws IllegalStateException {
		if (!savedCopyDir.exists()) {
			if (!savedCopyDir.mkdir()) {
				System.out.println ("Unable to create directory to save scripts in: " + savedCopyDir);
				throw new IllegalStateException("Unable to create directory to save scripts in: " + savedCopyDir);
			}
		}
		else if (!savedCopyDir.isDirectory()) {
			System.out.println ("Unable to create directory to save scripts in: " + savedCopyDir);
			throw new IllegalStateException("Unable to create directory to save scripts in: " + savedCopyDir);
		}
		else if (!savedCopyDir.canWrite()) {
			System.out.println ("Unable to create directory to save scripts in: " + savedCopyDir);
			throw new IllegalStateException("Unable to create directory to save scripts in: " + savedCopyDir);
		}
	}

	/**
	 * Returns the filename to save a file into when copying to the saved file directory
	 * @param processName the name of the process executed 
	 * @param processFileTimestamp the timestamp of the process file executed
	 * @return the filename to save a file into when copying to the saved file directory
	 */
	public static String getSavedFileName(String processName,
			String processFileTimestamp) {
		return SAVED_FILE_DIRECTORY + processName + File.separatorChar + processFileTimestamp + File.separatorChar + processName;
	}

	/**
	 * Deletes the files associated with a particular ddg.  If this is the last ddg associated
	 * with a particular version of a script, it also deletes that version of the script.  If this
	 * is the last version of the script that had any ddgs, it deletes the directory for the 
	 * script.
	 * @param processName the name of the script
	 * @param timestamp the execution time of the ddg
	 * @throws IOException if the deletion fails
	 */
	public static void deleteFiles(String processName, String timestamp) throws IOException {
		File savedProcessDir = new File(SAVED_FILE_DIRECTORY + processName);
		File[] savedTimestampDirs = savedProcessDir.listFiles();
		
		// Make sure the directory exists.  
		if (savedTimestampDirs == null) {
			return;
		}
		
		// The file system is organized as:
		//    <process_name> / <process_timestamp> / <ddg_timestamp> / <saved_files>
		// So, we need to search the process timestamp directories to find the timestamp 
		// for the ddg we are interested in
		for (File savedTimestampDir : savedTimestampDirs) {
			File[] savedDDGDirs = savedTimestampDir.listFiles();
			
			// Look for the desired timestamp in the directory for one version of
			// the script.  If we find it, delete the associated files
			if (findAndDelete(timestamp, savedDDGDirs)) {
				
				// The script directory contains the R code for the script in addition to 
				// the subdirectories for eadh DDG.  If the length of the savedDDGDirs array
				// is 2, it means there was just 1 DDG plus the script.  In that case,
				// delete the entire directory for this timestamp version.
				if (savedDDGDirs.length == 2) {
					Files.walkFileTree(savedTimestampDir.toPath(), new RecursiveFileDeleter());
					
					// If there was only one version of the script, we should also delete the directory
					// for the script since we have no more DDGs associated with it.
					if (savedTimestampDirs.length == 1) {
						Files.walkFileTree(savedProcessDir.toPath(), new RecursiveFileDeleter());
					}
				}
				return;
			}
		}
	}

	/**
	 * Search for a specific DDG directory.  If found, delete the directory and return true.
	 * @param timestamp the timestamp of the DDG we are looking for
	 * @param savedDDGDirs the directory we are searching for the timestamp 
	 * @return true if the directory for the specified DDG was found.  Otherwise return false.
	 * @throws IOException if the deletion fails
	 */
	private static boolean findAndDelete(String timestamp, File[] savedDDGDirs) throws IOException {
		File ddgDir = find(timestamp, savedDDGDirs);
		if (ddgDir == null) {
			return false;
		}

		Files.walkFileTree(ddgDir.toPath(), new RecursiveFileDeleter());
		return true;
	}
	
	/**
	 * Find the directory that contains the version of the script used to create the desired DDG.
	 * @param processName the name of the process/script that was executed
	 * @param ddgTimestamp the timestamp for the process/script execution that created the desired DDG
	 * @return the directory where the process/script verison exists that was used to create the DDG with
	 *    the desired time.  Returns null if such a directory cannot be found.
	 */
	public static File findScriptVersionDir(String processName, String ddgTimestamp) {
		File savedProcessDir = new File(SAVED_FILE_DIRECTORY + processName);
		File[] savedTimestampDirs = savedProcessDir.listFiles();
		
		// Make sure the directory exists.  
		if (savedTimestampDirs == null) {
			return null;
		}
		
		// The file system is organized as:
		//    <process_name> / <process_timestamp> / <ddg_timestamp> / <saved_files>
		// So, we need to search the process timestamp directories to find the timestamp 
		// for the ddg we are interested in
		for (File savedTimestampDir : savedTimestampDirs) {
			File[] savedDDGDirs = savedTimestampDir.listFiles();
			
			// Look for the desired timestamp in the directory for one version of
			// the script.  If we find it, return the directory for that version of the process.
			if (find(ddgTimestamp, savedDDGDirs) != null) {
				
				return savedTimestampDir;
			}
		}
		
		return null;
	}
	
	/**
	 * Find the directory that contains a DDG with the given timestamp, within the list of 
	 * directories passed in
	 * @param timestamp the timestamp to search for
	 * @param savedDDGDirs the directories to search
	 * @return the directory containing the desired timestamp.  Returns null if the
	 *    timestamp cannot be found in the provided directories.
	 */
	private static File find(String timestamp, File[] savedDDGDirs) {
		for (File savedDDGDir : savedDDGDirs) {
			if (savedDDGDir.getName().equals(timestamp)) {
				return savedDDGDir;
			}
		}
		return null;
	}
	
	/**
	 * Recursively walk a directory, deleting all the files
	 * @author Barbara Lerner
	 * @version Oct 16, 2013
	 *
	 */
	private static class RecursiveFileDeleter extends SimpleFileVisitor<Path> {

		/**
		 * Delete the file
		 */
		@Override
		public FileVisitResult visitFile(Path file,
				BasicFileAttributes attrs) throws IOException {
			// make sure we can delete the file
			file.toFile().setWritable(true);
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Delete the directory after it has been emptied
		 */
		@Override
		public FileVisitResult postVisitDirectory(Path dir,
				IOException exc) throws IOException {
			dir.toFile().setWritable(true);
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
		
	}

}