public class UseOfKeyword extends Exception{

    int line;
    String key;

    public UseOfKeyword(String key, int line){
        super();
        this.line = line;
        this.key = key;
    }

    public String toString(){
        return String.format("Keyword %s was used incorrectly on line %d",key,line);
    }
}
