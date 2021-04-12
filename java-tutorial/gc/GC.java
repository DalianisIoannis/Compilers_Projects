class GC {
    private static char[] allocateByteArray(int size) {
        char[] ret = new char[size];
        for (int i = 0; i < size; ++i)
            ret[i] = (char) i;

        return ret;
    }

    public static void main(String[] args) {
        int MB = 1024 * 1024;
        int step = 0;
        while (true) {
            allocateByteArray(MB);
            System.out.println("Allocated so far: " + (++step) + "MB");
        }
    }
}
