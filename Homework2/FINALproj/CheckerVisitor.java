import syntaxtree.*;
import visitor.*;

import java.util.*;

class CheckerVisitor extends GJDepthFirst<String, Void> {

    private String curClass;
    private String curMethod;
    private Map<String, ST_ClassType> Symbol_Table;

    public CheckerVisitor(Map<String, ST_ClassType> SymTable) {
        this.Symbol_Table = SymTable;
    }

    public Map<String, ST_ClassType> retSymTable() {
        return this.Symbol_Table;
    }

    private Boolean isBoolean (String iden) {
        if( iden.equals("true") | iden.equals("false") ) {
            return true;
        }
        return false;
    }

    private Boolean isAnyValidType(String var) {
        if(var.equals("int") | var.equals("int[]") | var.equals("boolean") | this.checkIfClassNameExists(var)) {
            return true;
        }
        return false;
    }

    private Boolean checkIfClassNameExists(String Ident) {
        if( this.Symbol_Table.containsKey(Ident) ) {
            return true;
        }
        return false;
    }

    private int wordcount(String string) {
        int count=0;
        char ch[]= new char[string.length()];
        for(int i=0;i<string.length();i++)
        {
            ch[i]= string.charAt(i);
            if( ((i>0)&&(ch[i]!=' ')&&(ch[i-1]==' ')) || ((ch[0]!=' ')&&(i==0)) )
                count++;
        }
        return count;
    }

    private String checkIfMethodExistsInClass(String clName, String methName) {
        String ret = Symbol_Table.get(clName).MethodExistsInClass(methName);

        // check all fathers
        while (ret!=null) {
            if(this.wordcount(ret)==2) {
                // returned father class
                String[] sp = ret.split(" ");
                ret = Symbol_Table.get(sp[1]).MethodExistsInClass(methName);
            }
            else {
                break;
            }
        }
        
        return ret;
    }

    private String checkIfIdentifierExistsInClassOrMethod(String clName, String methName, String iden) {
        // is var or class instance or method of class
        // or is var or class instance of class
        
        // priority to method
        
        String ret = Symbol_Table.get(clName).IdentifierExists(methName, iden);

        // check all fathers
        while (ret!=null) {
            if(this.wordcount(ret)==2) {
                // returned father class
                String[] sp = ret.split(" ");
                ret = Symbol_Table.get(sp[1]).IdentifierExists(methName, iden);
            }
            else {
                break;
            }
        }

        return ret;
    }

    private Map<String, String> retMethArgMapFromCorrectClass(String startClass, String methName) {
        String ret = Symbol_Table.get(startClass).MethodExistsInClass(methName);
        
        // check all fathers
        while (ret!=null) {
            if(this.wordcount(ret)==2) {
                // returned father class
                String[] sp = ret.split(" ");
                // className
                startClass = sp[1];
                ret = Symbol_Table.get(sp[1]).MethodExistsInClass(methName);
            }
            else {
                break;
            }
        }

        return this.Symbol_Table.get(startClass).retMethArgMap(methName);
    }

    private Boolean checkArgsForMethodCall(String clName, String methName, String args) throws Exception {

        ArrayList<String> list  = new ArrayList<String>();
        StringTokenizer st      = new StringTokenizer(args, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            if(this.isInteger(token, 10)) {
                list.add("int");
            }
            else if (this.compareTypes(token, "boolean")) {
                list.add("boolean");
            }
            else if (this.compareTypes(token, "int[]")) {
                list.add("int[]");
            }
            else if (this.compareTypes(token, "this")) {
                list.add(this.curClass);
            }
            else {
                // args or vars
                String ifIsInstance = token;
                
                if(!this.checkIfClassNameExists(token)) {
                    ifIsInstance = this.checkIfIdentifierExistsInClassOrMethod(clName, methName, token);
                }
                
                if(ifIsInstance==null) {
                    // can be of current method
                    ifIsInstance = this.checkIfIdentifierExistsInClassOrMethod(clName, this.curMethod, token);
                }

                if(ifIsInstance==null) {
                    // can be of current class
                    ifIsInstance = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, token);
                }
                                
                list.add(ifIsInstance);
            }
        }

        // get map of args
        // might be method of father class
        Map<String, String> argMap = this.retMethArgMapFromCorrectClass(clName, methName);

        int listInd = 0;
        if(list.size()!=argMap.size()) {
            // mporei na einai methName apo alli klasi            
            throw new Exception( "\nNumber Of Arguments incorrect in class \"" + clName + "\" of method \"" + methName + "\" given \"" + args + "\"");
        }
        for (String key: argMap.keySet()) {

            String value = argMap.get(key).toString();
            String keepVal = value;
            
            while(value!=null) {
                if (!value.equals(list.get(listInd))) {
                    // check if is father class
                    if(this.checkIfClassNameExists(value)) {
                        value = this.Symbol_Table.get(value).returnExtendName();
                    }
                    else {
                        break;
                    }
                }
                else {
                    break;
                }
            }

            // might have to find father of list token
            value = keepVal;
            String listVal = list.get(listInd);
            while(listVal!=null) {
                if ( !value.equals(listVal) ) {
                    // check if is father class
                    if(this.checkIfClassNameExists(listVal)) {
                        listVal = this.Symbol_Table.get(listVal).returnExtendName();
                    }
                    else {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            
            if(listVal==null) {
                // ended
                return false;
            }

            listInd++;
        }

        return true;
    }

    public void printSymTable() {
        System.out.println("\n\n----------------\nSymbol_Table in CheckerVisitor:");
        for (String key: Symbol_Table.keySet()) {
            Symbol_Table.get(key).printClass();
        }
        System.out.println("----------------\n");
    }

    private boolean isInteger(String s, int radix) {
        if(s.equals("int")) { return true; }
        Scanner sc = new Scanner(s.trim());
        if(!sc.hasNextInt(radix)) return false;
        sc.nextInt(radix);
        return !sc.hasNext();
    }

    private Boolean compareTypes(String typeReturned, String typeNeeded) {
        if(typeReturned==null) {
            return false;
        }
        if(typeNeeded==null) {
            return false;
        }
        if (typeReturned.equals(typeNeeded)) {
            return true;
        }
        else {
            // mporei o enas na einai father tou allou
            if(this.checkIfClassNameExists(typeReturned)) {
                                
                String fatherClass = this.Symbol_Table.get(typeReturned).returnExtendName();
                
                while(fatherClass!=null) {
    
                    if (fatherClass.equals(typeNeeded)) {
                        return true;
                    }
                    else {
                        fatherClass = this.Symbol_Table.get(fatherClass).returnExtendName();
                    }
    
                }
            }
    
            // mporei o deuteros na einai father tou allou
            if(this.checkIfClassNameExists(typeNeeded)) {
                
                String fatherClass = this.Symbol_Table.get(typeNeeded).returnExtendName();
                                
                while(fatherClass!=null) {
    
                    if (fatherClass.equals(typeReturned)) {
                        return true;
                    }
                    else {
                        fatherClass = this.Symbol_Table.get(fatherClass).returnExtendName();
                    }
    
                }
            }
        }

        return false;
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    @Override
    public String visit(Goal n, Void argu) throws Exception {
        n.f0.accept(this, null);
        n.f1.accept(this, null);

    //    super.visit(n, null);

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        // System.out.println("Class: " + classname);

        this.curClass = classname;

        this.curMethod = "main";

        NodeListOptional varDecls = n.f14;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            // String varId = varDecl.f1.f0.tokenImage;
            if(this.isAnyValidType(varKey)==false) {
                throw new Exception( "\nType \"" + varKey + "\" has not been declared");
            }
        }

        super.visit(n, argu);

        // System.out.println();

        return null;
    }

    /**
     * f0 -> ClassDeclaration() | ClassExtendsDeclaration()
     */
    @Override
    public String visit(TypeDeclaration n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        // System.out.println("Class: " + classname);

        this.curClass = classname;

        NodeListOptional varDecls = n.f3;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            // String varId = varDecl.f1.f0.tokenImage;
            if(this.isAnyValidType(varKey)==false) {
                throw new Exception( "\nType \"" + varKey + "\" has not been declared");
            }
        }

        super.visit(n, argu);

        // System.out.println();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        // System.out.println("Class: " + classname);

        this.curClass = classname;

        NodeListOptional varDecls = n.f5;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            if(this.isAnyValidType(varKey)==false) {
                throw new Exception( "\nType \"" + varKey + "\" has not been declared");
            }
        }

        super.visit(n, argu);

        return null;
    }

//    /**
//     * f0 -> Type()
//     * f1 -> Identifier()
//     * f2 -> ";"
//     */
//    @Override
//    public String visit(VarDeclaration n, Void argu) throws Exception{
//        String type = n.f0.accept(this, null);
//        String name = n.f1.accept(this, null);
//        return type + " " + name;
//    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        // check that args are correct types

        StringTokenizer st = new StringTokenizer(argumentList, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            StringTokenizer stT = new StringTokenizer(token, " ");
            String token2 = stT.nextToken().trim();

            if(this.isAnyValidType(token2)==false) {
                throw new Exception( "\nType \"" + token2 + "\" has not been declared");
            }

        }

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        // System.out.println(myType + " " + myName + " -- " + argumentList);

        this.curMethod = myName;

        NodeListOptional varDecls = n.f7;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            if(this.isAnyValidType(varKey)==false) {
                throw new Exception( "\nType \"" + varKey + "\" has not been declared");
            }
        }

        n.f8.accept(this, null);
        
        String retExp = n.f10.accept(this, null);

        if(retExp.equals("this")) {
            retExp = this.curClass;
        }
        
        if(!myType.equals(retExp)) {
            // might be name of instance var

            String tmpRet = retExp;
            
            retExp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, retExp);

            if(retExp==null) {
                retExp = tmpRet;
            }

            if(!myType.equals(retExp)) {
                
                // might be subclass of searching type
                // // check all fathers
                String hasFather = null;
                if(this.Symbol_Table.containsKey(retExp)) {
                    hasFather = this.Symbol_Table.get((retExp)).returnExtendName();
                }
                while (hasFather!=null) {
                    if(myType.equals(hasFather)) {
                        retExp = hasFather;
                        break;
                    }
                    else {
                        hasFather = this.Symbol_Table.get((hasFather)).returnExtendName();
                    }
                }
                if(hasFather==null) {
                    if(this.Symbol_Table.containsKey(myType)) {
                        hasFather = this.Symbol_Table.get((myType)).returnExtendName();
                    }
                    while (hasFather!=null) {
                        if(retExp.equals(hasFather)) {
                            myType = hasFather;
                            break;
                        }
                        else {
                            hasFather = this.Symbol_Table.get((hasFather)).returnExtendName();
                        }
                    }
                    if(hasFather==null) {
                        throw new Exception( "\nMethodDeclaration \"" + myName + "\" wrong return \"" + retExp + "\"");
                    }
                }

                // throw new Exception( "\nMethodDeclaration \"" + myName + "\" wrong return \"" + retExp + "\"");

            }
        }

        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{

        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        return type + " " + name;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    @Override
    public String visit(Type n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    @Override
    public String visit(ArrayType n, Void argu) { return "int[]"; }

    @Override
    public String visit(BooleanType n, Void argu) { return "boolean"; }

    @Override
    public String visit(IntegerType n, Void argu) { return "int"; }

    /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    public String visit(Statement n, Void argu) throws Exception {
        // type or true
        return n.f0.accept(this, null);
    }

    /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    public String visit(Block n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        
        String Ident = n.f0.accept(this, null);
        // first give of current method if exists
        String IdentType = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);

        if(IdentType==null) {
            throw new Exception( "\nAssignmentStatement Identifier \"" + Ident + "\" has not been declared");
        }
        
        String exp = n.f2.accept(this, null);

        // if(!this.compareTypes(exp, "int") & !this.compareTypes(exp, "boolean") & !this.checkIfClassNameExists(exp)) {
        //     // not type or class instance so probably instance of class name
        //     exp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp);
        // }
        if(exp.equals("this")) {
            exp = this.curClass;
        }
        else if(!this.compareTypes(exp, "int") & !this.compareTypes(exp, "boolean") & !this.compareTypes(exp, "int[]") & !this.checkIfClassNameExists(exp)) {
            // not type or class instance so probably instance of class name
            exp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp);
        }

        // mporei lathos check if exp A is father of IdentType B
        // B b;
		// b = new A();
        // class A {}
        // class B extends A {}
        if(this.Symbol_Table.containsKey(IdentType)==true) {
            String isFather = this.Symbol_Table.get(IdentType).returnExtendName();
            while(isFather!=null) {
                if(isFather.equals(exp)) {
                    throw new Exception( "\nCannot assign father class \"" + exp + "\" to child class \"" + IdentType + "\"");
                }
                isFather = this.Symbol_Table.get(isFather).returnExtendName();
            }
        }

        if(!this.compareTypes(IdentType, exp)) {
            throw new Exception( "\nAssignmentStatement type Mismatch \"" + IdentType + "\" and \"" + exp + "\"");
        }
        
        return "true";
    }

    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        
        String Ident = n.f0.accept(this, null);
        String IdentType = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);

        if(!this.compareTypes(IdentType, "int[]")) {
            throw new Exception( "\nArrayAssignmentStatement type Mismatch \"" + Ident + "\" is not int[]");
        }
        
        String exp1 = n.f2.accept(this, null);

        if(!this.compareTypes(exp1, "int")) {
            
            // can be identifier
            String typeOfexp1 = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp1);

            if(typeOfexp1==null) {
                throw new Exception( "\nArrayAssignmentStatement type Mismatch \"" + exp1 + "\" is not int");
            }
            
            if(!typeOfexp1.equals("int")) {
                throw new Exception( "\nArrayAssignmentStatement type Mismatch \"" + exp1 + "\" is not int");
            }
        }

        String exp2 = n.f5.accept(this, null);

        if(!this.compareTypes(exp2, "int")) {

            String typeOfexp2 = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp2);
            
            if(!typeOfexp2.equals("int")) {
                throw new Exception( "\nArrayAssignmentStatement type Mismatch \"" + exp2 + "\" is not int");
            }
        }

        return "true";
    }
    
    /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public String visit(IfStatement n, Void argu) throws Exception {
        String exp = n.f2.accept(this, null);
        if(!this.compareTypes(exp, "boolean")) {
            exp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp);
            if(!this.compareTypes(exp, "boolean")) {
                throw new Exception( "\nIfStatement wrong \"" + exp + "\"");
            }
        }

        n.f4.accept(this, null);
        
        n.f6.accept(this, null);
        
        return "true";
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, Void argu) throws Exception {
        
        String exp = n.f2.accept(this, null);
        if(!this.compareTypes(exp, "boolean")) {
            exp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp);
            if(!this.compareTypes(exp, "boolean")) {
                throw new Exception( "\nWhileStatement wrong \"" + exp + "\"");
            }
        }

        n.f4.accept(this, null);
        
        return "true";
    }

    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, Void argu) throws Exception {
        String expr = n.f2.accept(this, null);
        
        // probably has to be checked for type
        // if(this.isBoolean(expr) | expr.equals("boolean")) {
        //         System.out.println("\nPrintStatement can only print int not " + expr + " in " + Thread.currentThread().getStackTrace()[1] + "\n");
        //         System.exit(0);
        // }

        if( !this.compareTypes(expr, "int") ) {
            String expType = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, expr);
            if(expType==null) {
                throw new Exception( "\nPrintStatement can only print int not \"" + expr + "\"");
            }
            if( !this.compareTypes(expType, "int") ) {
                throw new Exception( "\nPrintStatement can only print int not \"" + expr + "\"");
            }
        }
        return expr;
    }
    
    /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | PrimaryExpression()
    */
    public String visit(Expression n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }
    
    /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    public String visit(AndExpression n, Void argu) throws Exception {
        String primExp1 = n.f0.accept(this, null);
        
        if( !this.isInteger(primExp1, 10) ) {
            if(!primExp1.equals("boolean") & !this.isBoolean(primExp1)) {
                String primExp1TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp1);
                if(!primExp1TYPE.equals("boolean")) {
                    throw new Exception( "\nType Mismatch in AndExpression \"" + primExp1 + "\" not boolean");
                }
            }
        }
        
        String primExp2 = n.f2.accept(this, null);

        if( !this.isInteger(primExp2, 10) ) {
            if(!primExp2.equals("boolean") & !this.isBoolean(primExp2)) {
                String primExp2TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp2);
                if(!primExp2TYPE.equals("boolean")) {
                    throw new Exception( "\nType Mismatch in AndExpression \"" + primExp2 + "\" not boolean");
                }
            }

        }
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Void argu) throws Exception {
        String primExp1 = n.f0.accept(this, null);
        
        if( !this.isInteger(primExp1, 10) ) {
            String primExp1TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp1);
            if(!primExp1TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in AndExpression \"" + primExp1 + "\" not int");
            }
        }
        
        String primExp2 = n.f2.accept(this, null);

        if( !this.isInteger(primExp2, 10) ) {
            String primExp2TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp2);
            if(!primExp2TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in AndExpression \"" + primExp2 + "\" not int");
            }
        }
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, Void argu) throws Exception {
        String primExp1 = n.f0.accept(this, null);
        
        if( !this.isInteger(primExp1, 10) ) {
            String primExp1TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp1);
            if(!primExp1TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in PlusExpression \"" + primExp1 + "\" not int");
            }
        }
        
        String primExp2 = n.f2.accept(this, null);

        if( !this.isInteger(primExp2, 10) ) {
            String primExp2TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp2);
            if(!primExp2TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in PlusExpression \"" + primExp2 + "\" not int");
            }
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, Void argu) throws Exception {
        String primExp1 = n.f0.accept(this, null);
        
        if( !this.isInteger(primExp1, 10) ) {
            String primExp1TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp1);
            if(!primExp1TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in MinusExpression \"" + primExp1 + "\" not int");
            }
        }
        
        String primExp2 = n.f2.accept(this, null);

        if( !this.isInteger(primExp2, 10) ) {
            String primExp2TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp2);
            if(!primExp2TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in MinusExpression \"" + primExp2 + "\" not int");
            }
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, Void argu) throws Exception {
        String primExp1 = n.f0.accept(this, null);
        
        if( !this.isInteger(primExp1, 10) ) {
            String primExp1TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp1);
            if(!primExp1TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in TimesExpression \"" + primExp1 + "\" not int");
            }
        }
        
        String primExp2 = n.f2.accept(this, null);

        if( !this.isInteger(primExp2, 10) ) {
            String primExp2TYPE = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp2);
            if(!primExp2TYPE.equals("int")) {
                throw new Exception( "\nType Mismatch in TimesExpression \"" + primExp2 + "\" not int");
            }
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, Void argu) throws Exception {
        String exp1 = n.f0.accept(this, null);
        if(!this.compareTypes(exp1, "int[]")) {
            exp1 = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp1);
        }
        if(!this.compareTypes(exp1, "int[]")) {
            throw new Exception( "\nType Mismatch in ArrayLookup because of \"" + exp1 + "\"");
        }
        
        String exp2 = n.f2.accept(this, null);
        if(!this.compareTypes(exp2, "int")) {
            exp2 = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp2);
        }
        if(!this.compareTypes(exp2, "int")) {
            throw new Exception( "\nType Mismatch in ArrayLookup because of \"" + exp2 + "\"");
        }
        return "int";
    }
    
    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, Void argu) throws Exception {
        String primExp = n.f0.accept(this, null);
        if(this.compareTypes(primExp, "int[]")) {
            return "int";
        }
        
        // check if is identifier
        primExp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp);
        if(!this.compareTypes(primExp, "int[]")) {
            throw new Exception( "\nType Mismatch in ArrayLength because of \"" + primExp + "\"");
        }
        
        return "int";

    }
    
    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public String visit(MessageSend n, Void argu) throws Exception {
        
        String primExp = n.f0.accept(this, null);

        if(this.compareTypes(primExp, "int") | this.compareTypes(primExp, "int[]") | this.compareTypes(primExp, "boolean")) {
            throw new Exception( "\nType Mismatch between \"" + primExp + "\" and .");
        }

        if(this.compareTypes(primExp, "this")) {
            primExp = this.curClass;
        }
        else {
                
            // i apla na einai class
            if(!this.checkIfClassNameExists(primExp)) {

                // can be instance of class
                primExp = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp);
                
                if(primExp==null) {
                    throw new Exception( "\nType Mismatch in MessageSend \"" + primExp + "\" is not class");
                }
            }
        }

        // has to be method of class
        String Ident = n.f2.accept(this, null);

        String IdentType = this.checkIfMethodExistsInClass(primExp, Ident);

        if(IdentType==null) {
            throw new Exception( "\nType Mismatch in MessageSend \"" + Ident + "\" is not method of \"" + primExp + "\"");
        }

        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        // this.multipleArgs = new ArrayList<String>();

        // check if args are correct
        if(!this.checkArgsForMethodCall(primExp, Ident, argumentList)) {
            throw new Exception( "\nType Mismatch in MessageSend \"" + argumentList + "\" is incorrect for \"" + IdentType + "\"");
        }

        return IdentType;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
        }
    
        return ret;
	}

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        return n.f1.accept(this,null);
    }

    /**
    * f0 -> IntegerLiteral()                int
    *       | TrueLiteral()                 boolean
    *       | FalseLiteral()                boolean
    *       | Identifier()                  IDENTIFIER
    *       | ThisExpression()              this
    *       | ArrayAllocationExpression()   int
    *       | AllocationExpression()        type
    *       | NotExpression()               boolean
    *       | BracketExpression()           Expression()
    */
    public String visit(PrimaryExpression n, Void argu) throws Exception {
        String ret = n.f0.accept(this, argu);
        
        if( !this.compareTypes(ret, "int") & !this.compareTypes(ret, "int[]") & !this.isBoolean(ret) & !this.compareTypes(ret, "boolean")) {
            if(this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, ret)==null) {
                if(!this.checkIfClassNameExists(ret)) {
                    // can be this
                    if(!ret.equals("this")) {
                        throw new Exception( "\nPrimaryExpression \"" + ret + "\" has not been declared");
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, Void argu) throws Exception {
        // String num = n.f0.toString();
        // return num;
        return "int";
    }

    /**
    * f0 -> "true"
    */
    public String visit(TrueLiteral n, Void argu) throws Exception {
        // return n.f0.accept(this, null);
        return "boolean";
    }

    /**
     * f0 -> "false"
    */
    public String visit(FalseLiteral n, Void argu) throws Exception {
        // return n.f0.accept(this, null);
        return "boolean";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }

    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, Void argu) throws Exception { return "this"; }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(ArrayAllocationExpression n, Void argu) throws Exception {
        
        String ExpType = n.f3.accept(this, null);
        
        if(!this.compareTypes(ExpType, "int")) {
            
            // can be identifier
            String typeOfIdent = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, ExpType);
            
            if(!typeOfIdent.equals("int")) {
                throw new Exception( "\nType Mismatch in ArrayAllocationExpression because of \"" + ExpType + "\"");
            }
        }
                
        return "int[]";
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, Void argu) throws Exception {
        // return type of identifier
        String typ = n.f1.accept(this, null);
                
        // mporei na einai name i typos
        String typos = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, typ);

        if(typos==null) {
            // den einai name mporei na einai typos
            if(this.checkIfClassNameExists(typ)) {
                typos = typ;
            }
        }

        return typos;
    }

    /**
    * f0 -> "!"
    * f1 -> PrimaryExpression()
    */
    public String visit(NotExpression n, Void argu) throws Exception {
        String prim = n.f1.accept(this, null);
        if(!this.compareTypes(prim, "boolean")) {
            
            prim = this.checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, prim);
            
            if(!this.compareTypes(prim, "boolean")) {
                throw new Exception( "\nType Mismatch in NotExpression because of \"" + prim + "\"");
            }
        }
        return prim;
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, Void argu) throws Exception { return n.f1.accept(this, null); }

}