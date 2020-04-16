package miniJava.CodeGenerator;

import javax.swing.text.html.parser.Entity;

public class FieldRep  extends RuntimeEntity {
    public int offsetInClass;
    public int size;
    public int offsetFromSB; //used only if static   

    public FieldRep(int offsetInClass, int size) {
        this.offsetInClass = offsetInClass;
        this.size = size;
        this.offsetFromSB = 0;
    } 
}