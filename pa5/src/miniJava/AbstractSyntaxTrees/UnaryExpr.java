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

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError{
        v.visitUnaryExpr(this);
    }

    public <R> Object generate(Generator<R> generator) {
        return generator.visitUnaryExpr(this);
    }

    public Operator operator;
    public Expression expr;
}