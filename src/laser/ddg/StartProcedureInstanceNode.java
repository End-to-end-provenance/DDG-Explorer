package laser.ddg;

/**
 * During the traversal of a Process Derivation graph non-leaf nodes are
 * encountered multiple times in the processing of the data as the control
 * returns to them after the execution of each of their child nodes. The Start
 * PIN interface is used for constructing objects that are known to stand for
 * the first encounter of a non-leaf procedure instance node in the process
 * 
 * @author Sophia
 * 
 */
public interface StartProcedureInstanceNode extends ProcedureInstanceNode {
	/**
	 * Body is empty b/c a Procedure Instance Node only needs to be able to be
	 * marked as "completed" or "terminated" if it is a Finish PIN
	 */
}
