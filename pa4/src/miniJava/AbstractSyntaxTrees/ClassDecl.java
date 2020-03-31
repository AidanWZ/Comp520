/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.Traveller;
import miniJava.ContextualAnalyzer.Visitor;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassDecl extends Declaration {

  public ClassDecl(String cn, FieldDeclList fdl, MethodDeclList mdl, SourcePosition posn) {
	  super(cn, null, posn);
	  fieldDeclList = fdl;
	  methodDeclList = mdl;
  }
  
  public <A,R> R visit(Visitor<A, R> v, A o) {
      return v.visitClassDecl(this, o);
  }

  public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
    v.visitClassDecl(this);
  }
      
  public FieldDeclList fieldDeclList;
  public MethodDeclList methodDeclList;
}
