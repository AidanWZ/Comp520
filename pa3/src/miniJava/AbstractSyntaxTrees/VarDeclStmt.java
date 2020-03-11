/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;

public class VarDeclStmt extends Statement
{
    public VarDeclStmt(VarDecl vd, Expression e, SourcePosition posn){
        super(posn);
        varDecl = vd;
        initExp = e;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitVardeclStmt(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError {
        v.visitVardeclStmt(this);
      }

    public VarDecl varDecl;
    public Expression initExp;
}
