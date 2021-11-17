
public class FormattingError extends Exception{

	public FormattingError() {
		super();
	}
	
	public String toString(int lineNo) {
		return "Formatting error on line " + lineNo;
	}
}
