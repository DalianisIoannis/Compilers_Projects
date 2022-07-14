class And {
	public static void main(String[] a) {
	    
        boolean b;
	    boolean c;
	    int x;

        int j ;
        Base bas;
        
        int size;
        size = 10;

        bas = new Base();
	    
        b = false;
        c = true;

        if (b && c)
            x = 0;
        else
            x = 1;

	    System.out.println(x);
        System.out.println(size);

        j = 0 ;
        while (j < (size)) {
            
            System.out.println( j );
            
            j = j + 1 ;
        }

        System.out.println(size-1);

        System.out.println( bas.set(size - 1) );
    }
}

class Base {
	int data;
	public int set(int x) {
		data = x;
		return data;
	}
}