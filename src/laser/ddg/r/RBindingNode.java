package laser.ddg.r;

import laser.ddg.ProvenanceData;
import laser.ddg.SourcePos;

public class RBindingNode extends RFunctionInstanceNode {

	public RBindingNode(String name, ProvenanceData provData, double elapsedTime, SourcePos sourcePos) {
		super(name, null, provData, elapsedTime, sourcePos);
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
