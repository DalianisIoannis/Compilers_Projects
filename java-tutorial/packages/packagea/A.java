package packagea;

import packageb.*;

public class A {

    B b;

    /**
     * Note: With import packageB.*; at the top of the file
     * we still need packageB.packageB1.B1 to access B1 ...
     */
    packageb.packageb1.B1 b1;

    public A (){
        b = new B();
        b1 = new packageb.packageb1.B1();
    }

    public void foo(){
        System.out.println("I am A.foo");
    }

    public static void main(String[] args){
        A a = new A ();
        a.foo();
    }

}
