package laser.ddg;

import java.util.ArrayList;

import laser.ddg.gui.DDGExplorer;

public class DataNodeVisitor implements ProvenanceDataVisitor {

	private ArrayList<DataInstanceNode> dins;
	
	public DataNodeVisitor() {
		dins = new ArrayList<DataInstanceNode>();
	}
	
	/**
	 * Visits all nodes in a particular DDG.
	 */
	public void visitNodes() {
		ProvenanceData currDDG = DDGExplorer.getInstance().getCurrentDDG();
		currDDG.visitDins(this);
	}
	
	@Override
	public void visitPin(ProcedureInstanceNode pin) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Visits a data instance node and retrieves its MD5 hash value.
	 * 
	 * @param din The data instance node currently being visited.
	 */
	@Override
	public void visitDin(DataInstanceNode din) {
		String dinType = din.getType();
		if (dinType.equals("File")) {
			dins.add(din);
		}
	}
	
	public ArrayList<DataInstanceNode> getDins() {
		return dins;
	}

	@Override
	public void visitControlFlowEdge(ProcedureInstanceNode predecessor, ProcedureInstanceNode successor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitInputEdge(DataInstanceNode input, ProcedureInstanceNode consumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitOutputEdge(ProcedureInstanceNode producer, DataInstanceNode output) {
		// TODO Auto-generated method stub
		
	}

}
