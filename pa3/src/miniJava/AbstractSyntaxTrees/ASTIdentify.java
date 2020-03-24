/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
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
    
    public String className;
    public TypeKind returnKind;
    public String referenceName;

    public ASTIdentify(ErrorReporter idReporter, ErrorReporter typeReporter) {
        this.scopeIdentificationTable = new Stack<HashMap<String, Declaration>>();
        this.idReporter = idReporter;
        this.typeReporter = typeReporter;
        this.iteratorIndex = 0;
        
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
        temp.put("String", new ClassDecl("String", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
        
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

    public boolean isSameExprType(Expression expr1, Expression expr2) {
        return expr1.getClass().equals(expr2.getClass());
    }

    public void displayIdTable() {
        Iterator<HashMap<String, Declaration>> scopeIterator = scopeIdentificationTable.iterator();
        System.out.println("==============================================");
        System.out.println("NAME : TYPE");
        System.out.println("==============================================");
        boolean first = true;
        while (scopeIterator.hasNext()) {
            for(Map.Entry<String, Declaration> mapElement : scopeIterator.next().entrySet()) {
                if (mapElement.getValue().type == null) {
                    if (first) {
                        System.out.println(mapElement.getKey() + " : " + "class");
                    }
                    else {
                        System.out.println(mapElement.getKey() + " : " + mapElement.getValue());
                    }
                }
                else {
                    if (first) {
                        System.out.println(mapElement.getKey() + " : " + "class");
                    }
                    else {
                        System.out.println(mapElement.getKey() + " : " + mapElement.getValue().type.typeKind);
                    }
                }
            }
            first = false;
            System.out.println("==============================================");
        }
    }

    public Declaration search(String name) {
        int tempIndex = iteratorIndex;
        while (tempIndex > 0) {
            if (scopeIdentificationTable.elementAt(tempIndex).containsKey(name)) {
                return scopeIdentificationTable.get(tempIndex).get(name);
            }
            tempIndex--;
        }
        return null;
    }

    public Declaration searchAbove(String name) {
        int tempIndex = iteratorIndex-1;
        while (tempIndex > 0) {
            if (scopeIdentificationTable.elementAt(tempIndex).containsKey(name)) {
                return scopeIdentificationTable.get(tempIndex).get(name);
            }
            tempIndex--;
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

    public MemberDecl findMember(String memberName) {
        MethodDeclList classMethods = findMethods(this.className);
        FieldDeclList classFields = findFields(this.className);
        for(MethodDecl m: classMethods) {
            if (m.name == memberName) {
                return m;
            }
        }
        for(FieldDecl f: classFields) {
            if (f.name == memberName) {
                return f;
            }
        }
        return null;
    }

    public void addScope() {
        scopeIdentificationTable.push(new HashMap<String, Declaration>());
        iteratorIndex++;
    }

    public void removeScope() {
        scopeIdentificationTable.pop();
        iteratorIndex--;
    }

    void identificationError(int lineNumber, String methodName, String varName) throws IdentificationError {
        idReporter.reportIdError(lineNumber, varName);	
        display("Identification error in " + methodName);
		throw(new IdentificationError());	
    }

    void typeError(int lineNumber, String methodName) throws TypeError {
        typeReporter.reportTypeError(lineNumber);	
        display("Type error in " + methodName);
		throw(new TypeError());	
    }

    void display(String text) {
		System.out.println(text);
	}
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String visit(AST ast) throws TypeError, IdentificationError {
        ast.visit(this);
        return "";
    }

    public String visitPackage(Package prog) throws TypeError, IdentificationError {
        for (ClassDecl c: prog.classDeclList) {            
            className = c.name;
            scopeIdentificationTable.peek().put(c.name, c);
            c.visit(this);  
                      
        }    
        return "";    
    }

    public String visitClassDecl(ClassDecl clas) throws TypeError, IdentificationError {
        addScope();
        for (FieldDecl f: clas.fieldDeclList) { 
            scopeIdentificationTable.peek().put(f.name, f);           
            f.visit(this);            
        }
        for (MethodDecl m: clas.methodDeclList) {
            returnKind = m.type.typeKind;  
            scopeIdentificationTable.peek().put(m.name, m);          
            m.visit(this);
        }
        removeScope();
        return "";
    }

    public String visitFieldDecl(FieldDecl fd) throws TypeError, IdentificationError {        
        fd.type.visit(this);            
        return "";
    }

    public String visitMethodDecl(MethodDecl m) throws TypeError, IdentificationError {
        addScope();
        m.type.visit(this);
        ParameterDeclList pdl = m.parameterDeclList;
        addScope();
        for (ParameterDecl pd: pdl) {            
            pd.visit(this);
            scopeIdentificationTable.peek().put(pd.name, pd);
        }
        StatementList sl = m.statementList;
        for (Statement s: sl) {
            s.visit(this);
        } 
        removeScope();
        removeScope();
        return "";     
    }
    
    public String visitParameterDecl(ParameterDecl pd) throws TypeError, IdentificationError {
        pd.type.visit(this);
        scopeIdentificationTable.peek().put(pd.name, pd);
        return "";
    } 
    
    public String visitVarDecl(VarDecl vd) throws TypeError, IdentificationError {
        vd.type.visit(this);
        scopeIdentificationTable.peek().put(vd.name, vd);
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
        addScope();
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
            s.visit(this);
        }
        removeScope();
        return "";
    }
    
    public String visitVardeclStmt(VarDeclStmt stmt) throws TypeError, IdentificationError {     
        if (iteratorIndex == 3 && searchAbove(stmt.varDecl.name) != null) {
            identificationError(stmt.posn.start, "visitVardeclStmt", "duplicate local variable " + stmt.varDecl.name);
        }
        stmt.varDecl.visit(this);
        if (stmt.initExp != null) { 
            stmt.initExp.visit(this);        
            if (isSameType(stmt.varDecl.type.typeKind, stmt.initExp.type)) {                
                stmt.varDecl.type.typeKind = stmt.initExp.type;
            }
            else {
                typeError(stmt.posn.start, "visitVarDeclStmt");
            }
        }	
        else {
            if (search(stmt.varDecl.name) != null) {
                identificationError(stmt.posn.start, "visitVarDeclStmt", "variable with name " + stmt.varDecl.name + " already exists");
            }
            else {

            }
        }
        scopeIdentificationTable.peek().put(stmt.varDecl.name, stmt.varDecl); 
        return "";
    }
    
    public String visitAssignStmt(AssignStmt stmt) throws TypeError, IdentificationError {
        stmt.ref.visit(this);
        stmt.val.visit(this);
        if (search(((IdRef)stmt.ref).id.spelling) == null) {            
            identificationError(stmt.posn.start, "visitAssignStmt", "Variable may not have been initialized");                  
        }           
        else {
            if (isSameType(search(((IdRef)stmt.ref).id.spelling).type.typeKind, stmt.val.type)) {
                
            }
            else {
                typeError(stmt.posn.start, "visitAssignStmt");
            }
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
            typeError(stmt.posn.start, "visitIxAssignStmt");
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
                typeError(stmt.posn.start, "visitCallStmt");
            }
        }
        return "";
    }
    
    public String visitReturnStmt(ReturnStmt stmt) throws TypeError, IdentificationError {        
        if (stmt.returnExpr != null) {
            stmt.returnExpr.visit(this);
            if (isSameType(this.returnKind, stmt.returnExpr.type)) {
                
            }
            else {
                typeError(stmt.posn.start, "visitReturnStmt");
            }            
        }
        else if (stmt.returnExpr == null) {
            if (isSameType(this.returnKind, TypeKind.VOID)) {

            }
            else {
                typeError(stmt.posn.start, "visitReturnStmt");
            }
        }
        return "";
    }
    
    public String visitIfStmt(IfStmt stmt) throws TypeError, IdentificationError {
        if (stmt.elseStmt == null) {
            stmt.cond.visit(this);
            if (isSameType(stmt.cond.type, TypeKind.BOOLEAN)) {                                
                if (((BlockStmt) stmt.thenStmt).sl.size() == 1) {
                    if (((BlockStmt) stmt.thenStmt).sl.get(0).getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                        identificationError(stmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
                    }
                }
                else {
                    addScope();
                    stmt.thenStmt.visit(this);
                    removeScope();
                }
                
                return "";
            }
            else {
                typeError(stmt.posn.start, "visitIfStmt");
            }
        }
        else if (stmt.elseStmt != null) {
            if (isSameType(stmt.cond.type, TypeKind.BOOLEAN)) {
                stmt.cond.visit(this);
                if (((BlockStmt) stmt.thenStmt).sl.size() == 1) {
                    if (((BlockStmt) stmt.thenStmt).sl.get(0).getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                        identificationError(stmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
                    }
                }
                else if (((BlockStmt) stmt.elseStmt).sl.size() == 1) {
                    if (((BlockStmt) stmt.elseStmt).sl.get(0).getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                        identificationError(stmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
                    }
                }
                else {
                    addScope();
                    stmt.thenStmt.visit(this);
                    removeScope();
                    addScope();
                    stmt.elseStmt.visit(this);
                    removeScope();
                }                
                return "";
            }
            else {
                typeError(stmt.posn.start, "visitIfStmt");
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
            expr.type = expr.expr.type;
        } 
        else if (expr.operator.spelling == "-" && isSameType(expr.expr.type, TypeKind.INT)) {
            expr.operator.visit(this);
            expr.expr.visit(this);
            expr.type = expr.expr.type;
        }  
        else {
            typeError(expr.posn.start, "visitUnaryExpr");
        }
        return "";
    }
    
    public String visitBinaryExpr(BinaryExpr expr) throws TypeError, IdentificationError {
        expr.operator.visit(this);
        expr.left.visit(this);
        expr.right.visit(this);
        if (isSameType(expr.left.type, expr.right.type)) {            
            if (expr.operator.spelling == "==") {
                expr.type = TypeKind.BOOLEAN;
            }
            else {
                expr.type = expr.left.type;
            }            
        } 
        else {
            typeError(expr.posn.start, "visitBinaryExpr");
        }
        return "";
    }
    
    public String visitRefExpr(RefExpr expr) throws TypeError, IdentificationError {
        expr.ref.visit(this);
        if (expr.ref.decl != null) {
            expr.type = expr.ref.decl.type.typeKind;
        }
        else {
            identificationError(expr.posn.start, "visitRefExpr", "variable was not initialized");
        }
        return "";
    }
    
    public String visitIxExpr(IxExpr ie) throws TypeError, IdentificationError {
        if (isSameType(ie.ixExpr.type, TypeKind.INT) && isSameType(ie.type, ie.ref.decl.type.typeKind)) {
            ie.ref.visit(this);
            ie.ixExpr.visit(this);
            ie.type = ie.ref.decl.type.typeKind;
        }
        else {
            typeError(ie.posn.start, "visitIxExpr");
        }
        return "";
    }
    
    public String visitCallExpr(CallExpr expr) throws TypeError, IdentificationError {
        expr.functionRef.visit(this);
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this);
        }   
        expr.type = expr.functionRef.decl.type.typeKind;
        return "";     
    }
    
    public String visitLiteralExpr(LiteralExpr expr) throws TypeError, IdentificationError {
        expr.lit.visit(this);  
        expr.type = expr.lit.type;
        return "";      
    }

    public String visitNewObjectExpr(NewObjectExpr expr) throws TypeError, IdentificationError {
        expr.classtype.visit(this);
        expr.type = TypeKind.CLASS;
        return "";
    }

    public String visitNewArrayExpr(NewArrayExpr expr) throws TypeError, IdentificationError {
        if (isSameType(expr.sizeExpr.type, TypeKind.INT)) {
            expr.eltType.visit(this);
            expr.sizeExpr.visit(this);
            expr.type = TypeKind.ARRAY;
        }
        else {
            typeError(expr.posn.start, "visitNewArrayExpr");
        }
        return "";        
    }
    
    public String visitNewStringExpr(NewStringExpr expr) throws TypeError, IdentificationError {
        expr.sizeExpr.visit(this);
        expr.type = TypeKind.UNSUPPORTED;  
        return "";      
    }
    
    public String visitNullExpr(NullExpr expr) throws TypeError, IdentificationError {
        expr.type = TypeKind.ERROR;
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // REFERENCES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitThisRef(ThisRef ref) throws TypeError, IdentificationError {
        //if the thisRef was 'this' or 'this.field'
        if (ref.decl.getClass().equals(new QualRef(null, null, null))) {
            Declaration temp = search(ref.decl.name);
            if (temp != null && isSameType(temp.type.typeKind, ref.decl.type.typeKind)) {
                ref.decl.type.typeKind = temp.type.typeKind;
            }
            else {
                typeError(ref.posn.start, "visitThisRef");
            }
        }
        else {

        }
        
        return "";
    }
    
    public String visitIdRef(IdRef ref) throws TypeError, IdentificationError {
        ref.id.visit(this);   
        if (search(ref.id.spelling) != null) {
            ref.decl = search(ref.id.spelling);
        }    
        else {
            identificationError(ref.posn.start, "visitIdRef", "Variable " + ref.id.spelling + " may not have been initialized");
        }
        return "";        
    }
        
    public String visitQRef(QualRef qr) throws TypeError, IdentificationError {
        FieldDeclList fields = findFields(qr.ref.decl.name);
        MethodDeclList methods = findMethods(qr.ref.decl.name);
        boolean fieldValid = false;
        boolean methodValid = false;
        FieldDecl tempField = null;
        MethodDecl tempMethod = null;
        //check if the reference is a field reference
        for (FieldDecl f: fields) {
            if (f.name == qr.ref.decl.name) {
                fieldValid = true;
                tempField = f;
                break;
            }
        }
        //check if the reference is a method reference
        for (MethodDecl m: methods) {
            if (m.name == qr.ref.decl.name) {
                methodValid = true;
                tempMethod = m;
                break;
            }
        }
        if (fieldValid) {
            qr.id.visit(this);
            qr.ref.visit(this);
            //check if reference and identifier types match
            if (isSameType(qr.ref.decl.type.typeKind, search(qr.ref.decl.name).type.typeKind) &&
                isSameType(qr.id.decl.type.typeKind, tempField.type.typeKind)) {
                
            }
            else {
                typeError(qr.posn.start, "visitQRef");
            }
            qr.decl.type.typeKind = tempField.type.typeKind;
        }
        else if (methodValid) {
            qr.id.visit(this);
            qr.ref.visit(this);
            //check if reference and identifier types match
            if (isSameType(qr.ref.decl.type.typeKind, search(qr.ref.decl.name).type.typeKind) &&
                isSameType(qr.id.decl.type.typeKind, tempMethod.type.typeKind)) {
                
            }
            else {
                typeError(qr.posn.start, "visitQRef");
            }
            qr.decl.type.typeKind = tempField.type.typeKind;
        }
        else {
            identificationError(qr.posn.start, "visitQRef", "term " + qr.id.spelling + " does not exist");
        }
        return "";      
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TERMINALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public String visitIdentifier(Identifier id) throws TypeError, IdentificationError {
        id.decl = search(id.spelling);
        return "";
    }
    
    public String visitOperator(Operator op) throws TypeError, IdentificationError {
        return "";
    }
    
    public String visitIntLiteral(IntLiteral num) throws TypeError, IdentificationError {
        num.type = TypeKind.INT;
        return "";
    }

    public String visitNullLiteral(NullLiteral nul) throws TypeError, IdentificationError {
        nul.type = TypeKind.ERROR;
        return "";
    }
    
    public String visitBooleanLiteral(BooleanLiteral bool) throws TypeError, IdentificationError {
        bool.type = TypeKind.BOOLEAN;
        return "";
    }
}