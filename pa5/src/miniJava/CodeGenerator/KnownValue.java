package miniJava.CodeGenerator;

public class KnownValue extends Value {
    public int value; /* the known value */
    public boolean bool;
    public String string;

    public KnownValue(int value) {
        this.value = value;
    }

    public KnownValue(boolean bool) {
        this.bool = bool;
    }

    public KnownValue(String string) {
        this.string = string;
    }
}