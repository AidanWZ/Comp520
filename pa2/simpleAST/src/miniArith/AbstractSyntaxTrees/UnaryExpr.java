package miniArith.AbstractSyntaxTrees;

import miniArith.SyntacticAnalyzer.Token;

public class UnaryExpr extends Expr {
    public Token num;

    public UnaryExpr(Token num) {
        this.num = num;
    }
    
    public <Inh,Syn> Syn visit(Visitor<Inh,Syn> v, Inh arg) {
        return v.visitUnaryExpr(this, arg);
    }
}


