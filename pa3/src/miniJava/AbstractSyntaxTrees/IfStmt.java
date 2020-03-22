/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.IdentificationError;

public class IfStmt extends Statement
{
    public IfStmt(Expression b, Statement t, Statement e, SourcePosition posn){
        super(posn);
        cond = b;
        thenStmt = t;
        elseStmt = e;
    }
    
    public IfStmt(Expression b, Statement t, SourcePosition posn){
        super(posn);
        cond = b;
        thenStmt = t;
        elseStmt = null;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitIfStmt(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitIfStmt(this);
      }
    
    public Expression cond;
    public Statement thenStmt;
    public Statement elseStmt;
}