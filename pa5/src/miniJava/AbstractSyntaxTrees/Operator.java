/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.Generator;
import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.ContextualAnalyzer.Visitor;
import miniJava.SyntacticAnalyzer.Token;

public class Operator extends Terminal {

  public Operator (Token t) {
    super (t);
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
    return v.visitOperator(this, o);
  }

  public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
    v.visitOperator(this);
  }

  public <R> Object generate(Generator<R> generator) {
    return generator.visitOperator(this);
  }
}
