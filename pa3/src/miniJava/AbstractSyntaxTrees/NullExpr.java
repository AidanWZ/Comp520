/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class NullExpr extends Expression {
	
	public NullExpr(SourcePosition posn) {
	    super (posn);
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNullExpr(this, o);
	}
	
	public <R> void visit(Traveller<R> v) {
        v.visitNullExpr(this);
    }
}