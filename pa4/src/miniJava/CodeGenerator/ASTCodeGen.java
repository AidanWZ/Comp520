/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.CodeGenerator;

import miniJava.AbstractSyntaxTrees.AssignStmt;

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
import miniJava.SyntacticAnalyzer.SourcePosition;


public class ASTCodeGen implements Generator<Object> {

    private String inputFileName;
    private AST ast;
    private int mainCallAddr;
    private Machine machine;
    private int declarationLevel;
    private short frameSpaceUsed;
    private int numArgsUsed;
    
    public ASTCodeGen(String inputFileName, AST ast) {
        this.inputFileName = inputFileName;
        this.ast = ast;
        this.declarationLevel = 0;
        this.frameSpaceUsed = 0;
        this.numArgsUsed = 0;
        //this.Machine = new Machine();
    }

    public void writeToMjam() {
        //write code to object code file (.mJAM)
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
		ObjectFile objF = new ObjectFile(objectCodeFileName);
		System.out.print("Writing object code file " + objectCodeFileName + " ... ");
		if (objF.write()) {
			System.out.println("FAILED!");
			return;
		}
		else {
            System.out.println("SUCCEEDED");
        }
    }

    public void writeToAsm() {
        // create asm file using disassembler (.asm)
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        System.out.print("Writing assembly file " + objectCodeFileName + " ... ");
        Disassembler d = new Disassembler(objectCodeFileName);
        if (d.disassemble()) {
            System.out.println("FAILED!");
            return;
        }
        else {
            System.out.println("SUCCEEDED");
        }
    }

    public void runCode() {
        //run code using debugger
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        String asmCodeFileName = objectCodeFileName.replace(".mJAM",".asm");
        System.out.println("Running code in debugger ... ");
        Interpreter.debug(objectCodeFileName, asmCodeFileName);
        System.out.println("*** mJAM execution completed");
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
            if (cd.name == className) {
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

    public void patch(int instuctionAddr, int offSetFromCodeBase) {
        Machine.code[instuctionAddr].d = offSetFromCodeBase;
    }

    public void init() {
        //init CB
        Machine.initCodeGen();
        // Empty string array length 0
        Machine.emit(Op.LOADL,0);   
        Machine.emit(Prim.newarr);  
        // record instr addr where main is called         			
        mainCallAddr = Machine.nextInstrAddr(); 	
        return;
    }

    public void generate() {
        //init CB
        init();
	    //generate call to main
        Machine.emit(Op.CALL,Reg.CB,-1);
        // end execution
        Machine.emit(Op.HALT,0,0,0); 
        //generate code for rest of package
        visitPackage((Package)ast);
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
            c.entity = new ClassRep(classOffset);
            classOffset++;
        }
        return null;
    }

    public Object visitClassDecl(ClassDecl clas){
        int fieldOffset = 0;
        for (FieldDecl f: clas.fieldDeclList) {
            f.generate(this);
            f.entity = new FieldRep(fieldOffset, 1);
            fieldOffset++;
        }
        int methodOffset = 0;
        for (MethodDecl m: clas.methodDeclList) {
            m.generate(this); 
            m.entity = new MethodRep(methodOffset);
            methodOffset++;
        }
        return null;
    }
    
    public Object visitFieldDecl(FieldDecl f) {
        if (f.isStatic) {
            Machine.emit(Machine.Op);
        }
        return null;
    }
    
    public Object visitMethodDecl(MethodDecl m){
        for (Statement s: m.statementList) {
            s.generate(this);
        }
        if (isSameTypeKind(m.type.typeKind, TypeKind.VOID)) {
            Machine.emit(Machine.Op.RETURN, 0, 0, m.parameterDeclList.size());
        }
        else {
            Machine.emit(Machine.Op.RETURN, 0, 0, m.parameterDeclList.size());
        }
        
        return null;
    }
    
    public Object visitParameterDecl(ParameterDecl pd){
        pd.generate(this);
        return null;
    } 
    
    public Object visitVarDecl(VarDecl vd){
        vd.generate(this);
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
    
    public Object visitClassType(ClassType ct){
        ct.className.generate(this);
        return null;
    }
    
    public Object visitArrayType(ArrayType type){
        type.generate(this);
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
        	s.generate(this);
        }
        return null;
    }
    
    public Object visitVardeclStmt(VarDeclStmt stmt){         
        stmt.varDecl.generate(this);        
        short s = stmt.varDecl.entity.size;
        short gs = this.frameSpaceUsed;
        this.frameSpaceUsed += s;
        Machine.emit(Machine.Op.PUSH, 0, 0, s);
        stmt.varDecl.entity = new KnownAddress(declarationLevel, gs);
        return new Short(s);
    }
    
    public Object visitAssignStmt(AssignStmt stmt){
        stmt.ref.generate(this);
        stmt.val.generate(this);
        return null;
    }
    
    public Object visitIxAssignStmt(IxAssignStmt stmt){
        stmt.ref.generate(this);
        stmt.ix.generate(this);
        stmt.exp.generate(this);
        return null;
    }
        
    public Object visitCallStmt(CallStmt stmt){
        stmt.methodRef.generate(this);
        ExprList al = stmt.argList;
        for (Expression e: al) {
            e.generate(this);
        }
        return null;
    }
    
    public Object visitReturnStmt(ReturnStmt stmt){
         if (stmt.returnExpr != null)
            stmt.returnExpr.generate(this);
        return null;
    }
    
    public Object visitIfStmt(IfStmt stmt){
        stmt.cond.generate(this);
        stmt.thenStmt.generate(this);
        if (stmt.elseStmt != null)
            stmt.elseStmt.generate(this);
        return null;
    }
    
    public Object visitWhileStmt(WhileStmt stmt){
        int j = Machine.nextInstrAddr();
        Machine.emit(Machine.Op.JUMP, Machine.Reg.CB, 0); // patchme
        int g = Machine.nextInstrAddr();
        stmt.body.generate(this);
        int h = Machine.nextInstrAddr();
        patch(j, h);
        stmt.cond.generate(this);
        Machine.emit(Machine.Op.JUMPIF, Machine.Reg.CB, g);                
        return null;
    }
    

	///////////////////////////////////////////////////////////////////////////////
	//
	// EXPRESSIONS
	//
	///////////////////////////////////////////////////////////////////////////////

    public Object visitUnaryExpr(UnaryExpr expr){        
        expr.expr.generate(this);
        int op;
        if (expr.operator.spelling.equals("!")) {
            op = Machine.primToInt(Machine.Prim.not);
            Machine.emit(Machine.Prim.not);
        }
        else if (expr.operator.spelling.equals("-")) {
            op = Machine.primToInt(Machine.Prim.neg);
            Machine.emit(Machine.Prim.neg);
        }
        return null;
    }
    
    public Object visitBinaryExpr(BinaryExpr expr){
        expr.operator.generate(this);
        expr.left.generate(this);
        expr.right.generate(this);
        return null;
    }
    
    public Object visitRefExpr(RefExpr expr){
        expr.ref.generate(this);
        return null;
    }
    
    public Object visitIxExpr(IxExpr ie){
        ie.ref.generate(this);
        ie.ixExpr.generate(this);
        return null;
    }
    
    public Object visitCallExpr(CallExpr expr){
        expr.functionRef.generate(this);
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.generate(this);
        }
        return null;
    }
    
    public Object visitLiteralExpr(LiteralExpr expr){
        expr.lit.generate(this);
        if (expr.lit.getClass().equals(new IntLiteral(null).getClass())) {
            expr.entity = new KnownValue(value)
        }
        else if (expr.lit.getClass().equals(new BooleanLiteral(null).getClass())) {
            expr.entity = new KnownValue(bool)
        }
        else if (expr.lit.getClass().equals(new StringLiteral(null).getClass())) {

        }
        
        return null;
    }
 
    public Object visitNewArrayExpr(NewArrayExpr expr){
        expr.eltType.generate(this);
        expr.sizeExpr.generate(this);

        return null;
    }

    public Object visitNewStringExpr(NewStringExpr expr) {
        return null;
    }
    
    public Object visitNewObjectExpr(NewObjectExpr expr){
        expr.classtype.generate(this);
        ClassDecl cd = findClass(expr.classtype.className.spelling);
        Machine.emit(Machine.Op.LOADL, -1);
        Machine.emit(Machine.Op.LOADL, cd.fieldDeclList.size());
        Machine.emit(Machine.Prim.newobj);
        Machine.emit(Machine.Op.STORE, Machine.Reg.HT, 0);
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
        return null;
    }
    
    public Object visitIntLiteral(IntLiteral num){
        short v = Short.parseShort(num.spelling);
        Machine.emit(Machine.Op.LOADL, 0, 0, v);
        return new Short(v);
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
