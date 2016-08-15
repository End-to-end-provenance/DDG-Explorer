package laser.ddg;

/**
 * Holds the values indicating where the source code is that 
 * corresponds to a node in the DDG.
 * 
 * @author Barbara Lerner
 * @version Aug 15, 2016
 *
 */
public class SourcePos {
	private final int scriptNumber;
	
	private final int startLine;
	private final int startCol;
	private final int endLine;
	private final int endCol;
	
	/**
	 * Creates an object to remember the location of some part of a source script
	 * @param scriptNumber the script number as used in the ddg.  Pass in -1 if unknown.
	 * @param startLine the line number on which the object starts.  Pass in -1 if unknown.
	 * @param startCol the column number on which it starts.  Pass in 0 if unknown.
	 * @param endLine the line number on which it ends.  Pass in -1 if unknown.
	 * @param endCol the column on which it ends.  Pass in 0 if unknown.
	 */
	public SourcePos(int scriptNumber, int startLine, int startCol, int endLine, int endCol) {
		super();
		this.scriptNumber = scriptNumber;
		this.startLine = startLine;
		this.startCol = startCol;
		this.endLine = endLine;
		this.endCol = endCol;
	}

	/**
	 * 
	 * @return the script number as used in the ddg.  Returns -1 if unknown.
	 */
	public int getScriptNumber() {
		return scriptNumber;
	}

	/**
	 * 
	 * @return the line number on which the object starts.  Returns -1 if unknown.
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * 
	 * @return the column on which the object starts.  Returns 0 if unknown.
	 */
	public int getStartCol() {
		return startCol;
	}

	/**
	 * 
	 * @return the line number on which the object ends.  Returns -1 if unknown.
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * 
	 * @return the column number on which the object ends.  Returns 0 if unknown.
	 */
	public int getEndCol() {
		return endCol;
	}
}
