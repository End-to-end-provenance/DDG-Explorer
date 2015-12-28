package laser.ddg.r;

import laser.ddg.ProvenanceData;

public class RBindingNode extends RFunctionInstanceNode {

	public RBindingNode(String name, ProvenanceData provData, double elapsedTime) {
		super(name, null, provData, elapsedTime);
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
