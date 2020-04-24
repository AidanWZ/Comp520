package miniJava.CodeGenerator;

public class UnknownValue extends Value {
    public int address; /* the address of the value on the stack */

    public UnknownValue(int address) {
        this.address = address;
    }
}