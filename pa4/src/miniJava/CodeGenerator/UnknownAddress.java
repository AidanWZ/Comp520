package miniJava.CodeGenerator;

public class UnknownAddress extends Address {

    public int declarationlevel;
    public int displacement;

    public UnknownAddress(int declarationlevel, int displacement) {
        this.declarationLevel = declarationlevel;
        this.displacement = displacement;
    }

}