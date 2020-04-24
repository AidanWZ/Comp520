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

public class CallStmt extends Statement
{
    public CallStmt(Reference m, ExprList el, SourcePosition posn){
        super(posn);
        methodRef = m;
        argList = el;
    }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitCallStmt(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitCallStmt(this);
    }

    public <R> Object generate(Generator<R> generator) {
        return generator.visitCallStmt(this);
    }
    
    public Reference methodRef;
    public ExprList argList;
}