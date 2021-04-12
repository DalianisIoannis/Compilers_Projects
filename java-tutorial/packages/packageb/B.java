package packageb;

public class B{

    private packageb.packageb1.B1 b1;

    public B(){
        b1 = new packageb.packageb1.B1();
    }

    public void foo(){
        System.out.println("I am B.foo");
    }

    public static void main(String[] args){
        B b = new B();
        b.foo();
    }

}
