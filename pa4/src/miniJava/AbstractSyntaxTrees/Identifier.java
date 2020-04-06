/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.Generator;
import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.ContextualAnalyzer.Visitor;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.Token;

public class Identifier extends Terminal {

  public Declaration decl;

  public Identifier (Token t) {
    super (t);
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
    return v.visitIdentifier(this, o);
  }

  public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
    v.visitIdentifier(this);
  }

  public <R> void generate(Generator<R> generator) {

  }
  
}
