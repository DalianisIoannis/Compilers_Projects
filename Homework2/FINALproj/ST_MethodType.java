import java.util.*;

public class ST_MethodType {

    private String methodName;
    private String methodType;

    private Map<String, String> Variables;
    private Map<String, String> Argments;
    private Map<String, String> MethClassInstances;

    ST_MethodType(String type, String name) {
        this.methodName = name;
        this.methodType = type;
        this.Variables  = new LinkedHashMap<String, String>();
        this.Argments   = new LinkedHashMap<String, String>();
        this.MethClassInstances = new LinkedHashMap<String, String>();
    }

    public Map<String, String> retArgMap() {
        return this.Argments;
    }

    public String getMethType() {
        return this.methodType;
    }

    public void printMeth() {
        System.out.println("\tMethod name: " + this.methodName);
        System.out.println("\t\tMethod type: " + this.methodType);
        System.out.println("\t\tMethod args:");
        for (String key: this.Argments.keySet()) {
            String value = this.Argments.get(key).toString();
            System.out.println("\t\t\t--" + key + " " + value);
        }

        System.out.println("\t\tMethod variables:");
        for (String key: this.Variables.keySet()) {
            String value = this.Variables.get(key).toString();
            System.out.println("\t\t\t--" + key + " " + value);
        }

        System.out.println("\t\tMethod class variables:");
        for (String key: this.MethClassInstances.keySet()) {
            String value = this.MethClassInstances.get(key).toString();
            System.out.println("\t\t\t--" + key + " " + value);
        }

    }

    public void newVar(String type, String name) throws Exception {
        if( this.IdentifierType(name)==null ) {
            this.Variables.put(name, type);
        }
        else {
            throw new Exception( "\nMethod Variable \"" + name + "\" exists in method \"" + this.methodName + "\"");
        }
    }

    public void newArg(String type, String Argu) throws Exception {
        if( this.IdentifierType(Argu)==null ) {
            this.Argments.put(Argu, type);
        }
        else {
            throw new Exception( "\nMethod Argument \"" + Argu + "\" exists in method \"" + this.methodName + "\"");
        }
    }

    public Boolean checkMethArgs(String args) {
                
        ArrayList<String> list  = new ArrayList<String>();
        StringTokenizer st      = new StringTokenizer(args, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            list.add(token);
        }

        if(list.size()!=this.Argments.size()) {
            return false;
        }

        // check Types
        int listInd = 0;
        for (String key: this.Argments.keySet()) {
            String value    = this.Argments.get(key).toString();
                        
            StringTokenizer stST = new StringTokenizer(list.get(listInd), " ");
            String ARgType  = stST.nextToken().trim();
            
            if ( !value.equals(ARgType) ) {
                return false;
            }

            listInd++;
        }

        return true;
    }

    public void newClVar(String type, String name) throws Exception {
        if( !this.MethClassInstances.containsKey(name) ) {
            this.MethClassInstances.put(name, type);
        }
        else {
            throw new Exception( "\nMethod Class Instance \"" + name + "\" exists in method \"" + this.methodName + "\"");
        }
    }

    public String IdentifierType(String Ident) {
        if( this.Variables.containsKey(Ident) ) {
            return this.Variables.get(Ident).toString();
        }
        else if (this.MethClassInstances.containsKey(Ident)) {
            return this.MethClassInstances.get(Ident).toString();
        }
        else if (this.Argments.containsKey(Ident)) {
            return this.Argments.get(Ident).toString();
        }
        else {
            return null;
        }
    }

}