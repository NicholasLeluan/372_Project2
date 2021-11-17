
public class UndefinedVariable extends Exception{
	
	public UndefinedVariable() {
		super();
	}
	
	public String toString(int lineNo) {
		return "Undefined variable on line " + lineNo;
	}
	
	public String toString() {
		return "An error occured in the main loop";
	}
}
