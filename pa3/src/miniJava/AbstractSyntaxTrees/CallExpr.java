/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.IdentificationError;

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
    
    public Reference functionRef;
    public ExprList argList;
}