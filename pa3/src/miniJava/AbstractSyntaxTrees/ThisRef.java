/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.IdentificationError;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;

public class ThisRef extends BaseRef {
	
	public ThisRef(SourcePosition posn) {
		super(posn);
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitThisRef(this, o);
	}

	public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitThisRef(this);
    }
	
}
