class Example {
    public static void main(String[] args) {
        int ena;
        int dyo;
        boolean lathos;

        System.out.println(12 + 21);
        System.out.println(12 * 21);
        // System.out.println(12 < 21);
        // System.out.println(ena * 21);
        // System.out.println(lathos * 21);

    }
}

class A {
    int i;
    A a;
    boolean lathos;

    // public int foo(int i, int j, int k) {
    // public int foo(int i, boolean j) {
    public int foo(int i, int j) {

        System.out.println(12 + 21);
		
		return i+j;
	}
	
    public int bar(){

        // System.out.println(this.foo(i, 2, i));
        System.out.println(this.foo(i, 2));
        
        return 1;
    }
}

class B extends A {
    int i;
    int j;

    public int foo(int i, int j) { return i+j; }
    public int foobar(boolean k){ return 1; }
}
