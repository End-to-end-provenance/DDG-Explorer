package laser.ddg.r;

import laser.ddg.ProvenanceData;

public class RBindingNode extends RFunctionInstanceNode {

	public RBindingNode(String name, ProvenanceData provData, double elapsedTime, int lineNum, int scriptNum) {
		super(name, null, provData, elapsedTime, lineNum, scriptNum);
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "Binding";
	}

	@Override
	public boolean canBeRoot() {
		// TODO Auto-generated method stub
		return false;
	}

}
