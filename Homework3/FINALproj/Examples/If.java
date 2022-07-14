class If {
	public static void main(String[] a) {
	    int x;
		BaseDot b;
		BaseDot c;

		b = new BaseDot();
		System.out.println( b.get() );
		System.out.println( new BaseDot().get() );

        x = 10;

        if (x < 2)
	        System.out.println(0);
	    else
	        System.out.println(1);
	}
}

class BaseDot {
	public int get() {
		return 3;
	}
}