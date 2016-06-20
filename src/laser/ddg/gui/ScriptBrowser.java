package laser.ddg.gui;

import java.io.File;

import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaLoader;

/**
 * A browser that allows the user to select an R script to view by choosing
 * the name of the script and the DDG execution the user is interested in.
 * 
 * @author Barbara Lerner
 * @version Oct 21, 2013
 *
 */
public class ScriptBrowser extends DBBrowser {

	/**
	 * Display the browser
	 * @param jenaLoader the object that can read from the database
	 */
	public ScriptBrowser(JenaLoader jenaLoader) {
		super(jenaLoader);
	}

	/**
	 * @return the file containing the R script that was used to generate a
	 * 		DDG based on the timestamp of the DDG.
	 */
	@Override
	public File getSelectedFile() {
		String scriptFilename = null;
		String script = getSelectedProcessName();
		
		// The timestamp displayed in the browser is the timestamp of the DDG that was
		// executed, so we need to search for the right script directory.
		File scriptDir = FileUtil.findScriptVersionDir(script, getSelectedTimestamp());
		
		if (scriptDir == null) {
			DDGExplorer.showErrMsg("Could not find directory for " + script + " with timestamp " + getSelectedTimestamp());
			return null;
			
		}
		
		scriptFilename = scriptDir.getAbsolutePath() + File.separatorChar + script;
		File scriptFile = new File(scriptFilename);
		if (scriptFile.exists()) {
			return scriptFile;
		}
		
		DDGExplorer.showErrMsg("Could not find " + script + " in " + scriptDir);
		return null;
		
	}


}
