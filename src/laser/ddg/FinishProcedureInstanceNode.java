package laser.ddg;

/**
 * During the traversal of a Process Derivation graph non-leaf nodes are
 * encountered multiple times in the processing of the data as the control
 * returns to them after the execution of each of their child nodes. The
 * Finish-PIN interface is used for constructing objects that are known to stand
 * for the last encounter of a non-leaf procedure instance node
 * 
 * @author Sophia
 * 
 */
public interface FinishProcedureInstanceNode extends ProcedureInstanceNode {

	/**
	 * Change the status to completed and timestamp the change.
	 * 
	 * @throws StatusAlreadySetException
	 * */
	public void complete() throws StatusAlreadySetException;

	/**
	 * Change the status to terminated and timestamp the change.
	 * 
	 * @throws StatusAlreadySetException
	 * */
	public void terminate() throws StatusAlreadySetException;

	/**
	 * @return true if the state of the procedure is completed
	 */
	public boolean isCompleted();

	/**
	 * @return true if the state of the procedure is terminated
	 */
	public boolean isTerminated();
}
