package miniJava.CodeGenerator;

public class UnknownRoutine extends Routine{
    public int declarationLevel;
    public int displacement;

    public UnknownRoutine(int declarationLevel, int displacement) {
        this.declarationLevel = declarationLevel;
        this.displacement = displacement;
    }
}