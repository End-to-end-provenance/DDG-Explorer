/**
 * 
 */
package laser.ddg.persist;

import java.text.DateFormat;
import java.util.Calendar;

import laser.ddg.DataBindingEvent;
import laser.ddg.DataInstanceNode;
import laser.ddg.Node;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.ProvenanceListener;
import laser.ddg.ScriptNode;

/**
 * @author xiang
 * 
 */
public class ProvenanceFileWriter implements ProvenanceListener {
	// The ddg being output
	private ProvenanceData provData;
	
	// Details about the ddg
	private String processName;
	private String timestamp;
	private String language;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laser.ddg.DataBindingListener#bindingCreated(laser.ddg.DataBindingEvent)
	 */
	@Override
	public void bindingCreated(DataBindingEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laser.ddg.ProvenanceListener#processStarted(java.lang.String,
	 * laser.ddg.ProvenanceData)
	 */
	@Override
	public void processStarted(String processName, ProvenanceData provData) {
		String time = DateFormat.getDateTimeInstance(DateFormat.FULL,
				DateFormat.FULL).format(Calendar.getInstance().getTime());
		processStarted(processName, provData, time, language);
	}
	
	@Override
	public void processStarted(String processName, ProvenanceData provData,String timestamp, String lang) {
		// TODO Auto-generated method stub
		this.processName = processName;
		this.provData = provData;
		this.timestamp = timestamp;
		this.language = lang;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laser.ddg.ProvenanceListener#processFinished()
	 */
	@Override
	public void processFinished() {
		// TODO Auto-generated method stub
		FileUtil.rewritetofile(System.getProperty("user.home")
				+ "/provenancedata.txt", provData.toString());
		provData.drawGraph();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laser.ddg.ProvenanceListener#procedureNodeCreated(laser.ddg.
	 * ProcedureInstanceNode)
	 */
	@Override
	public void procedureNodeCreated(ProcedureInstanceNode pin) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laser.ddg.ProvenanceListener#dataNodeCreated(laser.ddg.DataInstanceNode)
	 */
	@Override
	public void dataNodeCreated(DataInstanceNode din) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laser.ddg.ProvenanceListener#successorEdgeCreated(laser.ddg.
	 * ProcedureInstanceNode, laser.ddg.ProcedureInstanceNode)
	 */
	@Override
	public void successorEdgeCreated(ProcedureInstanceNode predecessor,
			ProcedureInstanceNode successor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rootSet(Node root) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scriptNodeCreated(ScriptNode sn) {
		// TODO Auto-generated method stub
		
	}

}
