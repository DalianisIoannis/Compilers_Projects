class Main {
    public static void main(String[] a) {}
}

    class A {
    // public A foo(A a) { return new C(); }
    public A foo(A a) { return new A(); }
}

class B extends A {
}

class C extends B {
    
    public C foo(C a) { return new C(); }
    
    // public C foo(C a) { return 5; }
}