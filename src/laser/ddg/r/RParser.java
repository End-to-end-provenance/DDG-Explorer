package laser.ddg.r;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import laser.ddg.LanguageParser;
import laser.ddg.gui.DDGExplorer;

/**
 * Minimal parser for R.  It can find function definitions.
 * 
 * @author Barbara Lerner
 * @version Jul 26, 2013
 *
 */
public class RParser implements LanguageParser {
	// Name of the file that a script was read from
	private String fileName = null;
	
	// Contents of the read file
	private String fileContents = null;
	
	/**
	 * Reads a script from a file, setting the fileName and
	 * fileContents instance variables.
	 * @param script full path to the file to read.  It should
	 * 		contain R code.
	 */
	private void readScript(String script) {
		fileName = script;
		File theFile = new File(script);
		Scanner readFile = null;
    	StringBuilder contentsBuilder = new StringBuilder(); 

		try {
			readFile = new Scanner(theFile);

			//System.out.println("\n" + str);
			while (readFile.hasNextLine()) {
				String line = readFile.nextLine();
				int commentStart = line.indexOf("#");
				
				// Remove everything following a comment character
				if (commentStart >= 0) {
					line = line.substring(0, commentStart);
				}
				
				// If the entire line is a comment, don't add it to fileContents
				if (commentStart != 0) {
					contentsBuilder.append(line + '\n');
				}
			}
		} catch (FileNotFoundException e) {
			DDGExplorer.showErrMsg("There is no script available for " + script + "\n\n");
		} finally {
			if (readFile != null) {
				fileContents = contentsBuilder.toString();
				readFile.close();
			}
		}
	}

	/**
	 * Builds a table mapping function names to function bodies.
	 * @param script the name of the file to parse
	 * @return the function table constructed
	 */
	@Override
	public Map<String, String> buildFunctionTable(String script) {
		if (!script.equals(fileName)) {
			readScript(script);
		}

		if (fileContents != null) {
			Map<String, String> functionTable = findFunctionDeclarations(fileContents.toString());
			//System.out.println(fileContents.toString());
			return functionTable;
			
		} 
		
		return new HashMap<>();
	}

	/**
	 * Find blocks of code set off by ddg.start and ddg.finish calls and add those to
	 * the function table
	 * @param script name of the file to examine
	 * @return a table mapping block names to the code within
	 * 	the blocks.
	 */
        @Override
	public Map<String, String> buildBlockTable(String script) {
		if (!script.equals(fileName)) {
			readScript(script);
		}

		if (fileContents == null) {
			return new HashMap<>();
		}
		
		Map<String, String> blockTable = new HashMap<>();
		//ErrorLog.showErrMsg("Searching for start-finish blocks");
		int nextStart = fileContents.indexOf("ddg.start");
		int count = 0;
		while (nextStart != -1) {
			//ErrorLog.showErrMsg("Found ddg.start");
			String blockName = getBlockName(fileContents, nextStart);
			if (blockName != null) {
				//ErrorLog.showErrMsg("Block: " + blockName);
				//System.out.println("Found block start: " + blockName);
				if (blockTable.containsKey(blockName)) {
					DDGExplorer.showErrMsg("There is more than one definition of block " + blockName + "\n\n");
					blockTable.put(blockName, "There is more than one definition of block " + blockName + ".");
				}
				else {
					//ErrorLog.showErrMsg("Not previously defined");
					count++;
					int blockFinish = getBlockFinish(fileContents, nextStart, blockName);
					if (blockFinish == -1) {
						JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "ddg.finish is missing for block " + blockName + "\n\n");
					}
					else {
						//ErrorLog.showErrMsg("Found block finish");
						//System.out.println("Found block end: " + blockName);
						int blockStart = fileContents.indexOf("\n", nextStart) + 1;
						String block = fileContents.substring(blockStart, blockFinish); 
						blockTable.put(blockName, block);
						//ErrorLog.showErrMsg(block);
						//System.out.println(blockName);
						//System.out.println(block + "\n\n\n");
					}
				}
			}
			nextStart = fileContents.indexOf("ddg.start", nextStart + 1);
		}
		//ErrorLog.showErrMsg("Found " + count + " block starts");
		//System.out.println("Found " + count + " block starts");
		return blockTable;
	}

	/**
	 * Get the name associated with a block
	 * @param script the entire script
	 * @param nextStart the beginning of the ddg.start call
	 * @return the name passed to ddg.start
	 */
	private String getBlockName(String script, int nextStart) {
		int nameStart = script.indexOf("\"", nextStart) + 1;
		int nameFinish = script.indexOf("\"", nameStart);
		int rightParen = script.indexOf(")", nextStart) + 1;
		
		// Check for the case where no name is provided
		if (rightParen < nameStart) {
			return null;
		}
		return script.substring(nameStart, nameFinish);
	}

	/**
	 * Get the location of the ddg.finish call for a block
	 * @param script the entire R script
	 * @param nextStart where the ddg.start call is
	 * @param blockName the name of the block we are searching for
	 * @return the location of the start of the ddg.finish call
	 */
	private int getBlockFinish(String script, int nextStart, String blockName) {
		int blockFinish = script.indexOf("ddg.finish", nextStart);
		String finishBlockName = null;
		while (blockFinish != -1) {
			finishBlockName = getBlockName(script, blockFinish);
			if (blockName.equals(finishBlockName)) {
				return blockFinish;
			}
			blockFinish = script.indexOf("ddg.finish", blockFinish + 1);
		}
		return -1;
	}

	private Map<String, String> findFunctionDeclarations(String script) {
		Map<String, String> functionTable = new HashMap<>();
		int nextFunctionKeyword = script.indexOf("function");
		int count = 0;
		while (nextFunctionKeyword != -1) {
			int bindSymbolStart = getPrecedingTokenBindSymbol(script, nextFunctionKeyword);
			if(bindSymbolStart != -1) {
				String functionName = getBoundName(script, bindSymbolStart);
				if (!functionName.equals("")) {
					if (functionTable.containsKey(functionName)) {
						// What about overloadings?  Overridings?  R does not have these.  However, function
						// names are bound late to function bodies, so the same name can be assigned more
						// than one function.  Also, functions can anonymous and be passed as parameters.
						DDGExplorer.showErrMsg("There is more than one definition of the function " + functionName + "\n\n");
						functionTable.put(functionName, "There is more than one definition of the function " + functionName + ".");
					}
					else {
						count++;
						String functionBody = getFunctionBody(script, nextFunctionKeyword);
						functionTable.put(functionName, functionBody);
						//System.out.println(functionName);
						//System.out.println(functionBody + "\n\n\n");
					}
				}
			}
			nextFunctionKeyword = script.indexOf("function", nextFunctionKeyword + 1);
		}
		//System.out.println("Found " + count + " function declarations");
		return functionTable;
	}

	/**
	 * Function syntax is:
	 * 	  function ( arglist ) body
	 * 
	 * body is either an expression or a group of expressions enclosed in {  }
	 * @param script
	 * @param i
	 * @return
	 */
	private String getFunctionBody(String script, int startIndex) {
		int argListEnd = script.indexOf(')', startIndex);
		
		// Skip over whitespace
		int i = argListEnd+1;
		char nextChar = script.charAt(i);
		for (; Character.isWhitespace(nextChar); i++) {
			nextChar = script.charAt(i);
		}
		
		if (nextChar != '{') {
			//System.out.println("Function contains a single expression.  I don't know how to parse that!!!");
			int endOfLine = script.indexOf('\n', i);
			return script.substring(startIndex, endOfLine);
		}
		
		// Count { and }.  End of function is reached when count is 0.
		int openBrackets = 1;
		i++;
		nextChar = script.charAt(i);
		for (; openBrackets != 0; i++) {
			if (nextChar == '{') {
				openBrackets++;
			}
			else if (nextChar == '}') {
				openBrackets--;
			}
			nextChar = script.charAt(i);
		}
		
		return script.substring(startIndex, i);
	}

	private String getBoundName(String script, int bindSymbolStart) {
		String name = "";
		
		// Skip over white space
		int i = bindSymbolStart - 1;
		for (; i >= 0 && Character.isWhitespace(script.charAt(i)); i--) {
		}
		
		// Collect the letters of the name
		for (; i > 0; i--) {
			char prevChar = script.charAt(i);
			if (isNameChar(prevChar)) {
				name = prevChar + name;
			}
			else {
				return name;
			}
		}
		
		return name;
	}
	
	private boolean isNameChar(char c) {
		if (Character.isAlphabetic(c)) {
			return true;
		}
		
		if (c == '.') {
			return true;
		}
		
		if (c == '_') {
			return true;
		}
		
		return false;
	}

	private int getPrecedingTokenBindSymbol(String script, int startingPos) {
		int bindStart = script.lastIndexOf("<-", startingPos);
		if (bindStart == -1) {
			return -1;
		}
		
		// Make sure everything between the previous bind symbol and startingPos is whitespace
		for (int i = bindStart + 2; i < startingPos; i++) {
			if (!Character.isWhitespace(script.charAt(i))) {
				return -1;
			}
		}
		
		return bindStart;
	}

	public static void main (String[] args) {
		JFileChooser fileChooser = new JFileChooser (System.getProperty("user.home"));
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			new RParser().buildFunctionTable(fileChooser.getSelectedFile().getAbsolutePath());	
		}
		
	}
}
