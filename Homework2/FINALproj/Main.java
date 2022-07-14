import syntaxtree.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {

        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        FileInputStream fis = null;
        for (String argument: args) {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println(argument);

            try{
                fis = new FileInputStream(argument);

                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                MyVisitor eval = new MyVisitor();

                root.accept(eval, null);

                // eval.printSymTable();
                
                eval.PrintOffsets();

                CheckerVisitor checker = new CheckerVisitor( eval.retSymTable() );

                // checker.printSymTable();

                fis = new FileInputStream(argument);
                parser = new MiniJavaParser(fis);
                root = parser.Goal();

                root.accept(checker, null);

                System.out.println("--------------------------------");

            }

            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }

            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }

            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}