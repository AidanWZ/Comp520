/**
 * COMP 520
 * Object creation and update
 */
class MainClass {
    public static void main (String [] args) {
 
       FirstClass f = new FirstClass ();
       f.s = new SecondClass ();
 
       // write and then read;
       f.s.n = 1;
       
       System.out.println(f.s.n);
    }
 }
 
 class FirstClass
 {
    int n;
    SecondClass s;
 
 }
 
 class SecondClass
 {
    int n;
    FirstClass f;
 
 }
 
 
 
 