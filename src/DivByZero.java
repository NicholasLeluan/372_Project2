
public class DivByZero extends Exception{
	int line;
	public DivByZero(int line) {
		super();
		this.line = line;
	}
	
	public String toString() {
		return "Dividing by 0 on line " + line;
	}
}
