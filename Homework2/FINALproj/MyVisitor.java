import syntaxtree.*;
import visitor.*;

import java.util.*;

class MyVisitor extends GJDepthFirst<String, Void> {

    private String curClass;
    private String curMethod;
    private Map<String, ST_ClassType> Symbol_Table;

    private Integer varOfset;
    private Integer methOfset;

    private boolean isSubclass;

    public MyVisitor() {
        Symbol_Table    = new LinkedHashMap<String, ST_ClassType>();
        this.varOfset   = 0;
        this.methOfset  = 0;
        isSubclass      = false;
    }

    public Map<String, ST_ClassType> retSymTable() {
        return this.Symbol_Table;
    }

    private List<String> returnMethodsOfAllFathers(String startClass) {
        
        List<String> FatherMethods=new ArrayList<String>();  
        String hasFather = this.Symbol_Table.get((startClass)).returnExtendName();

        while(hasFather!=null) {
            FatherMethods.addAll(this.Symbol_Table.get(hasFather).retMeths());
            hasFather = this.Symbol_Table.get((hasFather)).returnExtendName();
        }

        return FatherMethods;

    }

    public void PrintOffsets() {

        for (String key: Symbol_Table.keySet()) {
            Symbol_Table.get(key).printClassOfs();
        }
    }

    public void insertClass(String clName, ST_ClassType Stype, String fathClassIfExists) throws Exception {
        if(this.Symbol_Table.containsKey(clName)) {
            throw new Exception( "\nClass \"" + clName + "\" already exists");
        }
        
        this.Symbol_Table.put(clName, Stype);

        if(fathClassIfExists!=null) {
            this.insertClassExtends(clName, fathClassIfExists);
        }

        if(this.isSubclass==false) {
            // new class new ofs
            this.methOfset = 0;
            this.varOfset = 0;
        }
        else {
            // go on from father
            String fatherIs = Symbol_Table.get(clName).returnExtendName();
            while(fatherIs!=null) {
                String otherFather = this.Symbol_Table.get(fatherIs).returnExtendName();
                if(otherFather!=null) {
                    fatherIs = otherFather;
                }
                else {
                    // we are in first superclass
                    this.methOfset  = this.Symbol_Table.get(fatherIs).retMethOfStayed();
                    this.varOfset   = this.Symbol_Table.get(fatherIs).retVarOfStayed();
                    fatherIs        = null;
                }
            }
        }

    }

    public void insertClassExtends(String clName, String Snam) throws Exception {
        if(!this.Symbol_Table.containsKey(Snam)) {
            throw new Exception( "\nClass \"" + Snam + "\" has not been declared");
        }
        Symbol_Table.get(clName).setExtendsName(Snam);
    }

    public void insertVar(String clName, String varType, String varName) throws Exception {
        if(varType.equals("int") || varType.equals("boolean") || varType.equals("int[]")) {
            if(this.varOfset==null) {
                this.varOfset = 0;
            }
            this.varOfset = Symbol_Table.get(clName).newVariable(varName, varType, this.varOfset);
        }
        else {
            if(this.varOfset==null) {
                this.varOfset = 0;
            }
            this.varOfset = Symbol_Table.get(clName).newVariableClassInstance(varName, varType, this.varOfset);
        }
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

    private String FatherclassWithSameMethodIfSuch(String curClas, String methName) {
        
        // father class
        String fatherClass = Symbol_Table.get(curClas).returnExtendName();
        if(fatherClass==null) {
            return null;
        }
        String existsOrCheckAbove = this.Symbol_Table.get(fatherClass).MethodExistsInClass(methName);
        String clNameFound = fatherClass;
        
        while ( existsOrCheckAbove!=null ) {
            if(this.wordcount(existsOrCheckAbove)==2) {
                // father + classname
                String[] sp = existsOrCheckAbove.split(" ");
                existsOrCheckAbove = sp[1];
                clNameFound = existsOrCheckAbove;
                existsOrCheckAbove = this.Symbol_Table.get(existsOrCheckAbove).MethodExistsInClass(methName);
            }
            else {
                // found type here exists
                return clNameFound;
            }
        }
        return null;
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

    private Boolean checkIfClassNameExists(String Ident) {
        if( this.Symbol_Table.containsKey(Ident) ) {
            return true;
        }
        return false;
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

        // found in ret
        return this.Symbol_Table.get(startClass).retMethArgMap(methName);
        
    }

    private String checkIfIdentifierExistsInClassOrMethod(String clName, String methName, String iden) {
        // returns type or null
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

    public void insertMeth(String clName, String methType, String methName, String Vars) throws Exception {
                
        String Exists = this.FatherclassWithSameMethodIfSuch(clName, methName);
        if(Exists!=null) {
            // yparxei check args
            if(Symbol_Table.get(Exists).checkIfMethodArgsAreCorrect(methName, Vars)==false) {
                // break vars
                ArrayList<String> list  = new ArrayList<String>();
                StringTokenizer st      = new StringTokenizer(Vars, ",");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();

                    StringTokenizer stST = new StringTokenizer(token, " ");

                    list.add(stST.nextToken().trim());
                }

                String giveVars = "";
                for (int i = 0; i < list.size(); i++) {
                    giveVars += list.get(i);
                    if(i+1!=list.size()) {
                        giveVars += ", ";
                    }
                }

                // if(this.checkArgsForMethodCall(clName, methName, Vars)==false) {
                if(this.checkArgsForMethodCall(clName, methName, giveVars)==false) {
                    throw new Exception( "\nMethod \"" + methName + "\" exists in father class with different args");
                }
            }
        }

        List<String> FathMeths = this.returnMethodsOfAllFathers(clName);

        if(this.methOfset==null) {
            this.methOfset = 0;
        }
    
        this.methOfset = Symbol_Table.get(clName).newMethodOfClass(methType, methName, Vars, FathMeths, this.methOfset);

    }

    public void insertMethVar(String clName, String methName, String varType, String varName) throws Exception {
        Symbol_Table.get(clName).newMethVar(methName, varName, varType);
    }

    public void printSymTable() {
        System.out.println("\n\n----------------\nSymbol_Table:");
        for (String key: Symbol_Table.keySet()) {
            Symbol_Table.get(key).printClass();
        }
        System.out.println("----------------\n");
    }

    private boolean isInteger(String s, int radix) {
        Scanner sc = new Scanner(s.trim());
        if(!sc.hasNextInt(radix)) return false;
        sc.nextInt(radix);
        return !sc.hasNext();
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

//        super.visit(n, null);
        
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

        ST_ClassType newClass = new ST_ClassType(classname);

        this.insertClass(classname, newClass, null);
        
        this.Symbol_Table.get(classname).setisMainClass();

        this.isSubclass = false;

        this.curMethod = "main";
        this.insertMeth(this.curClass, "static void", this.curMethod, "String[] " + n.f11.accept(this, null));

        NodeListOptional varDecls = n.f14;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            String varId = varDecl.f1.f0.tokenImage;
            this.insertMethVar(this.curClass, this.curMethod, varKey, varId);
        }

        super.visit(n, argu);

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

        this.isSubclass = false;

        ST_ClassType newClass = new ST_ClassType(classname);
        this.insertClass(classname, newClass, null);

        NodeListOptional varDecls = n.f3;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            String varId = varDecl.f1.f0.tokenImage;
            this.insertVar(classname, varKey, varId);
        }

        super.visit(n, argu);

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

        this.isSubclass = true;

        String ExtendSclassname = n.f3.accept(this, null);
        // this.insertClassExtends(classname, ExtendSclassname);

        ST_ClassType newClass = new ST_ClassType(classname);
        this.insertClass(classname, newClass, ExtendSclassname);

        NodeListOptional varDecls = n.f5;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varKey = varDecl.f0.accept(this, null);
            String varId = varDecl.f1.f0.tokenImage;
            this.insertVar(classname, varKey, varId);
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

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        // System.out.println(myType + " " + myName + " -- " + argumentList);

        this.curMethod = myName;

        this.insertMeth(this.curClass, myType, myName, argumentList);

        NodeListOptional varDecls = n.f7;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);

            String varKey = varDecl.f0.accept(this, null);
            String varId = varDecl.f1.f0.tokenImage;

            this.insertMethVar(this.curClass, this.curMethod, varKey, varId);
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
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, Void argu) throws Exception {
        return n.f0.toString();
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }

}