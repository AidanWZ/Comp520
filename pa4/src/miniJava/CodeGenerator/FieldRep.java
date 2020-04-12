package miniJava.CodeGenerator;

public class FieldRep  extends RuntimeEntity {
    public int offsetInClass;
    public int size;

    public FieldRep(int offsetInClass, int size) {
        this.offsetInClass = offsetInClass;
        this.size = size;
    } 
}