/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.HashMap;
import java.util.Stack;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;

/*
 * Display AST in text form, one node per line, using indentation to show 
 * subordinate nodes below a parent node.
 *   
 * Performs an in-order traversal of AST, visiting an AST node of type XXX 
 * with a method of the form  
 *   
 *       public String visitXXX(XXX astnode)
 *       
 *   where arg is a prefix string (indentation) to precede display of ast node
 *   and a null String is returned as the result.
 *   The display is produced by printing a line of output at each node visited.
 */
public class ASTIdentify implements Traveller<String> {

    public Stack<HashMap<String,Declaration>> scopeIdentificationTable;

    public ASTIdentify() {
        this.scopeIdentificationTable = new Stack<HashMap<String, Declaration>>();
        HashMap<String, Declaration> temp = new HashMap<String, Declaration>();

        FieldDeclList tempFieldsList;
        MethodDeclList tempMethodsList;
        ParameterDeclList tempParameterList;
        
        tempFieldsList = new FieldDeclList();
        tempFieldsList.add(new FieldDecl(false, true, new ClassType(new Identifier(new Token(0, "System", new SourcePosition(0,0))), new SourcePosition(0, 0)), "System", new SourcePosition(0, 0)));
        temp.put("System", new ClassDecl("System", null, null, new SourcePosition(0, 0)));

        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempParameterList.add(new ParameterDecl(new BaseType(TypeKind.INT, new SourcePosition(0,0)), "x", new SourcePosition(0,0)));
        tempMethodsList.add(new MethodDecl(null, tempParameterList, new StatementList(), new SourcePosition(0,0)));
        temp.put("_PrintStream", new ClassDecl("_PrintStream", null, tempMethodsList, new SourcePosition(0,0)));
        
        temp.put("System", new ClassDecl("System", null, null, new SourcePosition(0, 0)));
        
        scopeIdentificationTable.add(temp);
        System.out.println(scopeIdentificationTable.size());
    }

    public String visit(AST ast) {
        ast.visit(this);
        return "";
    }

    public String visitPackage(Package prog) {
        for (ClassDecl c: prog.classDeclList){
            c.visit(this);
        }    
        return "";    
    }

    public String visitClassDecl(ClassDecl clas) {
        for (FieldDecl f: clas.fieldDeclList)
            f.visit(this);
        for (MethodDecl m: clas.methodDeclList)
            m.visit(this);
        return "";
    }

    public String visitFieldDecl(FieldDecl fd) {
        fd.type.visit(this);    
        return "";
    }

    public String visitMethodDecl(MethodDecl m){
        m.type.visit(this);
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {
            pd.visit(this);
        }
        StatementList sl = m.statementList;
        for (Statement s: sl) {
            s.visit(this);
        } 
        return "";     
    }
    
    public String visitParameterDecl(ParameterDecl pd){
        pd.type.visit(this);
        return "";
    } 
    
    public String visitVarDecl(VarDecl vd){
        vd.type.visit(this);
        return "";
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TYPES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitBaseType(BaseType type){
        return "";
    }
    
    public String visitClassType(ClassType ct){
        ct.className.visit(this);
        return "";
    }
    
    public String visitArrayType(ArrayType type){
        type.eltType.visit(this);
        return "";
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // STATEMENTS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public String visitBlockStmt(BlockStmt stmt){
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
            s.visit(this);
        }
        return "";
    }
    
    public String visitVardeclStmt(VarDeclStmt stmt){
        stmt.varDecl.visit(this);	
        stmt.initExp.visit(this);
        return "";
    }
    
    public String visitAssignStmt(AssignStmt stmt){
        stmt.ref.visit(this);
        stmt.val.visit(this);
        return "";
    }
    
    public String visitIxAssignStmt(IxAssignStmt stmt){
        stmt.ref.visit(this);
        stmt.ix.visit(this);
        stmt.exp.visit(this);
        return "";
    }
        
    public String visitCallStmt(CallStmt stmt){
        stmt.methodRef.visit(this);
        ExprList al = stmt.argList;
        for (Expression e: al) {
            e.visit(this);
        }
        return "";
    }
    
    public String visitReturnStmt(ReturnStmt stmt){
        if (stmt.returnExpr != null) {
            stmt.returnExpr.visit(this);
        }
        return "";
    }
    
    public String visitIfStmt(IfStmt stmt){
        stmt.cond.visit(this);
        stmt.thenStmt.visit(this);
        if (stmt.elseStmt != null) {
            stmt.elseStmt.visit(this);
        }
        return "";
    }
    
    public String visitWhileStmt(WhileStmt stmt){
        stmt.cond.visit(this);
        stmt.body.visit(this);
        return "";
    }
    

    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public String visitUnaryExpr(UnaryExpr expr){
        expr.operator.visit(this);
        expr.expr.visit(this);
        return "";
    }
    
    public String visitBinaryExpr(BinaryExpr expr){
        expr.operator.visit(this);
        expr.left.visit(this);
        expr.right.visit(this);
        return "";
    }
    
    public String visitRefExpr(RefExpr expr){
        expr.ref.visit(this);
        return "";
    }
    
    public String visitIxExpr(IxExpr ie){
        ie.ref.visit(this);
        ie.ixExpr.visit(this);
        return "";
    }
    
    public String visitCallExpr(CallExpr expr){
        expr.functionRef.visit(this);
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this);
        }   
        return "";     
    }
    
    public String visitLiteralExpr(LiteralExpr expr){
        expr.lit.visit(this);  
        return "";      
    }

    public String visitNewArrayExpr(NewArrayExpr expr){
        expr.eltType.visit(this);
        expr.sizeExpr.visit(this);
        return "";        
    }
    
    public String visitNewStringExpr(NewStringExpr expr){
        //expr.classtype.visit(this);  
        return "";      
    }
    
    public String visitNullExpr(NullExpr expr) {
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // REFERENCES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitThisRef(ThisRef ref) {
        return "";
    }
    
    public String visitIdRef(IdRef ref) {
        ref.id.visit(this);
        return "";        
    }
        
    public String visitQRef(QualRef qr) {
        qr.id.visit(this);
        qr.ref.visit(this);  
        return "";      
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TERMINALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitIdentifier(Identifier id){
        return "";
    }
    
    public String visitOperator(Operator op){
        return "";
    }
    
    public String visitIntLiteral(IntLiteral num){
        return "";
    }
    
    public String visitBooleanLiteral(BooleanLiteral bool){
        return "";
    }

    public String visitNewObjectExpr(NewObjectExpr expr) {
        return "";
    }
}