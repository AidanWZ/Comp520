package miniJava.CodeGenerator;

public class ClassRep extends RuntimeEntity {
    public int offsetInPackage;

    public ClassRep(int offsetInPackage) {
        this.offsetInPackage = offsetInPackage;
    }
}