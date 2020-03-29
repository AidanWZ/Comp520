/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.IdentificationError;

public class MethodDecl extends MemberDecl {
	
	public MethodDecl(MemberDecl md, ParameterDeclList pl, StatementList sl, SourcePosition posn){
    super(md,posn);
    parameterDeclList = pl;
    statementList = sl;
	}
	
	public <A, R> R visit(Visitor<A, R> v, A o) {
        return v.visitMethodDecl(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitMethodDecl(this);
      }
	
	public ParameterDeclList parameterDeclList;
	public StatementList statementList;
}
