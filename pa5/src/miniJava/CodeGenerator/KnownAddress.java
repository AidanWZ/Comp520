package miniJava.CodeGenerator;

import mJAM.Machine;

public class KnownAddress extends Address {

    public int displacement;
    public Machine.Reg register;
    public varType vType;

    public enum varType {
        varType, 
        parameterType,
    }

    public KnownAddress(int declarationLevel, int displacement, Machine.Reg register) {
        this.declarationLevel = declarationLevel;
        this.displacement = displacement;
        this.size = 1;
        this.register = register;
    }

    public KnownAddress(int declarationLevel, int displacement, Machine.Reg register, varType vType) {
        this.declarationLevel = declarationLevel;
        this.displacement = displacement;
        this.size = 1;
        this.register = register;
        this.vType = vType;
    }

}