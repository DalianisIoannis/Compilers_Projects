public class Swapper {
    private static void foo(A a1){
        a1.i++;
    }

    private static void dudeWhereIsMySwap(A a1, A a2){
        A tmp = a1;
        a1 = a2;
        a2 = tmp;
    }

    public static void main(String[] args){
        A a1 = new A();

        System.out.printf("a1.i: %d\n", a1.i);

        foo(a1);

        System.out.printf("a1.i: %d\n", a1.i);

        //Let's create one more A
        A a2 = new A();
        System.out.printf("a2.i: %d\n", a2.i);

        System.out.println("Before swap, a1.i: " + a1.i + " a2.i: " + a2.i);

        dudeWhereIsMySwap(a1, a2);

        System.out.println("After swap,  a1.i: " + a1.i + " a2.i: " + a2.i);
        System.out.println("Wat?");
    }
}


class A {
    int i;

    A(){ i = 3; }
}
