/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TypeError;

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
    public ListIterator<HashMap<String, Declaration>> iterator;
    public ErrorReporter reporter;

    public ASTIdentify(ErrorReporter reporter) {
        this.scopeIdentificationTable = new Stack<HashMap<String, Declaration>>();
        this.reporter = reporter;
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
        
        iterator = scopeIdentificationTable.listIterator(0);
    }

    public boolean isSameType(TypeKind type1, TypeKind type2) {
        if (type1.equals(type2)) {
            if (type1.equals(TypeKind.UNSUPPORTED) || type2.equals(TypeKind.UNSUPPORTED)) {
                return false;
            }
            else {
                return true;
            }
        }
        else if (type1.equals(TypeKind.ERROR)) {
            if (type2.equals(TypeKind.UNSUPPORTED)) {
                return false;
            }
            else {
                return true;
            }
        }
        else if (type2.equals(TypeKind.ERROR)) {
            if (type1.equals(TypeKind.UNSUPPORTED)) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    void typeError(int lineNumber) throws TypeError {
		reporter.reportError(lineNumber);	
		throw(new TypeError());	
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String visit(AST ast) throws TypeError {
        ast.visit(this);
        return "";
    }

    public String visitPackage(Package prog) throws TypeError {
        for (ClassDecl c: prog.classDeclList) {
            scopeIdentificationTable.peek().put(c.name, c);
            c.visit(this);            
        }    
        return "";    
    }

    public String visitClassDecl(ClassDecl clas) throws TypeError {
        scopeIdentificationTable.push(new HashMap<String, Declaration>());
        for (FieldDecl f: clas.fieldDeclList) {
            scopeIdentificationTable.peek().put(f.name, f);
            f.visit(this);
        }
        for (MethodDecl m: clas.methodDeclList) {
            scopeIdentificationTable.peek().put(m.name, m);
            m.visit(this);
        }
        scopeIdentificationTable.pop();
        return "";
    }

    public String visitFieldDecl(FieldDecl fd) throws TypeError {        
        fd.type.visit(this);    
        return "";
    }

    public String visitMethodDecl(MethodDecl m) throws TypeError {
        scopeIdentificationTable.push(new HashMap<String, Declaration>());
        m.type.visit(this);
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {
            scopeIdentificationTable.peek().put(pd.name, pd);
            pd.visit(this);
        }
        StatementList sl = m.statementList;
        for (Statement s: sl) {
            s.visit(this);
        } 
        scopeIdentificationTable.pop();
        return "";     
    }
    
    public String visitParameterDecl(ParameterDecl pd) throws TypeError {
        scopeIdentificationTable.peek().put(pd.name, pd);
        pd.type.visit(this);
        return "";
    } 
    
    public String visitVarDecl(VarDecl vd) throws TypeError {
        scopeIdentificationTable.peek().put(vd.name, vd);
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
    
    public String visitClassType(ClassType ct) throws TypeError{
        ct.className.visit(this);
        return "";
    }
    
    public String visitArrayType(ArrayType type) throws TypeError{
        type.eltType.visit(this);
        return "";
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // STATEMENTS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public String visitBlockStmt(BlockStmt stmt) throws TypeError {
        scopeIdentificationTable.push(new HashMap<String, Declaration>());
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
            s.visit(this);
        }
        scopeIdentificationTable.pop();
        return "";
    }
    
    public String visitVardeclStmt(VarDeclStmt stmt) throws TypeError {
        scopeIdentificationTable.peek().put(stmt.varDecl.name, stmt.varDecl);
        stmt.varDecl.visit(this);	
        stmt.initExp.visit(this);
        return "";
    }
    
    public String visitAssignStmt(AssignStmt stmt) throws TypeError {
        if (isSameType(stmt.ref.decl.type.typeKind, stmt.val.type)) {

        }
        stmt.ref.visit(this);
        stmt.val.visit(this);
        return "";
    }
    
    public String visitIxAssignStmt(IxAssignStmt stmt) throws TypeError {
        stmt.ref.visit(this);
        stmt.ix.visit(this);
        stmt.exp.visit(this);
        return "";
    }
        
    public String visitCallStmt(CallStmt stmt) throws TypeError {
        stmt.methodRef.visit(this);
        ExprList al = stmt.argList;
        for (Expression e: al) {
            e.visit(this);
        }
        return "";
    }
    
    public String visitReturnStmt(ReturnStmt stmt) throws TypeError {
        if (stmt.returnExpr != null) {
            stmt.returnExpr.visit(this);
        }
        return "";
    }
    
    public String visitIfStmt(IfStmt stmt) throws TypeError {
        stmt.cond.visit(this);
        stmt.thenStmt.visit(this);
        if (stmt.elseStmt != null) {
            stmt.elseStmt.visit(this);
        }
        return "";
    }
    
    public String visitWhileStmt(WhileStmt stmt) throws TypeError {
        stmt.cond.visit(this);
        stmt.body.visit(this);
        return "";
    }
    

    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public String visitUnaryExpr(UnaryExpr expr) throws TypeError {
        expr.operator.visit(this);
        expr.expr.visit(this);
        return "";
    }
    
    public String visitBinaryExpr(BinaryExpr expr) throws TypeError {
        expr.operator.visit(this);
        expr.left.visit(this);
        expr.right.visit(this);
        return "";
    }
    
    public String visitRefExpr(RefExpr expr) throws TypeError {
        expr.ref.visit(this);
        return "";
    }
    
    public String visitIxExpr(IxExpr ie) throws TypeError {
        ie.ref.visit(this);
        ie.ixExpr.visit(this);
        return "";
    }
    
    public String visitCallExpr(CallExpr expr) throws TypeError {
        expr.functionRef.visit(this);
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this);
        }   
        return "";     
    }
    
    public String visitLiteralExpr(LiteralExpr expr) throws TypeError {
        expr.lit.visit(this);  
        return "";      
    }

    public String visitNewArrayExpr(NewArrayExpr expr) throws TypeError {
        expr.eltType.visit(this);
        expr.sizeExpr.visit(this);
        return "";        
    }
    
    public String visitNewStringExpr(NewStringExpr expr) throws TypeError {
        //expr.classtype.visit(this);  
        return "";      
    }
    
    public String visitNullExpr(NullExpr expr) throws TypeError {
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // REFERENCES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitThisRef(ThisRef ref) throws TypeError {
        ListIterator<HashMap<String, Declaration>> tmpIterator = scopeIdentificationTable.listIterator(0);
        if (tmpIterator.hasNext()) {

        }
        return "";
    }
    
    public String visitIdRef(IdRef ref) throws TypeError {
        ref.id.visit(this);
        return "";        
    }
        
    public String visitQRef(QualRef qr) throws TypeError {
        qr.id.visit(this);
        qr.ref.visit(this);  
        return "";      
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TERMINALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitIdentifier(Identifier id) throws TypeError {
        if (scopeIdentificationTable.peek().containsKey(id.spelling)) {
            id.decl = scopeIdentificationTable.peek().get(id.spelling);
        }
        while (iterator.hasPrevious()) {
            if (iterator.previous().containsKey(id.spelling)) {
                id.decl = scopeIdentificationTable.peek().get(id.spelling);
                while (iterator.hasNext()) {
                    iterator.next();
                }
                return "";
            }
        }
        return "";
    }
    
    public String visitOperator(Operator op) throws TypeError {
        return "";
    }
    
    public String visitIntLiteral(IntLiteral num) throws TypeError {
        return "";
    }
    
    public String visitBooleanLiteral(BooleanLiteral bool) throws TypeError {
        return "";
    }

    public String visitNewObjectExpr(NewObjectExpr expr) throws TypeError {
        return "";
    }
}