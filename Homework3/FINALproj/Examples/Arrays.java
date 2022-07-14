class Arrays {
	public static void main(String[] a) {
	    int[] x;
		int k;
	    x = new int[10];
		k = 4;

        x[0] = 1;
        x[1] = 2;
		x[2] = 32;
		x[3] = 5;
		x[4] = 10;
		x[5] = 20;
        x[6] = 15;
		x[3] = 8;
        x[7] = 9;

		System.out.println( x[0] );
		System.out.println( x[1] );
	    
		System.out.println( 2 + 4 );

		System.out.println( (x[0]) + (x[1]) );
		System.out.println( ((x[0]) + (x[1])) );

		System.out.println( ((x[6]) - (x[4])) );

		System.out.println( ((x[3]) * (x[7])) );
		
		System.out.println( x[k] );
	}
	// 1
	// 2
	// 6
	// 3
	// 3
	// 5
	// 72
	// 10
}


