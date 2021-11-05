import java.util.HashMap;

public class Data {

    public static void main(String args[]){
        HashMap<String,Data> test = new HashMap<>();
        test.put("x",new Data(true,"x"));
        test.put("y",new Data(1,"y"));
        test.put("z",new Data("STRING","z"));

        System.out.println(test.get("x").get());
        if (test.get("x").get().equals(true)){
            System.out.println("this is how we will handle booleans");
        }
        System.out.println((Integer) test.get("y").get() + 100);
        System.out.println(test.get("z").get());

    }

    private String variableName;
    private String string;
    private Integer integer;
    private Boolean bool;
    private Object value;

    public Data(Object o, String variableName){
        this.variableName = variableName;
        //this.value = o;
        if(o instanceof String){
            this.string = (String) o;
        }else if(o instanceof Integer){
            this.integer = (Integer) o;
        }else if (o instanceof Boolean){
            this.bool = (Boolean) o;
        }
    }

    public Object get(){
        if(this.bool != null){
            return this.bool;
        }else if(this.string != null){
            return this.string;
        }
        else if(this.integer != null){
            return this.integer;
        }
        return null;
    }

}

