import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

class LLVMVisitor extends GJDepthFirst<String, String> {

    private Map<String, ST_ClassType> Symbol_Table;

    private String LLFile;
    private String progName;
    private String curClass;
    private String curMethod;

    private Integer registerCounter;
    private Integer ifLabelCounter;
    private Integer WhileLabelCounter;

    private Boolean checkIfClassNameExists(String Ident) {
        if( this.Symbol_Table.containsKey(Ident) ) {
            return true;
        }
        return false;
    }

    private Integer newRegister() {
        Integer tmp = this.registerCounter;
        this.registerCounter += 1;
        return tmp;
    }
    
    private Integer newLabelCounter() {
        Integer tmp = this.ifLabelCounter;
        this.ifLabelCounter += 1;
        return tmp;
    }

    private Integer newWhileLabelCounter() {
        Integer tmp = this.WhileLabelCounter;
        this.WhileLabelCounter += 1;
        return tmp;
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
            else { break; }
        }
        return ret;
    }

    // return ofset of variable
    private Integer returnVarOfset(String clasName, String iden) throws Exception {

        if( !this.Symbol_Table.get(clasName).VarsOfs.containsKey(iden) ) {
            
            String fatherClass = this.Symbol_Table.get(clasName).returnExtendName();
            
            while(fatherClass!=null) {

                if( this.Symbol_Table.get(fatherClass).VarsOfs.containsKey(iden) ) {
                    return this.Symbol_Table.get(fatherClass).returnVariableOfset(iden);
                }
                else {
                    fatherClass = this.Symbol_Table.get(fatherClass).returnExtendName();
                }

            }
            // // mporei na einai to idio klasi
            // if (this.checkIfClassNameExists(iden)) { }
            throw new Exception( "\nCould not find " + iden + " in Ofsets");
        }
        else { return this.Symbol_Table.get(clasName).returnVariableOfset(iden); }
    }

    // return ofset of method
    private Integer returnMethOfset(String clasName, String iden) throws Exception {

        if( !this.Symbol_Table.get(clasName).MethodsOfs.containsKey(iden) ) {
            
            String fatherClass = this.Symbol_Table.get(clasName).returnExtendName();
            
            while(fatherClass!=null) {

                if( this.Symbol_Table.get(fatherClass).MethodsOfs.containsKey(iden) ) {
                    return this.Symbol_Table.get(fatherClass).returnMethOfset(iden);
                }
                else {
                    fatherClass = this.Symbol_Table.get(fatherClass).returnExtendName();
                }

            }
            throw new Exception( "\nCould not find " + iden + " in Ofsets");
        }
        else { return this.Symbol_Table.get(clasName).returnMethOfset(iden); }
    }

    // return totalVarSum of class and all father classes
    private Integer retVarOfsetRec(String clasName) throws Exception {
        Integer ret = this.Symbol_Table.get(clasName).retVarOfStayed();
                
        if(ret==null) {
            
            String fatherClass = this.Symbol_Table.get(clasName).returnExtendName();
            
            while(fatherClass!=null) {

                ret = this.Symbol_Table.get(fatherClass).retVarOfStayed();

                if( ret!=null ) { return ret; }
                else { fatherClass = this.Symbol_Table.get(fatherClass).returnExtendName(); }

            }

            // mporei na einai to idio klasi
            if (this.checkIfClassNameExists(clasName)) {
                // System.out.println( "\nCould not find " + clasName + " in Ofsets return 0");
                return 0;
            }

            throw new Exception( "\nCould not find " + clasName + " in Ofsets");
        }
        else { return ret; }
    }

    private String checkIfIdentifierExistsInClassAsClassInstOrVar(String clName, String iden) {

        String ret = Symbol_Table.get(clName).IdentExistsAsClassField(iden);

        // check all fathers
        while (ret!=null) {
            if(this.wordcount(ret)==2) {
                // returned father class
                String[] sp = ret.split(" ");
                ret = Symbol_Table.get(sp[1]).IdentExistsAsClassField(iden);
            }
            else { break; }
        }
        return ret;
    }
    
    private void emit(String llst) throws Exception {
        try {
            Files.write( Paths.get(this.LLFile), llst.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND );
        }catch (IOException e) {
            throw new Exception( "\nCould not emit in file " + this.LLFile);
        }
    }

    public LLVMVisitor(Map<String, ST_ClassType> SymTable, String inputFile) throws Exception {
        
        this.Symbol_Table = SymTable;
        
        this.progName = inputFile;
        this.progName = inputFile.substring(inputFile.lastIndexOf("/") + 1);

        this.LLFile = new String(inputFile + ".ll");

        this.registerCounter = 0;
        this.ifLabelCounter = 0;
        this.WhileLabelCounter = 0;
        
    }

    public void printSymTable() {
        System.out.println("\n\n----------------\nSymbol_Table in LLVMVisitor:");
        for (String key: Symbol_Table.keySet()) {
            Symbol_Table.get(key).printClass();
        }
        System.out.println("----------------\n");
    }


    String typeNameToJavaLLVMI(String type) {
        if      ( type.equals("boolean") )      { return "i1"; }
		else if ( type.equals("int") )          { return "i32"; }
        else if ( type.startsWith("%") )        { return "i32"; }
		else if ( type.equals("int[]") )        { return "i32*"; }
        else if ( this.isInteger(type, 10) )    { return "i32"; }
		else                                    { return "i8*"; }
    }

    String typeNameToJavaLLVMIOrITYPE(String type) {
        if      ( type.equals("boolean") )      { return "i1"; }
		else if ( type.equals("int") )          { return "i32"; }
        else if ( type.startsWith("%") )        { return "i32"; }
        else if ( type.startsWith("i") )        { return type; }
		else if ( type.equals("int[]") )        { return "i32*"; }
        else if ( this.isInteger(type, 10) )    { return "i32"; }
		else                                    { return "i8*"; }
    }

    // return total number of methods of a class and father classes
    private Integer retMeths(String Clss) {
        Integer meths = Symbol_Table.get(Clss).retMethMap().size();

        // find father's
        String father = Symbol_Table.get(Clss).retExtendsClass();
        while(father!=null) {
            for (String Fathkey: Symbol_Table.get(father).retMethMap().keySet()) {
                if( !Symbol_Table.get(Clss).retMethMap().containsKey(Fathkey) ) { // add
                    meths += 1;
                }
            }
            father = Symbol_Table.get(father).retExtendsClass();
        }
        return meths;
    }

    private boolean isInteger(String s, int radix) {
        if(s.equals("int")) { return true; }
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
    public String visit(Goal n, String argu) throws Exception {
        

        for (String key2: Symbol_Table.keySet()) {
                        
            int methsNum = this.retMeths(key2);
            if(Symbol_Table.get(key2).className.equals(this.progName)) { methsNum -= 1;}
            
            this.emit(
                "@." + Symbol_Table.get(key2).className + "_vtable = global ["
                + String.valueOf(methsNum) + " x i8*] ["
            );

            if(methsNum>0) { this.emit("\n"); }
            
            // find father's
            String father = Symbol_Table.get(key2).retExtendsClass();
            int CounterTot = 0;
            ArrayList<String> list=new ArrayList<String>(); // list of added meths
            while(father!=null) {
                
                int methsNumFath = this.retMeths(father);
                if(Symbol_Table.get(father).className.equals(this.progName)) { methsNum -= 1;}
                int Count2 = 0;

                for (String Fathkey: Symbol_Table.get(father).retMethMap().keySet()) {
                    Count2 += 1;
                    CounterTot += 1;
                    if( !Symbol_Table.get(key2).retMethMap().containsKey(Fathkey) ) { // add
                        
                        // CounterTot += 1;

                        this.emit(
                            "\ti8* bitcast (" + this.typeNameToJavaLLVMI(Symbol_Table.get(father).retMethMap().get(Fathkey).getMethType())    
                            + " (i8*"
                            );
                            
                        for (String keyARG: Symbol_Table.get(father).retMethMap().get(Fathkey).retArgMap().keySet()) {
                            this.emit( ", " + this.typeNameToJavaLLVMI(Symbol_Table.get(father).retMethMap().get(Fathkey).retArgMap().get(keyARG).toString()) );
                        }
                        
                        this.emit(
                            ")* @" + Symbol_Table.get(father).className
                            + "." + Symbol_Table.get(father).retMethMap().get(Fathkey).getMethName()
                            + " to i8*)"
                        );
                        
                    }
                    else {  // father class to be overwritten
                        
                        list.add(Fathkey);

                        this.emit(
                            "\ti8* bitcast (" + this.typeNameToJavaLLVMI(Symbol_Table.get(key2).retMethMap().get(Fathkey).getMethType())    
                            + " (i8*"
                        );
                        
                        for (String keyARG: Symbol_Table.get(key2).retMethMap().get(Fathkey).retArgMap().keySet()) {
                            this.emit( ", " + this.typeNameToJavaLLVMI(Symbol_Table.get(key2).retMethMap().get(Fathkey).retArgMap().get(keyARG).toString()) );
                        }
                        
                        this.emit(
                        ")* @" + Symbol_Table.get(key2).className
                        + "." + Symbol_Table.get(key2).retMethMap().get(Fathkey).getMethName()
                        + " to i8*)"
                        );
                    }
                    if(Count2<methsNumFath) {
                        this.emit(",");
                    }
                    else if (CounterTot<methsNum) {
                        this.emit(",");
                    }
                    if(CounterTot<=methsNum) { this.emit("\n"); }
                }
                father = Symbol_Table.get(father).retExtendsClass();

            }

            // every Method
            int Count = CounterTot;
            for (String key: Symbol_Table.get(key2).retMethMap().keySet()) {
                Count += 1 ;
                if(!key.equals("main") && !list.contains(key) ) {

                    this.emit(
                        "\ti8* bitcast (" + this.typeNameToJavaLLVMI(Symbol_Table.get(key2).retMethMap().get(key).getMethType())    
                        + " (i8*"
                    );
                    
                    // params
                    for (String keyARG: Symbol_Table.get(key2).retMethMap().get(key).retArgMap().keySet()) {
                        this.emit( ", " + this.typeNameToJavaLLVMI(Symbol_Table.get(key2).retMethMap().get(key).retArgMap().get(keyARG).toString()) );
                    }
                    
                    this.emit(
                        ")* @" + Symbol_Table.get(key2).className
                        + "." + Symbol_Table.get(key2).retMethMap().get(key).getMethName()
                        + " to i8*)"
                    );
                    if(Count<methsNum) { this.emit(","); }
                    this.emit("\n");
                }
                
            }
            
            this.emit("]\n\n");
            
        }
        
        this.emit(
            "declare i8* @calloc(i32, i32)\n" +  "declare i32 @printf(i8*, ...)\n" +
            "declare void @exit(i32)\n\n@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
            "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
            "@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n\n" +
            "define void @print_int(i32 %i) {\n" +
            "    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
            "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
            "    ret void\n}\n\n" +
            "define void @throw_oob() {\n" +
            "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
            "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
            "    call void @exit(i32 1)\n" +
            "    ret void\n}\n\n" +
            "define void @throw_nsz() {\n" +
            "    %_str = bitcast [15 x i8]* @_cNSZ to i8*\n" +
            "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
            "    call void @exit(i32 1)\n" +
            "    ret void\n}\n\n"
        );

        n.f0.accept(this, null);
        n.f1.accept(this, null);
        
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
    public String visit(MainClass n, String argu) throws Exception {

        this.curClass = n.f1.accept(this, argu);
        this.curMethod = "main";

        emit("\ndefine i32 @main() {\n");

        for (String key: Symbol_Table.get(this.curClass).retMethMap().get("main").retVarsMap().keySet()) {
            emit("\t%" + key + " = alloca " + this.typeNameToJavaLLVMI( Symbol_Table.get(this.curClass).retMethMap().get("main").retVarsMap().get(key) ) + "\n");
        }

        for (String key: Symbol_Table.get(this.curClass).retMethMap().get("main").retVarsClass().keySet()) {
            emit("\t%" + key + " = alloca " + this.typeNameToJavaLLVMI( Symbol_Table.get(this.curClass).retMethMap().get("main").retVarsClass().get(key) ) + "\n");
        }

        emit("\n");

        n.f15.accept(this, argu);

        emit("\n\tret i32 1\n");
		emit("}\n\n");
        
        return null;
    }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
    public String visit(TypeDeclaration n, String argu) throws Exception {
        this.registerCounter = 0;
        this.ifLabelCounter = 0;
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
    public String visit(ClassDeclaration n, String argu) throws Exception {

        this.curClass = n.f1.accept(this, argu);

        n.f4.accept(this, null);
        
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
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {

        this.curClass = n.f1.accept(this, null);

        // String extenDer = n.f3.accept(this, null);
                
        n.f6.accept(this, null);

        return null;
    }
    
    // iSSec when object in message send or second for load
    private String returnForIdentifier(String Ident, Boolean isSec) throws Exception {
        // param or local var
        if( this.Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).IdentifierType(Ident)!=null ) {

            // check if is object
            if(!this.Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).isClassInstVar(Ident)) {

                String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
                String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
                
                if( (exp1Typ.equals("int") || exp1Typ.equals("int[]") || exp1Typ.equals("boolean")) && isSec==false ) {
                    return "%" + Ident;
                }
                else {
                    emit("\t%_" + this.newRegister() + " = load " + llvmTyp1 + ", " + llvmTyp1 + "* " +  "%" + Ident + "\n");
                    return "%_" + (this.registerCounter-1);
                }
                
            }
            if(isSec) {
                // second object nedds load
                String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
                String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
                emit("\t%_" + this.newRegister() + " = load " + llvmTyp1 + ", " + llvmTyp1 + "* " +  "%" + Ident + "\n");
                return "%_" + (this.registerCounter-1);
            }
            
            return "%" + Ident;
        }
        // class field
        else if( this.checkIfIdentifierExistsInClassAsClassInstOrVar(this.curClass, Ident)!=null ) {
            
            String type = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
            String llvmTyp = this.typeNameToJavaLLVMI(type);

            emit( "\t%_" + this.newRegister() + " = getelementptr i8, i8* %this, i32 " + (this.returnVarOfset(this.curClass, Ident) + 8) + "\n" );
            emit("\t%_" + this.newRegister() + " = bitcast i8* %_" + (this.registerCounter - 2) + " to " + llvmTyp + "*\n");

            // SOOOOOOOOOOOOOOOOOOS
            if(isSec) {
                // second object nedds load
                String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
                String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
                emit("\t%_" + this.newRegister() + " = load " + llvmTyp1 + ", " + llvmTyp1 + "* " +  "%_" + (this.registerCounter-2) + "\n");
            }
            
            return "%_" + (this.registerCounter-1);

        }
        else if(this.isInteger(Ident, 10)) {
            return Ident;
        }
        else if ( Ident.startsWith("%") ) {
            return Ident;
        }
        else if(Ident.equals("this")) {
            return "%this";
        }
        // else { System.out.println("ident " + Ident + " NOTHING"); }
        return null;
    }

    private String retValForMethod(String Ident, String typeRet) throws Exception {
        // param or local var
        if( this.Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).IdentifierType(Ident)!=null ) {

            // check if is object
            if(!this.Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).isClassInstVar(Ident)) {
                String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
                String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
                
                if( (exp1Typ.equals("int") || exp1Typ.equals("int[]") || exp1Typ.equals("boolean")) ) {
                    emit("\t%_" + this.newRegister() + " = load " + llvmTyp1 + ", " + llvmTyp1 + "* " +  "%" + Ident + "\n");
                    return "%_" + (this.registerCounter-1);
                }
                else {
                    emit("\t%_" + this.newRegister() + " = load " + llvmTyp1 + ", " + llvmTyp1 + "* " +  "%" + Ident + "\n");
                    return "%_" + (this.registerCounter-1);
                }
                
            }
            String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
            String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
            emit("\t%_" + this.newRegister() + " = load " + llvmTyp1 + ", " + llvmTyp1 + "* " +  "%" + Ident + "\n");
            return "%_" + (this.registerCounter-1);
            // SOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOS
        }
        // class field
        else if( this.checkIfIdentifierExistsInClassAsClassInstOrVar(this.curClass, Ident)!=null ) {
            
            String type = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, Ident);
            String llvmTyp = this.typeNameToJavaLLVMI(type);

            emit( "\t%_" + this.newRegister() + " = getelementptr i8, i8* %this, i32 " + (this.returnVarOfset(this.curClass, Ident) + 8) + "\n" );
            emit("\t%_" + this.newRegister() + " = bitcast i8* %_" + (this.registerCounter - 2) + " to " + llvmTyp + "*\n");
            emit("\t%_" + this.newRegister() + " = load " + llvmTyp + ", " + llvmTyp + "* %_" + (this.registerCounter-2) + "\n");
            
            return "%_" + (this.registerCounter-1);

        }
        else if(this.isInteger(Ident, 10)) {
            return Ident;
        }
        else if ( Ident.startsWith("%") ) {
            return Ident;
        }
        // else { System.out.println("ident " + Ident + " NOTHING"); }
        return null;
    }

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
    public String visit(MethodDeclaration n, String argu) throws Exception {

        String type = n.f1.accept(this, null);
        this.curMethod = n.f2.accept(this, null);
        String LLtype = this.typeNameToJavaLLVMI(type);
        
        emit("\ndefine " + LLtype + " @" + this.curClass + "." + this.curMethod + "(i8* %this");

        // params
        for (String keyARG: Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retArgMap().keySet()) {
            this.emit(
                ", "
                + this.typeNameToJavaLLVMI(Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retArgMap().get(keyARG).toString())
                + " %." + keyARG
            );
        }
        
        emit(") {\n");

        // alloc params
        for (String keyARG: Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retArgMap().keySet()) {
            this.emit(
                "\t%" + keyARG + " = alloca " +
                this.typeNameToJavaLLVMI(Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retArgMap().get(keyARG).toString()) +
                "\n" + "\tstore " +
                this.typeNameToJavaLLVMI(Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retArgMap().get(keyARG).toString()) +
                " %." + keyARG + ", " +
                this.typeNameToJavaLLVMI(Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retArgMap().get(keyARG).toString()) +
                "* %" + keyARG + "\n\n"
            );
        }

        for (String key: Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retVarsMap().keySet()) {
            emit("\t%" + key + " = alloca " + this.typeNameToJavaLLVMI( Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retVarsMap().get(key) ) + "\n");
        }

        for (String key: Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retVarsClass().keySet()) {
            emit("\t%" + key + " = alloca " + this.typeNameToJavaLLVMI( Symbol_Table.get(this.curClass).retMethMap().get(this.curMethod).retVarsClass().get(key) ) + "\n");
        }

        n.f8.accept(this, null);

        String retExp = n.f10.accept(this, null);

        if(retExp.contains("##")) {
            // retExp = breakPrimExpGetType(retExp);
            retExp = breakPrimExpGetReg(retExp);
        }

        retExp = retValForMethod(retExp, LLtype + "*");
        // retExp = retValForMethod(retExp, LLtype);
        
        emit("\n\tret " + LLtype + " " + retExp + "\n");
        
        emit("}\n\n");

        this.registerCounter = 0;
        this.ifLabelCounter = 0;
        
        return null;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, String argu) throws Exception { return n.f0.accept(this, null); }

    /** * f0 -> "int" * f1 -> "[" * f2 -> "]" */
    @Override
    public String visit(ArrayType n, String argu) throws Exception { return "int[]"; }

    /** * f0 -> "boolean" */
    @Override
    public String visit(BooleanType n, String argu) throws Exception { return "boolean"; }

    /** * f0 -> "int" */
    @Override
    public String visit(IntegerType n, String argu) throws Exception { return "int"; }

    /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    @Override
    public String visit(Statement n, String argu) throws Exception { return n.f0.accept(this, null); }

    /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    @Override
    public String visit(Block n, String argu) throws Exception { return n.f1.accept(this, null); }


    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception {
        
        String ident = n.f0.accept(this, null);
        String type = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, ident);
        String llvmTyp = this.typeNameToJavaLLVMI(type);
        
        String retExp = n.f2.accept(this, null);

        // if(retExp.contains("##")) {
        //     retExp = breakPrimExpGetType(retExp);
        // }
        
        // String type2;
        if ( retExp.startsWith("%") ) {
            retExp = this.breakPrimExpGetReg(retExp);
        }
        // else {  System.out.println("OXI ALLOCEXPRESSION"); }

        String checkIdentSec = this.returnForIdentifier(retExp, true);
        String checkIdent = this.returnForIdentifier(ident, false);
        
        emit("\tstore " + llvmTyp + " " + checkIdentSec + ", " + llvmTyp + "* " + checkIdent + "\n\n");

        return null;
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
    @Override
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {

        String Ident = n.f0.accept(this, null);
        String identifyExp = returnForIdentifier(Ident, true);

        emit("\t%_" + this.newRegister() + " = load i32, i32* %_" + (this.registerCounter-2) + "\n");

        String exp1 = n.f2.accept(this, null);
        
        if(exp1.contains("##")) {
            // exp1 = breakPrimExpGetType(exp1);
            exp1 = breakPrimExpGetReg(exp1);
        }

        String identifyExp2 = returnForIdentifier(exp1, true);
        
        emit("\t%_" + this.newRegister() + " = icmp sge i32 " + identifyExp2 + ", 0\n");

        emit("\t%_" + this.newRegister() + " = icmp slt i32 " + identifyExp2 + ", %_" + (this.registerCounter-3) + "\n");
        
        emit("\n\t%_" + this.newRegister() + " = and i1 %_" + (this.registerCounter-3) + ", %_" + (this.registerCounter-2) + "\n");

        emit("\tbr i1 %_" + (this.registerCounter-1) + ", label %oob_ok_As_" + this.newLabelCounter() + ", label %oob_err_As_" + (this.ifLabelCounter-1));
        
        emit("\n\n\toob_err_As_" + (this.ifLabelCounter-1) + ":\n");
        emit("\tcall void @throw_oob()\n");

        emit("\tbr label %oob_ok_As_" + (this.ifLabelCounter-1) + "\n\n");

        emit("\toob_ok_As_" + (this.ifLabelCounter-1) + ":\n");

        emit("\n\t%_" + this.newRegister() + " = add i32 1, " + identifyExp2 + "\n");

        emit("\t%_" + this.newRegister() + " = getelementptr i32, i32* %_" + (this.registerCounter-7) + ", i32 %_" + (this.registerCounter-2) + "\n");

        String exp2 = n.f5.accept(this, null);
        
        if(exp2.contains("##")) {
            // exp2 = breakPrimExpGetType(exp2);
            exp2 = breakPrimExpGetReg(exp2);
        }

        String identifyExp3 = returnForIdentifier(exp2, true);

        emit("\tstore i32 " + identifyExp3 + ", i32* %_" + (this.registerCounter-1) + "\n\n");
        
        return null;
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
    @Override
    public String visit(IfStatement n, String argu) throws Exception {
        String exp = n.f2.accept(this, null);

        if(exp.contains("##")) {
            // exp = breakPrimExpGetType(exp);
            exp = breakPrimExpGetReg(exp);
        }

        // mporei na einai boolean identifier
        String identifyExp = returnForIdentifier(exp, true);

        emit("\tbr i1 " + identifyExp + ", label %if_then_" + this.newLabelCounter() + ", label %if_else_" + (this.ifLabelCounter-1) + "\n\n");
        emit("\tif_else_" + (this.ifLabelCounter-1) + ":" + "\n");
        
        Integer lastLab = (this.ifLabelCounter-1);
        
        n.f6.accept(this, null);
        
        emit("\tbr label %if_end_" + lastLab + "\n\n");
        emit("\tif_then_" + lastLab + ":" + "\n");
        
        n.f4.accept(this, null);

        emit("\tbr label %if_end_" + lastLab + "\n\n");
        emit("\tif_end_" + lastLab + ":" + "\n");

        return null;
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, String argu) throws Exception {
		
        emit("\tbr label %loop" + this.newWhileLabelCounter() + "\n\n");
		
        emit("loop" + (this.WhileLabelCounter-1) + ":\n");

        String exp = n.f2.accept(this, null);
        // h false???????????????
        
        if(exp.contains("##")) {
            exp = breakPrimExpGetType(exp);
        }

        String identifyExp = returnForIdentifier(exp, true);
		
        emit("\tbr i1 " + identifyExp + ", label %loop" + this.newWhileLabelCounter() + ", label %loop" + this.newWhileLabelCounter() + "\n\n");

		emit("loop" + (this.WhileLabelCounter-2) + ":\n");
		
        n.f4.accept(this, null);
		
        emit("\tbr label %loop" + (this.WhileLabelCounter-3) + "\n");
		emit("loop" + (this.WhileLabelCounter-1) + ":\n");
		
        return null;
    }

    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
    public String visit(PrintStatement n, String argu) throws Exception {

        String IdenReg = n.f2.accept(this, null);

        if(IdenReg.contains("##")) {
            IdenReg = breakPrimExpGetReg(IdenReg);
        }
        
        String identifyExp = returnForIdentifier(IdenReg, true);

        emit("\tcall void (i32) @print_int(i32 " + identifyExp +")\n\n");
        
        return null;
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
    @Override
    public String visit(Expression n, String argu) throws Exception { return n.f0.accept(this, null); }
    
    /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    public String visit(AndExpression n, String argu) throws Exception {
        
        String exp1 = n.f0.accept(this, null);
        // String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp1);
        // String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
        String checkIdent = this.returnForIdentifier(exp1, true);

        emit("\n\tbr i1 %_" + (this.registerCounter-1) + ", label %exp_res_" + (this.newLabelCounter()+1) + ", label %exp_res_" + (this.ifLabelCounter-1) + "\n");

        emit("\n\texp_res_" + (this.ifLabelCounter-1) + ":\n");
        emit("\tbr label %exp_res_" + (this.ifLabelCounter+2) + "\n");

        emit("\n\texp_res_" + (this.ifLabelCounter) + ":\n");
        
        String exp2 = n.f2.accept(this, null);
        String checkIdent2 = this.returnForIdentifier(exp2, true);
        
        emit("\tbr label %exp_res_" + (this.ifLabelCounter+1) + "\n");
        
        emit("\n\texp_res_" + (this.ifLabelCounter+1) + ":\n");
        emit("\tbr label %exp_res_" + (this.ifLabelCounter+2) + "\n");

        emit("\n\texp_res_" + (this.ifLabelCounter+2) + ":\n");

        // emit("\t%_" + (this.newRegister()) + " = phi i1 [ 0, %exp_res_" + (this.ifLabelCounter-1) + " ] , [ %_" + (this.registerCounter-2) + ", %exp_res_" + (this.registerCounter-1) + " ]\n");
        emit("\t%_" + (this.newRegister()) + " = phi i1 [ 0, %exp_res_" + (this.ifLabelCounter-1) + " ] , [ %_" + (this.registerCounter-2) + ", %exp_res_" + (this.ifLabelCounter+1) + " ]\n");
        
        return "%_" + (this.registerCounter-1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception {
        
        String exp1 = n.f0.accept(this, null);
        // String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp1);
        // String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
        String checkIdent = this.returnForIdentifier(exp1, true);

        String exp2 = n.f2.accept(this, null);
        String checkIdent2 = this.returnForIdentifier(exp2, true);

        emit("\t%_" + this.newRegister() + " = icmp slt i32 " + checkIdent + ", " + checkIdent2 + "\n");
        
        return "%_" + (this.registerCounter-1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, String argu) throws Exception {
        
        String exp1 = n.f0.accept(this, null);
        String checkIdent = this.returnForIdentifier(exp1, true);

        String exp2 = n.f2.accept(this, null);
        String checkIdent2 = this.returnForIdentifier(exp2, true);

        emit("\t%_" + this.newRegister() + " = add i32 " + checkIdent + ", " + checkIdent2 + "\n");
        
        return "%_" + (this.registerCounter-1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, String argu) throws Exception {
        
        String exp1 = n.f0.accept(this, null);
        String checkIdent = this.returnForIdentifier(exp1, true);

        String exp2 = n.f2.accept(this, null);
        String checkIdent2 = this.returnForIdentifier(exp2, true);

        emit("\t%_" + this.newRegister() + " = sub i32 " + checkIdent + ", " + checkIdent2 + "\n");
        
        return "%_" + (this.registerCounter-1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception {

        String exp1 = n.f0.accept(this, null);
        
        String checkIdent = this.returnForIdentifier(exp1, true);
        String exp2 = n.f2.accept(this, null);
        String checkIdent2 = this.returnForIdentifier(exp2, true);

        emit("\t%_" + this.newRegister() + " = mul i32 " + checkIdent + ", " + exp2 + "\n");
        
        return "%_" + (this.registerCounter-1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {

        String exp1 = n.f0.accept(this, null);
        String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp1);
        String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
        String checkIdent = this.returnForIdentifier(exp1, true);

        // if() {}
        String exp2 = n.f2.accept(this, null);
        String checkIdent2 = this.returnForIdentifier(exp2, true);
        
        Integer lastReg = this.registerCounter-1;

        emit("\t%_" + this.newRegister() + " = getelementptr i32, i32* " + checkIdent + ", i32 0\n");
        emit("\t%_" + this.newRegister() + " = load i32, " + llvmTyp1 + " %_" + (this.registerCounter-2) + "\n");
        emit("\t%_" + this.newRegister() + " = icmp ult i32 " + checkIdent2 + ", %_" + (this.registerCounter-2) + "\n");
        emit("\t%_" + this.newRegister() + " = xor i1 %_" + (this.registerCounter-2) + ",  1\n");

        emit("\tbr i1 %_" + (this.registerCounter-1) + ", label %oob_err_L_" + (this.ifLabelCounter-1) + ", label %oob_ok_L_" + this.newLabelCounter());

        emit("\n\n\toob_err_L_" + (this.ifLabelCounter-2) + ":\n");
        emit("\tcall void @throw_oob()\n");

        emit("\tbr label %oob_ok_L_" + (this.ifLabelCounter-1) + "\n\n");

        emit("\toob_ok_L_" + (this.ifLabelCounter-1) + ":\n");

        emit("\n\t%_" + this.newRegister() + " = add i32 1, " + checkIdent2 + "\n");
        // emit("\t%_" + this.newRegister() + " = getelementptr i32, i32* " + checkIdent2 + ", i32 %_" + (this.registerCounter-2) + "\n");
        
        // if(checkIdent2.startsWith("%")) {
            emit("\t%_" + this.newRegister() + " = getelementptr i32, i32* " + checkIdent + ", i32 %_" + (this.registerCounter-2) + "\n");
        // }
        // else {
        //     emit("\t%_" + this.newRegister() + " = getelementptr i32, i32* " + checkIdent2 + ", i32 %_" + (this.registerCounter-2) + "\n");
        // }
        
        emit("\t%_" + this.newRegister() + " = load i32, " + llvmTyp1 + " %_" + (this.registerCounter-2) + "\n");
        
        return "%_" + (this.registerCounter-1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception {
        
        String exp1 = n.f0.accept(this, null);
        // String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exp1);
        // String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
        String checkIdent = this.returnForIdentifier(exp1, true);

        emit("\t%_" + this.newRegister() + " = getelementptr i32, i32* " + (this.registerCounter-2) + ", i32 0\n");

        emit("\t%_" + this.newRegister() + " = load i32, i32* " + (this.registerCounter-2) + "\n");

        return "%_" + (this.registerCounter-1);
    }

    private String breakPrimExpGetType(String primExp) {

        StringTokenizer st = new StringTokenizer(primExp, "##");
        String token = st.nextToken().trim();
        token = st.nextToken().trim();
        return token;
    }

    private String breakPrimExpGetReg(String primExp) {
        StringTokenizer st = new StringTokenizer(primExp, "##");
        String token = st.nextToken().trim();
        return token;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    @Override
    public String visit(MessageSend n, String argu) throws Exception {
        
        // mporei na einai reg + ## + type
        String primExp = n.f0.accept(this, null);
        
        String type;
        if( primExp.equals("this") ) {
            type = this.curClass;
        }
        else if ( !primExp.startsWith("%") ) {
            type = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, primExp);
        }
        // 
        // else if(!primExp.contains("##")) {
        // 
        else {
            type = this.breakPrimExpGetType(primExp);
        }

        // String llvmTyp = this.typeNameToJavaLLVMI(type);
        String checkIdent = this.returnForIdentifier(primExp, true);

        // %_0##BaseDot
        if( checkIdent.contains("##") ) {
            checkIdent = this.breakPrimExpGetReg(checkIdent);
        }

        emit("\t%_" + this.newRegister() + " = bitcast i8* " + checkIdent + " to i8***\n");
        emit("\t%_" + this.newRegister() + " = load i8**, i8*** %_" + (this.registerCounter-2) + "\n");

        String Iden = n.f2.accept(this, null);

        Integer IdOfs = this.returnMethOfset(type, Iden);    // type is className
        emit("\t%_" + this.newRegister() + " = getelementptr i8*, i8** %_" + (this.registerCounter-2) + ", i32 " + (IdOfs/8) + "\n");
        emit("\t%_" + this.newRegister() + " = load i8*, i8** %_" + (this.registerCounter-2) + "\n");

        String TypeOfCallingMeth = this.Symbol_Table.get(type).retMethMap().get(Iden).getMethType();
        String llvmTypTypeOfCallingMeth = this.typeNameToJavaLLVMI(TypeOfCallingMeth);
        
        Integer lastReg = (this.registerCounter-1);
        
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        // list of types
        StringTokenizer st = new StringTokenizer(argumentList, ",");
        var listFer = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            if(isInteger(token, 10)) {
                // emit(", " + this.typeNameToJavaLLVMI(token));
                listFer.add(this.typeNameToJavaLLVMI(token));
            }
            else if(token.startsWith("%")) {
                // emit(", " + token);
                listFer.add(token);
            }
            else {
                String exp1Typ = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, token);
                String llvmTyp1 = this.typeNameToJavaLLVMI( exp1Typ );
                // emit(", " + llvmTyp1);
                listFer.add(llvmTyp1);
            }

        }

        // for (int i = 0; i < listFer.size(); i++) { System.out.println("LISTA FER " + listFer.get(i)); }
        
        // emit("\t%_" + this.newRegister() + " = bitcast i8* %_" + (this.registerCounter-2) + " to " + llvmTypTypeOfCallingMeth + " (i8*");
        emit("\t%_" + this.newRegister() + " = bitcast i8* %_" + lastReg + " to " + llvmTypTypeOfCallingMeth + " (i8*");
        
        int countingFer = 0;
        st = new StringTokenizer(argumentList, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            
            if( !listFer.get(countingFer).startsWith("%") ) {
                if( listFer.get(countingFer).contains("##") ) {
                    emit(", " + breakPrimExpGetReg(listFer.get(countingFer)));
                }
                else {
                    emit(", " + listFer.get(countingFer));
                }
            }
            else {
                emit(", " + this.typeNameToJavaLLVMI(token));
            }
            
            countingFer += 1;
        }
        
        emit(")*\n");
        
        lastReg = (this.registerCounter-1);
        
        // give in args as well

        st = new StringTokenizer(argumentList, ",");
        ArrayList<String> list=new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            // emit(", " + this.typeNameToJavaLLVMI(token) + " " + token);
            String checkToken = this.returnForIdentifier(token, true);
            
            list.add(checkToken);
            
            // if( this.isInteger(token, 10)) { emit(", " + this.typeNameToJavaLLVMI(token) + " " + token); }
            // else { emit(", %" + this.typeNameToJavaLLVMI(token) + " %" + token); }
        }

        // emit("\t;EDO EIMAI\n");
        emit("\t%_" + this.newRegister() + " = call " + llvmTypTypeOfCallingMeth + " %_" + lastReg + "(i8* " + checkIdent);

        // for (int i = 0; i < list.size(); i++) { System.out.println("LISTA " + list.get(i)); }

        st = new StringTokenizer(argumentList, ",");
        int counting = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            // // emit(", " + this.typeNameToJavaLLVMI(token) + " " + list.get(counting));
            // emit(", " + this.typeNameToJavaLLVMI(list.get(counting)) + " " + list.get(counting));
            
            if( list.get(counting).contains("##") ) {
                // emit(", " + this.typeNameToJavaLLVMI( breakPrimExpGetType(list.get(counting)) ) + " " + breakPrimExpGetReg(list.get(counting)));
                // emit(", " + this.typeNameToJavaLLVMI( breakPrimExpGetType(listFer.get(counting)) ) + " " + breakPrimExpGetReg(list.get(counting)));
                emit(", " + this.typeNameToJavaLLVMIOrITYPE( breakPrimExpGetType(listFer.get(counting)) ) + " " + breakPrimExpGetReg(list.get(counting)));
                // emit(", " + this.typeNameToJavaLLVMI( breakPrimExpGetReg(list.get(counting)) ) + " " + breakPrimExpGetReg(list.get(counting)));
            }
            else {
                // emit(", " + this.typeNameToJavaLLVMI(list.get(counting)) + " " + list.get(counting));
                // emit(", " + this.typeNameToJavaLLVMI(listFer.get(counting)) + " " + list.get(counting));
                emit(", " + this.typeNameToJavaLLVMIOrITYPE(listFer.get(counting)) + " " + list.get(counting));
                // emit(", " + this.typeNameToJavaLLVMI(type) + " " + list.get(counting));
            }

            counting += 1;
        }

        emit(")\n");

        return "%_" + (this.registerCounter - 1) + "##" + type;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception {     
        String ret = n.f0.accept(this, null);
        
        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception {
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
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception { return n.f1.accept(this,null); }
 
    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception { return n.f0.accept(this, null); }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception { return n.f0.toString(); }

    /**
    * f0 -> "true"
    */
    public String visit(TrueLiteral n, String argu) throws Exception { return "1"; }

    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception { return "0"; }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n, String argu) { return n.f0.toString(); }
    
    /**
    * f0 -> "this"
    */
    @Override
   public String visit(ThisExpression n, String argu) throws Exception { return "this"; }
    
    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {

        String reg = n.f3.accept(this, null);

        if(reg.contains("##")) {
            // reg = breakPrimExpGetType(reg);
            reg = breakPrimExpGetReg(reg);
        }

        String checkIdent = this.returnForIdentifier(reg, true);    // h false?

        emit("\t%_" + this.newRegister() + " = add i32 1, " + checkIdent + "\n");

        emit("\t%_" + this.newRegister() + " = icmp sge i32 %_" + (this.registerCounter-2) + ", 1" + "\n");

        emit("\tbr i1 %_" + (this.registerCounter-1) + ", label %nsz_ok_" + this.newLabelCounter() + ", label %nsz_err_" + (this.ifLabelCounter-1) + "\n");

        emit("\n\tnsz_err_" + (this.ifLabelCounter-1) + ":\n");
        emit("\tcall void @throw_nsz()\n");
        emit("\tbr label %nsz_ok_" + (this.ifLabelCounter-1) + "\n");

        emit("\n\tnsz_ok_" + (this.ifLabelCounter-1) + ":\n");

        emit("\t%_" + this.newRegister() + " = call i8* @calloc(i32 %_" + (this.registerCounter-3) + ", i32 4)\n");

        emit("\t%_" + this.newRegister() + " = bitcast i8* %_" + (this.registerCounter-2) + " to i32*\n");

        emit("\tstore i32 " + checkIdent + ", i32* %_" + (this.registerCounter-1) + "\n\n");
        
        this.ifLabelCounter = 0;

        return "%_" + (this.registerCounter-1);
    }
 
    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        
        String exP = n.f1.accept(this, null);

        String type = checkIfIdentifierExistsInClassOrMethod(this.curClass, this.curMethod, exP);
        if(type==null) {
            // den einai name mporei na einai typos
            if(this.checkIfClassNameExists(exP)) { type = exP; }
        }

        // String llvmTyp = this.typeNameToJavaLLVMI( type );

        emit("\t%_" + this.newRegister() + " = call i8* @calloc(i32 1, i32 " + (8 + this.retVarOfsetRec(type) ) + ")\n");

        emit("\t%_" + this.newRegister() + " = bitcast i8* %_" + (this.registerCounter - 2) + " to i8***\n");

        Integer numOfMeths = this.retMeths(exP);
        emit("\t%_" + this.registerCounter + " = getelementptr [" + numOfMeths + " x i8*], [" + numOfMeths + " x i8*]* @." + exP + "_vtable, i32 0, i32 0\n");

        emit("\tstore i8** %_" + this.newRegister() + ", i8*** %_" + (this.registerCounter - 2) + "\n\n");
        
        return "%_" + (this.registerCounter - 3) + "##" + type;
    }

    /**
    * f0 -> "!"
    * f1 -> PrimaryExpression()
    */
    @Override
    public String visit(NotExpression n, String argu) throws Exception {
        		
        String expr = n.f1.accept(this, null);

        if(expr.contains("##")) {
            // reg = breakPrimExpGetType(reg);
            expr = breakPrimExpGetReg(expr);
        }

        String checkIdent = this.returnForIdentifier(expr, true);

		emit("\t%_" + this.newRegister() + " = xor i1 1, " + checkIdent + "\n");
		
        return "%_" + (this.registerCounter - 1);
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception {
        String expr = n.f1.accept(this, null);
        // if(expr.contains("##")) {
        //     expr = breakPrimExpGetType(expr);
        // }
        return expr;
    }

}