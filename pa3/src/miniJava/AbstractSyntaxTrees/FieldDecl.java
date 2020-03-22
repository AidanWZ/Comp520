/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.IdentificationError;

public class FieldDecl extends MemberDecl {
	
	public FieldDecl(boolean isPrivate, boolean isStatic, TypeDenoter t, String name, SourcePosition posn){
    super(isPrivate, isStatic, t, name, posn);
	}
	
	public FieldDecl(MemberDecl md, SourcePosition posn) {
		super(md,posn);
	}
	
	public <A, R> R visit(Visitor<A, R> v, A o) {
        return v.visitFieldDecl(this, o);
	}
	
	public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
		v.visitFieldDecl(this);
	  }
}

