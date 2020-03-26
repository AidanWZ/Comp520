/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.IdentificationError;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;

public class QualRef extends Reference {
	
	public QualRef(Reference ref, Identifier id, SourcePosition posn){
		super(posn);
		this.ref = ref;
		this.id  = id;
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitQRef(this, o);
	}

	public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitQRef(this);
    }

	public Reference ref;
	public Identifier id;
}
