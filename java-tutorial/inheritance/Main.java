public class Main {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();

        System.out.println(a.foo());
        System.out.println(b.foo());
        System.out.println(b.bar());
    }
}

interface IA {
    int foo();
}

class A {
    int i = 0;

    public int foo() {
        return i;
    }
}

class B extends A {
    int i = 1;

    int bar() {
        return i;
    }
}
