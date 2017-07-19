package laser.ddg.persist;

public class HashtableEntry {

	/**
	 * This class represents an entry in the hashtable.
	 * 
	 * @param ScriptPath The path to the script
	 * @param FilePath The path to the file referred to. Referred to to generate node names
	 * @param DDGPath The path to the DDG information
	 * @param NodePath The path to the node
	 * @param NodeNumber The numerical id of the original node
	 * @param SHA1Hash The SHA1Hash value of the file
	 * @param ReadWrite Whether the file was read from or written to
	 * @param Timestamp The timestamp of the file
	 * @param Value The ddg csv value for the file
	 */
	public HashtableEntry(String ScriptPath, String FilePath, String DDGPath, String NodePath, 
			int NodeNumber, String SHA1Hash, String ReadWrite, String Timestamp, String Value) {
		this.ScriptPath = ScriptPath;
		this.FilePath = FilePath;
		this.DDGPath = DDGPath;
		this.NodePath = NodePath;
		this.NodeNumber = NodeNumber;
		this.SHA1Hash = SHA1Hash;
		this.ReadWrite = ReadWrite;
		this.Timestamp = Timestamp;
		this.Value = Value;
	}
	
	private String ScriptPath;
	private String FilePath;
	private String DDGPath;
	private String NodePath;
	private int NodeNumber;
	private String SHA1Hash;
	private String ReadWrite;
	private String Timestamp;
	private String Value;
	
	public String getScriptPath() {
		return ScriptPath;
	}
	
	public String getFilePath() {
		return FilePath;
	}
	
	public String getDDGPath() {
		return DDGPath;
	}
	
	public String getNodePath() {
		return NodePath;
	}
	
	public int getNodeNumber() {
		return NodeNumber;
	}
	
	public String getSHA1Hash() {
		return SHA1Hash;
	}
	
	public String getReadWrite() {
		return ReadWrite;
	}
	
	public String getTimestamp() {
		return Timestamp;
	}
	
	public String getValue() {
		return Value;
	}
}
