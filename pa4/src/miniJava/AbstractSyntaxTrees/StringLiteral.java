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

  public <R> void generate(Generator<R> generator) {
    generator.visitStringLiteral(this);
  }
}