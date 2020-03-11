/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.TypeError;

/**
 * An implementation of the Visitor interface provides a method visitX for each
 * non-abstract AST class X.
 */
public interface Traveller<ResultType> {

  // Package
    public ResultType visitPackage(Package prog) throws TypeError;

  // Declarations
    public ResultType visitClassDecl(ClassDecl cd) throws TypeError;
    public ResultType visitFieldDecl(FieldDecl fd) throws TypeError;
    public ResultType visitMethodDecl(MethodDecl md) throws TypeError;
    public ResultType visitParameterDecl(ParameterDecl pd) throws TypeError;
    public ResultType visitVarDecl(VarDecl decl) throws TypeError;
 
  // Types
    public ResultType visitBaseType(BaseType type) throws TypeError;
    public ResultType visitClassType(ClassType type) throws TypeError;
    public ResultType visitArrayType(ArrayType type) throws TypeError;
    
  // Statements
    public ResultType visitBlockStmt(BlockStmt stmt) throws TypeError;
    public ResultType visitVardeclStmt(VarDeclStmt stmt) throws TypeError;
    public ResultType visitAssignStmt(AssignStmt stmt) throws TypeError;
    public ResultType visitIxAssignStmt(IxAssignStmt stmt) throws TypeError;
    public ResultType visitCallStmt(CallStmt stmt) throws TypeError;
    public ResultType visitReturnStmt(ReturnStmt stmt) throws TypeError;
    public ResultType visitIfStmt(IfStmt stmt) throws TypeError;
    public ResultType visitWhileStmt(WhileStmt stmt) throws TypeError;
    
  // Expressions
    public ResultType visitUnaryExpr(UnaryExpr expr) throws TypeError;
    public ResultType visitBinaryExpr(BinaryExpr expr) throws TypeError;
    public ResultType visitRefExpr(RefExpr expr) throws TypeError;
    public ResultType visitIxExpr(IxExpr expr) throws TypeError;
    public ResultType visitCallExpr(CallExpr expr) throws TypeError;
    public ResultType visitLiteralExpr(LiteralExpr expr) throws TypeError;
    public ResultType visitNewObjectExpr(NewObjectExpr expr) throws TypeError;
    public ResultType visitNewStringExpr(NewStringExpr expr) throws TypeError;
    public ResultType visitNewArrayExpr(NewArrayExpr expr) throws TypeError;
    public ResultType visitNullExpr(NullExpr expr) throws TypeError;
    
  // References
    public ResultType visitThisRef(ThisRef ref) throws TypeError;
    public ResultType visitIdRef(IdRef ref) throws TypeError;
    public ResultType visitQRef(QualRef ref) throws TypeError;

  // Terminals
    public ResultType visitIdentifier(Identifier id) throws TypeError;
    public ResultType visitOperator(Operator op) throws TypeError;
    public ResultType visitIntLiteral(IntLiteral num) throws TypeError;
    public ResultType visitBooleanLiteral(BooleanLiteral bool) throws TypeError;
}
