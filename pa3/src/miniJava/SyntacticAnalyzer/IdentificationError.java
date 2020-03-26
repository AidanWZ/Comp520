package miniJava.SyntacticAnalyzer;

public class IdentificationError extends Exception {
	private static final long serialVersionUID = 2583239898992018218L;
	public IdentificationError() {
		super();
	};
	public IdentificationError (String s) {
		super(s);
	}

}