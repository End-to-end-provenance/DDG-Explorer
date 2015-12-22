package laser.ddg.r;

import laser.ddg.ProvenanceData;

public class RBindingNode extends RFunctionInstanceNode {

	public RBindingNode(String name, ProvenanceData provData, String time) {
		super(name, null, provData, time);
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
