class Add {
	public static void main(String[] a){
		System.out.println(12 + 21);
	}
}

class A {
	int i;

	public A foo() {
		return this;
	}
}

class B extends A {
	int i;

	public B foo() {
		return this;
	}
}