package packageb.packageb1;

import packagea.A;

public class B1{

    A a;

    packageb.B b;

    public void foo(){
        System.out.println("I am B1.foo");
    }

    public static void main(String[] args){
        A a = new A();
        packageb.B newB = new packageb.B();
        B1 b1 = new B1();
        b1.foo();
    }


}
