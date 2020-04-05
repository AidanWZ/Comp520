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
import miniJava.SyntacticAnalyzer.SourcePosition;

public class Package extends AST {

  public Package(ClassDeclList cdl, SourcePosition posn) {
    super(posn);
    classDeclList = cdl;
  }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitPackage(this, o);
    }

    public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
			v.visitPackage(this);
    }
    
    public void generate(Generator generator) {
      generator.visitPackage(this);
    }

    public ClassDeclList classDeclList;
}
