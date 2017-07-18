package laser.ddg;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 * The data instance node holds a state of a data entry whose processing is
 * being recorded with the use of a DDG. Similarly to the data it wraps, it can
 * only have one producer, but multiple users. The producer and users of the DIN
 * can be used as connections to the respective procedure instance nodes in the
 * graph to provide for the bidirectional traversal of the DDG. (It is
 * considered impossible for two DINs to be immediately adjacent in a DDG since
 * a new DIN is constructed only in the event that the data undergoes a
 * transformation, which is represented as a Procedure Instance Node). The Data
 * Instance Nodes are not assigned unique IDs as they are constructed. The ID
 * number is reset to a unique number when the DIN is added to the set of DINs
 * in the ProvenanceData class.
 * 
 * Customized to be a java bean to allow for the use of the JenaBean toolkit to
 * make persistent nodes. Particularly: - has a nullary constructor - has 
 * @RdfProperty tags for properties that need to be made persistent - has 
 * getters and setters for properties that need to be made persistent - has 
 * @Transient tags for properties that must not persist - has an @Id tag for 
 * the unique identifier of each instance of the class
 * 
 * 
 * @author Sophia
 * 
 */
// To specify custom namespace use @Namespace("") and import
// thewebsemantic.Namespace;
public abstract class AbstractDataInstanceNode implements DataInstanceNode {

	// The Java data produced by the running process
	private Serializable value;

	// The id assigned to a DIN
	private int id;

	/**
	 * DIN name
	 */
	private String nameOfDIN;

	// The date & time the DIN was created.
	private String timeCreated;

	// The original location of a file.  Null if this is not a file node.
	private String location;

	// The hash of the original file. Null if this is not a file node.
	private String hash;
	
	// The read/write status of the file. Null if this is not a file node.
	private String rw;

	// The message digest object for use in generating md5 hash values
	private MessageDigest md;

	// The procedure that created this data
	private ProcedureInstanceNode producedBy;

	//Flag to ensure producer is only set once.
	private int hasProducer;

	// The procedures that use this data
	private Set<ProcedureInstanceNode> usedByPIN = new LinkedHashSet<>();

	// The provenance data that this node belongs to
	private ProvenanceData provData;

	// Attribute-value pairs to allow arbitrary extensions
	private Map<String, Object> attributeValues = new TreeMap<>();
	
	// Script location
	private String scrloc;

	/**
	 * Create a data instance node wrapping the value passed in the process.
	 * 
	 * @param val
	 *            the value passed in the process
	 * @param name
	 *            the name of the DIN
	 * @param producer the procedure node that output this data value
	 * @param provData
	 *            the provenance data this node is added to
	 */
	public AbstractDataInstanceNode(Serializable val, String name,
			ProcedureInstanceNode producer, ProvenanceData provData) {
		value = val;
		nameOfDIN = name;
		producedBy = producer;
		this.hasProducer = 1;
		timeCreated = Calendar.getInstance().toString();
		id = 0;
		this.provData = provData;
		this.hash = null;
		this.scrloc = null;
	}

	/**
	 * Create a data instance node wrapping the value passed in the process.
	 * 
	 * @param val
	 *            the value passed in the process
	 * @param name
	 *            the name of the DIN
	 * @param provData
	 *            the provenance data this node is added to
	 */
	public AbstractDataInstanceNode(Serializable val, String name,
			ProvenanceData provData) {
		value = val;
		nameOfDIN = name;
		id = 0;
		this.provData = provData;
		this.hash = null;
		this.scrloc = null;
	}

	/**
	 * Create a data instance node wrapping the value passed in the process.  This
	 * version is used when reading an existing DDG either from a file or the 
	 * database.
	 * 
	 * @param val
	 *            the value passed in the process
	 * @param name
	 *            the name of the DIN
	 * @param time
	 *            the time that the data node was originally created
	 * @param location
	 * 			   the original location of a file, or null if not a file
	 */
	public AbstractDataInstanceNode(String val, String name, String time, String location) {
		value = val;
		nameOfDIN = name;
		timeCreated = time;
		this.location = location;
		if (location != null) {
			try {
				this.hash = doFileHashing(location);
			} catch (IOException e) {
				this.hash = null;
				e.printStackTrace();
			}
		} else {
			this.hash = null;
		}
	}
	
	public AbstractDataInstanceNode(String val, String name, String time, String location, String hash, String scrloc) {
		value = val;
		nameOfDIN = name;
		timeCreated = time;
		this.location = location;
		this.hash = hash;
		this.scrloc = scrloc;
	}

	/**
	 * Produces the MD5 hash of the file of the given node.
	 * 
	 * @param location
	 * @return hexString, a hexadecimal string representation of the file's MD5 hash.
	 * @throws IOException
	 */
	public String doFileHashing(String location) throws IOException {
		try {
			this.md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		//http://howtodoinjava.com/core-java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
		FileInputStream in = new FileInputStream(location);
		byte[] b = new byte[1024];
		int numbytes = 0;
		while ((numbytes = in.read(b)) != -1) {
			md.update(b, 0, numbytes);
		}
		byte[] digest = md.digest();
		String hexString = Hex.encodeHexString(digest);
		in.close();
		return hexString;
	}

	/**
	 * Nullary constructor required for objects to be java beans
	 */
	public AbstractDataInstanceNode() {
		// no producer node set yet
		this.hasProducer = -1;
	}

	/**
	 * Produces a short display representation of the node
	 * 
	 * @return DIN name
	 * 
	 */
	@Override
	public String toString() {
		return getType() + " d" + id + " \"" + nameOfDIN + "\"";
	}

	/**
	 * Returns a simplified string representation of the type of node
	 * 
	 * @return a simplified string representation of the type of node
	 */

	@Override
	public String getType() {
		return "Data";
	}

	/**
	 * @return the name of the Data Instance Node
	 */

	@Override
	public String getName() {
		return nameOfDIN;
	}



	/**
	 * @return the Java data produced by the running process
	 */

	@Override
	public Serializable getValue() {
		return value;
	}

	@Override
	public void setValue(Serializable value) {
		this.value = value;
	}

	@Override
	public String getLocation() {
		return location;
	}

	/**
	 * @return date & time the Data Instance Node was created
	 */

	@Override
	public String getCreatedTime() {
		return timeCreated;
	}

	@Override
	public double getElapsedTime() {
		return 0.0;
	}

	/**
	 * Adds a producer node to a data node
	 * @param p the procedure/function node that serves as the producer for the data
	 * @param d the relevant data node to be connected with
	 */
	@Override
	public void setProducer(ProcedureInstanceNode p, DataInstanceNode d){
		//make sure producer is not already set
		if(this.hasProducer >= -1){
			this.producedBy = p;
			this.hasProducer = 1;
		}else{
			System.err.println("Cannot reset the producer for this data node.");
		}
	}

	/**
	 * @return the ProcedureInstanceNode that created the data of this DIN
	 */

	@Override
	public ProcedureInstanceNode getProducer() {
		return producedBy;
	}

	/**
	 * @return an iterator through the procedures (ProcedureInstanceNodes) that
	 *         use this data
	 */

	@Override
	public Iterator<ProcedureInstanceNode> users() {
		return usedByPIN.iterator();
	}

	/**
	 * Record that a procedure is using this value
	 * 
	 * @param user
	 *            the using procedure
	 */
	@Override
	public void addUserPIN(ProcedureInstanceNode user) {
		usedByPIN.add(user);

	}

	/**
	 * Getter for the DIN ID
	 * 
	 * @return the ID assigned to a DIN
	 */

	@Override
	public int getId() {
		return id;

	}

	/**
	 * @return all outputs coming from the given Data Instance Node
	 */

	@Override
	public Set<DataInstanceNode> getProcessOutputsDerived() {
		HashSet<DataInstanceNode> processOutputs = new HashSet<>();
		if (provData.isProcessOutput(this)) {
			processOutputs.add(this);
		}
		Iterator<ProcedureInstanceNode> it1 = this.users();
		while (it1.hasNext()) {
			processOutputs.addAll(it1.next().getProcessOutputsDerived());
		}
		return processOutputs;
	}

	/**
	 * @return the "raw data" input for a given DIN
	 */

	@Override
	public Set<DataInstanceNode> getProcessInputsDerived() {
		Set<DataInstanceNode> processInputs = new HashSet<>();
		if (provData.isProcessInput(this)) {
			processInputs.add(this);
		}
		processInputs.addAll(this.getProducer().getProcessInputsDerived());
		return processInputs;
	}

	/**
	 * Set the ID that will be assigned to a DIN. The new ID must not be the
	 * same as the ID assigned to any other DIN. It also must not be zero
	 * because all DINs that have not been added to the ProvenanceData's list of
	 * DINs have zero IDs.
	 * 
	 * @param newId
	 *            the integer ID to be assigned
	 * @throws IdAlreadySetException
	 * 
	 * 
	 */
	@Override
	public void setId(int newId) throws IdAlreadySetException {
		assert newId != 0;
		if (id == 0) {
			id = newId;
		} else {
			throw new IdAlreadySetException("Cannot reset the ID of a node that has already been assigned an ID.");
		}
	}

	/**
	 * @return an iterator over all the attribute names attached to this node.
	 */
	@Override
	public Iterator<String> attributes() {
		return attributeValues.keySet().iterator();
	}

	/**
	 * @param name
	 *            the name of the attribute
	 * @return the value associated with an attribute. Returns null if the
	 *         attribute name is not known.
	 */
	@Override
	public Object getAttributeValue(String name) {
		return attributeValues.get(name);
	}

	/**
	 * Changes the value associated with an attribute. Creates the attribute if
	 * it is previously unknonwn.
	 * 
	 * @param name
	 *            the attribute name
	 * @param value
	 *            the attribute value
	 */
	@Override
	public void setAttribute(String name, Object value) {
		attributeValues.put(name, value);
	}

	/**
	 * @return the hash value, if a file node. If not a file node, then return null.
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * @return the read or write value, if a file node. If not a file node, then return null.
	 */
	public String getRw() {
		return rw;
	}

	/**
	 * @return the location of the script. If not a file node, then return null.
	 */
	public String getScrloc() {
		return scrloc;
	}

	/**
	 * @param scrloc the location of the script.
	 */
	public void setScrloc(String scrloc) {
		this.scrloc = scrloc;
	}


}
