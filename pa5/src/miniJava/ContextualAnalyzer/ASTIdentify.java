/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.ContextualAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NewStringExpr;
import miniJava.AbstractSyntaxTrees.NullExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.StringLiteral;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.CodeGenerator.ClassRep;
import miniJava.CodeGenerator.FieldRep;
import miniJava.CodeGenerator.MethodRep;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;

/*
 * Display AST in text form, one node per line, using indentation to show 
 * subordinate nodes below a parent node.
 *   
 * Performs an in-order traversal of AST, visiting an AST node of type -- 
 * with a method of the form  
 *   
 *       public Object visit--(-- astnode)
 *       
 *   where arg is a prefix Object (indentation) to precede display of ast node
 *   and a null Object is returned as the result.
 *   The display is produced by printing a line of output at each node visited.
 */
public class ASTIdentify implements Traveller<Object> {

    public Stack<HashMap<String, Declaration>> scopeIdentificationTable;
    public ArrayList<Stack<HashMap<String, Declaration>>> allMembers;
    public AST ast;
    public ASTDisplay astDisplay;
    public int iteratorIndex;
    public ErrorReporter idReporter;
    public ErrorReporter typeReporter;

    public String className;
    public String methodName;
    public String varName;
    public TypeDenoter returnKind;
    public String referenceName;
    public boolean methodStatic;
    public boolean staticReference;
    public List<String> classNames;
    public List<String> classNamesInclusive;
    public List<String> parameterNames;
    public boolean checkingConditionals;
    public boolean returnStatementPresent;
    public boolean methodRef;

    public ASTIdentify(ErrorReporter idReporter, ErrorReporter typeReporter, AST ast) throws IdentificationError {
        this.scopeIdentificationTable = new Stack<HashMap<String, Declaration>>();
        this.ast = ast;
        this.astDisplay = new ASTDisplay();
        this.allMembers = new ArrayList<Stack<HashMap<String, Declaration>>>();
        this.idReporter = idReporter;
        this.typeReporter = typeReporter;
        this.iteratorIndex = -1;
        this.varName = "";
        this.parameterNames = new ArrayList<String>();
        this.classNames = new ArrayList<String>();
        this.classNamesInclusive = new ArrayList<String>();
        this.methodRef = false;
        classNamesInclusive.add("System");
        classNamesInclusive.add("_PrintStream");
        classNamesInclusive.add("String");        
        
        addScope();
        addPreMadeClasses();                       
        loadClassMembers();
        //checkForMain();
    }

    public ArrayList<String> getClassNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (ClassDecl c: ((Package)ast).classDeclList) {
            result.add(c.name);
        }
        return result;
    }

    public void addPreMadeClasses() {

        HashMap<String, Declaration> temp = new HashMap<String, Declaration>();
        ArrayList<String> classes = getClassNames();

        FieldDeclList tempFieldsList;
        MethodDeclList tempMethodsList;
        ParameterDeclList tempParameterList;
        ClassDecl tempClassDecl;
        FieldDecl tempFieldDecl;
        MethodDecl tempMethodDecl;

        boolean added = false;

        // adding System class
        if (!classes.contains("System")) {
            tempFieldsList = new FieldDeclList();
            tempMethodsList = new MethodDeclList();
            tempParameterList = new ParameterDeclList();
            tempFieldDecl = new FieldDecl(false, true,
            new ClassType(new Identifier(new Token(0, "_PrintStream", new SourcePosition(0, 0))),
                          new SourcePosition(0, 0)),
                          "out", new SourcePosition(0, 0));
            tempFieldDecl.entity = new FieldRep(0, 0);
            tempFieldsList.add(tempFieldDecl);
            temp.put("System", new ClassDecl("System", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
            ((Package)ast).classDeclList.add(new ClassDecl("System", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
            added = true;
        }
                
        // adding _PrintStream class
        if (!classes.contains("_PrintStream")) {
            tempFieldsList = new FieldDeclList();
            tempMethodsList = new MethodDeclList();
            tempParameterList = new ParameterDeclList();
            tempParameterList.add(
                    new ParameterDecl(new BaseType(TypeKind.INT, new SourcePosition(0, 0)), "n", new SourcePosition(0, 0)));
            tempMethodDecl = new MethodDecl(
                new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println",
                        new SourcePosition(0, 0)),
                        tempParameterList, new StatementList(), new SourcePosition(0, 0));
            tempMethodDecl.entity = new MethodRep(0);
            tempMethodsList.add(tempMethodDecl);
            temp.put("_PrintStream",
                    new ClassDecl("_PrintStream", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
            ((Package)ast).classDeclList.add(new ClassDecl("_PrintStream", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
            added = true;
        }

        // adding String class
        if (!classes.contains("String")) {
            tempFieldsList = new FieldDeclList();
            tempMethodsList = new MethodDeclList();
            tempParameterList = new ParameterDeclList();
            temp.put("String", new ClassDecl("String", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));
            ((Package)ast).classDeclList.add(new ClassDecl("String", tempFieldsList, tempMethodsList, new SourcePosition(0, 0)));        
            added = true;
        }

        //add top level scope
        if (added) {
            scopeIdentificationTable.set(0, temp); 
        }       
    }

    public void loadClassMembers() {
        //loads classes and members before starting main traveral
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
    }

    public void checkForMain() throws IdentificationError {
        ArrayList<Declaration> mainMethods = new ArrayList<Declaration>();
        for (Stack<HashMap<String, Declaration>> clas: allMembers) {
            Iterator<HashMap<String, Declaration>> scopeIterator = clas.iterator();
            //for each scope in the stack
            while(scopeIterator.hasNext()) {
                Iterator<Map.Entry<String, Declaration>> memberIterator = scopeIterator.next().entrySet().iterator();
                //for each element in the scope
                while (memberIterator.hasNext()) {                    
                    Declaration candidate = memberIterator.next().getValue();
                    if (candidate.name.equals("main")) {
                        if (candidate.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {
                            if (isSameTypeKind(candidate.type.typeKind, TypeKind.VOID)) {
                                if (((MethodDecl) candidate).parameterDeclList.size() == 1 && !((MethodDecl) candidate).isPrivate && ((MethodDecl) candidate).isStatic) {                                        
                                    if (((MethodDecl) candidate).parameterDeclList.get(0).type.getClass().equals(new ArrayType(new BaseType(TypeKind.STRING, new SourcePosition()), new SourcePosition()).getClass())) {                        
                                        if (isSameTypeKind(((ArrayType)((MethodDecl) candidate).parameterDeclList.get(0).type).eltType.typeKind, TypeKind.STRING)) {                            
                                            mainMethods.add(candidate);
                                        }
                                    }                    
                                }
                            }                            
                        }
                    }
                }
            }
        } 
        if (mainMethods.size() == 0) {
            identificationError(0, "checkForMain", "no main method present in package");
        }
        else if (mainMethods.size() > 1) {
            identificationError(0, "checkForMain", "duplicte main methods present in package");
        }
        else {
            return;
        }                
    }

    public boolean isMain(MethodDecl candidate) {
        if (candidate.name.equals("main")) {
            if (candidate.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {
                if (isSameTypeKind(candidate.type.typeKind, TypeKind.VOID)) {
                    if (((MethodDecl) candidate).parameterDeclList.size() == 1 && !((MethodDecl) candidate).isPrivate && ((MethodDecl) candidate).isStatic) {                                        
                        if (((MethodDecl) candidate).parameterDeclList.get(0).type.getClass().equals(new ArrayType(new BaseType(TypeKind.STRING, new SourcePosition()), new SourcePosition()).getClass())) {                        
                            if (isSameTypeKind(((ArrayType)((MethodDecl) candidate).parameterDeclList.get(0).type).eltType.typeKind, TypeKind.STRING)) {                            
                                return true;
                            }
                        }                    
                    }
                }                            
            }
        }
        return false;        
    }

    public boolean isSameTypeKind(TypeKind type1, TypeKind type2) {
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

    public void displayAST() {
        astDisplay.showTree(this.ast);
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
        if (search(memberName) != null) {
            return search(memberName);
        }
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
                        if (current.getClass().equals(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)), new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)).getClass()) ||
                            current.getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass()) ||
                            current.getClass().equals(new ClassDecl(null, null, null, null).getClass())) {
                            return current;  
                        }                        
                    }
                }
            }
        }            
        return null;
    }

    public void removeClass(String className) {
        int index = 0;
        for (Stack<HashMap<String, Declaration>> clas: allMembers) {            
            Iterator<HashMap<String, Declaration>> scopeIterator = clas.iterator();
            if (scopeIterator.next().containsKey(className)) {
                allMembers.remove(index); 
                return;
            }
            index++;
        }  
    }

    public MemberDecl findMember(String memberName) {
        MethodDeclList classMethods = ((ClassDecl) search(this.className)).methodDeclList;
        FieldDeclList classFields = ((ClassDecl) search(this.className)).fieldDeclList;
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

    public ArrayList<MemberDecl> getMembers(String className) {
        ArrayList<MemberDecl> members = new ArrayList<MemberDecl>();
        for (Stack<HashMap<String, Declaration>> clas: allMembers) {            
            Iterator<HashMap<String, Declaration>> scopeIterator = clas.iterator();
            HashMap<String, Declaration> next = scopeIterator.next();
            if (next.containsKey(className)) {
                ClassDecl c = (ClassDecl)next.get(className);
                for (FieldDecl f: c.fieldDeclList) {
                    members.add(f);
                }
                for (MethodDecl m: c.methodDeclList) {
                    members.add(m);
                }
                return members;
            }
        }
        return null;
    }

    public MemberDecl getMemberInClass(String className, String memberName) {
        ArrayList<MemberDecl> members = getMembers(className);
        for (MemberDecl m: members) {
            if (m.name.equals(memberName)) {
                return m;
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

    public short getClassSize(FieldDeclList fl) {
        short counter = 0;
        for (FieldDecl f: fl) {
            if (!f.isStatic) {                
                counter++;
            }
        }
        return counter;
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

    public Object visit(AST ast) throws TypeError, IdentificationError {
        ast.visit(this);
        return null;
    }

    public Object visitPackage(Package prog) throws TypeError, IdentificationError {
        int classOffset = 0;
        for (ClassDecl c: prog.classDeclList) {  
            if (classNames.contains(c.name)) {                
                identificationError(c.posn.start, "visitPackage", "duplicate class declaration " + c.name);
            }                                     
            c.entity = new ClassRep(classOffset, getClassSize(c.fieldDeclList));     
            className = c.name;
            scopeIdentificationTable.peek().put(c.name, c);
            classNames.add(c.name); 
            classNamesInclusive.add(c.name);
            c.visit(this); 
            classOffset+= c.fieldDeclList.size();                    
        }         
        return null;    
    }

    public Object visitClassDecl(ClassDecl clas) throws TypeError, IdentificationError {        
        addScope(); //adding level 1
        int fieldOffset = 0;
        for (FieldDecl f: clas.fieldDeclList) { 
            if (search(f.name) != null)     {
                identificationError(f.posn.start, "visitClassDecl", "duplicate declaration of member " + f.name + " in class " + className);
            }
            scopeIdentificationTable.peek().put(f.name, f);
            // save offset in class in runtime description
            f.entity = new FieldRep(fieldOffset, 1, f.isStatic);   
            f.visit(this);
            if (!f.isStatic) {
                fieldOffset++;
            }                    
        }
       
        for (MethodDecl m: clas.methodDeclList) {
            if (search(m.name) != null) {
                identificationError(m.posn.start, "visitClassDecl", "Duplicate member declaration with identifier " + m.name + " in class " + className);
            }  
            this.returnKind = m.type;  
            this.methodStatic = m.isStatic;
            this.staticReference = m.isStatic;
            this.methodName = m.name;
            m.className = this.className;                                 
            m.visit(this);
            scopeIdentificationTable.peek().put(m.name, m);
        }
        removeScope(); //back to level 0
        return null;
    }

    public Object visitFieldDecl(FieldDecl fd) throws TypeError, IdentificationError { 
        fd.type.visit(this);             
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
        return null;
    }

    public Object visitMethodDecl(MethodDecl m) throws TypeError, IdentificationError {
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
        parameterNames.clear();        
        for (ParameterDecl pd: pdl) {            
            pd.visit(this);
            scopeIdentificationTable.peek().put(pd.name, pd);
            parameterNames.add(pd.name);
        }
        
        StatementList sl = m.statementList;
        //checking for return stmt outside of conditional in non main method
        if (!isMain(m)) {
            boolean returnInList = false;
            for (Statement s: sl) {
                if (s.getClass().equals(new ReturnStmt(null, null).getClass())) {
                    returnInList = true;
                    break;
                }
            } 
            //if no return statement outside of conditional we must check every branch for return
            if (!returnInList) {
                this.returnStatementPresent = false;
                if (!isSameTypeKind(m.type.typeKind, TypeKind.VOID)) {
                    this.checkingConditionals = true;
                }  
                else {
                    this.checkingConditionals = false;
                }              
            } 
        }               
        
        addScope(); //adding level 3
        for (Statement s: sl) {
            s.visit(this);
        } 
        removeScope(); //going back to level 1
        if (this.checkingConditionals) {
            if (!this.returnStatementPresent) {
                typeError(m.posn.start, "visitIfStmt", "missing return statement");
            }                    
        } 
        return null;     
    }
    
    public Object visitParameterDecl(ParameterDecl pd) throws TypeError, IdentificationError {
        pd.type.visit(this);
        //checking for undeclared types       
        if (pd.type.getClass().equals(new ClassType(null, null).getClass())) {                        
            if (search(((ClassType)pd.type).className.spelling) == null) {                
                identificationError(pd.posn.start, "visitParameterDecl", "undeclared class " + ((ClassType)pd.type).className.spelling);
            }
            else if (!search(((ClassType)pd.type).className.spelling).getClass().equals(new ClassDecl(null, null, null, null).getClass())) {
                identificationError(pd.posn.start, "visitParameterDecl", "undeclared class " + ((ClassType)pd.type).className.spelling);
            }
        }
        //checking for duplicate parameter names
        if (parameterNames.contains(pd.name)) {
            identificationError(pd.posn.start, "visitParameterDecl", "parameter " + pd.name + " is already declared in method " + methodName);
        }
        scopeIdentificationTable.peek().put(pd.name, pd);
        return null;
    } 
    
    public Object visitVarDecl(VarDecl vd) throws TypeError, IdentificationError {
        vd.type.visit(this);
        scopeIdentificationTable.peek().put(vd.name, vd);
        return null;
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TYPES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public Object visitBaseType(BaseType type){
        return null;
    }
    
    public Object visitClassType(ClassType ct) throws TypeError, IdentificationError {
        ct.className.visit(this);
        return null;
    }
    
    public Object visitArrayType(ArrayType type) throws TypeError, IdentificationError {
        type.eltType.visit(this);
        return null;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // STATEMENTS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public Object visitBlockStmt(BlockStmt stmt) throws TypeError, IdentificationError {
        addScope(); //adding scope 
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
            s.visit(this);
        }
        removeScope(); //removing scope
        return null;
    }
    
    public Object visitVardeclStmt(VarDeclStmt stmt) throws TypeError, IdentificationError {    
        if (iteratorIndex == 3 && searchAbove(stmt.varDecl.name) != null) {
            identificationError(stmt.posn.start, "visitVardeclStmt", "duplicate local variable " + stmt.varDecl.name);
        }
        stmt.varDecl.visit(this);
        this.varName = stmt.varDecl.name;
        if (stmt.initExp != null) { 
            stmt.initExp.visit(this);
            //if the initExpr is a classType
            if (isSameTypeKind(stmt.initExp.type, TypeKind.CLASS)) {                
                // if the init expression is new
                if (stmt.initExp.getClass().equals(new NewObjectExpr(null, null).getClass())) {                    
                    //cannot assign class type to primitive type  
                    if (!isSameTypeKind((((VarDecl)stmt.varDecl).type).typeKind, TypeKind.CLASS)) {                       
                        typeError(stmt.posn.start, "visitVarDeclStmt", "declaration (" + stmt.varDecl.type.typeKind + ") and assignment (" + TypeKind.CLASS +  ") types dont match");
                    }
                    
                    // if class types match
                    if (stmt.varDecl.type.getClass().equals(new ClassType(null, null).getClass())) {
                        if (((ClassType)stmt.varDecl.type).className.spelling.equals(((NewObjectExpr)stmt.initExp).classtype.className.spelling)) {
                            stmt.varDecl.type.typeKind = stmt.initExp.type;
                        }
                    }                    
                    else {
                        typeError(stmt.posn.start, "visitVarDeclStmt", "declaration (" + ((ClassType)stmt.varDecl.type).className.spelling + ") and assignment (" + ((NewObjectExpr)stmt.initExp).classtype.className.spelling + ") types dont match");
                    } 
                }
                // if the init expression is a field or variable
                else if (stmt.initExp.getClass().equals(new RefExpr(null, null).getClass())) { 
                    //cannot assign class type to primitive type
                    if (!isSameTypeKind(stmt.varDecl.type.typeKind, TypeKind.CLASS)) {
                        typeError(stmt.posn.start, "visitVarDeclStmt", "declaration (" + stmt.varDecl.type.typeKind + ") and assignment (" + TypeKind.CLASS +  ") types dont match");
                    }
                    
                    // if class types match
                    if (stmt.varDecl.type.getClass().equals(new ClassType(null, null).getClass())) {
                        if (((ClassType)stmt.varDecl.type).className.spelling == ((ClassType)search(stmt.varDecl.name).type).className.spelling) {
                            stmt.varDecl.type.typeKind = stmt.initExp.type;
                        }
                    }                    
                    else {
                        typeError(stmt.posn.start, "visitVarDeclStmt", "declaration (" + ((ClassType)((VarDecl)stmt.varDecl).type).className.spelling + ") and assignment (" + ((NewObjectExpr)stmt.initExp).classtype.className.spelling + ") types dont match");
                    }                               
                }                              
            }   
            // if the init expression is an NewArrayExpr
            else if (stmt.initExp.getClass().equals(new NewArrayExpr(null, null, null).getClass())) {              
                if (isSameTypeKind(stmt.varDecl.type.typeKind, ((NewArrayExpr)stmt.initExp).type)) {
                    stmt.varDecl.type.typeKind = stmt.initExp.type;                    
                }
                else {
                    typeError(stmt.posn.start, "visitVarDeclStmt", "declaration (" + stmt.varDecl.type.typeKind + ") and assignment (" + stmt.initExp.type +  ") types dont match");
                }  
            }
            // if the init expression is a primative
            else {
                if (isSameTypeKind(stmt.varDecl.type.typeKind, stmt.initExp.type)) {
                    stmt.varDecl.type.typeKind = stmt.initExp.type;
                }
                else {
                    typeError(stmt.posn.start, "visitVarDeclStmt", "declaration (" + stmt.varDecl.type.typeKind + ") and assignment (" + stmt.initExp.type +  ") types dont match");
                }  
            }                 
        }	               
        scopeIdentificationTable.peek().put(stmt.varDecl.name, stmt.varDecl); 
        this.varName = "";        
        return null;
    }
    
    public Object visitAssignStmt(AssignStmt stmt) throws TypeError, IdentificationError {
        stmt.ref.visit(this);
        stmt.val.visit(this); 
        Reference ref = stmt.ref;     
        //cannot assign to length field  
        if (stmt.ref.getClass().equals(new QualRef(null, null, null).getClass())) {             
            if (((QualRef)ref).id.spelling.equals("length") &&
                ref.decl.type.getClass().equals(new ArrayType(null, null).getClass())) {                
                typeError(stmt.posn.start, "visitAssignStmt", "Cannot assign value to length field of an array");                
            }
        }
        //if the reference is a this reference
        if (stmt.ref.getClass().equals(new QualRef(null, null, null).getClass())) {
            if (((QualRef)ref).ref.getClass().equals(new ThisRef(new SourcePosition()).getClass())) {                                            
                if (search(((QualRef)ref).id.spelling) == null) {            
                    identificationError(stmt.posn.start, "visitAssignStmt", "Variable may not have been initialized");                  
                }                           
                if (!isSameTypeKind(search(((QualRef)ref).id.spelling).type.typeKind, stmt.val.type)) {
                    typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(((QualRef)ref).id.spelling).type.typeKind);
                }                                 
            }  
        }        
        // if the reference is a variable, parameter or member of the current class
        else if (search(stmt.ref.decl.name) != null) { 
            if (!isSameTypeKind(stmt.ref.decl.type.typeKind, stmt.val.type)) {
                typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(((QualRef)stmt.ref).id.spelling).type.typeKind);
            }   
        }
        //if the reference is a member of a different class 
        if (searchAllMembers(stmt.ref.decl.name) != null) {                
            if (!isSameTypeKind(searchAllMembers(ref.decl.name).type.typeKind, stmt.val.type)) {
                typeError(stmt.posn.start, "visitAssignStmt", "Type mismatch: cannot convert from " + stmt.val.type + " to " + search(stmt.ref.decl.name).type.typeKind);
            }            
        }                                      
        return null;
    }
    
    public Object visitIxAssignStmt(IxAssignStmt stmt) throws TypeError, IdentificationError {
        stmt.ref.visit(this);
        stmt.ix.visit(this);
        stmt.exp.visit(this);
        if (!(isSameTypeKind(stmt.ref.decl.type.typeKind, TypeKind.ARRAY) && // ref type must be array type
            isSameTypeKind(stmt.ix.type, TypeKind.INT) &&                  // index must be int type
            isSameTypeKind(((ArrayType)stmt.ref.decl.type).eltType.typeKind, stmt.exp.type))) {  // assignment type must equal reference type            
                typeError(stmt.posn.start, "visitIxAssignStmt", "Type mismatch: cannot convert from " + stmt.exp.type + " to " + ((ArrayType)stmt.ref.decl.type).eltType.typeKind);            
        }  
        return null;              
    }
        
    public Object visitCallStmt(CallStmt stmt) throws TypeError, IdentificationError {
        //if the call statement has more than 2 members
        this.methodRef = true;    
        stmt.methodRef.visit(this);
        this.methodRef = false;
        if (!stmt.methodRef.decl.getClass().equals(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)), new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)).getClass())) {
            typeError(stmt.posn.start, "visitCallStmt", stmt.methodRef.decl + " is not a method");
        }                   
        ExprList al = stmt.argList;
        int counter = 0;
        for (Expression e: al) { 
            e.visit(this);             
            if (!isSameTypeKind(e.type, ((MethodDecl) stmt.methodRef.decl).parameterDeclList.get(counter).type.typeKind)) {
                typeError(stmt.posn.start, "visitCallStmt", "The " + counter + "th parameter of method " + stmt.methodRef.decl.name + " in the type " + ((MethodDecl) stmt.methodRef.decl).parameterDeclList.get(counter).type.typeKind + " is not applicable for the arguments " + e.type);
            }                                                                          
            counter++;
        }                                
        return null;
    }
    
    public Object visitReturnStmt(ReturnStmt stmt) throws TypeError, IdentificationError {        
        if (stmt.returnExpr != null) {
            stmt.returnExpr.visit(this);
            if (!isSameTypeKind(this.returnKind.typeKind, stmt.returnExpr.type)) {
                typeError(stmt.posn.start, "visitReturnStmt", "Type mismatch: cannot convert from " + stmt.returnExpr.type + " to " + this.returnKind.typeKind);
            }         
        }
        else if (stmt.returnExpr == null) {
            if (!isSameTypeKind(this.returnKind.typeKind, TypeKind.VOID)) {
                typeError(stmt.posn.start, "visitReturnStmt", "Void methods cannot return a value");
            }
        }
        this.returnStatementPresent = true;
        return null;
    }
    
    public Object visitIfStmt(IfStmt stmt) throws TypeError, IdentificationError {
        stmt.cond.visit(this);
        boolean returnInAllBranches = false;
        this.returnStatementPresent = false;
        if (isSameTypeKind(stmt.cond.type, TypeKind.BOOLEAN)) {                                            
            //if the if statement is a block statement              
            if (stmt.thenStmt.getClass().equals(new BlockStmt(null, null).getClass())) {
                //cannot have sole var decl in conditional branch
                if (((BlockStmt) stmt.thenStmt).sl.get(0).getClass().equals(new VarDeclStmt(null, null, null).getClass()) && ((BlockStmt) stmt.thenStmt).sl.size() == 1) {
                    identificationError(stmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
                }
                else {
                    stmt.thenStmt.visit(this);
                }
            }
            //if the only statement is a variable declaration
            else if (stmt.thenStmt.getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                identificationError(stmt.thenStmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
            }
            else {
                stmt.thenStmt.visit(this);
            }  
            if (this.checkingConditionals) {
                if (!this.returnStatementPresent) {
                    typeError(stmt.posn.start, "visitIfStmt", "missing return statement");
                }                    
            } 
            //if else statement is not null 
            if (stmt.elseStmt != null) {
                this.returnStatementPresent = false;
                //if the else statement is a block statement              
                if (stmt.elseStmt.getClass().equals(new BlockStmt(null, null).getClass())) {
                    //if the only statement is a variable declaration
                    if (((BlockStmt) stmt.elseStmt).sl.get(0).getClass().equals(new VarDeclStmt(null, null, null).getClass()) && ((BlockStmt) stmt.elseStmt).sl.size() == 1) {
                        identificationError(stmt.elseStmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
                    }
                    else {
                        stmt.elseStmt.visit(this);
                    }
                }
                //if the only statement is a variable declaration
                else if (stmt.elseStmt.getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                    identificationError(stmt.elseStmt.posn.start, "visitIfStmt", "Cannot have only var declaration in conditional branch");
                }
                else {
                    stmt.elseStmt.visit(this);
                }
                if (this.checkingConditionals) {
                    if (!this.returnStatementPresent) {
                        typeError(stmt.posn.start, "visitIfStmt", "missing return statement");
                    }                    
                } 
            }               
        }
        else {
            typeError(stmt.posn.start, "visitIfStmt", "Type mismatch: cannot convert from " + stmt.cond.type +" to boolean");
        }
        return null;
    }
    
    public Object visitWhileStmt(WhileStmt stmt) throws TypeError, IdentificationError {
        stmt.cond.visit(this);
        if (isSameTypeKind(stmt.cond.type, TypeKind.BOOLEAN)) {            
            if (stmt.body.getClass().equals(new BlockStmt(null, null).getClass())) {
                if (((BlockStmt) stmt.body).sl.get(0).getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                    identificationError(stmt.body.posn.start, "visitIfStmt", "Cannot have only var declaration in while loop");
                }
                else {
                    stmt.body.visit(this);
                }
            }
            //if the only statement is a variable declaration
            else if (stmt.body.getClass().equals(new VarDeclStmt(null, null, null).getClass())) {
                identificationError(stmt.body.posn.start, "visitIfStmt", "Cannot have only var declaration in while loop");
            }
            else {
                stmt.body.visit(this);
            } 
        }
        return null;
    }
    

    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    public Object visitUnaryExpr(UnaryExpr expr) throws TypeError, IdentificationError {        
        expr.operator.visit(this);
        expr.expr.visit(this);
        if (expr.operator.spelling.equals("!") && isSameTypeKind(expr.expr.type, TypeKind.BOOLEAN)) {                        
            expr.type = expr.expr.type;
        } 
        else if (expr.operator.spelling.equals("-") && isSameTypeKind(expr.expr.type, TypeKind.INT)) {
            expr.type = expr.expr.type;
        }  
        else {
            typeError(expr.posn.start, "visitUnaryExpr", "The operator " + expr.operator.spelling + " is undefined for the argument type(s) " + expr.expr.type);
        }
        return null;
    }
    
    public Object visitBinaryExpr(BinaryExpr expr) throws TypeError, IdentificationError {
        expr.operator.visit(this);
        expr.left.visit(this);
        expr.right.visit(this);        
        //if comparing two class types
        if (isSameTypeKind(expr.left.type, TypeKind.CLASS) && isSameTypeKind(expr.right.type, TypeKind.CLASS)) {
            if (expr.left.getClass().equals(new NewObjectExpr(null, null).getClass()) &&
                expr.right.getClass().equals(new NewObjectExpr(null, null).getClass())) {
                if (((NewObjectExpr)expr.left).classtype == ((NewObjectExpr)expr.right).classtype) {            
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }
                    else {
                        typeError(expr.posn.start, "visitBinaryExpr", "Operand  " + expr.operator.spelling + " is incompatable for types " + ((NewObjectExpr)expr.left).classtype.className.spelling + " and " + ((NewObjectExpr)expr.right).classtype.className.spelling);
                    }
                }
                else {
                    typeError(expr.posn.start, "visitBinaryExpr", "Incompatable operand types " + ((NewObjectExpr)expr.left).classtype.className.spelling + " and " + ((NewObjectExpr)expr.right).classtype.className.spelling);
                }
            }
            else if (expr.left.getClass().equals(new RefExpr(null, null).getClass()) &&
                expr.right.getClass().equals(new NewObjectExpr(null, null).getClass())) {
                if (((RefExpr)expr.left).ref.decl.name == ((NewObjectExpr)expr.right).classtype.className.spelling) {            
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }
                    else {
                        typeError(expr.posn.start, "visitBinaryExpr", "Operand  " + expr.operator.spelling + " is incompatable for types " + ((RefExpr)expr.left).ref.decl.name + " and " + ((NewObjectExpr)expr.right).classtype.className.spelling);
                    }
                }
                else {
                    typeError(expr.posn.start, "visitBinaryExpr", "Incompatable operand types " + ((RefExpr)expr.left).ref.decl.name + " and " + ((NewObjectExpr)expr.right).classtype.className.spelling);
                }
            }
            else if (expr.left.getClass().equals(new NewObjectExpr(null, null).getClass()) &&
                     expr.right.getClass().equals(new RefExpr(null, null).getClass())) {
                if (((NewObjectExpr)expr.left).classtype.className.spelling == ((RefExpr)expr.right).ref.decl.name) {            
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }
                    else {
                        typeError(expr.posn.start, "visitBinaryExpr", "Operand  " + expr.operator.spelling + " is incompatable for types " + ((NewObjectExpr)expr.left).classtype.className.spelling + " and " + ((RefExpr)expr.right).ref.decl.name);
                    }
                }
                else {
                    typeError(expr.posn.start, "visitBinaryExpr", "Incompatable operand types " + ((NewObjectExpr)expr.left).classtype.className.spelling + " and " + ((RefExpr)expr.right).ref.decl.name);
                }
            }
            else if (expr.left.getClass().equals(new NullExpr(null).getClass())) {
                if (expr.operator.spelling.equals("==") ||
                    expr.operator.spelling.equals("!=")) {
                    expr.type = TypeKind.BOOLEAN;
                }
                else {
                    typeError(expr.posn.start, "visitBinaryExpr", "Operand  " + expr.operator.spelling + " is incompatable for types null and " + ((RefExpr)expr.right).ref.decl.name);
                }
            }
            else if (expr.right.getClass().equals(new NullExpr(null).getClass())) {
                if (expr.operator.spelling.equals("==") ||
                    expr.operator.spelling.equals("!=")) {
                    expr.type = TypeKind.BOOLEAN;
                }
                else {
                    typeError(expr.posn.start, "visitBinaryExpr", "Operand  " + expr.operator.spelling + " is incompatable for types " + ((RefExpr)expr.right).ref.decl.name + " and null");
                }
            }
            else {
                if (((RefExpr)expr.left).ref.decl.name == ((RefExpr)expr.right).ref.decl.name) {            
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }
                    else {
                        typeError(expr.posn.start, "visitBinaryExpr", "Operand  " + expr.operator.spelling + " is incompatable for types " + ((RefExpr)expr.left).ref.decl.name + " and " + ((RefExpr)expr.right).ref.decl.name);
                    }
                } 
                else {
                    typeError(expr.posn.start, "visitBinaryExpr", "Incompatable operand types " + ((RefExpr)expr.left).ref.decl.name + " and " + ((RefExpr)expr.right).ref.decl.name);
                } 
            }
        }
        // if comparing primitive types
        else if (isSameTypeKind(expr.left.type, expr.right.type)) {            
            if (expr.operator.spelling.equals("==") ||
                expr.operator.spelling.equals("!=") ||
                expr.operator.spelling.equals("<") ||
                expr.operator.spelling.equals(">") ||
                expr.operator.spelling.equals(">=") ||
                expr.operator.spelling.equals("<=")) {
                expr.type = TypeKind.BOOLEAN;
            }            
            else {
                expr.type = expr.left.type;
            }            
        } 
        else {
            //if both side is an array
            if (expr.left.getClass().equals(new IxExpr(null, null, null).getClass()) && expr.right.getClass().equals(new IxExpr(null, null, null).getClass())) {
                if (isSameTypeKind(((ArrayType)search(((IdRef)((IxExpr)expr.left).ref).id.spelling).type).eltType.typeKind, ((ArrayType)search(((IdRef)((IxExpr)expr.right).ref).id.spelling).type).eltType.typeKind)) {                    
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=") ||
                        expr.operator.spelling.equals("<") ||
                        expr.operator.spelling.equals(">") ||
                        expr.operator.spelling.equals(">=") ||
                        expr.operator.spelling.equals("<=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }            
                    else {
                        expr.type = ((ArrayType)search(((IdRef)((IxExpr)expr.left).ref).id.spelling).type).eltType.typeKind;
                    }                    
                }
            }
            //if left side is an array
            else if (expr.left.getClass().equals(new IxExpr(null, null, null).getClass())) {
                if (isSameTypeKind(((ArrayType)search(((IdRef)((IxExpr)expr.left).ref).id.spelling).type).eltType.typeKind, expr.right.type)) {                    
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=") ||
                        expr.operator.spelling.equals("<") ||
                        expr.operator.spelling.equals(">") ||
                        expr.operator.spelling.equals(">=") ||
                        expr.operator.spelling.equals("<=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }            
                    else {
                        expr.type = ((ArrayType)search(((IdRef)((IxExpr)expr.left).ref).id.spelling).type).eltType.typeKind;                        
                    }                   
                }
            }
            //if right side is an array
            else if (expr.right.getClass().equals(new IxExpr(null, null, null).getClass())) {
                if (isSameTypeKind(((ArrayType)search(((IdRef)((IxExpr)expr.right).ref).id.spelling).type).eltType.typeKind, expr.left.type)) {
                    if (expr.operator.spelling.equals("==") ||
                        expr.operator.spelling.equals("!=") ||
                        expr.operator.spelling.equals("<") ||
                        expr.operator.spelling.equals(">") ||
                        expr.operator.spelling.equals(">=") ||
                        expr.operator.spelling.equals("<=")) {
                        expr.type = TypeKind.BOOLEAN;
                    }            
                    else {
                        expr.type = ((ArrayType)search(((IdRef)((IxExpr)expr.left).ref).id.spelling).type).eltType.typeKind;
                    }                   
                }
            }
            //if neither side is an array
            else {
                typeError(expr.posn.start, "visitBinaryExpr", "The operator " + expr.operator.spelling + " is undefined for the argument type(s) " + expr.left.type + "," + expr.right.type);
            }
        }
        return null;
    }
    
    public Object visitRefExpr(RefExpr expr) throws TypeError, IdentificationError {
        expr.ref.visit(this);      
        //length special case
        if (expr.ref.getClass().equals(new QualRef(null, null, null).getClass())) {
            Reference ref = expr.ref;            
            while (ref.getClass().equals(new QualRef(null, null, null).getClass())) {
                ref = ((QualRef)ref).ref; 
            } 
            if (((IdRef)ref).id.spelling.equals("length")) {                
                expr.type = TypeKind.INT;
                expr.ref.decl = new FieldDecl(false, true, new BaseType(TypeKind.INT, new SourcePosition(0, 0)), "length", new SourcePosition(0, 0));
                return null;
            }
        }
        //if a this reference           
        if (expr.ref.getClass().equals(new ThisRef(null).getClass())) {                                      
            expr.type = TypeKind.CLASS;
        }                    
        //if the reference is a qualified reference
        else if (expr.ref.getClass().equals(new QualRef(null, null, null).getClass())) {                                    
            if (searchAllMembers(((QualRef)expr.ref).id.spelling) != null) {  
                //check for private membership 
                if (searchAllMembers(((QualRef)expr.ref).id.spelling).getClass().equals(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)), new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)).getClass()) ||
                    searchAllMembers(((QualRef)expr.ref).id.spelling).getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass())) {                        
                        if (((MemberDecl)searchAllMembers(((QualRef)expr.ref).id.spelling)).isPrivate) {
                        if (((MemberDecl)findMember(((QualRef)expr.ref).id.spelling)) != null) {
                            //if the field is a member of an instance of the current class
                            expr.type = findMember(((QualRef)expr.ref).id.spelling).type.typeKind;                            
                        }
                        else {
                            identificationError(expr.posn.start, "visitRefExpr", "The field " + ((MemberDecl)searchAllMembers(((QualRef)expr.ref).id.spelling)).name + " is not visible");
                        }
                    }
                    else {
                        expr.type = searchAllMembers(((QualRef)expr.ref).id.spelling).type.typeKind;    
                    }
                } 
                else {
                    expr.type = searchAllMembers(((QualRef)expr.ref).id.spelling).type.typeKind;    
                }                                                                        
                return null;                                        
            }
            else {
                identificationError(expr.posn.start, "visitRefExpr", "variable " + ((QualRef)expr.ref).id.spelling + " was not initialized");
            }
        }
        //if the reference is an id reference
        else if (expr.ref.getClass().equals(new IdRef(null, null).getClass())) {
            //checking for initialization
            if (expr.ref.decl.name.equals(this.varName)) {
                typeError(expr.posn.start, "visitRefExpr", "The local variable " + expr.ref.decl.name + " may not have been initialized");
            }  
            //checking for static reference 
            if (this.methodStatic && 
            (expr.ref.decl.getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass()) ||
            expr.ref.decl.getClass().equals(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)), new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)).getClass())) &&
            search(((IdRef)expr.ref).id.spelling) != null) {
                if (!((MemberDecl)expr.ref.decl).isStatic) {
                    typeError(expr.posn.start, "visitRefExpr", "Cannot make a static reference to the non-static field " + ((IdRef)expr.ref).id.spelling);
                }
            }    

            if (search(((IdRef)expr.ref).id.spelling) != null) {                
                if (!(search(((IdRef)expr.ref).id.spelling).getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass()) ||
                    search(((IdRef)expr.ref).id.spelling).getClass().equals(new ParameterDecl(null, null, null).getClass()) ||
                    search(((IdRef)expr.ref).id.spelling).getClass().equals(new VarDecl(null, null, null).getClass()))) {
                        typeError(expr.posn.start, "visitRefExpr", ((IdRef) expr.ref).id.spelling + " cannot be resolved to a variable");
                }
                else {
                    expr.type = search(((IdRef)expr.ref).id.spelling).type.typeKind;
                    return null;
                }           
            }
            else {
                identificationError(expr.posn.start, "visitRefExpr", "variable " + ((IdRef)expr.ref).id.spelling + " was not initialized");
            }
        }        
        return null;
    }
    
    public Object visitIxExpr(IxExpr ie) throws TypeError, IdentificationError {
        ie.ref.visit(this);
        ie.ixExpr.visit(this);
        if (isSameTypeKind(ie.ixExpr.type, TypeKind.INT)) {
            if (ie.ref.decl.type.getClass().equals(new ArrayType(null, null).getClass())) {
                ie.type = ((ArrayType)ie.ref.decl.type).eltType.typeKind;                
            }  
            else {
                typeError(ie.posn.start, "visitIxExpr", "The type of the expression must be an array type but it resolved to " + ie.ref.decl.type.typeKind);
            }                                
        }
        else {
            typeError(ie.posn.start, "visitIxExpr", "Type mismatch: cannot convert from " + ie.ixExpr.type + " to int");
        }                                
        return null;
    }
    
    public Object visitCallExpr(CallExpr expr) throws TypeError, IdentificationError {
        this.methodRef = true;
        expr.functionRef.visit(this);
        this.methodRef = false;
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this);
        }                  
        Reference ref = expr.functionRef;            
        //if parameter list types and lengths match
        if (((MethodDecl)ref.decl).parameterDeclList.size() != expr.argList.size()) {
            identificationError(expr.posn.start, "visitCallExpr", "length of supplied arguments " + expr.argList.size() + "does not match the method declaration size " + ((MethodDecl)searchAllMembers((((IdRef)expr.functionRef).id.spelling))).parameterDeclList.size());
        }
        else {
            ParameterDeclList params = ((MethodDecl)ref.decl).parameterDeclList;
            int counter = 0;
            for (Expression e : expr.argList) {
                if (!isSameTypeKind(e.type, params.get(counter).type.typeKind)) {                    
                    identificationError(expr.posn.start, "visitCallExpr", "parameter at position " + (counter + 1) + " does not match the declaration type");
                }
                counter++;
            }
        }
        expr.type = searchAllMembers(ref.decl.name).type.typeKind;
        return null;     
    }
    
    public Object visitLiteralExpr(LiteralExpr expr) throws TypeError, IdentificationError {        
        expr.lit.visit(this);          
        expr.type = expr.lit.type;
        return null;      
    }

    public Object visitNewObjectExpr(NewObjectExpr expr) throws TypeError, IdentificationError {
        expr.classtype.visit(this);
        expr.type = TypeKind.CLASS;
        return null;
    }

    public Object visitNewArrayExpr(NewArrayExpr expr) throws TypeError, IdentificationError {
        expr.eltType.visit(this);
        expr.sizeExpr.visit(this);
        if (isSameTypeKind(expr.sizeExpr.type, TypeKind.INT)) {            
            expr.type = TypeKind.ARRAY;
        }
        else {
            typeError(expr.posn.start, "visitNewArrayExpr", "Type mismatch: cannot convert from " + expr.sizeExpr.type + " to int");
        }
        return null;        
    }
    
    public Object visitNewStringExpr(NewStringExpr expr) throws TypeError, IdentificationError {
        expr.sizeExpr.visit(this);
        expr.type = TypeKind.UNSUPPORTED;  
        return null;      
    }
    
    public Object visitNullExpr(NullExpr expr) throws TypeError, IdentificationError {
        expr.type = TypeKind.ERROR;
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // REFERENCES
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public Object visitThisRef(ThisRef ref) throws TypeError, IdentificationError {        
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
        return null;
    }
    
    public Object visitIdRef(IdRef ref) throws TypeError, IdentificationError {        
        ref.id.visit(this); 
        //length special case
        if (ref.id.spelling.equals("length")) {
            ref.decl = new FieldDecl(false, true, new BaseType(TypeKind.INT, new SourcePosition(0, 0)), "length", new SourcePosition(0, 0));
            return null;
        }       
        // if identifier is a defined within the current class
        if (search(ref.id.spelling) != null) {                                                              
            ref.decl = search(ref.id.spelling);                                                     
        } 
        //if the identifier is a member of another class       
        else if (searchAllMembers(ref.id.spelling) != null) { 
            ref.decl = searchAllMembers(ref.id.spelling);            
        }    
        else {
            displayAllMembers();
            identificationError(ref.posn.start, "visitIdRef", "Variable " + ref.id.spelling + " may not have been initialized");
        }                      
        return null;        
    }
        
    public Object visitQRef(QualRef qr) throws TypeError, IdentificationError {         
        qr.ref.visit(this);
        qr.id.visit(this);
        String classname = null;
        String membername = null; 
        if(qr.ref.decl.type.getClass().equals(new ClassType(null, null).getClass())) {
            classname = ((ClassType)qr.ref.decl.type).className.spelling;
            membername = qr.id.spelling;
        }
        else if (qr.ref.decl.type.getClass().equals(new ArrayType(null, null).getClass())) {
            classname = qr.ref.decl.name;
            membername = qr.id.spelling;
        }  
        else {
            identificationError(qr.ref.posn.start, "visitQRef", "The primitive type " + search(qr.id.spelling).type.typeKind + " of " + qr.ref.decl.name + " does not have a field " + qr.id.spelling);
        }           
        
        //length special case, create length field
        if (qr.ref.getClass().equals(new IdRef(null, null).getClass())) {                       
            if (qr.id.spelling.equals("length") && qr.ref.decl.type.getClass().equals(new ArrayType(null, null).getClass())) {
                qr.ref.decl = new FieldDecl(false, true, new BaseType(TypeKind.INT, new SourcePosition(0, 0)), "length", new SourcePosition(0, 0));                 
                return null;
            }
        } 

        if (getMemberInClass(classname, membername) != null) {
            qr.decl = getMemberInClass(classname, membername);
        }

        // //if finding a method ref
        // if (this.methodRef) {            
        //     if (!qr.ref.decl.type.getClass().equals(new ClassType(null, null).getClass()) &&
        //         !this.classNamesInclusive.contains(qr.ref.decl.name)) {
        //     } 
        //     if (getMemberInClass(classname, qr.id.spelling) != null) {                
        //         qr.decl = getMemberInClass(classname, membername);
        //     }
        //     else {
        //         identificationError(qr.posn.start, "visitQRef", "Class member " + membername + " must be declared static");
        //     }
        //     return null;
        // }

        // //if the qref is a this reference        
        // if (qr.ref.getClass().equals(new ThisRef(new SourcePosition()).getClass())) {  
        //     Declaration temp = getMemberInClass(this.className, qr.id.spelling);      
        //     if (temp == null) {
        //         identificationError(qr.posn.start, "visitQRef", qr.id.spelling + " cannot be resolved or is not a field");
        //     }
        //     if (!methodStatic) {
        //         qr.decl = temp;
        //     }
        //     else {
        //         identificationError(qr.posn.start, "visitQRef", "Cannot use this in a static context");
        //     }            
        // } 
        // //if the reference is a variable, parameter or member declaration of the current class
        // else if (search(qr.id.spelling) != null) {                                                                              
        //     if (search(qr.id.spelling).getClass().equals(new VarDecl(null, null, null).getClass()) ||
        //         search(qr.id.spelling).getClass().equals(new ParameterDecl(null, null, null).getClass())) {   
        //         //primitive items can not have qualified references
        //         if (!qr.ref.decl.type.getClass().equals(new ClassType(null, null).getClass()) &&
        //             !search(qr.id.spelling).type.getClass().equals(new ArrayType(null, null).getClass())) {                        
        //             identificationError(qr.ref.posn.start, "visitQRef", "The primitive type " + search(qr.ref.decl.name).type.typeKind + " of " + qr.ref.decl.name + " does not have a field " + qr.id.spelling);
        //         }    
        //         qr.decl = getMemberInClass(this.className, qr.id.spelling);                                               
        //     }   
        //     // if the member is a class member
        //     else if (search(qr.id.spelling).getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass()) ||
        //              search(qr.id.spelling).getClass().equals(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)), new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)).getClass())) {                          
        //                 //primitive items can not have qualified references
        //         if (!qr.ref.decl.type.getClass().equals(new ClassType(null, null).getClass()) &&
        //             !this.classNamesInclusive.contains(qr.ref.decl.name)) {;
        //             identificationError(qr.ref.posn.start, "visitQRef", "The primitive type " + search(qr.id.spelling).type.typeKind + " of " + qr.ref.decl.name + " does not have a field " + qr.id.spelling);
        //         }            
        //         if (((MemberDecl)searchAllMembers(qr.id.spelling)).isStatic ||
        //             qr.ref.decl.getClass().equals(new ParameterDecl(null, null, null).getClass()) ||
        //             qr.ref.decl.getClass().equals(new VarDecl(null, null, null).getClass()) ||
        //             qr.ref.decl.getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass())) {
        //             qr.decl = getMemberInClass(this.className, qr.id.spelling);                   
        //         }
        //         else {
        //             identificationError(qr.posn.start, "visitQRef", "Class member " + qr.id.spelling+ " must be declared static");
        //         }                          
        //     }                          
        // }
        // //if reference is member of another class
        // else if (searchAllMembers(qr.id.spelling) != null) { 
        //     //check for invalid refs                   
        //     if (searchAllMembers(qr.id.spelling).getClass().equals(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)), new ParameterDeclList(), new StatementList(), new SourcePosition(0, 0)).getClass()) ||
        //         searchAllMembers(qr.id.spelling).getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass())) {
        //         qr.decl = searchAllMembers(qr.id.spelling);
        //     }                             
        // }    
        return null;      
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TERMINALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    public Object visitIdentifier(Identifier id) throws TypeError, IdentificationError {
        id.decl = searchAllMembers(id.spelling);
        return null;
    }
    
    public Object visitOperator(Operator op) throws TypeError, IdentificationError {
        return null;
    }
    
    public Object visitIntLiteral(IntLiteral num) throws TypeError, IdentificationError {
        num.type = TypeKind.INT;
        return null;
    }

    public Object visitNullLiteral(NullLiteral nul) throws TypeError, IdentificationError {
        nul.type = TypeKind.ERROR;
        return null;
    }
    
    public Object visitBooleanLiteral(BooleanLiteral bool) throws TypeError, IdentificationError {
        bool.type = TypeKind.BOOLEAN;
        return null;
    }

    public void visitStringLiteral(StringLiteral StringLiteral) throws TypeError, IdentificationError {
        StringLiteral.type = TypeKind.STRING;
    }
}