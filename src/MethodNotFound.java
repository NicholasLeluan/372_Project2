public class MethodNotFound extends Exception{
    int line;
    String method;
    public MethodNotFound(int line, String method){
        super();
        this.line = line;
        this.method = method;
    }

    public String toString(){
        return "Method: " + this.method + " on line :" + this.line + " is not defined";
    }
}
