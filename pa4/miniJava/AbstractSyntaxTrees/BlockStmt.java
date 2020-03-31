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

public class BlockStmt extends Statement
{
    public BlockStmt(StatementList sl, SourcePosition posn){
        super(posn);
        this.sl = sl;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitBlockStmt(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
        v.visitBlockStmt(this);
    }
   
    public StatementList sl;
}