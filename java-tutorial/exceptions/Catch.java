import java.util.Stack;
import java.util.EmptyStackException;

public class Catch{

    private static Stack<Integer> s = new Stack<Integer>();

    private static Integer dummy1() throws EmptyStackException{
        return s.pop();
    }

    private static Integer dummy2() throws EmptyStackException{
        return dummy1();
    }


    public static void main(String[] args){
        try{
            dummy2();
        }
        catch(EmptyStackException ex){
            //class 'Exception' is the base class of all Exceptions
            //check its methods to see what else is available

            System.out.println("Caught EmptyStackException. Stack trace:");
            ex.printStackTrace();
        }
        finally {
            System.out.println("Gracefully terminating...");
        }
    }
}
