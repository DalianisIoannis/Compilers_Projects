import java_cup.runtime.*;
import java.io.*;

class Driver {
    public static void main(String[] argv) throws Exception{
        
        PrintStream outputFile = new PrintStream(new File("./Main.java"));
        System.setOut(outputFile);
        
        // System.err.println("Expression ending with EOF:");
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();
    }
}
