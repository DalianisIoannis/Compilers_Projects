import java.io.InputStream;

class Echo {
    public static void main(String[] args) throws Exception {
        InputStream in = System.in;

        int index = 0;

        int data;
        while ((data = in.read()) != -1) {
            char c = (char)data;
            index++;
            System.out.println(index + ": " + c);
        }
    }
}
