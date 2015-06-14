package laser.ddg;

/**
 * An event that describes a data binding action. The event identifies whether
 * the action is an input or output binding, the data, the procedure and the
 * parameter being bound.
 * 
 * @author Barbara Lerner
 * @version Jan 10, 2011
 * 
 */
public class DataBindingEvent {
	/**
	 * Identifies if the binding is an input parameter or an output parameter
	 * being bound.
	 */
	public enum BindingEvent {
		/** Data is an input to the procedure */
		INPUT,

		/** Data is an output from the procedure */
		OUTPUT
	}

	private final BindingEvent event;
	private final DataInstanceNode dataNode;
	private final ProcedureInstanceNode procNode;
	private final String paramName;

	/**
	 * Creates a data binding event
	 * 
	 * @param inOrOut
	 *            indicates if the data is an input or an output
	 * @param dataNode
	 *            the data being passed
	 * @param procNode
	 *            the procedure reading or writing the data
	 * @param paramName
	 *            the parameter that the data is referred to inside the
	 *            procedure
	 */
	public DataBindingEvent(BindingEvent inOrOut, DataInstanceNode dataNode,
			ProcedureInstanceNode procNode, String paramName) {
		this.event = inOrOut;
		this.dataNode = dataNode;
		this.procNode = procNode;
		this.paramName = paramName;
	}

	/**
	 * @return whether this is an input or output event
	 */
	public BindingEvent getEvent() {
		return event;
	}

	/**
	 * @return the data being passed
	 */
	public DataInstanceNode getDataNode() {
		return dataNode;
	}

	/**
	 * @return the procedure doing the passing
	 */
	public ProcedureInstanceNode getProcNode() {
		return procNode;
	}

	/**
	 * @return the parameter name for the data within the procedure
	 */
	public String getParamName() {
		return paramName;
	}

}
