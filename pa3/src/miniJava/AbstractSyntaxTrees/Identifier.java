/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TypeError;

public class Identifier extends Terminal {

  public Declaration decl;

  public Identifier (Token t) {
    super (t);
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
    return v.visitIdentifier(this, o);
  }

  public <R> void visit(Traveller<R> v) throws TypeError {
    v.visitIdentifier(this);
  }

}
