/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.CodeGenerator;

import miniJava.AbstractSyntaxTrees.AssignStmt;
import mJAM.Disassembler;
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
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;


public class ASTCodeGen implements Generator<Object> {
	
    public void generate(String inputFileName, AST ast) {
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        Machine.initCodeGen();
		
	    //generate call to main
		Machine.emit(Op.LOADL,0);            			// array length 0
		Machine.emit(Prim.newarr);           			// empty String array argument
		int mainCallAddr = Machine.nextInstrAddr(); 	// record instr addr where main is called                                                // "main" is called
		Machine.emit(Op.CALL,Reg.CB,-1);     			// static call main (address to be patched)
		Machine.emit(Op.HALT,0,0,0);         			// end execution
		                           			
	    //write code to object code file (.mJAM)
		ObjectFile objF = new ObjectFile(objectCodeFileName);
		System.out.print("Writing object code file " + objectCodeFileName + " ... ");
		if (objF.write()) {
			System.out.println("FAILED!");
			return;
		}
		else {
            System.out.println("SUCCEEDED");
        }
						
        // create asm file using disassembler (.asm)
        System.out.print("Writing assembly file " + objectCodeFileName + " ... ");
        Disassembler d = new Disassembler(objectCodeFileName);
        if (d.disassemble()) {
            System.out.println("FAILED!");
            return;
        }
        else {
            System.out.println("SUCCEEDED");
        }                
        
        //run code using debugger
        String asmCodeFileName = objectCodeFileName.replace(".mJAM",".asm");
        System.out.println("Running code in debugger ... ");
        Interpreter.debug(objectCodeFileName, asmCodeFileName);
        System.out.println("*** mJAM execution completed");
    }
    
    ///////////////////////////////////////////////////////////////////////////////
	//
	// PACKAGE
	//
	///////////////////////////////////////////////////////////////////////////////
 
    public Object visitPackage(Package prog){
        for (ClassDecl c: prog.classDeclList){
            c.generate(this);
        }
        return null;
    }
    
	///////////////////////////////////////////////////////////////////////////////
	//
	// DECLARATIONS
	//
	///////////////////////////////////////////////////////////////////////////////
    
    public Object visitClassDecl(ClassDecl clas){
        for (FieldDecl f: clas.fieldDeclList) {
            f.generate(this);
        }
        for (MethodDecl m: clas.methodDeclList) {
        	m.generate(this); 
        }
        return null;
    }
    
    public Object visitFieldDecl(FieldDecl f){
    	f.generate(this);
        return null;
    }
    
    public Object visitMethodDecl(MethodDecl m){
    	m.generate(this);
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {
            pd.generate(this);
        }
        StatementList sl = m.statementList;
        for (Statement s: sl) {
            s.generate(this);
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
        if (stmt.initExp != null) {
            stmt.initExp.generate(this);
        }        
        return null;
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
        stmt.cond.generate(this);
        stmt.body.generate(this);
        return null;
    }
    

	///////////////////////////////////////////////////////////////////////////////
	//
	// EXPRESSIONS
	//
	///////////////////////////////////////////////////////////////////////////////

    public Object visitUnaryExpr(UnaryExpr expr){
        expr.operator.generate(this);
        expr.expr.generate(this);
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
        return null;
    }
    
    public Object visitNullExpr(NullExpr expr) {
        return null;
    }

	///////////////////////////////////////////////////////////////////////////////
	//
	// REFERENCES
	//
	///////////////////////////////////////////////////////////////////////////////
	
    public Object visitThisRef(ThisRef ref) {
    	return null;
    }
    
    public Object visitIdRef(IdRef ref) {
    	ref.id.generate(this);
    	return null;
    }
        
    public Object visitQRef(QualRef qr) {
    	qr.id.generate(this);
    	qr.ref.generate(this);
	    return null;
    }
      
    
	///////////////////////////////////////////////////////////////////////////////
	//
	// TERMINALS
	//
	///////////////////////////////////////////////////////////////////////////////
    
    public Object visitIdentifier(Identifier id){
        return null;
    }
    
    public Object visitOperator(Operator op){
        return null;
    }
    
    public Object visitIntLiteral(IntLiteral num){
        return null;
    }
    
    public Object visitBooleanLiteral(BooleanLiteral bool){
        return null;
    }

    public Object visitNullLiteral(NullLiteral nullLiteral) {
        return null;
    }

    public Object visitStringLiteral(StringLiteral stringLiteral) {
        return null;
    }
}
