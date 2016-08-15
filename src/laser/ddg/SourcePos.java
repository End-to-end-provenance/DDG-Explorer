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
	
	public SourcePos(int scriptNumber, int startLine, int startCol, int endLine, int endCol) {
		super();
		this.scriptNumber = scriptNumber;
		this.startLine = startLine;
		this.startCol = startCol;
		this.endLine = endLine;
		this.endCol = endCol;
	}

	public int getScriptNumber() {
		return scriptNumber;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartCol() {
		return startCol;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndCol() {
		return endCol;
	}
}
