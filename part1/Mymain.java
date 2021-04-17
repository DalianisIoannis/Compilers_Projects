import java.io.IOException;

class Mymain {
    public static void main(String[] args) {

        try {
            System.out.println( (new evaluator(System.in)).eval() );
        }
        catch (IOException | ParseError e) {
            System.err.println(e.getMessage());
        }
        
    }
}