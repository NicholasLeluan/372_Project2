import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//need help with how to write a cla to a java file - T
//how to match with negative numbers? 
//can int mod real be allowed?


public class Translator {
	static FileWriter f = null;

    private static Scanner scanner;
    HashMap<String, Integer> integerVariables = new HashMap<String, Integer>();
    HashMap<String,Data> variableMap = new HashMap<>();
    static ArrayList<String> keywords = new ArrayList<String>();
    static ArrayList<String> variables = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
    	//@Nick - dont cringe its good enough for now lol
    	keywords.add("var");
    	keywords.add("add");
    	keywords.add("mult");
    	keywords.add("div");
    	keywords.add("mod");
    	keywords.add("true");
    	keywords.add("false");
    	keywords.add("or");
    	keywords.add("not");
    	keywords.add("less than");
    	keywords.add("greater than");
    	keywords.add("less than or equal to");
    	keywords.add("greater than or equal to");
    	keywords.add("equal to");
    	keywords.add("not equal to");
    	keywords.add("if");
    	keywords.add("then");
    	keywords.add("or if");
    	keywords.add("or");
    	//keywords.add("while this");
    	//keywords.add("increment by");
    	//keywords.add("do that");
    	keywords.add("end");
    	keywords.add("from");
    	keywords.add("to");
    	keywords.add("output");
    	keywords.add("outputs");
    	keywords.add("text");
    	keywords.add("cla");
    	
    	
    	//creates java file
    	f = new FileWriter("Test3.java");
    	f.write("public class Test3 {\n");
    	f.write("\tpublic static void main(String[] args){\n");
    	
        Pattern variableAssignmentPattern = Pattern.compile("var (.+)");
        // we will read the file
        // for each line -> read the "header" words that indicate what the statement is
        // based on the context call certain methods for parsing
        // if line[0] == var

        File file = new File(args[0]);
        System.out.println(String.format("OPENED FILE:: %s\n",args[0]));
        scanner = new Scanner(file);
        while (scanner.hasNext()){
            String line = scanner.nextLine();
            Matcher variableAssignment = variableAssignmentPattern.matcher(line);
            // if the read in line is a variable assignment call the variableAssignment method
            if (variableAssignment.matches()){
                addVariable(line);
            }
        }
        f.write("\t}\n");
        f.write("}");
        f.close();
    }

    /**
     * This is called when a line is suspected (sus) of attempting to assing a variable
     * to some value
     * TODO:
     * - assign value for integers
     * - assign value for reals
     * - ** assingn value for Strings
     * - throw any errors:
     *  - there is no match leftside of equals
     *  - varible assingment is made when variable was already declared (i.e. the map already has
     *  the variable)
     * @param line
     * @throws IOException 
     */

    private static void addVariable(String line) throws IOException{

        // maybe split at "=" so we can ingnore white space

        Pattern integerPattern = Pattern.compile("var (.+) = (\\d+)");
        Matcher integerMatcher = integerPattern.matcher(line);
        
        Pattern realsPattern = Pattern.compile("var (.+) = (\\d+\\.\\d+)");
        Matcher realsMatcher = realsPattern.matcher(line);
        
        //not sure on these
        Pattern addPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) add ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
        Matcher addPatternMatcher = addPattern.matcher(line);
        
        Pattern subPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) sub ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
        Matcher subPatternMatcher = subPattern.matcher(line);
        
        Pattern multPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) mult ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
        Matcher multPatternMatcher = multPattern.matcher(line);
        
        Pattern divPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) div ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
        Matcher divPatternMatcher = divPattern.matcher(line);
        
        Pattern modPattern = Pattern.compile("((\\d+)|(\\d+\\.\\d+)) mod ((\\\\d+)|(\\\\d+\\\\.\\\\d+))");
        Matcher modPatternMatcher = modPattern.matcher(line);
        
        Pattern commandLinePattern = Pattern.compile("var (.+) = cmd\\((\\d+)\\)");
        Matcher commandLineMatcher = commandLinePattern.matcher(line);


        if(integerMatcher.matches()){ //matches ints
        	if (keywords.contains(integerMatcher.group(1))) {
        		//TODO Throw an error message!!!!!!!!!!!!!!!!!!!!
        	}
        	variables.add(integerMatcher.group(1));
        	f.write("\t\tint " +  integerMatcher.group(1) + " = " + integerMatcher.group(2)+ ";\n");
        	//f.write("\t\tSystem.out.println(" + integerMatcher.group(1) + ");\n");
        	
            System.out.println(String.format("Matched %s with INTEGER",integerMatcher.group(2)));
<<<<<<< HEAD
            // write to file : int [var] = [val];
        }else if(realsMatcher.matches()){
=======
            
        }else if(realsMatcher.matches()){ //matches reals
        	if (keywords.contains(realsMatcher.group(1))) {
        		//TODO Throw an error message!!!!!!!!!!!!!!!!!!!!
        	}
        	variables.add(integerMatcher.group(1));
        	f.write("\t\tdouble " +  integerMatcher.group(1) + " = " + integerMatcher.group(2)+ ";\n");
        	//f.write("\t\tSystem.out.println(" + integerMatcher.group(1) + ");\n");
        	
>>>>>>> bae06e47352c40b620b3f3395134c763f7a4112f
            System.out.println(String.format("Matched %s with FLOAT",realsMatcher.group(2)));
            
        }else if(addPatternMatcher.matches()){ //matches add
        	//what is this adding to though??????
        	f.write(addPatternMatcher.group(0) + " + " + addPatternMatcher.group(2)+ ";\n");
        	
            System.out.println(String.format("Matched %s with ADD",addPatternMatcher.group(1)));
            
        }else if(commandLineMatcher.matches()){ //matches cla
        	if (keywords.contains(commandLineMatcher.group(1))) {
        		//TODO Throw an error message!!!!!!!!!!!!!!!!!!!!
        	}
        	
        	//TODO write to java file!!!!!!!!!
        	
            System.out.println(String.format("Matched %s with COMMAND LINE",commandLineMatcher.group(2)));
            
        }else{
        	//TODO Error messgage!!!!!!!!!!!!!!!!!!!!
            System.out.println("Did not match, likely throw an error here");
        }

    }

}
