class Add {
	public static void main(String[] a){
		System.out.println(12 + 21);
	}
}

class A {
	int i;

	public int foo() {
		return i;
	}
}

class B extends A {
	boolean i;

	public boolean bar() {
		return i;
	}
}