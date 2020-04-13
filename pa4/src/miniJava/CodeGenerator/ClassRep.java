package miniJava.CodeGenerator;

public class ClassRep extends RuntimeEntity {
    public int offsetInPackage;
    public int size;

    public ClassRep(int offsetInPackage, int size) {
        this.offsetInPackage = offsetInPackage;
        this.size = size;
    }
}