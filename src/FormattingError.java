
public class FormattingError extends Exception{
	int line;
	public FormattingError(int line) {
		super();
		this.line = line;
	}
	
	public String toString() {
		return "Formatting error on line " + line;
	}
}
