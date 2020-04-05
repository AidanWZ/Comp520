/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.Generator;
import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.ContextualAnalyzer.Visitor;
import miniJava.SyntacticAnalyzer.SourcePosition;

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

    public <R> void generate(Generator<R> generator) {
        generator.visitMethodDecl(this);
    }
	
	public ParameterDeclList parameterDeclList;
	public StatementList statementList;
}
