import java.util.Stack;
import java.util.EmptyStackException;

public class NoCatch{

    private static Stack<Integer> s = new Stack<Integer>();

    private static Integer dummy1() throws EmptyStackException{
        return s.peek();
    }

    private static Integer dummy2() throws EmptyStackException{
        return dummy1();
    }


    public static void main(String[] args) throws EmptyStackException{
        dummy2();

        System.out.println("Done!");
    }
}
