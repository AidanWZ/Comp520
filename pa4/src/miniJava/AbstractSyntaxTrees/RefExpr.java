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

public class RefExpr extends Expression
{
    public RefExpr(Reference r, SourcePosition posn){
        super(posn);
        ref = r;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitRefExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitRefExpr(this);
    }

    public <R> void generate(Generator<R> generator) {
        generator.visitRefExpr(this);
    }

    public Reference ref;
}
