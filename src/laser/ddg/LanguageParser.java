package laser.ddg;

import java.util.Map;

/**
 * Implementations of this class allow us to parse source programs written in 
 * different languages so that we can look up and/or save information about 
 * those programs.
 * 
 * @author Barbara Lerner
 * @version Jul 26, 2013
 *
 */
public interface LanguageParser {

	/**
	 * Parse the script and create a table where the keys are function names
	 * and the values are the function bodies
	 * @param script file containing the program text
	 * @return the table
	 */
	public Map<String, String> buildFunctionTable(String script);

	/**
	 * Parse the script and create a table where the keys are block names
	 * and the values are the block bodies
         * @param fileName
	 * @return the table
	 */
	public Map<String, String> buildBlockTable(String fileName);

}
