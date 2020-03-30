/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.IdentificationError;
import miniJava.SyntacticAnalyzer.TypeError;

/**
 * An implementation of the Visitor interface provides a method visitX for each
 * non-abstract AST class X.
 */
public interface Traveller<ResultType> {

  // Package
    public ResultType visitPackage(Package prog) throws TypeError, IdentificationError;

  // Declarations
    public ResultType visitClassDecl(ClassDecl cd) throws TypeError, IdentificationError;
    public ResultType visitFieldDecl(FieldDecl fd) throws TypeError, IdentificationError;
    public ResultType visitMethodDecl(MethodDecl md) throws TypeError, IdentificationError;
    public ResultType visitParameterDecl(ParameterDecl pd) throws TypeError, IdentificationError;
    public ResultType visitVarDecl(VarDecl decl) throws TypeError, IdentificationError;
 
  // Types
    public ResultType visitBaseType(BaseType type) throws TypeError, IdentificationError;
    public ResultType visitClassType(ClassType type) throws TypeError, IdentificationError;
    public ResultType visitArrayType(ArrayType type) throws TypeError, IdentificationError;
    
  // Statements
    public ResultType visitBlockStmt(BlockStmt stmt) throws TypeError, IdentificationError;
    public ResultType visitVardeclStmt(VarDeclStmt stmt) throws TypeError, IdentificationError, IdentificationError;
    public ResultType visitAssignStmt(AssignStmt stmt) throws TypeError, IdentificationError;
    public ResultType visitIxAssignStmt(IxAssignStmt stmt) throws TypeError, IdentificationError;
    public ResultType visitCallStmt(CallStmt stmt) throws TypeError, IdentificationError;
    public ResultType visitReturnStmt(ReturnStmt stmt) throws TypeError, IdentificationError;
    public ResultType visitIfStmt(IfStmt stmt) throws TypeError, IdentificationError;
    public ResultType visitWhileStmt(WhileStmt stmt) throws TypeError, IdentificationError;
    
  // Expressions
    public ResultType visitUnaryExpr(UnaryExpr expr) throws TypeError, IdentificationError;
    public ResultType visitBinaryExpr(BinaryExpr expr) throws TypeError, IdentificationError;
    public ResultType visitRefExpr(RefExpr expr) throws TypeError, IdentificationError;
    public ResultType visitIxExpr(IxExpr expr) throws TypeError, IdentificationError;
    public ResultType visitCallExpr(CallExpr expr) throws TypeError, IdentificationError;
    public ResultType visitLiteralExpr(LiteralExpr expr) throws TypeError, IdentificationError;
    public ResultType visitNewObjectExpr(NewObjectExpr expr) throws TypeError, IdentificationError;
    public ResultType visitNewStringExpr(NewStringExpr expr) throws TypeError, IdentificationError;
    public ResultType visitNewArrayExpr(NewArrayExpr expr) throws TypeError, IdentificationError;
    public ResultType visitNullExpr(NullExpr expr) throws TypeError, IdentificationError;
    
  // References
    public ResultType visitThisRef(ThisRef ref) throws TypeError, IdentificationError;
    public ResultType visitIdRef(IdRef ref) throws TypeError, IdentificationError;
    public ResultType visitQRef(QualRef ref) throws TypeError, IdentificationError;

  // Terminals
    public ResultType visitIdentifier(Identifier id) throws TypeError, IdentificationError;
    public ResultType visitOperator(Operator op) throws TypeError, IdentificationError;
    public ResultType visitIntLiteral(IntLiteral num) throws TypeError, IdentificationError;
    public ResultType visitBooleanLiteral(BooleanLiteral bool) throws TypeError, IdentificationError;
    public ResultType visitNullLiteral(NullLiteral nullLiteral) throws TypeError, IdentificationError;
	  public void visitStringLiteral(StringLiteral stringLiteral) throws TypeError, IdentificationError;
}
