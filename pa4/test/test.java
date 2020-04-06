class Counter {
    
    public static void increase(int k) {
        count = count + k;
    }
    public static void main(String [] args){
        Counter counter = new Counter();
        counter.increase(3);
        System.out.println(counter.count);
    }
    public static int count;
}

// class Tester {
//     public static void m1() {
//         derp++;
//     }
//     public int derp;
// }