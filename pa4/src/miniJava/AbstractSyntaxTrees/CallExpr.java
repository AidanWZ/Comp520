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

public class CallExpr extends Expression
{
    public CallExpr(Reference f, ExprList el, SourcePosition posn){
        super(posn);
        functionRef = f;
        argList = el;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitCallExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitCallExpr(this);
    }

    public void generate(Generator generator) {
        generator.visitCallExpr(this);
    }
    
    public Reference functionRef;
    public ExprList argList;
}