/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;

public class UnaryExpr extends Expression
{
    public UnaryExpr(Operator o, Expression e, SourcePosition posn){
        super(posn);
        operator = o;
        expr = e;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitUnaryExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError{
        v.visitUnaryExpr(this);
    }

    public Operator operator;
    public Expression expr;
}