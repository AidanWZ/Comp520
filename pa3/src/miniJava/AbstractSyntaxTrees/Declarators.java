/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class Declarators extends Declaration {

    public Declarators(boolean isPrivate, boolean isStatic, TypeDenoter mt, String name, SourcePosition posn) {
        super(name, mt, posn);
        this.isPrivate = isPrivate;
        this.isStatic = isStatic;
        this.type = mt;
    }
    
    public Declarators(MemberDecl md, SourcePosition posn){
    	super(md.name, md.type, posn);
    	this.isPrivate = md.isPrivate;
        this.isStatic = md.isStatic;
    }
    
    public boolean isPrivate;
    public boolean isStatic;
    public TypeDenoter type;

    @Override
    public <A, R> R visit(Visitor<A, R> v, A o) {
        // TODO Auto-generated method stub
        return null;
    }
}
