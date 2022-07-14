class Test {
	public static void main(String[] args) {
		System.out.println(100);
	}
}

class A {
	int i;

	public int foo(int x){
		return 10;
	}
}

class B extends A{
	int y;

	public int foo(int x,int zero){
	//public boolean foo(int x){
		return 20;
	}
}