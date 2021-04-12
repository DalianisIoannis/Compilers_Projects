import java.io.InputStream;
import java.io.IOException;

class evaluator {

    private final InputStream in;
    private int lookahead;

    public evaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else {
            System.out.println("Parse Error in Consumer");
            throw new ParseError();
        }
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    private static int pow(int base, int exponent) {
        // if (exponent &lt; 0)
        //     return 0;
        if (exponent == 0)
            return 1;
        if (exponent == 1)
            return base;    
    
        if (exponent % 2 == 0) //even exp -&gt; b ^ exp = (b^2)^(exp/2)
            return pow(base * base, exponent/2);
        else                   //odd exp -&gt; b ^ exp = b * (b^2)^(exp/2)
            return base * pow(base * base, exponent/2);
    }

    public int eval() throws IOException, ParseError {
        int value = exp();

        if (lookahead != -1 && lookahead != '\n' && lookahead != '\r'  && lookahead != '\0') {
            throw new ParseError();
        }

        return value;
    }

    private int exp() throws IOException, ParseError {

        if(isDigit(lookahead) || lookahead=='(') {
                                    
            int cond2 = term();
            return exp2(cond2);
        }
        
        throw new ParseError();

    }

    private int exp2(int condition) throws IOException, ParseError {    // check for consumes

        switch (lookahead) {
            
            case '+':
                consume('+');
                condition += term();
                return exp2(condition);
            case '-':
                consume('-');
                condition -= term();
                return exp2(condition);
            case ')':
                return condition;
            case '\n':
                return condition;
            case '\r':
                return condition;
            case '\0':
                return condition;
        }
        
        throw new ParseError();

    }

    private int term() throws IOException, ParseError {

        if(isDigit(lookahead) || lookahead=='(') {

            
            int cond2 = num(0);
            return term2(cond2);
        }
        
        throw new ParseError();

    }

    private int term2(int condition) throws IOException, ParseError {

        switch (lookahead) {
            case '+':
                return condition;
            case '-':
                return condition;
            case '*':
                consume('*');
                consume('*');
                int cond2 = num(0); // anexartito
                // condition = (int)Math.pow(condition, cond2);
                condition = (int)pow(condition, cond2);
                return term2(condition);
            case ')':
                return condition;            
            case '\n':
                return condition;
            case '\r':
                return condition;
            case '\0':
                return condition;
        }
        
        throw new ParseError();

    }

    private int num(int condition) throws IOException, ParseError {

        if(lookahead=='(') {
            consume('(');
            int cond = exp();
            consume(')');
            return cond;
        }

        if(isDigit(lookahead)) {
                        
            if(evalDigit(lookahead)==0) { // 0 to proto psifio
                consume(lookahead);
                return 0;
            }            
            
            return num2(condition);
        }
        
        throw new ParseError();

    }

    private int num2(int condition) throws IOException, ParseError {

        switch (lookahead) {
            case '+':
                return condition;
            case '-':
                return condition;
            case '*':
                return condition;
            case ')':
                return condition;            
            case '\n':
                return condition;
            case '\r':
                return condition;
            case '\0':
                return condition;
        }
                
        if(isDigit(lookahead)) {
            int condition2 = condition*10 + evalDigit(lookahead);
            consume(lookahead);
            return num2(condition2);
        }
        
        throw new ParseError();
    }

}