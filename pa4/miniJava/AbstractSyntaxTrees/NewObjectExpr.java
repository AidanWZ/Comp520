/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.ContextualAnalyzer.Visitor;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class NewObjectExpr extends NewExpr
{
    public NewObjectExpr(ClassType ct, SourcePosition posn){
        super(posn);
        classtype = ct;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNewObjectExpr(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitNewObjectExpr(this);
    }
    
    public ClassType classtype;
}
