class Classes {
	public static void main(String[] a) {
		Base b;
		Derived d;

  		b = new Base();
 		d = new Derived();

		System.out.println(b.set(1));

		b = d;

		System.out.println(b.set(3));

		System.out.println(b.set(4));
		System.out.println(b.get());
		System.out.println(b.retNum(4));
		System.out.println(b.pipi());
		System.out.println(d.pipi2());
	}
}

class Base {
	int data;

	public int set(int x) {
		data = x;

		return data;
	}

	public int get() {
		
		return data;
	}
	
	public int retNum(int x) {
		
		data = x * 22;
		
		return data;
	}

	public int pipi() {

		return 12345;
	}
}

class Derived extends Base {
	public int set(int x) {
		data = x * 2;

		return data;
	}
	
	public int pipi2() {

		return 67890;
	}
}