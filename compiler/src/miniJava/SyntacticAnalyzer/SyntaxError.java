package miniJava.SyntacticAnalyzer;

public class SyntaxError extends Exception {
	private static final long serialVersionUID = 2583239898992018218L;

	SyntaxError() {
		super();
	};
	SyntaxError (String s) {
		super(s);
	}

}