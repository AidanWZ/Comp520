/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.Generator;
import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.ContextualAnalyzer.Visitor;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class NullExpr extends Expression {
	
	public NullExpr(SourcePosition posn) {
	    super (posn);
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNullExpr(this, o);
	}
	
	public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitNullExpr(this);
	}
	
	public <R> void generate(Generator<R> generator) {
        generator.visitNullExpr(this);
    }
}
