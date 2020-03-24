class Tester {
    private int bar;
    private Tester temp;
    private void adder() {
        temp = null;
        this.bar = 2;
        bar = bar + 1;
    }

    public boolean changer(int bar) {
        boolean var = true;
        if (var == true) {
            adder();
        }
        return true;
    }
}