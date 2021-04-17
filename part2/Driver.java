// https://github.com/redhat-developer/vscode-java/issues/366
import java_cup.runtime.*;
import java.io.*;

class Driver {
    public static void main(String[] argv) throws Exception{
        
        System.err.println("Expression(s) separated by ';':");
        
        // new File("./results").mkdirs();
        // PrintStream result = new PrintStream(new File("./results/arxeio.txt"));
        // System.setOut(result);
        
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();

        // System.out.println( "EDO" );
    }
}
