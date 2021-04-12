class StringCmp {
    public static void main(String[] args) {
        String a = new String("a");
        String b = new String("a");

        System.out.println("Checking for equality using a == \"a\" ...");
        if (a == "a")
            System.out.println("Equal!");
        else
            System.out.println("Not Equal!");

        System.out.println("Checking for equality using StringConstants.a == \"a\" ...");
        if ((new StringConstants()).a == "a")
            System.out.println("Equal!");
        else
            System.out.println("Not Equal!");

        System.out.println("Checking for equality using a == b ...");
        if (a == b)
            System.out.println("Equal!");
        else
            System.out.println("Not Equal!");

        System.out.println("Checking for equality using Object.equals ...");
        if (a.equals(b))
            System.out.println("Equal!");
        else
            System.out.println("Not Equal!");
    }
}
