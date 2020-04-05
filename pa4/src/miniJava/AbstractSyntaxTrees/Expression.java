/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.Generator;
import  miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Expression extends AST {

  public Expression(SourcePosition posn) {
    super (posn);
  }

  public TypeKind type;

public void generate(Generator generator) {
}
  
}
