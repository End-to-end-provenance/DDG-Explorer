package laser.ddg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.FileUtil;

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
	private String filepath;
	
	// The modification date of the file containing this script
	private String timestamp;

	public ScriptInfo(String filepath, String timestamp) {
		System.out.println("Creating ScriptInfo for " + filepath + " " + timestamp);
		this.filepath = filepath;
		this.timestamp = timestamp;
	}

	public String getFilepath() {
		return filepath;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String copyScriptFile() {
		System.out.println("File to copy: " + filepath + "\n");
		File theFile = new File(filepath);
		//Scanner readFile;

		try {
			//readFile = new Scanner(theFile);
			//PrintWriter writeFile = null;
			File savedFile = FileUtil.createSavedFile(theFile);
			System.out.println("Copying " + theFile.toPath() + " to " + savedFile.toPath() + "\n");
			
			// It may have been copied on a previous execution.
			if (!savedFile.exists()) {
				System.out.println("Copying");
				Files.copy(theFile.toPath(), savedFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
				if (!savedFile.exists()) {
					DDGExplorer.showErrMsg("Copy failed!!!\n\n");
				}
				savedFile.setReadOnly();
			}
			else {
				System.out.println("Not copying.  Already there.");
			}
			return savedFile.getAbsolutePath();
			
			//System.out.println(fileContents.toString());
		} catch (FileNotFoundException e) {
			DDGExplorer.showErrMsg("Cannot find file: " + filepath + "\n\n");
			return null;
		} catch (IOException e) {
			DDGExplorer.showErrMsg("Exception copying" + filepath + "to database: " + e);
			return null;
		}
	}
	
	@Override
	public String toString () {
		return filepath + " " + timestamp;
	}

}
