package laser.ddg;

/**
 * A provenance listener is notified of all changes to a DDG:
 * <ul>
 * <li> When the process starts
 * <li> When the process finishes
 * <li> When a procedure node is created
 * <li> When a data node is created
 * <li> When a control flow edge is created
 * <li> When a data flow edge is created (via the superinterface DataBindingListener
 * </ul>
 * @author Barbara Lerner
 * @version Jun 25, 2012
 *
 */
public interface ProvenanceListener extends DataBindingListener {
	/**
	 * Called when a process starts
	 * @param processName the name of the process started
	 * @param provData the DDG that holds the details of the process execution
	 * @throws RemoveListenerException 
	 */
	public void processStarted (String processName, ProvenanceData provData) throws RemoveListenerException;
	
	/**
	 * Called when a process starts
	 * @param processName the name of the process started
	 * @param provData the DDG that holds the details of the process execution
	 * @param timestamp timestamp of the process
	 * @param language the language the process is written in
	 * @throws RemoveListenerException 
	 * 
	 */
	public void processStarted (String processName, ProvenanceData provData, String timestamp, String language) throws RemoveListenerException;
	
	/**
	 * Called when a process finishes
	 */
	public void processFinished();
	
	/**
	 * Called when a procedure node is created in the DDG
	 * @param pin the node created
	 */
	public void procedureNodeCreated (ProcedureInstanceNode pin);
	
	/**
	 * Called when a data node is add to the DDG
	 * @param din the node created
	 */
	public void dataNodeCreated (DataInstanceNode din);
	
	/**
	 * Called when a control flow edge is created
	 * @param predecessor the predecessor procedure node
	 * @param successor the successor procedure node
	 */
	public void successorEdgeCreated (ProcedureInstanceNode predecessor, ProcedureInstanceNode successor);

	/**
	 * Called when the root node is set
	 * @param root the root node
	 */
	public void rootSet(Node root);
}
