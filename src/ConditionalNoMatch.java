
public class ConditionalNoMatch extends Exception{
	int line;
	public ConditionalNoMatch(int line) {
		super();
		this.line = line;
	}
	
	public String toString() {
		return "Conditionals do not match on line " + line;
	}
}
