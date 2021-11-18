
public class ConditionalNoMatch extends Exception{

	public ConditionalNoMatch() {
		super();
	}
	
	public String toString(int lineNo) {
		return "Conditionals do not match on line " + lineNo;
	}
}
