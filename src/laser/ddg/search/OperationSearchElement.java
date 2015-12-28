package laser.ddg.search;

public class OperationSearchElement extends SearchElement {
	private final double time; 

	public OperationSearchElement(String type, String name, int id, double parsedTime) {
		super(type, name, id);
		time = parsedTime;
	}

	public double getTimeTaken(){
		return time; 
	}

}
