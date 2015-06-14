package laser.ddg;

public class NoSuchNodeException extends Exception {
	public NoSuchNodeException (int id) {
		super("" + id);
	}
}
