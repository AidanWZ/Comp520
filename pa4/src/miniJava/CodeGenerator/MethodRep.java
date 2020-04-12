package miniJava.CodeGenerator;

public class MethodRep extends RuntimeEntity {
    public int offsetInClass;

    public MethodRep(int offsetInClass) {
        this.offsetInClass = offsetInClass;
    }
}