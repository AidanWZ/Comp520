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
import miniJava.SyntacticAnalyzer.SourcePosition;

public class ArrayType extends TypeDenoter {

	    public ArrayType(TypeDenoter eltType, SourcePosition posn){
	        super(TypeKind.ARRAY, posn);
			this.eltType = eltType;
			this.length = 0;
	    }
	        
	    public <A,R> R visit(Visitor<A,R> v, A o) {
	        return v.visitArrayType(this, o);
		}
		
		public <R> void visit(Traveller<R> v) throws TypeError, IdentificationError {
			v.visitArrayType(this);
		}

		public <R> void generate(Generator<R> generator) {
			generator.visitArrayType(this);
		}

		public TypeDenoter eltType;
		public int length;
	}

