public class VariableAlreadyDefined extends Exception{
    int line;

    public VariableAlreadyDefined(int line){
        super();
        this.line = line;
    }


    public String toString(){
        return String.format("Variable at line %d was already defined!",line);
    }

}

