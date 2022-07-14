import java.util.*;

public class ST_ClassType {

    public String className;
    private String extendsClass;

    private Map<String, String> Variables;
    private Map<String, String> ClassInstances;
    private Map<String, ST_MethodType> Methods;

    public Map<String, Integer> VarsOfs;
    public Map<String, Integer> MethodsOfs;

    Integer methOfStayed;
    Integer varsOfStayed;

    private Boolean isMainClass;

    public Integer retMethOfStayed() {
        return this.methOfStayed;
    }

    public Map<String, ST_MethodType> retMethMap() {
        return this.Methods;
    }

    public Map<String, String> retClassVars() {
        return this.Variables;
    }

    public Integer retVarOfStayed() {
        return this.varsOfStayed;
    }

    public String retExtendsClass() {
        return this.extendsClass;
    }

    public Integer returnVariableOfset(String var) {
        return this.VarsOfs.get(var);
    }

    public Integer returnMethOfset(String var) {
        return this.MethodsOfs.get(var);
    }

    private int putVarInOfset(String name, String type,  int startOf) {
        if(this.isMainClass==true) {
            return startOf;
        }
        VarsOfs.put(name, startOf);
        if(type=="boolean") {
            startOf += 1;
        }
        if(type=="int") {
            startOf += 4;
        }
        if(type=="int[]") {
            startOf += 8;
        }
        
        this.varsOfStayed = startOf;

        return startOf;
    }

    private int putClassInOfset(String name,  int startOf) {
        if(this.isMainClass==true) {
            return startOf;
        }
        VarsOfs.put(name, startOf);
        startOf += 8;
        
        this.varsOfStayed = startOf;

        return startOf;
    }

    public void printClassOfs() {

        if(this.isMainClass==true) {
            return;
        }

        System.out.println("-----------Class " + this.className + "-----------");
        System.out.println("---Variables---");
        for (String key: VarsOfs.keySet()) {

            String value = VarsOfs.get(key).toString();
            System.out.println(this.className + "." + key + " : " + value);
        }
        System.out.println("---Methods---");
        for (String key: MethodsOfs.keySet()) {

            String value = MethodsOfs.get(key).toString();
            System.out.println(this.className + "." + key + " : " + value);
        }

        System.out.println();
    }

    public ST_ClassType(String clName) {
        this.className  = clName;
        this.Variables  = new LinkedHashMap<String, String>();
        this.Methods    = new LinkedHashMap<String, ST_MethodType>();
        this.ClassInstances = new LinkedHashMap<String, String>();
        this.extendsClass   = null;
        this.isMainClass    = false;
        this.VarsOfs        = new LinkedHashMap<String, Integer>();
        this.MethodsOfs     = new LinkedHashMap<String, Integer>();
    }

    public void setisMainClass() {
        this.isMainClass = true;
    }

    public void setExtendsName(String name) {
        this.extendsClass = name;
    }

    public String returnExtendName() {
        return this.extendsClass;
    }

    public void printClass() {
        if( !(this.extendsClass==null) ) {
            System.out.println("\nClass name:" + className + " extends " + this.extendsClass);
        }
        else {
            System.out.println("\nClass name:" + className);
        }
        System.out.println("Class vars:");
        for (String key: Variables.keySet()) {

            String value = Variables.get(key).toString();
            System.out.println("\t--" + key + " " + value);
        }
        System.out.println("Class instances:");
        for (String key: ClassInstances.keySet()) {

            String value = ClassInstances.get(key).toString();
            System.out.println("\t--" + key + " " + value);
        }
        System.out.println("Class methods:");
        for (String key: this.Methods.keySet()) {

            this.Methods.get(key).printMeth();
        }
        System.out.println();
    }

    public int newVariable(String name, String type, int startOf) throws Exception {
        if( !this.Variables.containsKey(name) ) {
            this.Variables.put(name, type);
        }
        else {
            throw new Exception( "\nVariable \"" + name + "\" exists in class \"" + this.className + "\"");
        }
        
        return this.putVarInOfset(name, type, startOf);

    }

    public int newVariableClassInstance(String name, String type, int startOf) throws Exception {
        if( !this.ClassInstances.containsKey(name) ) {
            this.ClassInstances.put(name, type);
        }
        else {
            throw new Exception( "\nClass Instance \"" + name + "\" exists in class \"" + this.className + "\"");
        }

        return this.putClassInOfset(name, startOf);
    }

    private int putMethInOfset(List<String> FathMeths, String MethNam, int startOf) {
        
        if(!FathMeths.contains(MethNam)) {
            this.MethodsOfs.put(MethNam, startOf);
            startOf += 8;
        }

        this.methOfStayed = startOf;

        return startOf;
    }

    private Boolean IdentExistsAsMethodOnly(String Ident) {
        if( this.Methods.containsKey(Ident) ) {
            return true;
        }

        return false;
    }

    public int newMethodOfClass(String type, String name, String methodArgs, List<String> FathMeths, int startOf) throws Exception {

        if(this.IdentExistsAsMethodOnly(name)) {
            throw new Exception( "\nClass Method \"" + name + "\" exists in class \"" + this.className + "\"");
        }

        ST_MethodType newMeth = new ST_MethodType(type, name);
        this.Methods.put(name, newMeth);

        StringTokenizer st = new StringTokenizer(methodArgs, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            StringTokenizer stST = new StringTokenizer(token, " ");
            String ARgType = stST.nextToken().trim();
            String ARgNam = stST.nextToken().trim();
            this.newMethArg(name, ARgType, ARgNam);
        }

        if(name.equals("main")) {
            return startOf;
        }
        return this.putMethInOfset(FathMeths, name, startOf);
    }

    public void newMethVar(String methName, String name, String type) throws Exception {
        if(type.equals("int") || type.equals("boolean") || type.equals("int[]")) {
            this.Methods.get(methName).newVar(type, name);
        }
        else {
            this.Methods.get(methName).newClVar(type, name);
        }
    }

    public void newMethArg(String methName, String type, String name) throws Exception {
        this.Methods.get(methName).newArg(type, name);
    }

    public String IdentifierExists(String methName, String IdentName) {
        
        // check if exists in methName
        if (this.Methods.containsKey(methName)) { // can be method
            String methRet = this.Methods.get(methName).IdentifierType(IdentName);
            if(methRet!=null) {
                return methRet;
            }
        }

        // IdentName does not exist in method
        
        // System.out.println("Class instances:");
        // for (String key: ClassInstances.keySet()) {

        //     String value = ClassInstances.get(key).toString();
        //     System.out.println("\t--" + key + " " + value);
        // }

        if( this.Variables.containsKey(IdentName) ) {
            return this.Variables.get(IdentName).toString();
        }
        else if (this.ClassInstances.containsKey(IdentName)) {
            return this.ClassInstances.get(IdentName).toString();
        }
        else if (this.Methods.containsKey(IdentName)) { // can be method
            return this.Methods.get(IdentName).getMethType();
        }

        // not in this class
        if (this.extendsClass!=null) {
            return "father " + this.extendsClass;
        }

        return null;

    }

    // return type
    public String IdentExistsAsClassField(String IdentName) {
        if( this.Variables.containsKey(IdentName) ) {
            return this.Variables.get(IdentName).toString();
        }
        else if (this.ClassInstances.containsKey(IdentName)) {
            return this.ClassInstances.get(IdentName).toString();
        }
        
        // not in this class
        if (this.extendsClass!=null) {
            return "father " + this.extendsClass;
        }

        return null;
    }

    public String MethodExistsInClass(String methName) {
        if( this.Methods.containsKey(methName) ) {
            return this.Methods.get(methName).getMethType();
        }
        if (this.extendsClass!=null) {
            return "father " + this.extendsClass;
        }
        return null;
    }

    public Map<String, String> retMethArgMap(String methName) {
        return this.Methods.get(methName).retArgMap();
    }

    public List<String> retMeths() {
        
        List<String> MethodsRet=new ArrayList<String>();  

        for (String key: this.Methods.keySet()) {

            MethodsRet.add(key);
        }

        return MethodsRet;
    }

    public Boolean checkIfMethodArgsAreCorrect(String methName, String args) {        
        return this.Methods.get(methName).checkMethArgs(args);
    }

}