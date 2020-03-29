/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.IdentificationError;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TypeError;

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

		public TypeDenoter eltType;
		private int length;

		public int length() {
			return this.length;
		}
	}
