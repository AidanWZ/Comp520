package miniJava.CodeGenerator;

public class KnownAddress extends Address {

    public int displacement;

    public KnownAddress(int declarationLevel, int displacement) {
        this.declarationLevel = declarationLevel;
        this.displacement = displacement;
    }

}