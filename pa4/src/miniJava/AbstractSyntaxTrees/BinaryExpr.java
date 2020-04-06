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

public class BinaryExpr extends Expression
{
    public BinaryExpr(Operator o, Expression e1, Expression e2, SourcePosition posn){
        super(posn);
        operator = o;
        left = e1;
        right = e2;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitBinaryExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitBinaryExpr(this);
    }

    public <R> void generate(Generator<R> generator) {
        generator.visitBinaryExpr(this);
    }
    
    public Operator operator;
    public Expression left;
    public Expression right;
}