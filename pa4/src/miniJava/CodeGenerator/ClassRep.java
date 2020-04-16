package miniJava.CodeGenerator;

public class ClassRep extends RuntimeEntity {
    public int offsetInPackage;

    public ClassRep(int offsetInPackage, short size) {
        this.offsetInPackage = offsetInPackage;
        this.size = size;
    }
}