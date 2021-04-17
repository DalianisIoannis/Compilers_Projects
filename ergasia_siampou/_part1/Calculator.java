import java.io.InputStream;
import java.io.IOException;

class Calculator {
    
    private int lookaheadToken;
    private InputStream in;

    public class ParseError extends Exception {
        static final long serialVersionUID = 42L;
        public String getMessage() {
            return "parse errror";
        }
    }
    
    public Calculator(InputStream input) throws IOException, ParseError {
        this.in = input;
        lookaheadToken = in.read();
    }
    
    /* convert to string */
    private int strEval(int num) {
        return num - '0';
    } 

    /* { 0-9 or "(" } */
    private int Expression() throws IOException, ParseError {
        if ((lookaheadToken >= '0' && lookaheadToken <='9') || (lookaheadToken == '(')) {
            return Expression2(Term()); 
        } 
        else {
            throw new ParseError();
        }
    }

    /* { "^", ")", e or EOF } */
    private int Expression2(int num) throws IOException, ParseError {
        if ((lookaheadToken == '&') || (lookaheadToken >= '0' && lookaheadToken <='9') || (lookaheadToken == '(')) {
            throw new ParseError();
        }

        if ((lookaheadToken == '^')) {
            lookaheadToken = in.read();
            return Expression2(num ^ Term());
        } 
        else {
            return num;
        }
    }

    /* { 0-9 or "(" } */
    private int Term() throws IOException, ParseError {
        if ((lookaheadToken >= '0' && lookaheadToken <='9') || (lookaheadToken == '(')) {
            return Term2(Factor()); 
        }
        else {
            throw new ParseError();
        }
    }

    /* { "^", "&", ")", e or EOF } */
    private int Term2(int num) throws IOException, ParseError {
        if ((lookaheadToken >= '0' && lookaheadToken <='9') || (lookaheadToken == '(')) {
            throw new ParseError();
        }

        if ((lookaheadToken == '&')) {
            lookaheadToken = in.read();
            return Term2(num & Factor());
        }
        else {
            return num;
        }
    }

    /* { 0-9 or "(" } */
    private int Factor() throws IOException, ParseError {
        if ((lookaheadToken == '&') || (lookaheadToken == '^') || (lookaheadToken == ')')) {
            throw new ParseError();
        }
        
        if (lookaheadToken == '(') {
            lookaheadToken = in.read();
            int ret = Expression();
            if(lookaheadToken == ')') {
                lookaheadToken = in.read();
                return ret;
            }
            else {
                throw new ParseError();
            }
        }
        else if (lookaheadToken >= '0' && lookaheadToken <='9') {
            int temp = strEval(lookaheadToken);
            lookaheadToken = in.read();
            return temp;
        }
        else {
            throw new ParseError();
        }
    }

    public static void main(String[] args) {
        try {
            Calculator calc = new Calculator(System.in);
            System.out.println(calc.Expression());
        } 
        catch (IOException err) {
            System.err.println(err.getMessage());
        } 
        catch (ParseError err) {
            System.err.println(err.getMessage());
        }
    }
}