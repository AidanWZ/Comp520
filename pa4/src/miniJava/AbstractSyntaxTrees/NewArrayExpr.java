/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.IdentificationError;

public class NewArrayExpr extends NewExpr
{
    public NewArrayExpr(TypeDenoter et, Expression e, SourcePosition posn){
        super(posn);
        eltType = et;
        sizeExpr = e;
    }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNewArrayExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitNewArrayExpr(this);
    }

    public TypeDenoter eltType;
    public Expression sizeExpr;
}