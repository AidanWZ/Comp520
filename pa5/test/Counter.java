class Counter {
    
    public void increase(int k) {
        count = count + k;
    }
    public static void main(String [] args){
        Counter counter = new Counter();
        counter.increase(3);
        System.out.println(counter.count);
    }
    public int count;
}