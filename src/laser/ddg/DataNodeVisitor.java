package laser.ddg;

import java.util.ArrayList;

import laser.ddg.gui.DDGExplorer;

public class DataNodeVisitor implements ProvenanceDataVisitor {

	private ArrayList<String> nodehashes;
	
	public DataNodeVisitor() {
		nodehashes = new ArrayList<String>();
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
			String dinHash = din.getHash();
			nodehashes.add(dinHash);
		}
	}
	
	public ArrayList<String> getNodeHashes() {
		return nodehashes;
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
