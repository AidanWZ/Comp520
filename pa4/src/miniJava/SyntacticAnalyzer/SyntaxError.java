package miniJava.SyntacticAnalyzer;

public class SyntaxError extends Exception {
	private static final long serialVersionUID = 2583239898992018218L;
	public SyntaxError() {
		super();
	};
	public SyntaxError (String s) {
		super(s);
	}

}