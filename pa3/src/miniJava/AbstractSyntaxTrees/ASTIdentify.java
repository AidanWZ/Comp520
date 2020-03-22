/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.SyntacticAnalyzer.IdentificationError;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TypeError;

/*
 * Display AST in text form, one node per line, using indentation to show 
 * subordinate nodes below a parent node.
 *   
 * Performs an in-order traversal of AST, visiting an AST node of type -- 
 * with a method of the form  
 *   
 *       public String visit--(-- astnode)
 *       
 *   where arg is a prefix string (indentation) to precede display of ast node
 *   and a null String is returned as the result.
 *   The display is produced by printing a line of output at each node visited.
 */
public class ASTIdentify implements Traveller<String> {

    public Stack<HashMap<String,Declaration>> scopeIdentificationTable;
    public ListIterator<HashMap<String, Declaration>> iterator;
    public int iteratorIndex;
    public ErrorReporter idReporter;
    public ErrorReporter typeReporter;
    public TypeKind returnKind;

    public ASTIdentify(ErrorReporter idReporter, ErrorReporter typeReporter) {
        this.scopeIdentificationTable = new Stack<HashMap<String, Declaration>>();
        this.idReporter = idReporter;
        this.typeReporter = typeReporter;
        
        HashMap<String, Declaration> temp = new HashMap<String, Declaration>();

        FieldDeclList tempFieldsList;
        MethodDeclList tempMethodsList;
        ParameterDeclList tempParameterList;
        
        //adding System class
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempFieldsList.add(new FieldDecl(
            false, 
            true, 
            new ClassType(
                new Identifier(
                    new Token(
                        0, 
                        "System", 
                        new SourcePosition(0,0))), 
                    new SourcePosition(0, 0)), 
                "System", 
            new SourcePosition(0, 0)));
        temp.put("System", new ClassDecl("System", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));

        //adding _PrintStream class
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempParameterList.add(new ParameterDecl(new BaseType(TypeKind.INT, new SourcePosition(0,0)), "x", new SourcePosition(0,0)));
        tempMethodsList.add(new MethodDecl(
            new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0,0)), "println", new SourcePosition(0,0)), 
            new ParameterDeclList(), 
            new StatementList(), 
            new SourcePosition(0,0)));
        temp.put("_PrintStream", new ClassDecl("_PrintStream", tempFieldsList, tempMethodsList, new SourcePosition(0,0)));
        
        //adding String class
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        temp.put("String", new ClassDecl("System", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
        
        //creating top level scope
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

    public Declaration search(String name) {
        if (scopeIdentificationTable.get(iteratorIndex).containsKey(name)) {
            return scopeIdentificationTable.get(iteratorIndex).get(name);
        }
        int counter = 0;
        while (iterator.hasPrevious()) {
            HashMap<String, Declaration> tmpMap = iterator.previous();
            iteratorIndex--;
            counter++;
            if (tmpMap.containsKey(name)) {
                for (int i = 0; i < counter; i++) {
                    iterator.next();
                }
                return tmpMap.get(name);
            }
        }
        return null;
    }

    public FieldDeclList findFields(String className) {
        ClassDecl decl = (ClassDecl) search(className);
        return decl.fieldDeclList;
    }

    public MethodDeclList findMethods(String className) {
        ClassDecl decl = (ClassDecl) search(className);
        return decl.methodDeclList;
    }

    void identificationError(int lineNumber) throws IdentificationError {
		idReporter.reportError(lineNumber);	
		throw(new IdentificationError());	
    }

    void typeError(int lineNumber) throws TypeError {
		typeReporter.reportError(lineNumber);	
		throw(new TypeError());	
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String visit(AST ast) throws TypeError, IdentificationError {
        ast.visit(this);
        return "";
    }

    public String visitPackage(Package prog) throws TypeError, IdentificationError {
        for (ClassDecl c: prog.classDeclList) {
            scopeIdentificationTable.peek().put(c.name, c);
            c.visit(this);            
        }    
        return "";    
    }

    public String visitClassDecl(ClassDecl clas) throws TypeError, IdentificationError {
        scopeIdentificationTable.push(new HashMap<String, Declaration>());
        for (FieldDecl f: clas.fieldDeclList) {
            scopeIdentificationTable.peek().put(f.name, f);
            f.visit(this);
        }
        for (MethodDecl m: clas.methodDeclList) {
            returnKind = m.type.typeKind;
            scopeIdentificationTable.peek().put(m.name, m);
            m.visit(this);
        }
        scopeIdentificationTable.pop();
        return "";
    }

    public String visitFieldDecl(FieldDecl fd) throws TypeError, IdentificationError {        
        fd.type.visit(this);    
        return "";
    }

    public String visitMethodDecl(MethodDecl m) throws TypeError, IdentificationError {
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
    
    public String visitParameterDecl(ParameterDecl pd) throws TypeError, IdentificationError {
        scopeIdentificationTable.peek().put(pd.name, pd);
        pd.type.visit(this);
        return "";
    } 
    
    public String visitVarDecl(VarDecl vd) throws TypeError, IdentificationError {
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
    
    public String visitClassType(ClassType ct) throws TypeError, IdentificationError {
        ct.className.visit(this);
        return "";
    }
    
    public String visitArrayType(ArrayType type) throws TypeError, IdentificationError {
        type.eltType.visit(this);
        return "";
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // STATEMENTS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public String visitBlockStmt(BlockStmt stmt) throws TypeError, IdentificationError {
        scopeIdentificationTable.push(new HashMap<String, Declaration>());
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
            s.visit(this);
        }
        scopeIdentificationTable.pop();
        return "";
    }
    
    public String visitVardeclStmt(VarDeclStmt stmt) throws TypeError, IdentificationError {
        scopeIdentificationTable.peek().put(stmt.varDecl.name, stmt.varDecl);
        stmt.varDecl.visit(this);
        if (stmt.initExp != null) {
            if (isSameType(stmt.varDecl.type.typeKind, stmt.initExp.type)) {
                stmt.initExp.visit(this);
            }
            else {
                typeError(0);
            }
        }	
        else {
            if (search(stmt.varDecl.name) != null) {
                identificationError(0);
            }
        }        
        return "";
    }
    
    public String visitAssignStmt(AssignStmt stmt) throws TypeError, IdentificationError {
        if (isSameType(stmt.ref.decl.type.typeKind, stmt.val.type)) {
            stmt.ref.visit(this);
            stmt.val.visit(this);
        }
        else {
            typeError(0);
        }
        return "";
    }
    
    public String visitIxAssignStmt(IxAssignStmt stmt) throws TypeError, IdentificationError {
        if (isSameType(stmt.ref.decl.type.typeKind, TypeKind.ARRAY) && // ref type must be array type
            isSameType(stmt.ix.type, TypeKind.INT) &&                  // index must be int type
            isSameType(stmt.ref.decl.type.typeKind, stmt.exp.type)) {  // assignment type must equal reference type
            stmt.ref.visit(this);
            stmt.ix.visit(this);
            stmt.exp.visit(this);
            return "";
        }  
        else {
            typeError(0);
            return "";
        }                
    }
        
    public String visitCallStmt(CallStmt stmt) throws TypeError, IdentificationError {
        stmt.methodRef.visit(this);
        ExprList al = stmt.argList;
        int counter = 0;
        for (Expression e: al) {
            if (isSameType(e.type, ((MethodDecl) stmt.methodRef.decl).parameterDeclList.get(counter).type.typeKind)) {
                e.visit(this);
            }
            else {
                typeError(0);
            }
        }
        return "";
    }
    
    public String visitReturnStmt(ReturnStmt stmt) throws TypeError, IdentificationError {
        
        if (stmt.returnExpr != null) {
            if (isSameType(this.returnKind, stmt.returnExpr.type)) {
                stmt.returnExpr.visit(this);
            }
            else {
                typeError(0);
            }            
        }
        else if (stmt.returnExpr == null) {
            if (isSameType(this.returnKind, stmt.returnExpr.type)) {

            }
            else {
                typeError(0);
            }
        }
        return "";
    }
    
    public String visitIfStmt(IfStmt stmt) throws TypeError, IdentificationError {
        if (stmt.elseStmt == null) {
            if (isSameType(stmt.cond.type, TypeKind.BOOLEAN)) {                
                stmt.cond.visit(this);
                scopeIdentificationTable.push(new HashMap<String, Declaration>());
                stmt.thenStmt.visit(this);
                scopeIdentificationTable.pop();
                return "";
            }
            else {
                typeError(0);
            }
        }
        else if (stmt.elseStmt != null) {
            if (isSameType(stmt.cond.type, TypeKind.BOOLEAN)) {
                stmt.cond.visit(this);
                scopeIdentificationTable.push(new HashMap<String, Declaration>());
                stmt.thenStmt.visit(this);
                scopeIdentificationTable.pop();
                scopeIdentificationTable.push(new HashMap<String, Declaration>());
                stmt.elseStmt.visit(this);
                scopeIdentificationTable.pop();
                return "";
            }
            else {
                typeError(0);
                return "";
            }            
        }
        return "";
    }
    
    public String visitWhileStmt(WhileStmt stmt) throws TypeError, IdentificationError {
        if (isSameType(stmt.cond.type, TypeKind.BOOLEAN)) {
            stmt.cond.visit(this);
            stmt.body.visit(this);
        }
        return "";
    }
    

    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public String visitUnaryExpr(UnaryExpr expr) throws TypeError, IdentificationError {
        if (expr.operator.spelling == "!" && isSameType(expr.expr.type, TypeKind.BOOLEAN)) {
            expr.operator.visit(this);
            expr.expr.visit(this);
        } 
        else if (expr.operator.spelling == "-" && isSameType(expr.expr.type, TypeKind.INT)) {
            expr.operator.visit(this);
            expr.expr.visit(this);
        }  
        else {
            typeError(0);
        }
        return "";
    }
    
    public String visitBinaryExpr(BinaryExpr expr) throws TypeError, IdentificationError {
        if (isSameType(expr.left.type, expr.right.type)) {
            expr.operator.visit(this);
            expr.left.visit(this);
            expr.right.visit(this);
        } 
        else {
            typeError(0);
        }
        return "";
    }
    
    public String visitRefExpr(RefExpr expr) throws TypeError, IdentificationError {
        expr.ref.visit(this);
        return "";
    }
    
    public String visitIxExpr(IxExpr ie) throws TypeError, IdentificationError {
        if (isSameType(ie.ixExpr.type, TypeKind.INT) && isSameType(ie.type, ie.ref.decl.type.typeKind)) {
            ie.ref.visit(this);
            ie.ixExpr.visit(this);
        }
        else {
            typeError(0);
        }
        return "";
    }
    
    public String visitCallExpr(CallExpr expr) throws TypeError, IdentificationError {
        expr.functionRef.visit(this);
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this);
        }   
        return "";     
    }
    
    public String visitLiteralExpr(LiteralExpr expr) throws TypeError, IdentificationError {
        expr.lit.visit(this);  
        return "";      
    }

    public String visitNewArrayExpr(NewArrayExpr expr) throws TypeError, IdentificationError {
        if (isSameType(expr.sizeExpr.type, TypeKind.INT)) {
            expr.eltType.visit(this);
            expr.sizeExpr.visit(this);
        }
        else {
            typeError(0);
        }
        return "";        
    }
    
    public String visitNewStringExpr(NewStringExpr expr) throws TypeError, IdentificationError {
        expr.sizeExpr.visit(this);  
        return "";      
    }
    
    public String visitNullExpr(NullExpr expr) throws TypeError, IdentificationError {
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // REFERENCES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitThisRef(ThisRef ref) throws TypeError, IdentificationError {
        ListIterator<HashMap<String, Declaration>> tmpIterator = scopeIdentificationTable.listIterator(0);
        Declaration temp = search(ref.decl.name);
        if (temp != null && isSameType(temp.type.typeKind, ref.decl.type.typeKind)) {

        }
        else {
            typeError(0);
        }
        return "";
    }
    
    public String visitIdRef(IdRef ref) throws TypeError, IdentificationError {
        ref.id.visit(this);
        return "";        
    }
        
    public String visitQRef(QualRef qr) throws TypeError, IdentificationError {
        FieldDeclList fields = findFields(qr.ref.decl.name);
        MethodDeclList methods = findMethods(qr.ref.decl.name);
        boolean fieldValid = false;
        boolean methodValid = false;
        for (FieldDecl f: fields) {
            if (f.name == qr.ref.decl.name) {
                fieldValid = true;
                break;
            }
        }
        for (MethodDecl m: methods) {
            if (m.name == qr.ref.decl.name) {
                methodValid = true;
                break;
            }
        }
        if (fieldValid || methodValid) {
            qr.id.visit(this);
            qr.ref.visit(this);
        }
        else {
            
        }
        return "";      
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TERMINALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitIdentifier(Identifier id) throws TypeError, IdentificationError {
        
        return "";
    }
    
    public String visitOperator(Operator op) throws TypeError, IdentificationError {
        return "";
    }
    
    public String visitIntLiteral(IntLiteral num) throws TypeError, IdentificationError {
        return "";
    }
    
    public String visitBooleanLiteral(BooleanLiteral bool) throws TypeError, IdentificationError {
        return "";
    }

    public String visitNewObjectExpr(NewObjectExpr expr) throws TypeError, IdentificationError {
        return "";
    }
}