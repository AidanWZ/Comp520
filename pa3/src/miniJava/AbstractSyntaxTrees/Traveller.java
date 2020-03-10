/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

/**
 * An implementation of the Visitor interface provides a method visitX
 * for each non-abstract AST class X.  
 */
public interface Traveller<ResultType> {

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
}
