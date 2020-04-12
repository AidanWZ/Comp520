package miniJava.CodeGenerator;

public class KnownAddress extends Address {

    public int declarationlevel;
    public int displacement;

    public KnownAddress(int declarationlevel, int displacement) {
        this.declarationLevel = declarationlevel;
        this.displacement = displacement;
    }

}