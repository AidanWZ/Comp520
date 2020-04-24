package miniJava.CodeGenerator;

public class UnknownAddress extends Address {

    public int displacement;

    public UnknownAddress(int declarationLevel, int displacement) {
        this.declarationLevel = declarationLevel;
        this.displacement = displacement;
    }

}