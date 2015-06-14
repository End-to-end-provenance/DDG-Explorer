package laser.ddg.gui;

import java.io.File;

import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaLoader;

/**
 * A browser that allows the user to select a DDG currently stored in the database
 * @author Barbara Lerner
 * @version Oct 21, 2013
 *
 */
public class DDGBrowser extends DBBrowser {

	/**
	 * Creates and displays the browser window
	 * @param jenaLoader the object that can read DDGs from the database
	 */
	public DDGBrowser(JenaLoader jenaLoader) {
		super(jenaLoader);
	}

	/**
	 * @return the directory containing the files corresponding to the DDG execution
	 * 		selected by the user
	 */
	@Override
	public File getSelectedFile() {
		String filename = FileUtil.getSavedFileName(getSelectedProcessName(), getSelectedTimestamp());
		return new File(filename);
	}

}
