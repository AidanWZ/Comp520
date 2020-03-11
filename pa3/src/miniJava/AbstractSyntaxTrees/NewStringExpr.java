/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;

public class NewStringExpr extends NewExpr
{
    public NewStringExpr(Expression e, SourcePosition posn){
        super(posn);
    }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNewStringExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError {
        v.visitNewStringExpr(this);
      }

    public TypeDenoter eltType;
    public Expression sizeExpr;
}