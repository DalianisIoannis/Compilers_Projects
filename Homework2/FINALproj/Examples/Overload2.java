class Overload2 {

    public static void main(String[] args){ }

}


class A {

    int x;

    public int x(){

        System.out.println(x);

        System.out.println(this.x());

        x = this.x();

        return 1;
    }

}
