import java.io.IOException;
import java.util.Scanner;  

class Mymain {
    public static void main(String[] args) {
        
        
        
        // try {
        //     System.out.println( "Return Value is " + (new evaluator(System.in)).eval() );
        // }
        // catch (IOException | ParseError e) {
        //     System.err.println(e.getMessage());
        // }

        // System.out.println("Finished");

        
        
        
        Scanner userInput = new Scanner(System.in);
        while(true){
            System.out.print("Enter input: ");
            String foo = userInput.nextLine();

            if (foo.equals("$")) {
                break;
            }

            try {
                System.out.println( (new evaluator(System.in)).eval() );
            }
            catch (IOException | ParseError e) {
                System.err.println(e.getMessage());
            }
            
            System.out.println("\n");
        }
        userInput.close();
    }
}