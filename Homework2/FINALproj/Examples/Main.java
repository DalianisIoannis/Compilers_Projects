class Main {

    public static void main(String[] a){
        
		ArrayTest ab;
		int dilosi;
        
		ab=new ArrayTest ();

		System.out.println(ab.test(3));
						
		dilosi = 5;
		System.out.println(dilosi);
		
    }

}

class ArrayTest {
	int weee;
    int wre;
    int aaa;
    public int test(int num){
        int i;
        int[] intArray;
		
		System.out.println(3);
        
		intArray = new int[num];
		
		aaa=0;
		
		System.out.println(aaa);
		
		System.out.println(intArray.length);
		
		i = 0;
		
		while (i < (intArray.length)) {
            System.out.println(i + 1);
            intArray[i] = i + 1;
            i = i + 1;
        }
		
		i = 0;
        while (i < (intArray.length)) {
            System.out.println(intArray[i]);
            i = i + 1;
        }
		
        return intArray.length;
    }
	
}

class B extends ArrayTest {
	int aaa;
	int we;
	public int test(int num){
		int i;
		int[] intArray;
		
		System.out.println(333);
		
		intArray = new int[num];
		
		aaa=12;
        System.out.println(aaa);
		
		System.out.println(intArray.length);
		
		i = 0;
		
		while (i < (intArray.length)) {
            System.out.println(i + 1);
			i = i + 1;
        }
		
		while (i < (intArray.length)) {
            intArray[i] = i + 1;
        }
		
		i = 0;
        while (i < (intArray.length)) {
            System.out.println(intArray[i]);
            i = i + 1;
        }
		
		return 3;
	}
}