/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.CodeGenerator;

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
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.StringLiteral;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;
/**
 * An implementation of the Visitor interface provides a method visitX
 * for each non-abstract AST class X.  
 */
public interface ASTCodeGen<ArgType,ResultType> {

  // Package
    public ResultType visitPackage(Package prog);

  // Declarations
    public ResultType visitClassDecl(ClassDecl cd);
    public ResultType visitFieldDecl(FieldDecl fd);
    public ResultType visitMethodDecl(MethodDecl md);
    public ResultType visitParameterDecl(ParameterDecl pd);
    public ResultType visitVarDecl(VarDecl decl);
 
  // Types
    public ResultType visitBaseType(BaseType type);
    public ResultType visitClassType(ClassType type);
    public ResultType visitArrayType(ArrayType type);
    
  // Statements
    public ResultType visitBlockStmt(BlockStmt stmt);
    public ResultType visitVardeclStmt(VarDeclStmt stmt);
    public ResultType visitAssignStmt(AssignStmt stmt);
    public ResultType visitIxAssignStmt(IxAssignStmt stmt);
    public ResultType visitCallStmt(CallStmt stmt);
    public ResultType visitReturnStmt(ReturnStmt stmt);
    public ResultType visitIfStmt(IfStmt stmt);
    public ResultType visitWhileStmt(WhileStmt stmt);
    
  // Expressions
    public ResultType visitUnaryExpr(UnaryExpr expr);
    public ResultType visitBinaryExpr(BinaryExpr expr);
    public ResultType visitRefExpr(RefExpr expr);
    public ResultType visitIxExpr(IxExpr expr);
    public ResultType visitCallExpr(CallExpr expr);
    public ResultType visitLiteralExpr(LiteralExpr expr);
    public ResultType visitNewObjectExpr(NewObjectExpr expr);
    public ResultType visitNewStringExpr(NewStringExpr expr);
    public ResultType visitNewArrayExpr(NewArrayExpr expr);
    public ResultType visitNullExpr(NullExpr expr);
    
  // References
    public ResultType visitThisRef(ThisRef ref);
    public ResultType visitIdRef(IdRef ref);
    public ResultType visitQRef(QualRef ref);

  // Terminals
    public ResultType visitIdentifier(Identifier id);
    public ResultType visitOperator(Operator op);
    public ResultType visitIntLiteral(IntLiteral num);
    public ResultType visitBooleanLiteral(BooleanLiteral bool);

	public ResultType visitNullLiteral(NullLiteral nullLiteral);

	public ResultType visitStringLiteral(StringLiteral stringLiteral);
}
