/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import javax.swing.plaf.nimbus.State;

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

    public Stack<HashMap<String, Declaration>> scopeIdentificationTable;
    public ArrayList<Stack<HashMap<String, Declaration>>> allMembers;
    public AST ast;
    public int iteratorIndex;
    public ErrorReporter idReporter;
    public ErrorReporter typeReporter;

    public String className;
    public String methodName;
    public TypeKind returnKind;
    public String referenceName;
    public boolean methodStatic;

    public ASTIdentify(ErrorReporter idReporter, ErrorReporter typeReporter, AST ast) throws IdentificationError {
        this.scopeIdentificationTable = new Stack<HashMap<String, Declaration>>();
        this.ast = ast;
        this.allMembers = new ArrayList<Stack<HashMap<String, Declaration>>>();
        this.idReporter = idReporter;
        this.typeReporter = typeReporter;
        this.iteratorIndex = -1;

        HashMap<String, Declaration> temp = new HashMap<String, Declaration>();

        FieldDeclList tempFieldsList;
        MethodDeclList tempMethodsList;
        ParameterDeclList tempParameterList;

        // adding System class
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempFieldsList.add(new FieldDecl(false, true,
                new ClassType(new Identifier(new Token(0, "_PrintStream", new SourcePosition(0, 0))),
                        new SourcePosition(0, 0)),
                "out", new SourcePosition(0, 0)));
        temp.put("System", new ClassDecl("System", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));

        // adding _PrintStream class
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempParameterList.add(
                new ParameterDecl(new BaseType(TypeKind.INT, new SourcePosition(0, 0)), "n", new SourcePosition(0, 0)));
        tempMethodsList.add(new MethodDecl(
                new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println",
                        new SourcePosition(0, 0)),
                        tempParameterList, new StatementList(), new SourcePosition(0, 0)));
        temp.put("_PrintStream",
                new ClassDecl("_PrintStream", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));

        // adding String class
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        temp.put("String", new ClassDecl("String", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));

        // creating top level scope
        addScope();
        scopeIdentificationTable.set(0, temp);        
        //loading file classes and members
        loadClassMembers();
        //checking for required main method (PA4)
        checkForMain();
    }

    public void loadClassMembers() {
        //loads classes and members before
        //starting main traveral
        int index = 0;
        for (ClassDecl c: ((Package) this.ast).classDeclList) {
            allMembers.add(new Stack<HashMap<String, Declaration>>());
            allMembers.get(index).push(new HashMap<String, Declaration>());
            allMembers.get(index).peek().put(c.name, c);
            allMembers.get(index).push(new HashMap<String, Declaration>());
            for (FieldDecl f: c.fieldDeclList) { 
                allMembers.get(index).peek().put(f.name, f);                
            }
            for (MethodDecl m: c.methodDeclList) {
                returnKind = m.type.typeKind;   
                allMembers.get(index).peek().put(m.name, m); 
                if (m.parameterDeclList.size() > 0) {
                    allMembers.get(index).push(new HashMap<String, Declaration>());
                    for (ParameterDecl p : m.parameterDeclList) {
                        allMembers.get(index).peek().put(p.name, p);
                    }
                }
            }            
            index++;
        } 

        HashMap<String, Declaration> classItems;
        HashMap<String, Declaration> memberItems;
        FieldDeclList tempFieldsList;
        MethodDeclList tempMethodsList;
        ParameterDeclList tempParameterList;

        //addng System class 
        classItems = new HashMap<String, Declaration>();
        memberItems = new HashMap<String, Declaration>();  
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempFieldsList.add(new FieldDecl(false, true,
                new ClassType(new Identifier(new Token(0, "_PrintStream", new SourcePosition(0, 0))),
                        new SourcePosition(0, 0)),
                "out", new SourcePosition(0, 0)));
        classItems.put("System", new ClassDecl("System", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
        memberItems.put("out", new FieldDecl(false, true,
                new ClassType(new Identifier(new Token(0, "_PrintStream", new SourcePosition(0, 0))),
                        new SourcePosition(0, 0)),
                "out", new SourcePosition(0, 0)));
        allMembers.add(new Stack<HashMap<String, Declaration>>());
        allMembers.get(index).push(new HashMap<String, Declaration>());
        allMembers.get(index).set(0, classItems);
        allMembers.get(index).push(new HashMap<String, Declaration>());  
        allMembers.get(index).set(1, memberItems);
        index++;

        //adding _PrintStream class
        classItems = new HashMap<String, Declaration>();
        memberItems = new HashMap<String, Declaration>(); 
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        tempParameterList.add(
                new ParameterDecl(new BaseType(TypeKind.INT, new SourcePosition(0, 0)), "n", new SourcePosition(0, 0)));
        tempMethodsList.add(new MethodDecl(
                new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println",
                        new SourcePosition(0, 0)),
                new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)));
        classItems.put("_PrintStream",
                new ClassDecl("_PrintStream", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
        memberItems.put("println", new MethodDecl(
                new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println",
                        new SourcePosition(0, 0)),
                        tempParameterList, new StatementList(), new SourcePosition(0, 0)));
        allMembers.add(new Stack<HashMap<String, Declaration>>());
        allMembers.get(index).push(new HashMap<String, Declaration>());
        allMembers.get(index).set(0, classItems);
        allMembers.get(index).push(new HashMap<String, Declaration>());  
        allMembers.get(index).set(1, memberItems);
        index++;

        //adding String class
        classItems = new HashMap<String, Declaration>();
        memberItems = new HashMap<String, Declaration>(); 
        tempFieldsList = new FieldDeclList();
        tempMethodsList = new MethodDeclList();
        tempParameterList = new ParameterDeclList();
        classItems.put("String", new ClassDecl("String", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
        allMembers.add(new Stack<HashMap<String, Declaration>>());
        allMembers.get(index).push(new HashMap<String, Declaration>());
        allMembers.get(index).set(0, classItems);
    }

    public void checkForMain() throws IdentificationError {
        if (searchAllMembers("main") != null) {
            Declaration candidate = searchAllMembers("main");
            if (candidate.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {
                if (((MethodDecl) candidate).parameterDeclList.size() == 1 && !((MethodDecl) candidate).isPrivate && ((MethodDecl) candidate).isStatic) {                                        
                    if (((MethodDecl) candidate).parameterDeclList.get(0).type.getClass().equals(new ArrayType(new BaseType(TypeKind.STRING, new SourcePosition()), new SourcePosition()).getClass())) {                        
                        if (isSameType(((ArrayType)((MethodDecl) candidate).parameterDeclList.get(0).type).eltType.typeKind, TypeKind.STRING)) {                            
                            return;
                        }
                    }                    
                }
            }
        }   
        identificationError(0, "checkForMain", "no main method present in package");            
    }

    public boolean isSameType(TypeKind type1, TypeKind type2) {
        if (type1.equals(type2)) {
            if (type1.equals(TypeKind.UNSUPPORTED) || type2.equals(TypeKind.UNSUPPORTED)) {
                return false;
            } else {
                return true;
            }
        } else if (type1.equals(TypeKind.ERROR)) {
            if (type2.equals(TypeKind.UNSUPPORTED)) {
                return false;
            } else {
                return true;
            }
        } else if (type2.equals(TypeKind.ERROR)) {
            if (type1.equals(TypeKind.UNSUPPORTED)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isSameExprType(Expression expr1, Expression expr2) {
        return expr1.getClass().equals(expr2.getClass());
    }

    public void displayIdTable() {        
        Iterator<HashMap<String, Declaration>> scopeIterator = scopeIdentificationTable.iterator();
        int level = 0;
        System.out.println("================ScopedIdTable=================");
        boolean first = true;
        while (scopeIterator.hasNext()) {
            System.out.println("------------Level: "+ level + "-------------");
            for (Map.Entry<String, Declaration> mapElement : scopeIterator.next().entrySet()) {
                if (mapElement.getValue().type == null) {
                    if (first) {
                        System.out.println(mapElement.getKey() + " : " + "class");
                    } else {
                        System.out.println(mapElement.getKey() + " : " + mapElement.getValue());
                    }
                } else {
                    if (first) {
                        System.out.println(mapElement.getKey() + " : " + "class");
                    } else {
                        System.out.println(mapElement.getKey() + " : " + mapElement.getValue().type.typeKind);
                    }
                }
            }
            first = false;
            level++;
        }
        System.out.println("==============================================");
    }

    private void displayAllMembers() {
        //for each stack/class in the file list
        for (Stack<HashMap<String, Declaration>> clas: allMembers) {
            System.out.println("-----------------Class-------------");
            Iterator<HashMap<String, Declaration>> scopeIterator = clas.iterator();
            //for each scope in the stack
            int counter = 0;
            while(scopeIterator.hasNext()) {
                System.out.println("**********Level " + counter);
                Iterator<Map.Entry<String, Declaration>> memberIterator = scopeIterator.next().entrySet().iterator();
                //for each element in the scope
                while (memberIterator.hasNext()) {                    
                    Declaration current = memberIterator.next().getValue();
                    System.out.println("Name: " + current.name);
                }
                counter++;
            }            
        }    
        System.out.println("----------------------------------------");
    }

    public Declaration search(String name) {        
        int tempIndex = iteratorIndex;        
        while (tempIndex >= 0) {
            if (scopeIdentificationTable.elementAt(tempIndex).containsKey(name)) {
                return scopeIdentificationTable.get(tempIndex).get(name);
            }
            tempIndex--;
        }
        return null;
    }

    public Declaration searchAbove(String name) {
        int tempIndex = iteratorIndex - 1;
        while (tempIndex >= 0) {
            if (scopeIdentificationTable.elementAt(tempIndex).containsKey(name)) {
                return scopeIdentificationTable.get(tempIndex).get(name);
            }
            tempIndex--;
        }
        return null;
    }

    public Declaration searchAllMembers(String memberName) {
        // for each class
        for (Stack<HashMap<String, Declaration>> clas: allMembers) {
            Iterator<HashMap<String, Declaration>> scopeIterator = clas.iterator();
            //for each scope in the stack
            while(scopeIterator.hasNext()) {
                Iterator<Map.Entry<String, Declaration>> memberIterator = scopeIterator.next().entrySet().iterator();
                //for each element in the scope
                while (memberIterator.hasNext()) {                    
                    Declaration current = memberIterator.next().getValue();
                    if (current.name.equals(memberName)) {
                        return current;
                    }
                }
            }
        }            
        return null;
    }

    public FieldDeclList findFields(String classname) {
        ClassDecl decl = (ClassDecl) search(classname);
        return decl.fieldDeclList;
    }

    public MethodDeclList findMethods(String classname) {
        ClassDecl decl = (ClassDecl) search(classname);
        return decl.methodDeclList;
    }

    public MemberDecl findSpecifiedMember(String className, String memberName) {
        FieldDeclList fields = findFields(className);
        MethodDeclList methods = findMethods(className);
        for (FieldDecl f: fields) {
            if (f.name.equals(memberName)) {
                return f;
            }
        }
        for (MethodDecl m: methods) {
            if (m.name.equals(memberName)) {
                return m;
            }
        }
        return null;
    }

    public FieldDecl findField(String fieldName) {
        ClassDecl decl = (ClassDecl) search(this.className);
        for (FieldDecl f: decl.fieldDeclList) {
            if (f.name.equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    public MethodDecl findMethod(String methodName) {
        ClassDecl decl = (ClassDecl) search(this.className);
        for (MethodDecl m: decl.methodDeclList) {
            if (m.name.equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    public MemberDecl findMember(String memberName) {
        MethodDeclList classMethods = findMethods(this.className);
        FieldDeclList classFields = findFields(this.className);
        for(MethodDecl m: classMethods) {
            if (m.name.equals(memberName)) {
                return m;
            }
        }
        for(FieldDecl f: classFields) {
            if (f.name.equals(memberName)) {
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

    void identificationError(int lineNumber, String methodName, String problem) throws IdentificationError {
        idReporter.reportIdError(lineNumber, problem);	
        display("Identification error in " + methodName);
		throw(new IdentificationError());	
    }

    void typeError(int lineNumber, String methodName, String problem) throws TypeError {
        typeReporter.reportTypeError(lineNumber);	
        display("Type error in " + methodName + ": " + problem);
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
        addScope(); //adding level 1
        for (FieldDecl f: clas.fieldDeclList) { 
            scopeIdentificationTable.peek().put(f.name, f);     
            f.visit(this);            
        }
        for (MethodDecl m: clas.methodDeclList) {
            returnKind = m.type.typeKind;  
            methodStatic = m.isStatic;
            methodName = m.name;
            scopeIdentificationTable.peek().put(m.name, m);         
            m.visit(this);
        }
        removeScope(); //back to level 0
        return "";
    }

    public String visitFieldDecl(FieldDecl fd) throws TypeError, IdentificationError { 
        //if the field is a class type                   
        if (fd.type.getClass().equals(new ClassType(null, null).getClass())) {           
            if (searchAllMembers(((ClassType)fd.type).className.spelling) == null) {
                identificationError(fd.posn.start, "visitFieldDecl", "Type " + ((ClassType)fd.type).className.spelling + " has not been declared");
            }
        }   
        //if the field is an array of class types
        else if (fd.type.getClass().equals(new ArrayType(null, null).getClass())) {
            if (((ArrayType)fd.type).eltType.getClass().equals(new ClassType(null, null).getClass())) {
                if (searchAllMembers(((ClassType)((ArrayType)fd.type).eltType).className.spelling) == null) {
                    identificationError(fd.posn.start, "visitFieldDecl", "Type " + ((ClassType)((ArrayType)fd.type).eltType).className.spelling + " has not been declared");
                }
            }
        }  
        //if the field is a base type
        else {
            fd.type.visit(this);
        }                          
        return "";
    }

    public String visitMethodDecl(MethodDecl m) throws TypeError, IdentificationError {
        //if the method is a class type check the class exists     
        if (m.type.getClass().equals(new ClassType(null, null).getClass()))  {
            if (searchAllMembers(((ClassType)m.type).className.spelling) == null) {
                identificationError(m.posn.start, "visitMethodDecl", "Type " + ((ClassType)m.type).className.spelling + " has not been declared");
            }
        }   
        //if the method is an array of class types check the class exists
        else if (m.type.getClass().equals(new ArrayType(null, null).getClass())) {
            if (((ArrayType)m.type).eltType.getClass().equals(new ClassType(null, null).getClass())) {
                if (searchAllMembers(((ClassType)((ArrayType)m.type).eltType).className.spelling) == null) {
                    identificationError(m.posn.start, "visitMethodDecl", "Type " + ((ClassType)((ArrayType)m.type).eltType).className.spelling + " has not been declared");
                }
            }
        }         
        m.type.visit(this);
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {            
            pd.visit(this);
            scopeIdentificationTable.peek().put(pd.name, pd);
        }
        StatementList sl = m.statementList;
        addScope(); //adding level 3
        for (Statement s: sl) {
            s.visit(this);
        } 
        removeScope(); //going back to level 1
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
        addScope(); //adding scope 
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
            s.visit(this);
        }
        removeScope(); //removing scope
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
                typeError(stmt.posn.start, "visitVarDeclStmt", "declaration and assignment types dont match");
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
         
        if (stmt.ref.getClass().equals(new QualRef(null, null, null).getClass())) { 
            //if the reference is a this reference
            if (((QualRef)stmt.ref).ref.getClass().equals(new ThisRef(new SourcePosition()).getClass())) {                            
                if (search(((QualRef)stmt.ref).id.spelling) == null) {            
                    identificationError(stmt.posn.start, "visitAssignStmt", "Variable may not have been initialized");                  
                }           
                else {
                    //if the reference is a class
                    if (isSameType(search(((QualRef)stmt.ref).id.spelling).type.typeKind, TypeKind.CLASS)) {                
                        if (isSameType(search(((QualRef)stmt.ref).id.spelling).type.typeKind, stmt.val.type)) {
                            
                        }
                        else {
                            typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(stmt.ref.decl.name).type.typeKind);
                        }
                    }
                    else {
                        if (isSameType(search(((QualRef)stmt.ref).id.spelling).type.typeKind, stmt.val.type)) {
                            
                        }
                        else {
                            typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(((QualRef)stmt.ref).id.spelling).type.typeKind);
                        }
                    }            
                }               
            }  
            // if the reference is a member of the current class
            else if (search(((QualRef)stmt.ref).id.spelling) != null) {              
                if (isSameType(searchAllMembers(((IdRef)((QualRef)stmt.ref).ref).id.spelling).type.typeKind, stmt.val.type)) {
                    
                }
                else {
                    typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(((QualRef)stmt.ref).id.spelling).type.typeKind);
                }                         
            }                                     
        } 
        //if just an IdReference       
        else {     
            if (search(((IdRef)stmt.ref).id.spelling) == null) {            
                identificationError(stmt.posn.start, "visitAssignStmt", "Variable may not have been initialized");                  
            }           
            else {
                //if the reference is a class
                if (isSameType(search(((IdRef)stmt.ref).id.spelling).type.typeKind, TypeKind.CLASS)) {                
                    if (isSameType(search(((IdRef)stmt.ref).id.spelling).type.typeKind, stmt.val.type)) {
                        
                    }
                    else {
                        typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(stmt.ref.decl.name).type.typeKind);
                    }
                }
                else {
                    if (isSameType(search(((IdRef)stmt.ref).id.spelling).type.typeKind, stmt.val.type)) {
                        
                    }
                    else {
                        typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(stmt.ref.decl.name).type.typeKind);
                    }
                }            
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
            typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.exp.type + " to " + stmt.ref.decl.type.typeKind);
            return "";
        }                
    }
        
    public String visitCallStmt(CallStmt stmt) throws TypeError, IdentificationError {
        //if the call statement has more than 2 members
        if (stmt.methodRef.getClass().equals(new QualRef(null, null, null).getClass())) {
            stmt.methodRef.visit(this);
            Reference temp = ((QualRef)stmt.methodRef).ref;
            while (temp.getClass().equals(new QualRef(null, null, null).getClass())) {
                temp.visit(this);
                temp = ((QualRef)temp).ref;
            }
            ((IdRef)temp).visit(this);
            ExprList al = stmt.argList;
            int counter = 0;
            for (Expression e: al) { 
                e.visit(this);       
                if (isSameType(e.type, ((MethodDecl)searchAllMembers(((IdRef)temp).id.spelling)).parameterDeclList.get(counter).type.typeKind)) {
                    
                }
                else {
                    typeError(stmt.posn.start, "visitCallStmt", "The method " + ((IdRef)temp).id.spelling + " in the type " + ((MethodDecl) stmt.methodRef.decl).type.typeKind + " is not applicable for the arguments " + e.type);
                }
                counter++;
            }
        }
        else {
            stmt.methodRef.visit(this);
            ExprList al = stmt.argList;
            int counter = 0;
            for (Expression e: al) {
                if (isSameType(e.type, ((MethodDecl) stmt.methodRef.decl).parameterDeclList.get(counter).type.typeKind)) {
                    e.visit(this);
                }
                else {
                    typeError(stmt.posn.start, "visitCallStmt", "The method " + ((MethodDecl) stmt.methodRef.decl).name + " in the type " + ((MethodDecl) stmt.methodRef.decl).type.typeKind+ " is not applicable for the arguments " + e.type);
                }
                counter++;
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
                typeError(stmt.posn.start, "visitReturnStmt", "Type mismatch: cannot convert from " + stmt.returnExpr.type + " to " + returnKind);
            }            
        }
        else if (stmt.returnExpr == null) {
            if (isSameType(this.returnKind, TypeKind.VOID)) {

            }
            else {
                typeError(stmt.posn.start, "visitReturnStmt", "Void methods cannot return a value");
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
                    stmt.thenStmt.visit(this);                    
                }
                
                return "";
            }
            else {
                typeError(stmt.posn.start, "visitIfStmt", "Type mismatch: cannot convert from " + stmt.cond.type +" to boolean");
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
                    stmt.thenStmt.visit(this);
                    stmt.elseStmt.visit(this);
                }                
                return "";
            }
            else {
                typeError(stmt.posn.start, "visitIfStmt", "Type mismatch: cannot convert from " + stmt.cond.type +" to boolean");
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
        if (expr.operator.spelling.equals("!") && isSameType(expr.expr.type, TypeKind.BOOLEAN)) {
            expr.operator.visit(this);
            expr.expr.visit(this);
            expr.type = expr.expr.type;
        } 
        else {
            typeError(expr.posn.start, "visitUnaryExpr", "The operator ! is undefined for the argument type(s) " + expr.expr.type);
        }
        if (expr.operator.spelling.equals("-") && isSameType(expr.expr.type, TypeKind.INT)) {
            expr.operator.visit(this);
            expr.expr.visit(this);
            expr.type = expr.expr.type;
        }  
        else {
            typeError(expr.posn.start, "visitUnaryExpr", "The operator - is undefined for the argument type(s) " + expr.expr.type);
        }
        return "";
    }
    
    public String visitBinaryExpr(BinaryExpr expr) throws TypeError, IdentificationError {
        expr.operator.visit(this);
        expr.left.visit(this);
        expr.right.visit(this);
        if (isSameType(expr.left.type, expr.right.type)) {            
            if (expr.operator.spelling.equals("==")) {
                expr.type = TypeKind.BOOLEAN;
            }
            else {
                expr.type = expr.left.type;
            }            
        } 
        else {
            typeError(expr.posn.start, "visitBinaryExpr", "The operator " + expr.operator.spelling + " is undefined for the argument type(s) " + expr.left.type + "," + expr.right.type);
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
            if (!isSameType(ie.ixExpr.type, TypeKind.INT)) {
                typeError(ie.posn.start, "visitIxExpr", "Type mismatch: cannot convert from " + ie.ixExpr.type + " to int");
            }
            else if(!isSameType(ie.type, ie.ref.decl.type.typeKind)) {
                typeError(ie.posn.start, "visitIxExpr", "Type mismatch: cannot convert from " + ie.type + " to " + ie.ref.decl.type.typeKind);
            }
            
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
            typeError(expr.posn.start, "visitNewArrayExpr", "Type mismatch: cannot convert from " + expr.sizeExpr.type + " to int");
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
        //if in a static context this cannot be used
        if (this.methodStatic) {
            identificationError(ref.posn.start, "visitThisRef", "Cannot use this in a static context");
        }
        Declaration temp = search(this.className);
        if (temp != null) {
            ref.decl = temp;
        }
        else {
            identificationError(ref.posn.start, "visitThisRef", "this reference could not be resolved");
        }
        return "";
    }
    
    public String visitIdRef(IdRef ref) throws TypeError, IdentificationError {
        ref.id.visit(this);         
        // check for static membership
        if (findMember(ref.id.spelling) != null) {
            if (!((MemberDecl) findMember(ref.id.spelling)).isStatic && methodStatic) {
                identificationError(ref.posn.start, "visitIdRef", "Non static reference to static member");
            }
        } 
        else {
            if (searchAllMembers(ref.id.spelling) != null) {
                ref.decl = search(ref.id.spelling);
            }    
            else {
                identificationError(ref.posn.start, "visitIdRef", "Variable " + ref.id.spelling + " may not have been initialized");
            }
        }        
        return "";        
    }
        
    public String visitQRef(QualRef qr) throws TypeError, IdentificationError { 
        qr.id.visit(this);
        qr.ref.visit(this);
        FieldDeclList fields = new FieldDeclList();
        MethodDeclList methods = new MethodDeclList();
        boolean fieldValid = false;
        boolean methodValid = false;
        FieldDecl tempField = null;
        MethodDecl tempMethod = null;
        //if the qref is a this reference
        if (qr.ref.getClass().equals(new ThisRef(new SourcePosition()).getClass())) {
            fields = findFields(className);
            methods = findMethods(className);
            //check if the reference is a field reference
            for (FieldDecl f: fields) {  
                //within a static method in a class a reference cannot directly access a non-static member
                if ((methodStatic && !f.isStatic)) {
                    identificationError(qr.posn.start, "visitQRef", "Cannot use this in a static context");
                }
                if (f.name.equals(qr.id.spelling)) {
                    fieldValid = true;
                    tempField = f;
                    break;
                }
            }
            //check if the reference is a method reference
            for (MethodDecl m: methods) {
                //within a static method in a class a reference cannot directly access a non-static member
                if ((methodStatic && !m.isStatic)) {
                    identificationError(qr.posn.start, "visitQRef", "Cannot use this in a static context");
                }
                if (m.name.equals(qr.id.spelling)) {
                    methodValid = true;
                    tempMethod = m;
                    break;
                }
            }
        } 
        //if the reference is an member of the current class
        else if (search(qr.id.spelling) != null) {            
            if (!((FieldDecl)search(qr.id.spelling)).isStatic && methodStatic) {
                identificationError(qr.posn.start, "visitQRef", "1Cannot make a static reference to the non-static field " + qr.id.spelling);
            }
            else {
                if (search(qr.id.spelling).getClass().equals(new FieldDecl(true, true, null, null, new SourcePosition()).getClass())) {
                    fields.add(findField(((FieldDecl)search(qr.id.spelling)).name));
                }
                else {
                    methods.add(findMethod(((FieldDecl)search(qr.id.spelling)).name));
                }                                
            }            
            //check if the reference is a field reference
            for (FieldDecl f: fields) {                                    
                //within a static method in a class a reference cannot directly access a non-static member
                if (methodStatic && !f.isStatic) {
                    identificationError(qr.posn.start, "visitQRef", "2Cannot make a static reference to the non-static field " + qr.id.spelling);
                }
                //check for private conflict
                System.out.println(((IdRef)((QualRef)qr.ref).ref).id.spelling);
                if (((FieldDecl) searchAllMembers(((IdRef)((QualRef)qr.ref).ref).id.spelling)).isPrivate) {
                    identificationError(qr.posn.start, "visitQRef", "The field " + qr.id.spelling + "." + ((IdRef) qr.ref).id.spelling + " is not visible");
                } 
                //find the field
                if (f.name.equals(qr.id.spelling)) {
                    fieldValid = true;
                    tempField = f;
                    break;
                }
            }
            //check if the reference is a method reference            
            for (MethodDecl m: methods) {                                                        
                //within a static method in a class a reference cannot directly access a non-static member
                if ((methodStatic && !m.isStatic)) {
                    identificationError(qr.posn.start, "visitQRef", "3Cannot make a static reference to the non-static field " + qr.id.spelling);
                }
                //check for private conflict
                if (((MethodDecl) search(qr.id.spelling)).isPrivate) {
                    identificationError(qr.posn.start, "visitQRef", "The field " + qr.id.spelling + "." + ((IdRef) qr.ref).id.spelling + " is not visible");
                } 
                //find the method
                if (m.name.equals(qr.ref.decl.name)) {
                    methodValid = true;
                    tempMethod = m;
                    break;
                }
            }
        }
        //member of different class
        else {
            //if a member of a different class
            if (searchAllMembers(((IdRef)qr.ref).id.spelling).getClass().equals(new FieldDecl(true, true, null, null, new SourcePosition()).getClass())) {
                fields.add((FieldDecl)searchAllMembers(((IdRef)qr.ref).id.spelling));
            }
            else if (searchAllMembers(((IdRef)qr.ref).id.spelling).getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {
                methods.add((MethodDecl)searchAllMembers(((IdRef)qr.ref).id.spelling));
            }                        
            //check if the reference is a field reference
            for (FieldDecl f: fields) {
                //within a static method in a class a reference cannot directly access a non-static member
                if (methodStatic && !f.isStatic) {
                    identificationError(qr.posn.start, "visitQRef", "4Cannot make a static reference to the non-static field " + qr.id.spelling);
                }
                //check for private conflict
                if (((FieldDecl) searchAllMembers(((IdRef) qr.ref).id.spelling)).isPrivate) {
                    identificationError(qr.posn.start, "visitQRef", "The field " + qr.id.spelling + "." + ((IdRef) qr.ref).id.spelling + " is not visible");
                } 
                if (f.name.equals(((IdRef) qr.ref).id.spelling)) {
                    fieldValid = true;
                    tempField = f;
                    break;
                }
            }
            //check if the reference is a method reference            
            for (MethodDecl m: methods) {                                                
                //within a static method in a class a reference cannot directly access a non-static member
                if ((methodStatic && !m.isStatic)) {
                    identificationError(qr.posn.start, "visitQRef", "5Cannot make a static reference to the non-static field " + qr.id.spelling);
                }
                // if decl is null then search current class members
                if (search(qr.id.spelling) != null) {
                    if (findSpecifiedMember(((ClassType)search(qr.id.spelling).type).className.spelling, ((IdRef) qr.ref).id.spelling) != null) {
                        methodValid = true;
                        tempMethod = (MethodDecl) findSpecifiedMember(((ClassType)search(qr.id.spelling).type).className.spelling, ((IdRef) qr.ref).id.spelling);
                        break;
                    }
                }
                if (m.name.equals(searchAllMembers(((IdRef)qr.ref).id.spelling).name)) {   
                                                    
                    methodValid = true;
                    tempMethod = m;
                    break;
                }
            }         
        }     
             
        if (fieldValid) {
            //if the field is a this reference
            if (qr.ref.getClass().equals(new ThisRef(new SourcePosition()).getClass())) {
                //check for static membership
                if (isSameType(findField(qr.id.spelling).type.typeKind, TypeKind.CLASS)) {
                    if (findField(qr.id.spelling).isStatic) {

                    }
                    else {
                        identificationError(qr.posn.start, "visitQRef", "Non static reference to static member");
                    }
                }
                qr.decl = findField(qr.id.spelling);
            }
            //if the reference is an member of the current class
            else if (search(qr.id.spelling) != null) {
                if (((MemberDecl)findMember(qr.id.spelling)).isStatic) {

                }
                else {
                    identificationError(qr.posn.start, "visitQRef", "1Non static reference to static member");
                }                
            }     
            //if reference is member of another class       
            else {
                //check for static membership
                if (isSameType(searchAllMembers(((IdRef) qr.ref).id.spelling).type.typeKind, TypeKind.CLASS)) {
                    if (((MemberDecl)qr.ref.decl).isStatic) {

                    }
                    else {
                        identificationError(qr.posn.start, "visitQRef", "2Non static reference to static member");
                    }
                }
                qr.decl = tempField;
            }            
        }
        else if (methodValid) {            
            // if the reference is a this reference
            if (qr.ref.getClass().equals(new ThisRef(new SourcePosition()).getClass())) {
                //check for static membership
                if (isSameType(findField(qr.id.spelling).type.typeKind, TypeKind.CLASS)) {
                    if (findField(qr.id.spelling).isStatic) {

                    }
                    else {
                        identificationError(qr.posn.start, "visitQRef", "Non static reference to static member");
                    }
                }
            }
            //if the reference is an instance of the current class
            else if (search(qr.id.spelling) != null) {
                if (((MemberDecl)findMember(((IdRef)qr.ref).id.spelling)).isStatic) {

                }
                else {
                    identificationError(qr.posn.start, "visitQRef", "Non static reference to static member");
                }             
            }       
            else {
                //check for static membership
                if (isSameType(searchAllMembers(qr.id.spelling).type.typeKind, TypeKind.CLASS)) {
                    if (((MemberDecl)searchAllMembers(qr.id.spelling)).isStatic) {

                    }
                    else {
                        identificationError(qr.posn.start, "visitQRef", "Non static reference to static member");
                    }
                }
                qr.decl = searchAllMembers(qr.id.spelling);
            }            
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