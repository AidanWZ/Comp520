/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.IdentificationError;

public class StringLiteral extends Terminal {

  public StringLiteral(Token t) {
    super(t);
  }
 
  public <A,R> R visit(Visitor<A,R> v, A o) {
    return v.visitStringLiteral(this, o);
  }

  public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
    v.visitStringLiteral(this);
  }
}