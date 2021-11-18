
public class UndefinedVariable extends Exception{
	int line;
	public UndefinedVariable(int line) {
		super();
		this.line = line;
	}
	
	public String toString() {
		return "Undefined variable on line " + line;
	}

}
