/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.CodeGenerator;

import miniJava.AbstractSyntaxTrees.AssignStmt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Stack;

import mJAM.Disassembler;
import mJAM.Instruction;
import mJAM.Interpreter;
import mJAM.Machine;
import mJAM.ObjectFile;
import mJAM.Machine.*;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
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
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.StringLiteral;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.ContextualAnalyzer.ASTDisplay;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;


public class ASTCodeGen implements Generator<Object> {

    /**
     * The file name that holds the original program string
     */
    private String inputFileName;
    /**
     * the ast representing the code from teh original file
     */
    private AST ast;
    /**
     * the ast display class
     */
    private ASTDisplay astDisplay;
    /**
     * the address in the code segement from which main was called 
     */
    private int mainCallAddr;
    /**
     * the address in the code segement from which main is defined 
     */
    private int mainMethodAddr;
    /**
     * number of arguments in the current method
     */
    private int numArgs;
    /**
     * the number of args between OB and desired value
     */
    private int parameterDisplacement;
    /**
     * the level of the current routine in the execution stack
     */
    private int declarationLevel;
    /**
     * the amount of space used in the current routine (with linkage)
     */
    private short frameSpaceUsed;
    /**
     * the amount of space used in the global segment
     */
    private short stackSpaceUsed;
    
    public ASTCodeGen(String inputFileName, AST ast) {
        this.inputFileName = inputFileName;
        this.ast = ast;
        this.astDisplay = new ASTDisplay();
        this.parameterDisplacement = 0;
        this.declarationLevel = 0;
        this.frameSpaceUsed = 0;
        this.stackSpaceUsed = 0;
    }

    public void writeToMjam() {
        //write code to object code file (.mJAM)
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
		ObjectFile objF = new ObjectFile(objectCodeFileName);
        System.out.println();
        System.out.print("Writing object code file " + objectCodeFileName + " ... ");        
        if (objF.write()) {
			System.out.println("FAILED!");			
		}
		else {
            System.out.println("SUCCEEDED");
        }        
        return;
    }

    public void writeToAsm() {
        // create asm file using disassembler (.asm)
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        System.out.print("Writing assembly file " + objectCodeFileName + " ... ");
        Disassembler d = new Disassembler(objectCodeFileName);
        if (d.disassemble()) {
            System.out.println("FAILED!");
        }
        else {
            System.out.println("SUCCEEDED");
        }
        System.out.println();
        return;
    }

    public void runCode() {
        System.out.println("********** mJAM Interpreter (Version 2.3) **********");
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        Interpreter.interpret(objectCodeFileName);		
    }

    public void runCodeDebug() {
        //run code using debugger
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        String asmCodeFileName = objectCodeFileName.replace(".mJAM",".asm");
        System.out.println("Running code in debugger ... ");
        Interpreter.debug(objectCodeFileName, asmCodeFileName);
        System.out.println("*** mJAM execution completed");
    }

    public void displayAST() {
        astDisplay.showTree(this.ast);
    }

    public boolean isMain(MethodDecl candidate) {
        if (candidate.name.equals("main")) {
            if (candidate.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {
                if (((MethodDecl) candidate).parameterDeclList.size() == 1 && !((MethodDecl) candidate).isPrivate && ((MethodDecl) candidate).isStatic) {                                        
                    if (((MethodDecl) candidate).parameterDeclList.get(0).type.getClass().equals(new ArrayType(new BaseType(TypeKind.STRING, new SourcePosition()), new SourcePosition()).getClass())) {                        
                        if (isSameTypeKind(((ArrayType)((MethodDecl) candidate).parameterDeclList.get(0).type).eltType.typeKind, TypeKind.STRING)) {                            
                            return true;
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

    public ClassDecl findClass(String className) {
        for (ClassDecl cd: ((Package) ast).classDeclList) {
            if (cd.name.equals(className)) {
                return cd;
            }
        }
        return null;
    }

    public Declaration findMain() {
        for (ClassDecl clas: ((Package)ast).classDeclList) {
            for (MethodDecl m: clas.methodDeclList) {
                Declaration candidate = m;
                if (candidate.name.equals("main")) {
                    if (candidate.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {
                        if (((MethodDecl) candidate).parameterDeclList.size() == 1 && !((MethodDecl) candidate).isPrivate && ((MethodDecl) candidate).isStatic) {                                        
                            if (((MethodDecl) candidate).parameterDeclList.get(0).type.getClass().equals(new ArrayType(new BaseType(TypeKind.STRING, new SourcePosition()), new SourcePosition()).getClass())) {                        
                                if (isSameTypeKind(((ArrayType)((MethodDecl) candidate).parameterDeclList.get(0).type).eltType.typeKind, TypeKind.STRING)) {                            
                                    return candidate;
                                }
                            }                    
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public void generate() {
        //init CB
        Machine.initCodeGen();
        // Empty string array length 0
        Machine.emit(Op.LOADL,0);   
        // call new array primitive
        Machine.emit(Prim.newarr);  
        // record instr addr where main is called         			
        mainCallAddr = Machine.nextInstrAddr(); 
	    //generate call to main
        Machine.emit(Op.CALL,Reg.CB,-1);
        // end execution
        Machine.emit(Op.HALT,0,0,0); 
        //generate code for rest of package
        visitPackage((Package)ast);
        //patch main address 
        Machine.patch(mainCallAddr, mainMethodAddr);
        //write to files
        writeToMjam();
        writeToAsm();
        //run code for testing
        runCode();
        return;               			
    }
    
	///////////////////////////////////////////////////////////////////////////////
	//
	// DECLARATIONS
	//
	///////////////////////////////////////////////////////////////////////////////
    
    public Object visitPackage(Package prog) {
        int classOffset = 0;
        for (ClassDecl c: prog.classDeclList) {
            c.generate(this);
            c.entity = new ClassRep(classOffset, c.fieldDeclList.size());
            classOffset+= c.fieldDeclList.size();
        }
        return null;
    }

    public Object visitClassDecl(ClassDecl clas){
        // creat runtime entities for fields
        int fieldOffset = 0;
        for (FieldDecl f: clas.fieldDeclList) {
            // save offset in class in runtime description
            f.entity = new FieldRep(fieldOffset, 1);
            //evaluate field declaration
            f.generate(this);
            fieldOffset++;
        }
        //create runtime entities for methods
        for (MethodDecl m: clas.methodDeclList) {
            //set address of routine in method runtime entity
            m.generate(this);           
        }
        return null;
    }
    
    public Object visitFieldDecl(FieldDecl f) {
        //if static field add to class object in global segment
        f.type.generate(this);
        if (f.isStatic) { 
            ((FieldRep) f.entity).offsetFromSB = stackSpaceUsed;         
            stackSpaceUsed++;
        }
        return null;
    }
    
    public Object visitMethodDecl(MethodDecl m){
        if (isMain(m)) {
            this.mainMethodAddr = Machine.nextInstrAddr();
        }
        //create new runtime entity
        m.entity = new MethodRep(Machine.nextInstrAddr()-1); 
        //save number of arguments
        this.numArgs = m.parameterDeclList.size();  
        //add one to procedure level
        this.declarationLevel++;       
        //reset local framespace used to zero for new parameters and variables
        this.frameSpaceUsed = 0;
        //set address of stored argument values above object base on stack        
        this.parameterDisplacement = numArgs*-1;
        for (ParameterDecl d: m.parameterDeclList) {
            d.generate(this);            
            this.parameterDisplacement++;
        }
        //generate code for method body
        for (Statement s: m.statementList) {
            s.generate(this);
        }
        //emit return instruction to previous routine object base
        if (isSameTypeKind(m.type.typeKind, TypeKind.VOID)) {
            if (m.name != "println") {
                Machine.emit(Machine.Op.RETURN, 0, 0, m.parameterDeclList.size());
            }                        
        } 
        //remove procedure level
        this.declarationLevel--;
        //reset parameter displacement counter
        this.parameterDisplacement = 0;   
        this.numArgs = 0;
        return null;
    }
    
    public Object visitParameterDecl(ParameterDecl pd){
        pd.type.generate(this);        
        pd.entity= new KnownAddress(this.declarationLevel, this.parameterDisplacement);
        return null;
    } 
    
    public Object visitVarDecl(VarDecl vd){
        vd.type.generate(this);
        vd.entity = new KnownAddress(this.declarationLevel, this.frameSpaceUsed);
        return null;
    }
 
	
	///////////////////////////////////////////////////////////////////////////////
	//
	// TYPES (nothing to do here)
	//
	///////////////////////////////////////////////////////////////////////////////
    
    public Object visitBaseType(BaseType type){
        return null;
    }
    
    public Object visitClassType(ClassType ct){
        ct.className.generate(this);
        return null;
    }
    
    public Object visitArrayType(ArrayType type){
        type.eltType.generate(this);
        return null;
    }
    

	///////////////////////////////////////////////////////////////////////////////
	//
	// STATEMENTS
	//
	///////////////////////////////////////////////////////////////////////////////

    public Object visitBlockStmt(BlockStmt stmt){
        StatementList sl = stmt.sl;;
        for (Statement s: sl) {
            //generate code for each statement
        	s.generate(this);
        }
        return null;
    }
    
    public Object visitVardeclStmt(VarDeclStmt stmt){ 
        //create runtime entity for variable        
        stmt.varDecl.generate(this); 
        if (stmt.initExp != null) {
            stmt.initExp.generate(this);
        }       
        int s = stmt.varDecl.entity.size;
        this.frameSpaceUsed += s;
        return null;
    }
    
    public Object visitAssignStmt(AssignStmt stmt){
        int address = 0;
        if (stmt.ref.decl.getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass())) {
            address = ((FieldRep)stmt.ref.decl.entity).offsetFromSB;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.OB, address);
        }
        else if (stmt.ref.decl.getClass().equals(new ParameterDecl(null, null, null).getClass())) {
            address = ((KnownAddress)stmt.ref.decl.entity).displacement;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.OB, address);
        }
        else if (stmt.ref.decl.getClass().equals(new VarDecl(null, null, null).getClass())) {
            address = ((KnownAddress)stmt.ref.decl.entity).displacement;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        //fetch address relative to LB
        
        stmt.ref.generate(this);
        //push value of expression onto the top of the stack
        stmt.val.generate(this);
        //assign value to address
        Machine.emit(Machine.Op.STORE, 1, Machine.Reg.OB, address);
        return null;
    }
    
    public Object visitIxAssignStmt(IxAssignStmt stmt){
        //generate reference (does nothing)
        stmt.ref.generate(this);
        //fetch address of variable relative to LB
        int varAddress = ((KnownAddress)stmt.ref.decl.entity).displacement;
        //push address onto the stack
        Machine.emit(Machine.Op.PUSH, 1, Machine.Reg.LB, varAddress);                
        //push value of expression onto the stack
        stmt.exp.generate(this);        
        //push value of index expression onto the stack
        stmt.ix.generate(this);
        //add addres and offset to get new address
        Machine.emit(Machine.Prim.add);
        //store expression value at offset address 
        Machine.emit(Machine.Op.STOREI);
        return null;
    }
        
    public Object visitCallStmt(CallStmt stmt){
        //generate method reference (does nothing)
        stmt.methodRef.generate(this);
        //load arguments values onto the stack
        ExprList al = stmt.argList;
        for (Expression e: al) {
            e.generate(this);
        }
        MethodDecl method = (MethodDecl)stmt.methodRef.decl;
        //if the method call is println
        if (method.name == "println") {
            //fetch address of method ref from runtime entity
            //int offset = 0;
            //load address of method instruction onto stack
            // Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, offset);
            //print out int primitive call
            Machine.emit(Machine.Prim.putintnl);
        }
        else {
            //fetch address of method ref from runtime entity
            int offset = ((MethodRep)method.entity).routineAddress;
            //load address of method instruction onto stack
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, offset);
            //create new routine base on execution stack
            Machine.emit(Machine.Op.CALLI, Machine.Reg.CB, offset+1);
        }
        return null;
    }
    
    public Object visitReturnStmt(ReturnStmt stmt) {        
        //pop argument values off the stack
        Machine.emit(Machine.Op.POP, this.numArgs, 0, 0);
        if (stmt.returnExpr != null) {            
            // load value of return expression onto top of the stack
            stmt.returnExpr.generate(this);            
        }  
        return null;    
    }
    
    public Object visitIfStmt(IfStmt stmt){
        //generate code instruction for conditional
        stmt.cond.generate(this);
        //conditional jump statement to 
        Machine.emit(Machine.Op.JUMPIF, -1); //patchme
        //generate code for else block
        if (stmt.elseStmt == null) {
            
        }
        else {
            stmt.elseStmt.generate(this);
            //generate code for if block
            stmt.thenStmt.generate(this);
            //save address of jump instruction to be patched
            int jumpInst = Machine.nextInstrAddr();
            //jump to end of if else block
            Machine.emit(Machine.Op.JUMP, Machine.Reg.CB, 0); // patchme
            int nextInst = Machine.nextInstrAddr();
            Machine.patch(jumpInst, nextInst);
            return null;
        }
        
        
    }
    
    public Object visitWhileStmt(WhileStmt stmt){
        //saves value of current instruction address
        int j = Machine.nextInstrAddr();
        //jump to conditional of the loop
        Machine.emit(Machine.Op.JUMP, Machine.Reg.CB, 0); // patchme
        //save value of cuurent instruction address at top of the loop body
        int g = Machine.nextInstrAddr();
        //emit code for loop body
        stmt.body.generate(this);
        //save address of current instruction of conditional at j
        int h = Machine.nextInstrAddr();
        // add address to jump too for conditional h
        Machine.patch(j, h);
        //generate instruction for conditional
        stmt.cond.generate(this);
        //compare value on top of the stack with true
        Machine.emit(Machine.Op.JUMPIF, Machine.Reg.CB, g);                
        return null;
    }
    

	///////////////////////////////////////////////////////////////////////////////
	//
	// EXPRESSIONS
	//
	///////////////////////////////////////////////////////////////////////////////

    public Object visitUnaryExpr(UnaryExpr expr){        
        //loads value of expression onto stack
        int op;
        expr.expr.generate(this);
        //emit proper primitive op instruction
        if (expr.operator.spelling.equals("!")) {
            op = Machine.primToInt(Machine.Prim.not);
            Machine.emit(Machine.Prim.not);
            return new Integer(op);
        }
        else if (expr.operator.spelling.equals("-")) {
            op = Machine.primToInt(Machine.Prim.neg);
            Machine.emit(Machine.Prim.neg);
            return new Integer(op);
        }
        else {
            return null;
        }        
    }
    
    public Object visitBinaryExpr(BinaryExpr expr){
        //loads value of left onto stack        
        expr.left.generate(this);
        //loads value of right onto stack
        expr.right.generate(this);
        //emits proper primitive op instruction
        Prim op = (Prim)expr.operator.generate(this);
        Machine.emit(op);
        return null;
    }
    
    public Object visitRefExpr(RefExpr expr){
        //generate reference (does nothing)
        expr.ref.generate(this);
        //get address of reference declaration
        int address = 0;
        //load value held by reference onto the stack        
        if (expr.ref.decl.getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass())) {            
            address = ((FieldRep)expr.ref.decl.entity).offsetFromSB;
            //Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
            int offSetInClass = ((FieldRep)expr.ref.decl.entity).offsetInClass;
            if (expr.ref.getClass().equals(new QualRef(null, null, null).getClass())) {
                Machine.emit(Machine.Op.LOADL, offSetInClass);
                Machine.emit(Machine.Prim.fieldref);
            }
        }
        else if (expr.ref.decl.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {            
            address = ((MethodRep)expr.ref.decl.entity).routineAddress;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        else if (expr.ref.decl.getClass().equals(new ParameterDecl(null, null, null).getClass())) {
            address = ((KnownAddress)expr.ref.decl.entity).displacement;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        else if (expr.ref.decl.getClass().equals(new VarDecl(null, null, null).getClass())) {            
            address = ((KnownAddress)expr.ref.decl.entity).displacement;            
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        return null;
    }
    
    public Object visitIxExpr(IxExpr ie){
        //generate reference (does nothing)
        ie.ref.generate(this);
        //fetch address of reference from runtime entity
        int address = ((KnownAddress)ie.ref.decl.entity).displacement;
        //load value at address onto the stack
        Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        //load value of index expression onto the stack
        ie.ixExpr.generate(this);
        //add to get proper address
        Machine.emit(Machine.Prim.add);
        //push value held at new address onto the stack
        Machine.emit(Machine.Op.LOADI);
        return null;
    }
    
    public Object visitCallExpr(CallExpr expr){
        //generate function reference (does nothing)
        expr.functionRef.generate(this);
        //load arguments values onto the stack
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.generate(this);
        }
        MethodDecl method = (MethodDecl)expr.functionRef.decl;
        //if the method call is println
        if (method.name == "println") {
            Machine.emit(Machine.Prim.putint);
        }
        else {
            //fetch address of method ref from runtime entity
            int offset = ((MethodRep)method.entity).routineAddress;
            //load address of method instruction onto stack
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.CB, offset);
            //create new routine base on execution stack
            Machine.emit(Machine.Op.CALLI, offset);
        }
        return null;
    }
    
    public Object visitLiteralExpr(LiteralExpr expr){
        Object value = expr.lit.generate(this);
        if (expr.lit.getClass().equals(new IntLiteral(new Token(0, "", null)).getClass())) {
            expr.entity = new KnownValue(((Integer)value).intValue());
        }
        else if (expr.lit.getClass().equals(new BooleanLiteral(null).getClass())) {
            expr.entity = new KnownValue(((Boolean)value).booleanValue());
        }
        else if (expr.lit.getClass().equals(new StringLiteral(null).getClass())) {
            expr.entity = new KnownValue((String) value);
        }
        return value;
    }
 
    public Object visitNewArrayExpr(NewArrayExpr expr){
        expr.eltType.generate(this);
        if (expr.eltType.getClass().equals(new ClassType(null, null).getClass())) {
            //int classSize = getClass(((ClassType)expr.eltType).className.spelling);
            //push value of index expression onto the stack
            expr.sizeExpr.generate(this);
            //create instruction for call to new array
            Machine.emit(Machine.Prim.newarr); 
        }
        else {
            //push value of index expression onto the stack
            expr.sizeExpr.generate(this);
            //create instruction for call to new array
            Machine.emit(Machine.Prim.newarr);            
        }
        
        return null;
    }

    public Object visitNewStringExpr(NewStringExpr expr) {
        Machine.emit(Machine.Op.LOAD, 0, 0, Machine.nullRep);
        return null;
    }
    
    //load new instance onto heap
    public Object visitNewObjectExpr(NewObjectExpr expr){
        expr.classtype.generate(this);
        ClassDecl cd = findClass(expr.classtype.className.spelling);
        Machine.emit(Machine.Op.LOADL, -1);
        Machine.emit(Machine.Op.LOADL, cd.fieldDeclList.size());
        Machine.emit(Machine.Prim.newobj);
        return null;
    }
    
    public Object visitNullExpr(NullExpr expr) {
        Machine.emit(Machine.Op.LOADA, 0, 0, Machine.nullRep);
        return null;
    }

	///////////////////////////////////////////////////////////////////////////////
	//
	// REFERENCES
	//
	///////////////////////////////////////////////////////////////////////////////
	
    public Object visitThisRef(ThisRef ref) {
    	return ref.decl;
    }
    
    public Object visitIdRef(IdRef ref) {
    	ref.id.generate(this);
    	return ref.decl;
    }
        
    public Object visitQRef(QualRef qr) {
        qr.id.generate(this);
        qr.ref.generate(this);
        int address;
        if (qr.id.decl.getClass().equals(new FieldDecl(false, false, new BaseType(TypeKind.VOID, new SourcePosition(0, 0)), "println", new SourcePosition(0, 0)).getClass())) {
            address = ((FieldRep)qr.id.decl.entity).offsetFromSB;            
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        else if (qr.id.decl.getClass().equals(new MethodDecl(new FieldDecl(true, true, null, null, new SourcePosition()), new ParameterDeclList(), new StatementList(), new SourcePosition()).getClass())) {            
            address = ((MethodRep)qr.id.decl.entity).routineAddress;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        else if (qr.id.decl.getClass().equals(new ParameterDecl(null, null, null).getClass())) {
            address = ((KnownAddress)qr.id.decl.entity).displacement;
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
        else if (qr.id.decl.getClass().equals(new VarDecl(null, null, null).getClass())) {
            address = ((KnownAddress)qr.id.decl.entity).displacement + 3;            
            Machine.emit(Machine.Op.LOAD, 1, Machine.Reg.LB, address);
        }
	    return qr.decl;
    }
      
    
	///////////////////////////////////////////////////////////////////////////////
	//
	// TERMINALS
	//
	///////////////////////////////////////////////////////////////////////////////
    
    public Object visitIdentifier(Identifier id){
        return id.decl;
    }
    
    public Object visitOperator(Operator op){
        switch (op.spelling) {
            case "+":
                return Machine.Prim.add;
            case "-":
                return Machine.Prim.sub;
            case "<":
                return Machine.Prim.lt;
            case "<=":
                return Machine.Prim.le;
            case ">":
                return Machine.Prim.gt;
            case ">=":
                return Machine.Prim.ge;
            case "==":
                return Machine.Prim.eq;
            case "||":
                return Machine.Prim.or;
            case "&&":
                return Machine.Prim.and;
            case "*":
                return Machine.Prim.mult;
            case "/":
                return Machine.Prim.div;
            case "!":
                return Machine.Prim.not;
            default:
                return null;
        }        
    }
    
    public Object visitIntLiteral(IntLiteral num){
        int v = Integer.parseInt(num.spelling);
        Machine.emit(Machine.Op.LOADL, v);
        return new Integer(v);
    }
    
    public Object visitBooleanLiteral(BooleanLiteral bool){
        boolean b = Boolean.parseBoolean(bool.spelling);
        if (b) {
            Machine.emit(Machine.Op.LOADL, 0, 0, Machine.trueRep);
        }
        else {
            Machine.emit(Machine.Op.LOADL, 0, 0, Machine.falseRep);
        }
        return new Boolean(b);
    }

    public Object visitNullLiteral(NullLiteral nullLiteral) {        
        Machine.emit(Machine.Op.LOADA, 0, 0, Machine.nullRep);
        return null;
    }

    public Object visitStringLiteral(StringLiteral stringLiteral) {
        Machine.emit(Machine.Op.LOADA, 0, 0, Machine.nullRep);
        return stringLiteral.spelling;
    }
}
